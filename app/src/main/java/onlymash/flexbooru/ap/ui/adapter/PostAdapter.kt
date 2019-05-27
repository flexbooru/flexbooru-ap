package onlymash.flexbooru.ap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.extension.getPreviewUrl
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.base.BaseAdapter

class PostAdapter(
    private val glide: GlideRequests,
    private val clickItemCallback: (String, View, String, Int) -> Unit,
    retryCallback: () -> Unit) : BaseAdapter<Post, RecyclerView.ViewHolder>(
        diffCallback = POST_COMPARATOR,
        retryCallback = retryCallback) {

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = getItem(position)
        (holder as PostViewHolder).bind(post)
        holder.itemView.setOnClickListener {
            val query = post?.query ?: return@setOnClickListener
            clickItemCallback(query, holder.preview, "post_${post.id}", position)
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val preview: AppCompatImageView = itemView.findViewById(R.id.preview)

        fun bind(post: Post?) {
            if (post == null) return
            preview.transitionName = "post_${post.id}"
            val lp = preview.layoutParams as ConstraintLayout.LayoutParams
            lp.dimensionRatio = "H, ${post.width}:${post.height}"
            preview.layoutParams = lp
            glide.load(post.getPreviewUrl())
                .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.background_card))
                .into(preview)
        }
    }

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
        }
    }
}