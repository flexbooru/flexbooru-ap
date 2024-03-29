package onlymash.flexbooru.ap.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.common.USER_TOKEN_KEY
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.repository.comment.CommentAllRepositoryImpl
import onlymash.flexbooru.ap.databinding.FragmentListRefreshableBinding
import onlymash.flexbooru.ap.extension.asMergedLoadStates
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.adapter.CommentAllAdapter
import onlymash.flexbooru.ap.ui.viewmodel.CommentAllViewModel
import onlymash.flexbooru.ap.extension.setupBottomPaddingWithProgressBar
import onlymash.flexbooru.ap.ui.adapter.NetworkLoadStateAdapter
import onlymash.flexbooru.ap.ui.base.BaseFragment
import org.kodein.di.instance

class CommentAllFragment : BaseFragment<FragmentListRefreshableBinding>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val api by instance<Api>()

    private val list get() = binding.layoutList.layoutRv.list
    private val swipeRefresh get() = binding.layoutList.refresh
    private val progressBarHorizontal get() = binding.layoutProgressHorizontal.progressBarHorizontal

    private lateinit var token: String
    private lateinit var commentAllViewModel: CommentAllViewModel
    private lateinit var commentAllAdapter: CommentAllAdapter
    private val repo by lazy { CommentAllRepositoryImpl(api = api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = Settings.userToken
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListRefreshableBinding {
        commentAllViewModel = getViewModel(CommentAllViewModel(repo = repo))
        return FragmentListRefreshableBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val glide = GlideApp.with(this)
        val markdown = Markwon.builder(requireContext())
            .usePlugin(GlideImagesPlugin.create(glide))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .build()
        commentAllAdapter = CommentAllAdapter(glide, markdown)
        list.apply {
            setupBottomPaddingWithProgressBar(progressBarHorizontal)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            adapter = commentAllAdapter.withLoadStateFooterSafe(NetworkLoadStateAdapter(commentAllAdapter))
        }
        commentAllAdapter.addLoadStateListener { handleNetworkState(it) }
        lifecycleScope.launchWhenCreated {
            commentAllViewModel.comments.collectLatest {
                commentAllAdapter.submitData(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            commentAllAdapter.loadStateFlow
                .asMergedLoadStates()
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { list.scrollToPosition(0) }
        }
        commentAllViewModel.show(token)
        swipeRefresh.setOnRefreshListener {
            commentAllAdapter.refresh()
        }
    }

    private fun handleNetworkState(loadStates: CombinedLoadStates) {
        swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
        progressBarHorizontal.isVisible = loadStates.append is LoadState.Loading
    }

    override fun onDestroyView() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == USER_TOKEN_KEY) {
            commentAllViewModel.show(Settings.userToken)
            commentAllAdapter.refresh()
        }
    }
}