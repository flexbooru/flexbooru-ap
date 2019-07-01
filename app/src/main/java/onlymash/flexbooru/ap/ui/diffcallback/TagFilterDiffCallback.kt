package onlymash.flexbooru.ap.ui.diffcallback

import androidx.recyclerview.widget.DiffUtil
import onlymash.flexbooru.ap.data.model.TagFilter

class TagFilterDiffCallback(
    private val oldItems: MutableList<TagFilter>,
    private val newItems: MutableList<TagFilter>
) : DiffUtil.Callback() {

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition].name == newItems[newItemPosition].name

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean  =
        oldItems[oldItemPosition].uid == newItems[newItemPosition].uid

    override fun getNewListSize(): Int = newItems.size

    override fun getOldListSize(): Int = oldItems.size
}