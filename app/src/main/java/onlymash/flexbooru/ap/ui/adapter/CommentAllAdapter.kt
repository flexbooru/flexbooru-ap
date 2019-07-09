package onlymash.flexbooru.ap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import io.noties.markwon.Markwon
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.CommentAll
import onlymash.flexbooru.ap.data.model.getMarkdownText
import onlymash.flexbooru.ap.extension.formatDate
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.widget.LinkTransformationMethod

class CommentAllAdapter(private val glide: GlideRequests,
                        private val markwon: Markwon
) : PagedListAdapter<CommentAll, RecyclerView.ViewHolder>(COMMENT_ALL_COMPARATOR) {

    companion object {
        val COMMENT_ALL_COMPARATOR = object : DiffUtil.ItemCallback<CommentAll>() {
            override fun areItemsTheSame(oldItem: CommentAll, newItem: CommentAll): Boolean =
                oldItem.comment.id == newItem.comment.id
            override fun areContentsTheSame(oldItem: CommentAll, newItem: CommentAll): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        CommentAllViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment_all, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            (holder as CommentAllViewHolder).bind(it)
        }
    }

    inner class CommentAllViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val postPreview: AppCompatImageView = itemView.findViewById(R.id.post_preview)
        private val usernameView: AppCompatTextView = itemView.findViewById(R.id.user_name)
        private val dateView: AppCompatTextView = itemView.findViewById(R.id.date_text)
        private val userAvatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
        private val commentView: AppCompatTextView = itemView.findViewById(R.id.comment_text)

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

        fun bind(item: CommentAll) {
            comment = item
            usernameView.text = item.user.userName
            dateView.text = item.comment.datetime.formatDate()
            markwon.setMarkdown(commentView, item.comment.getMarkdownText())
            val context = itemView.context
            item.user.userAvatar?.let {
                glide.load(it)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar_user))
                    .into(userAvatar)
            }
            glide.load(item.post.smallPreview)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.background_placeholder))
                .into(postPreview)
        }
    }
}