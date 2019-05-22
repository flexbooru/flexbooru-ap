package onlymash.flexbooru.ap.ui.base

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.ui.viewholder.NetworkStateViewHolder

const val ITEM_TYPE_NETWORK_STATE = 0
const val ITEM_TYPE_NORMAL = 1

abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val retryCallback: () -> Unit) : PagedListAdapter<T, VH>(diffCallback) {

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1)
            ITEM_TYPE_NETWORK_STATE
        else
            ITEM_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        @Suppress("UNCHECKED_CAST")
        return when (viewType) {
            ITEM_TYPE_NETWORK_STATE -> NetworkStateViewHolder.create(parent, retryCallback) as VH
            else -> onCreateItemViewHolder(parent, viewType)
        }
    }

    abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder) {
            is NetworkStateViewHolder -> {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
                holder.bindTo(networkState)
            }
            else -> onBindItemViewHolder(holder, position)
        }
    }

    abstract fun onBindItemViewHolder(holder: VH, position: Int)

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}