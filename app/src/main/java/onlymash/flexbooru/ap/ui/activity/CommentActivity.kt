package onlymash.flexbooru.ap.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.launch
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.data.model.getMarkdownText
import onlymash.flexbooru.ap.data.repository.comment.CommentRepositoryImpl
import onlymash.flexbooru.ap.databinding.ActivityCommentBinding
import onlymash.flexbooru.ap.databinding.ItemCommentBinding
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.diffcallback.CommentDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.CommentViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.LinkTransformationMethod
import onlymash.flexbooru.ap.extension.setupInsets
import org.kodein.di.instance

class CommentActivity : KodeinActivity() {

    companion object {
        fun startActivity(context: Context, postId: Int) {
            context.startActivity(Intent(context, CommentActivity::class.java).apply {
                putExtra(POST_ID_KEY, postId)
            })
        }
    }

    private val comments: MutableList<Comment> = mutableListOf()

    private val api by instance<Api>()
    private val repo by lazy { CommentRepositoryImpl(api) }
    private val binding by viewBinding(ActivityCommentBinding::inflate)
    private val toolbarContainer get() = binding.layoutAppBar.containerToolbar
    private val toolbar get() = binding.layoutAppBar.toolbar

    private lateinit var commentViewModel: CommentViewModel
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets { insets ->
            toolbarContainer.minimumHeight = toolbar.minimumHeight + insets.top
            binding.containerCommentBox.updatePadding(bottom = insets.bottom + resources.getDimensionPixelSize(R.dimen.elevation))
        }
        val postId = intent?.getIntExtra(POST_ID_KEY, -1) ?: -1
        toolbarContainer.updateLayoutParams<AppBarLayout.LayoutParams> {
            scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        }
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_comments)
            subtitle = getString(R.string.placeholder_post_id, postId)
        }
        val glide = GlideApp.with(this)
        val markdown = Markwon.builder(this)
            .usePlugin(GlideImagesPlugin.create(glide))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .build()
        commentAdapter = CommentAdapter(glide = glide, markwon = markdown)
        binding.commentsList.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter
            addItemDecoration(DividerItemDecoration(this@CommentActivity, RecyclerView.VERTICAL))
        }
        TooltipCompat.setTooltipText(binding.commentSend, getText(R.string.action_comment_send))
        TooltipCompat.setTooltipText(binding.userAvatar, getText(R.string.title_account))
        val user = UserManager.getUserByUid(Settings.userUid) ?: return
        commentViewModel = getViewModel(CommentViewModel(
            repo = repo,
            token = user.token)
        )
        commentViewModel.comments.observe(this, Observer {
            val oldItems = mutableListOf<Comment>()
            oldItems.addAll(comments)
            comments.clear()
            comments.addAll(it)
            val result = DiffUtil.calculateDiff(CommentDiffCallback(oldItems, comments))
            result.dispatchUpdatesTo(commentAdapter)
        })
        commentViewModel.status.observe(this) {
            when (it) {
                is States.Loading -> {
                    binding.apply {
                        statusContainer.isVisible = true
                        retryButton.isVisible = false
                        errorMsg.isVisible = false
                        commentsRefresh.isRefreshing = true
                    }
                }
                is States.Success -> {
                    binding.statusContainer.isVisible = false
                    binding.commentsRefresh.isRefreshing = false
                }
                is States.Error -> {
                    binding.apply {
                        statusContainer.isVisible = true
                        retryButton.isVisible = true
                        errorMsg.isVisible = it.errorMsg.isNotEmpty()
                        errorMsg.text = it.errorMsg
                        commentsRefresh.isRefreshing = false
                    }
                }
            }
        }
        if (postId > 0) {
            commentViewModel.loadComments(postId)
        }
        binding.commentsRefresh.setOnRefreshListener { commentViewModel.loadComments(postId) }
        binding.retryButton.setOnClickListener {
            binding.statusContainer.isVisible = false
            commentViewModel.loadComments(postId)
        }
        binding.commentSend.setOnClickListener {
            val text = binding.commentEdit.text?.toString() ?: ""
            if (text.length >= 2) {
                binding.commentSend.isVisible = false
                binding.commentSendProgressBar.isVisible = true
                lifecycleScope.launch {
                    when (
                        val result = repo.createComment(
                            url = getCreateCommentUrl(postId),
                            text = text,
                            token = user.token
                        )) {

                        is NetResult.Success -> {
                            commentViewModel.loadComments(postId)
                            binding.commentEdit.text?.clear()
                        }
                        is NetResult.Error -> {
                            Toast.makeText(this@CommentActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                    binding.commentSendProgressBar.isVisible = false
                    binding.commentSend.isVisible = true
                }
            } else {
                binding.commentEdit.error = getString(R.string.msg_minimum_two_characters)
            }
        }
        binding.userAvatar.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
        val avatarUrl = user.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            glide.load(avatarUrl)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.avatar_user))
                .into(binding.userAvatar)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    inner class CommentAdapter(private val glide: GlideRequests,
                               private val markwon: Markwon) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = CommentViewHolder(parent)

        override fun getItemCount(): Int = comments.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CommentViewHolder).bindTo(comments[position])
        }

        inner class CommentViewHolder(binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

            constructor(parent: ViewGroup): this(parent.viewBinding(ItemCommentBinding::inflate))

            private val avatar = binding.userAvatar
            private val username = binding.userName
            private val date = binding.dateText
            private val commentView = binding.commentText
            private var comment: Comment? = null

            init {
                itemView.setOnClickListener {
                    comment?.let {
                        UserActivity.startUserActivity(
                            context = this@CommentActivity,
                            userId = it.user.id,
                            username = it.user.userName,
                            avatarUrl = it.user.userAvatar
                        )
                    }
                }
                commentView.apply {
                    movementMethod = BetterLinkMovementMethod.getInstance()
                    transformationMethod = LinkTransformationMethod()
                }
            }

            fun bindTo(data: Comment) {
                comment = data
                if (data.user.userAvatar != null) {
                    glide.load(data.user.userAvatar)
                        .placeholder(ContextCompat.getDrawable(this@CommentActivity, R.drawable.avatar_user))
                        .into(avatar)
                } else {
                    avatar.setImageDrawable(ContextCompat.getDrawable(this@CommentActivity, R.drawable.avatar_user))
                }
                username.text = data.user.userName
                date.text = data.comment.datetime.formatDate()
                markwon.setMarkdown(commentView, data.comment.getMarkdownText())
            }
        }
    }
}
