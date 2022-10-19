package onlymash.flexbooru.ap.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import onlymash.flexbooru.ap.databinding.ItemNetworkStateBinding
import onlymash.flexbooru.ap.viewbinding.viewBinding

class NetworkLoadStateAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private val adapter: PagingDataAdapter<T, VH>,
) : LoadStateAdapter<NetworkLoadStateAdapter.NetworkStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): NetworkStateViewHolder {
        return NetworkStateViewHolder(parent) {
            adapter.retry()
        }
    }

    override fun onBindViewHolder(holder: NetworkStateViewHolder, loadState: LoadState) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = true
        }
        holder.bindTo(loadState)
    }

    class NetworkStateViewHolder(binding: ItemNetworkStateBinding, retryCallback: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup, retryCallback: () -> Unit) : this(
            parent.viewBinding(ItemNetworkStateBinding::inflate),
            retryCallback
        )

        private val errorMsg = binding.errorMsg
        private val retryButton = binding.retryButton.also {
            it.setOnClickListener { retryCallback() }
        }

        fun bindTo(loadState: LoadState) {
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
            errorMsg.text = (loadState as? LoadState.Error)?.error?.message
        }
    }
}