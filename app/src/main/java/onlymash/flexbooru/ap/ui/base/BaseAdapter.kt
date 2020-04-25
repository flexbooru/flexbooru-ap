package onlymash.flexbooru.ap.ui.base

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.isFailed
import onlymash.flexbooru.ap.databinding.ItemNetworkStateBinding
import onlymash.flexbooru.ap.viewbinding.viewBinding

const val ITEM_TYPE_NETWORK_STATE = 0
const val ITEM_TYPE_NORMAL = 1

abstract class BaseAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val retryCallback: () -> Unit) : PagedListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    private var networkState: NetworkState? = null

    private fun hasExtraRow() = networkState.isFailed()

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1)
            ITEM_TYPE_NETWORK_STATE
        else
            ITEM_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_NETWORK_STATE -> NetworkStateViewHolder(parent)
            else -> onCreateItemViewHolder(parent, viewType)
        }
    }

    abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BaseAdapter<*>.NetworkStateViewHolder -> {
                val layoutParams = holder.itemView.layoutParams
                if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    layoutParams.isFullSpan = true
                }
                holder.bindTo(networkState)
            }
            else -> onBindItemViewHolder(holder, position)
        }
    }

    abstract fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int)

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

    inner class NetworkStateViewHolder(binding: ItemNetworkStateBinding) : RecyclerView.ViewHolder(binding.root) {
        constructor(parent: ViewGroup): this(parent.viewBinding(ItemNetworkStateBinding::inflate))
        private val retry = binding.retryButton
        private val errorMsg = binding.errorMsg
        init {
            retry.setOnClickListener {
                retryCallback.invoke()
            }
        }
        fun bindTo(networkState: NetworkState?) {
            itemView.isVisible = networkState.isFailed()
            errorMsg.text = networkState?.msg
        }
    }
}