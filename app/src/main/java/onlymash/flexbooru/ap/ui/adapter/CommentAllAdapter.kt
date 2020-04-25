package onlymash.flexbooru.ap.ui.adapter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.CommentAll
import onlymash.flexbooru.ap.data.model.getMarkdownText
import onlymash.flexbooru.ap.databinding.ItemCommentAllBinding
import onlymash.flexbooru.ap.extension.formatDate
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.ui.base.BaseAdapter
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.LinkTransformationMethod

class CommentAllAdapter(
    private val glide: GlideRequests,
    private val markwon: Markwon,
    retryCallback: () -> Unit
) : BaseAdapter<CommentAll>(COMMENT_ALL_COMPARATOR, retryCallback) {

    companion object {
        val COMMENT_ALL_COMPARATOR = object : DiffUtil.ItemCallback<CommentAll>() {
            override fun areItemsTheSame(oldItem: CommentAll, newItem: CommentAll): Boolean =
                oldItem.comment.id == newItem.comment.id
            override fun areContentsTheSame(oldItem: CommentAll, newItem: CommentAll): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommentAllViewHolder(parent)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommentAllViewHolder).bind(getItem(position))
    }

    inner class CommentAllViewHolder(binding: ItemCommentAllBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemCommentAllBinding::inflate))

        private val postPreview = binding.postPreview
        private val usernameView = binding.userName
        private val dateView = binding.dateText
        private val userAvatar = binding.userAvatar
        private val commentView = binding.commentText
        private val postIdView = binding.postId

        private var comment: CommentAll? = null

        init {
            itemView.setOnClickListener {
                comment?.let {
                    DetailActivity.startDetailActivityFromComment(itemView.context, it.post.id)
                }
            }
            userAvatar.setOnClickListener {
                comment?.let {
                    UserActivity.startUserActivity(
                        context = itemView.context,
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

        fun bind(item: CommentAll?) {
            comment = item ?: return
            usernameView.text = item.user.userName
            dateView.text = item.comment.datetime.formatDate()
            markwon.setMarkdown(commentView, item.comment.getMarkdownText())
            val context = itemView.context
            postIdView.text = context.getString(R.string.placeholder_post_id, item.post.id)
            if (item.user.userAvatar != null) {
                glide.load(item.user.userAvatar)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar_user))
                    .into(userAvatar)
            } else {
                userAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar_user))
            }
            glide.load(item.post.smallPreview)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.background_placeholder))
                .into(postPreview)
        }
    }
}