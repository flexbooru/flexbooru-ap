package onlymash.flexbooru.ap.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.fast_recyclerview.*
import kotlinx.android.synthetic.main.fragment_comment_all.error_msg
import kotlinx.android.synthetic.main.fragment_comment_all.retry_button
import kotlinx.android.synthetic.main.fragment_comment_all.status_container
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.common.USER_TOKEN_KEY
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.Status
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.repository.comment.CommentAllRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.adapter.CommentAllAdapter
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.CommentAllViewModel
import onlymash.flexbooru.ap.widget.ListListener
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

class CommentAllFragment : KodeinFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val api by instance<Api>()
    private val executor by instance<Executor>()

    private lateinit var token: String

    private lateinit var commentAllViewModel: CommentAllViewModel
    private val repo by lazy { CommentAllRepositoryImpl(
        api = api,
        networkExecutor = executor
    ) }

    private lateinit var commentAllAdapter: CommentAllAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = Settings.userToken
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comment_all, container, false)
        commentAllViewModel = getViewModel(CommentAllViewModel(
            repo = repo)
        )
        return view
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
            setOnApplyWindowInsetsListener(ListListener)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            adapter = commentAllAdapter
        }
        commentAllViewModel.comments.observe(this.viewLifecycleOwner, Observer {
            commentAllAdapter.submitList(it)
        })
        commentAllViewModel.networkState.observe(this.viewLifecycleOwner, Observer {
            if (progress_bar.isVisible && (it == NetworkState.LOADED || it.status == Status.FAILED)) {
                progress_bar.visibility = View.GONE
            }
            if (it.status == Status.FAILED) {
                status_container.isVisible = true
                error_msg.text = it.msg
            } else {
                status_container.isVisible = false
            }
        })
        commentAllViewModel.refreshState.observe(this.viewLifecycleOwner, Observer {
            if (it != NetworkState.LOADING) {
                refresh.isRefreshing = false
            }
        })
        commentAllViewModel.show(token)
        refresh.setOnRefreshListener {
            status_container.isVisible = false
            commentAllViewModel.refresh()
        }
        retry_button.setOnClickListener {
            status_container.isVisible = false
            commentAllViewModel.retry()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == USER_TOKEN_KEY) {
            commentAllViewModel.show(Settings.userToken)
            commentAllViewModel.refresh()
        }
    }
}