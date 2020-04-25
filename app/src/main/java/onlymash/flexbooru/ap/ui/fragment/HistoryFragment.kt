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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.diffcallback.DetailDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.ListListener
import org.kodein.di.erased.instance


const val HISTORY_JUMP_TO_TOP_KEY = "history_jump_to_top"
const val HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY = "history_jump_to_top_action_filter"

class HistoryFragment : KodeinFragment() {

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val list get() = binding.layoutRv.list
    private val progressBar get() = binding.layoutProgress.progressBar

    private lateinit var detailViewModel: DetailViewModel
    private lateinit var historyAdapter: HistoryAdapter

    private var details: MutableList<Detail> = mutableListOf()

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        detailViewModel = getViewModel(DetailViewModel(repo = DetailRepositoryImpl(api = api, detailDao = detailDao)))
        _binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter = HistoryAdapter()
        list.apply {
            setOnApplyWindowInsetsListener(ListListener)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = historyAdapter
        }
        detailViewModel.details.observe(this.viewLifecycleOwner, Observer {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    val oldItems = mutableListOf<Detail>()
                    oldItems.addAll(details)
                    details.clear()
                    details.addAll(it)
                    DiffUtil.calculateDiff(DetailDiffCallback(oldItems, details))
                }
                result.dispatchUpdatesTo(historyAdapter)
                progressBar.isVisible = false
            }
        })
        detailViewModel.loadAll()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    inner class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = details.size

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = HistoryViewHolder(parent)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as HistoryViewHolder).bind(details[position])
        }

        inner class HistoryViewHolder(binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
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
                postId.text = getString(R.string.placeholder_post_id, data.id)
                postSize.text = Formatter.formatFileSize(itemView.context, data.size.toLong())
                postRes.text = getString(R.string.placeholder_post_res, data.width, data.height)
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