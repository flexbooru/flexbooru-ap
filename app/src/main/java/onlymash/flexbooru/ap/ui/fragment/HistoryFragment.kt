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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.FROM_HISTORY
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.diffcallback.DetailDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import org.kodein.di.erased.instance


const val HISTORY_JUMP_TO_TOP_KEY = "history_jump_to_top"
const val HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY = "history_jump_to_top_action_filter"

class HistoryFragment : KodeinFragment() {

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()

    private lateinit var scheme: String
    private lateinit var host: String
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var historyAdapter: HistoryAdapter

    private var details: MutableList<Detail> = mutableListOf()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            if (intent.getBooleanExtra(HISTORY_JUMP_TO_TOP_KEY, false)) {
                history_list.scrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheme = Settings.scheme
        host = Settings.hostname
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY))
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        detailViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DetailViewModel(
                    repo = DetailRepositoryImpl(api = api, detailDao = detailDao),
                    scheme = scheme,
                    host = host
                ) as T
            }
        })
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter = HistoryAdapter()
        history_list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = historyAdapter
        }
        detailViewModel.details.observe(this, Observer {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    val oldItems = mutableListOf<Detail>()
                    oldItems.addAll(details)
                    details.clear()
                    details.addAll(it)
                    DiffUtil.calculateDiff(DetailDiffCallback(oldItems, details))
                }
                result.dispatchUpdatesTo(historyAdapter)
                progress_bar.visibility = View.GONE
            }
        })
        detailViewModel.loadAll()
    }

    inner class HistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = details.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as HistoryViewHolder).bind(details[position])
            holder.itemView.setOnClickListener {
                DetailActivity.startDetailActivity(
                    context = requireContext(),
                    fromWhere = FROM_HISTORY,
                    position = position
                )
            }
        }

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val postPreview: AppCompatImageView = itemView.findViewById(R.id.post_preview)
            private val postId: AppCompatTextView = itemView.findViewById(R.id.post_id)
            private val postSize: AppCompatTextView = itemView.findViewById(R.id.post_size)
            private var detail: Detail? = null

            fun bind(data: Detail?) {
                detail = data ?: return
                postId.text = getString(R.string.placeholder_post_id, data.id)
                postSize.text = getString(
                    R.string.placeholder_post_size,
                    Formatter.formatFileSize(itemView.context, data.size.toLong()),
                    data.width,
                    data.height
                )
                GlideApp.with(itemView.context)
                    .load(data.mediumPreview)
                    .centerCrop()
                    .into(postPreview)
            }
        }
    }

}