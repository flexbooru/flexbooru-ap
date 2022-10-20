package onlymash.flexbooru.ap.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.databinding.ItemPostBinding
import onlymash.flexbooru.ap.extension.getPreviewUrl
import onlymash.flexbooru.ap.glide.GlideRequests
import onlymash.flexbooru.ap.viewbinding.viewBinding

class PostAdapter(
    private val glide: GlideRequests,
    private val clickItemCallback: (String, View, String, Int) -> Unit
) : PagingDataAdapter<Post, PostAdapter.PostViewHolder>(POST_COMPARATOR) {

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id && oldItem.status == newItem.status
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun withLoadStateFooterSafe(
        footer: LoadStateAdapter<*>
    ): ConcatAdapter {
        val containerAdapter = ConcatAdapter(this)
        addLoadStateListener { loadStates ->
            footer.loadState = loadStates.append
            if (loadStates.append is LoadState.Error && !containerAdapter.adapters.contains(footer)) {
                containerAdapter.addAdapter(footer)
                footer.loadState = loadStates.append
            } else if (containerAdapter.adapters.contains(footer)){
                containerAdapter.removeAdapter(footer)
            }
        }
        return containerAdapter
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
}