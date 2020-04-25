package onlymash.flexbooru.ap.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.databinding.ItemPostBinding
import onlymash.flexbooru.ap.extension.getPreviewUrl
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.ui.base.BaseAdapter
import onlymash.flexbooru.ap.viewbinding.viewBinding

class PostAdapter(
    private val glide: GlideRequests,
    private val clickItemCallback: (String, View, String, Int) -> Unit,
    retryCallback: () -> Unit) : BaseAdapter<Post>(
        diffCallback = POST_COMPARATOR,
        retryCallback = retryCallback) {

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        viewType: Int): RecyclerView.ViewHolder = PostViewHolder(parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostViewHolder).bind(getItem(position))
    }

    inner class PostViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemPostBinding::inflate))

        val preview = binding.preview
        private var post: Post? = null

        init {
            itemView.setOnClickListener {
                post?.let {
                    clickItemCallback.invoke(it.query, preview, "post_${it.id}", layoutPosition)
                }
            }
        }

        fun bind(post: Post?) {
            this.post = post ?: return
            preview.transitionName = "post_${post.id}"
            preview.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = "${post.width}:${post.height}"
            }
            glide.load(post.getPreviewUrl())
                .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.background_placeholder))
                .into(preview)
        }
    }

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
        }
    }
}