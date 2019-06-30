package onlymash.flexbooru.ap.ui.fragment

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_post.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.extension.getSanCount
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.DetailActivity
import onlymash.flexbooru.ap.ui.FROM_POSTS
import onlymash.flexbooru.ap.ui.base.QueryListener
import onlymash.flexbooru.ap.ui.adapter.PostAdapter
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.base.PostActivity
import onlymash.flexbooru.ap.ui.viewmodel.PostViewModel
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

const val JUMP_TO_TOP_KEY = "jump_to_top"
const val JUMP_TO_TOP_QUERY_KEY = "jump_to_top_query"
const val JUMP_TO_TOP_ACTION_FILTER_KEY = "jump_to_top_action_filter"

const val JUMP_TO_POSITION_KEY = "jump_to_position"
const val JUMP_TO_POSITION_QUERY_KEY = "jump_to_position_query"
const val JUMP_TO_POSITION_ACTION_FILTER_KEY = "jump_to_position_action_filter"

class PostFragment : KodeinFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener, QueryListener {

    private lateinit var postViewModel: PostViewModel

    private val sp by instance<SharedPreferences>()

    private val db by instance<MyDatabase>()
    private val api by instance<Api>()
    private val ioExecutor by instance<Executor>()
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
        var color = ""
        arguments?.apply {
            query = getString(QUERY_KEY) ?: ""
            searchType = getSerializable(SEARCH_TYPE_KEY) as? SearchType ?: SearchType.NORMAL
            userId = getInt(USER_ID_KEY)
            color = getString(COLOR_KEY) ?: ""
        }
        search = Search(
            scheme = Settings.scheme,
            host = Settings.hostname,
            query = query,
            limit = Settings.pageLimit,
            type = searchType,
            userId = userId,
            token = Settings.userToken,
            color = color
        )
        postViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PostViewModel(
                    db = db,
                    api = api,
                    ioExecutor = ioExecutor
                ) as T
            }
        })
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_post, container, false)

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
            },
            retryCallback = {
                postViewModel.retry()
            }
        )
        list.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            adapter = postAdapter
        }
        postViewModel.posts.observe(this, Observer {
            postAdapter.submitList(it)
        })
        postViewModel.networkState.observe(this, Observer {
            postAdapter.setNetworkState(it)
        })
        initSwipeToRefresh()
        postViewModel.load(search)
    }

    private fun initSwipeToRefresh() {
        postViewModel.refreshState.observe(this, Observer {
            refresh.isRefreshing = it == NetworkState.LOADING
        })
        refresh.setOnRefreshListener {
            postViewModel.refresh()
        }
    }

    override fun onOrderChange(order: String) {
        search.order = order
        postViewModel.load(search)
        postViewModel.refresh()
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
        requireActivity().apply {
            ActivityCompat.setExitSharedElementCallback(this, sharedElementCallback)
            registerReceiver(broadcastReceiver, IntentFilter(JUMP_TO_TOP_ACTION_FILTER_KEY))
            registerReceiver(positionReceiver, IntentFilter(JUMP_TO_POSITION_ACTION_FILTER_KEY))
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().apply {
            ActivityCompat.setExitSharedElementCallback(this, null)
            unregisterReceiver(broadcastReceiver)
            unregisterReceiver(positionReceiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SETTINGS_PAGE_LIMIT_KEY -> {
                search.limit = Settings.pageLimit
                postViewModel.load(search)
            }
            SETTINGS_SCHEME_KEY -> {
                search.scheme = Settings.scheme
                postViewModel.load(search)
                postViewModel.refresh()
            }
            SETTINGS_HOST_KEY -> {
                search.host = Settings.hostname
                postViewModel.load(search)
                postViewModel.refresh()
            }
        }
    }
}