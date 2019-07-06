package onlymash.flexbooru.ap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import io.noties.markwon.Markwon
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.formatDate
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.widget.LinkTransformationMethod

class CommentAdapter(private val glide: GlideRequests,
                     private val markwon: Markwon) :
    PagedListAdapter<Comment, RecyclerView.ViewHolder>(COMMENT_COMPARATOR) {

    companion object {
        val COMMENT_COMPARATOR = object : DiffUtil.ItemCallback<Comment>() {

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.comment.id == newItem.comment.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommentViewHolder).bind(getItem(position))
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val avatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
        private val username: AppCompatTextView = itemView.findViewById(R.id.user_name)
        private val date: AppCompatTextView = itemView.findViewById(R.id.date)
        private val commentView: AppCompatTextView = itemView.findViewById(R.id.comment_text)
        private var common: Comment? =null

        init {
            itemView.setOnClickListener {
                common?.let {
                    UserActivity.startUserActivity(
                        context = itemView.context,
                        userId = it.user.id,
                        username = it.user.userName,
                        avatarUrl = it.user.userAvatar
                    )
                }
            }
        }

        fun bind(data: Comment?) {
            common = data ?: return
            data.user.userAvatar?.let {
                glide.load(it)
                    .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_user))
                    .into(avatar)
            }
            username.text = data.user.userName
            date.text = data.comment.datetime.formatDate()
            markwon.setMarkdown(commentView, data.comment.html)
            commentView.transformationMethod = LinkTransformationMethod()
        }
    }
}