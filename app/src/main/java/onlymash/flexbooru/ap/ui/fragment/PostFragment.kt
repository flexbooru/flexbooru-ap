package onlymash.flexbooru.ap.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_post.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.adapter.PostAdapter
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.PostViewModel
import org.kodein.di.generic.instance
import java.util.concurrent.Executor

const val QUERY_KEY = "query_key"
const val JUMP_TO_TOP_KEY = "jump_to_top"
const val JUMP_TO_TOP_ACTION_FILTER_KEY = "jump_to_top_action_filter"

class PostFragment : KodeinFragment() {

    private lateinit var postViewModel: PostViewModel

    private val db by instance<MyDatabase>()
    private val api by instance<Api>()
    private val ioExecutor by instance<Executor>()
    private lateinit var search: Search
    private lateinit var postAdapter: PostAdapter

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isJump = intent?.getBooleanExtra(JUMP_TO_TOP_KEY, false) ?: return
            if (isJump) {
                list.scrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        search = Search(
            scheme = Settings.scheme,
            host = Settings.hostname,
            query = arguments?.getString(QUERY_KEY) ?: ""
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val glide = GlideApp.with(requireContext())
        postAdapter = PostAdapter(glide) {
            postViewModel.retry()
        }
        list.apply {
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            adapter = postAdapter
        }
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(JUMP_TO_TOP_ACTION_FILTER_KEY))
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

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    companion object {
        fun newInstance(query: String): PostFragment {
            return PostFragment().apply {
                arguments = Bundle().apply {
                    putString(QUERY_KEY, query)
                }
            }
        }
    }
}