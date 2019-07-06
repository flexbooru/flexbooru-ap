package onlymash.flexbooru.ap.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.item_network_state.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.Status
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.repository.comment.CommentPagingRepositoryImpl
import onlymash.flexbooru.ap.data.repository.comment.CommentRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.extension.toVisibility
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.adapter.CommentAdapter
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.viewmodel.CommentViewModel
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

class CommentActivity : KodeinActivity() {

    companion object {
        fun startActivity(context: Context, postId: Int) {
            context.startActivity(Intent(context, CommentActivity::class.java).apply {
                putExtra(POST_ID_KEY, postId)
            })
        }
    }

    private val api by instance<Api>()
    private val ioExecutor by instance<Executor>()

    private val repo by lazy {
        CommentPagingRepositoryImpl(
            repo = CommentRepositoryImpl(api),
            networkExecutor = ioExecutor
        )
    }

    private lateinit var commentViewModel: CommentViewModel
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val postId = intent?.getIntExtra(POST_ID_KEY, -1) ?: -1
        (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_comments)
            subtitle = "Post: $postId"
        }
        val glide = GlideApp.with(this)
        val markdown = Markwon.builder(this)
            .usePlugin(GlideImagesPlugin.create(glide))
            .usePlugin(HtmlPlugin.create())
            .build()
        commentAdapter = CommentAdapter(glide = glide, markwon = markdown)
        comments_list.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter
            addItemDecoration(DividerItemDecoration(this@CommentActivity, RecyclerView.VERTICAL))
        }
        val scheme = Settings.scheme
        val host = Settings.hostname
        val token = Settings.userToken
        commentViewModel = getViewModel(CommentViewModel(
            repo = repo,
            scheme = scheme,
            host = host,
            token = token)
        )
        commentViewModel.comments.observe(this, Observer {
            commentAdapter.submitList(it)
        })
        commentViewModel.networkState.observe(this, Observer {
            if (it != null && it != NetworkState.LOADED) {
                network_state_container.toVisibility(true)
                progress_bar.toVisibility(it.status == Status.RUNNING)
                retry_button.toVisibility(it.status == Status.FAILED)
                error_msg.toVisibility(it.msg != null)
                error_msg.text = it.msg
            } else {
                network_state_container.toVisibility(false)
            }
        })
        commentViewModel.refreshState.observe(this, Observer {
            comments_refresh.isRefreshing = it == NetworkState.LOADING
        })
        if (postId > 0) {
            commentViewModel.loadComments(postId)
        }
        comments_refresh.setOnRefreshListener {
            commentViewModel.refresh()
        }
        retry_button.setOnClickListener {
            commentViewModel.retry()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
