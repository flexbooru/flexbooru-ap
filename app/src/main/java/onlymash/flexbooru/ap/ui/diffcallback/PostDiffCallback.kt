package onlymash.flexbooru.ap.ui.diffcallback

import androidx.recyclerview.widget.DiffUtil
import onlymash.flexbooru.ap.data.model.Post

class PostDiffCallback(
    private val oldItems: MutableList<Post>,
    private val newItems: MutableList<Post>
) : DiffUtil.Callback() {
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition].score == newItems[newItemPosition].score

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean  =
        oldItems[oldItemPosition].id == newItems[newItemPosition].id

    override fun getNewListSize(): Int = newItems.size

    override fun getOldListSize(): Int = oldItems.size
}