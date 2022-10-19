package onlymash.flexbooru.ap.ui.fragment

import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.flow.*
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.databinding.FragmentListRefreshableBinding
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.FROM_POSTS
import onlymash.flexbooru.ap.ui.base.QueryListener
import onlymash.flexbooru.ap.ui.adapter.PostAdapter
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.base.PostActivity
import onlymash.flexbooru.ap.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ap.ui.viewmodel.TagBlacklistViewModel
import onlymash.flexbooru.ap.ui.adapter.NetworkLoadStateAdapter
import org.kodein.di.instance

const val JUMP_TO_TOP_KEY = "jump_to_top"
const val JUMP_TO_TOP_QUERY_KEY = "jump_to_top_query"
const val JUMP_TO_TOP_ACTION_FILTER_KEY = "jump_to_top_action_filter"

const val JUMP_TO_POSITION_KEY = "jump_to_position"
const val JUMP_TO_POSITION_QUERY_KEY = "jump_to_position_query"
const val JUMP_TO_POSITION_ACTION_FILTER_KEY = "jump_to_position_action_filter"

class PostFragment : KodeinFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener, QueryListener {

    private lateinit var postViewModel: PostViewModel
    private lateinit var tagBlacklistViewModel: TagBlacklistViewModel

    private val sp by instance<SharedPreferences>()
    private val db by instance<MyDatabase>()
    private val tagBlacklistDao by instance<TagBlacklistDao>()
    private val api by instance<Api>()

    private var _binding: FragmentListRefreshableBinding? = null
    private val binding get() = _binding!!

    private val list get() = binding.layoutList.layoutRv.list
    private val swipeRefresh get() = binding.layoutList.refresh
    private val progressBarHorizontal get() = binding.layoutProgressHorizontal.progressBarHorizontal

    private lateinit var search: Search
    private lateinit var postAdapter: PostAdapter

    private var currentPosition = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            val isJump = intent.getBooleanExtra(JUMP_TO_TOP_KEY, false)
            val query = intent.getStringExtra(JUMP_TO_TOP_QUERY_KEY) ?: ""
            if (isJump && query == search.query) {
                list.scrollToPosition(0)
            }
        }
    }

    private val positionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.getStringExtra(JUMP_TO_POSITION_QUERY_KEY) != search.query) {
                return
            }
            currentPosition = intent.getIntExtra(JUMP_TO_POSITION_KEY, currentPosition)
            list.scrollToPosition(currentPosition)
        }
    }

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
            if (names == null || sharedElements == null) {
                super.onMapSharedElements(names, sharedElements)
                return
            }
            val holder = list.findViewHolderForAdapterPosition(currentPosition) as? PostAdapter.PostViewHolder
            if (holder != null) {
                names.clear()
                sharedElements.clear()
                val name = holder.preview.transitionName
                names.add(name)
                sharedElements[name] = holder.preview
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var query = ""
        var searchType = SearchType.NORMAL
        var userId = -1
        var uploaderId = -1
        var color = ""
        arguments?.apply {
            query = getString(QUERY_KEY) ?: ""
            searchType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getSerializable(SEARCH_TYPE_KEY, SearchType::class.java)
            } else {
                @Suppress("DEPRECATION")
                getSerializable(SEARCH_TYPE_KEY)
            } as? SearchType ?: SearchType.NORMAL
            userId = getInt(USER_ID_KEY, -1)
            uploaderId = getInt(UPLOADER_ID_KEY, -1)
            color = getString(COLOR_KEY) ?: ""
        }
        search = Search(
            query = query,
            limit = Settings.pageLimit,
            type = searchType,
            userId = userId,
            uploaderId = uploaderId,
            token = Settings.userToken,
            color = color
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        postViewModel = getViewModel(PostViewModel(db = db, api = api))
        tagBlacklistViewModel = getViewModel(TagBlacklistViewModel(tagBlacklistDao))
        _binding = FragmentListRefreshableBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val glide = GlideApp.with(requireContext())
        postAdapter = PostAdapter(
            glide = glide,
            clickItemCallback = { query, preview, name, position ->
                currentPosition = position
                activity?.let {
                    DetailActivity.startDetailActivityWithTransition(
                        activity = it,
                        fromWhere = FROM_POSTS,
                        query = query,
                        position = position,
                        view = preview,
                        transitionName = name
                    )
                }
            }
        )
        list.apply {
            setupBottomPaddingWithProgressBar(progressBarHorizontal)
            layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            adapter = postAdapter.withLoadStateFooterSafe(NetworkLoadStateAdapter(postAdapter))
        }
        tagBlacklistViewModel.tags.observe(this.viewLifecycleOwner) { tagsBlacklist ->
            var tagsString = ""
            tagsBlacklist.forEach { tag ->
                tagsString = if (tagsString.isEmpty()) {
                    tag.name
                } else {
                    tagsString + "||" + tag.name
                }
            }
            search.deniedTags = tagsString
            postViewModel.load(search)
        }
        tagBlacklistViewModel.loadAll()
        postAdapter.addLoadStateListener { handleNetworkState(it) }
        lifecycleScope.launchWhenCreated {
            postViewModel.posts.collectLatest {
                postAdapter.submitData(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            postAdapter.loadStateFlow
                .asMergedLoadStates()
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { list.scrollToPosition(0) }
        }
        swipeRefresh.setOnRefreshListener { postAdapter.refresh() }
        postViewModel.load(search)
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    private fun handleNetworkState(loadStates: CombinedLoadStates) {
        val refresh = loadStates.mediator?.refresh
        val append = loadStates.mediator?.append
        swipeRefresh.isRefreshing = refresh is LoadState.Loading
        progressBarHorizontal.isVisible = append is LoadState.Loading
    }

    override fun onOrderChange(order: String) {
        search.order = order
        postViewModel.load(search)
        postAdapter.refresh()
    }

    override fun onDateRangeChange(dateRange: Int) {
        search.dateRange = dateRange
        postViewModel.load(search)
        postAdapter.refresh()
    }

    override fun onAspectRatioChange(aspect: String) {
        search.aspect = aspect
        postViewModel.load(search)
        postAdapter.refresh()
    }

    override fun onExtensionChange(
        isCheckedJpg: Boolean,
        isCheckedPng: Boolean,
        isCheckedGif: Boolean
    ) {
        search.apply {
            extJpg = isCheckedJpg
            extPng = isCheckedPng
            extGif = isCheckedGif
        }
        postViewModel.load(search)
        postAdapter.refresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PostActivity) {
            context.setQueryListener(this)
        }
    }

    private val spanCount: Int
        get() = activity?.getSanCount() ?: 3

    override fun onStart() {
        super.onStart()
        activity?.apply {
            ActivityCompat.setExitSharedElementCallback(this, sharedElementCallback)
            registerReceiver(broadcastReceiver, IntentFilter(JUMP_TO_TOP_ACTION_FILTER_KEY))
            registerReceiver(positionReceiver, IntentFilter(JUMP_TO_POSITION_ACTION_FILTER_KEY))
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.apply {
            ActivityCompat.setExitSharedElementCallback(this, null)
            unregisterReceiver(broadcastReceiver)
            unregisterReceiver(positionReceiver)
        }
    }

    override fun onDestroyView() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SETTINGS_PAGE_LIMIT_KEY -> {
                search.limit = Settings.pageLimit
                postViewModel.load(search)
            }
            USER_TOKEN_KEY -> {
                search.token = Settings.userToken
                postViewModel.load(search)
                postAdapter.refresh()
            }
        }
    }
}