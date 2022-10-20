package onlymash.flexbooru.ap.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.databinding.FragmentListBinding
import onlymash.flexbooru.ap.databinding.ItemHistoryBinding
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.FROM_HISTORY
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.extension.setupBottomPadding
import onlymash.flexbooru.ap.ui.base.BaseFragment
import org.kodein.di.instance


const val HISTORY_JUMP_TO_TOP_KEY = "history_jump_to_top"
const val HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY = "history_jump_to_top_action_filter"

class HistoryFragment : BaseFragment<FragmentListBinding>() {

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private val list get() = binding.layoutRv.list
    private val progressBar get() = binding.layoutProgress.progressBar

    private lateinit var detailViewModel: DetailViewModel
    private lateinit var historyAdapter: HistoryAdapter

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.getBooleanExtra(HISTORY_JUMP_TO_TOP_KEY, false)) {
                list.scrollToPosition(0)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(broadcastReceiver, IntentFilter(HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY))
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListBinding {
        detailViewModel = getViewModel(DetailViewModel(repo = DetailRepositoryImpl(api = api, detailDao = detailDao)))
        return FragmentListBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter = HistoryAdapter()
        list.apply {
            setupBottomPadding()
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = historyAdapter
        }
        lifecycleScope.launch {
            detailViewModel.details.collectLatest {
                if (progressBar.isVisible) {
                    progressBar.isVisible = false
                }
                historyAdapter.submitData(it)
            }
        }
    }

    class HistoryAdapter : PagingDataAdapter<Detail, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

        companion object {
           private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Detail>() {
                override fun areItemsTheSame(oldItem: Detail, newItem: Detail): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Detail, newItem: Detail): Boolean {
                    return oldItem.id == newItem.id && oldItem.fileUrl == newItem.fileUrl
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            return HistoryViewHolder(parent)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class HistoryViewHolder(binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
            constructor(parent: ViewGroup): this(parent.viewBinding(ItemHistoryBinding::inflate))

            private val postPreview = binding.postPreview
            private val postId = binding.postId
            private val postRes = binding.postRes
            private val postSize = binding.postSize
            private val userAvatar = binding.userAvatar
            private var detail: Detail? = null

            init {
                itemView.setOnClickListener {
                    DetailActivity.startDetailActivity(
                        context = itemView.context,
                        fromWhere = FROM_HISTORY,
                        position = layoutPosition
                    )
                }
                userAvatar.setOnClickListener {
                    detail?.let {
                        UserActivity.startUserActivity(
                            context = itemView.context,
                            userId = it.userId,
                            username = it.userName,
                            avatarUrl = it.userAvatar)
                    }
                }
            }

            fun bind(data: Detail?) {
                detail = data ?: return
                postId.text = itemView.context.getString(R.string.placeholder_post_id, data.id)
                postSize.text = Formatter.formatFileSize(itemView.context, data.size.toLong())
                postRes.text = itemView.context.getString(R.string.placeholder_post_res, data.width, data.height)
                val context = itemView.context
                GlideApp.with(context)
                    .load(data.smallPreview)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.background_placeholder))
                    .centerCrop()
                    .into(postPreview)
                data.userAvatar?.let {
                    GlideApp.with(context)
                        .load(it)
                        .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar_user))
                        .centerCrop()
                        .into(userAvatar)
                }
            }
        }
    }

}