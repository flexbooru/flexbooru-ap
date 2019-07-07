package onlymash.flexbooru.ap.ui.diffcallback

import androidx.recyclerview.widget.DiffUtil
import onlymash.flexbooru.ap.data.model.Comment

class CommentDiffCallback(
    private val oldItems: MutableList<Comment>,
    private val newItems: MutableList<Comment>
) : DiffUtil.Callback() {
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition] == newItems[newItemPosition]

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean  =
        oldItems[oldItemPosition].comment.id == newItems[newItemPosition].comment.id

    override fun getNewListSize(): Int = newItems.size

    override fun getOldListSize(): Int = oldItems.size
}