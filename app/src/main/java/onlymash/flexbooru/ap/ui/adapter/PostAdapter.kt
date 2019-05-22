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
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.base.BaseAdapter

const val MAX_ITEM_ASPECT_RATIO = 1.3333f
const val MIN_ITEM_ASPECT_RATIO = 0.5625f

class PostAdapter(
    private val glide: GlideRequests,
    retryCallback: () -> Unit) : BaseAdapter<Post, RecyclerView.ViewHolder>(
        diffCallback = POST_COMPARATOR,
        retryCallback = retryCallback) {

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostViewHolder).bind(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val preview: AppCompatImageView = itemView.findViewById(R.id.preview)

        fun bind(post: Post?) {
            if (post == null) return
            val lp = preview.layoutParams as ConstraintLayout.LayoutParams
            val ratio = post.width.toFloat() / post.height.toFloat()
            when {
                ratio > MAX_ITEM_ASPECT_RATIO -> {
                    lp.dimensionRatio = "H, $MAX_ITEM_ASPECT_RATIO:1"
                }
                ratio < MIN_ITEM_ASPECT_RATIO -> {
                    lp.dimensionRatio = "H, $MIN_ITEM_ASPECT_RATIO:1"
                }
                else -> {
                    lp.dimensionRatio = "H, $ratio:1"
                }
            }
            preview.layoutParams = lp
            glide.load(post.mediumPreview)
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