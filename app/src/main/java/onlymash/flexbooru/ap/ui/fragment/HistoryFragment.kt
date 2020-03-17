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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fast_recyclerview.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.activity.FROM_HISTORY
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.diffcallback.DetailDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.widget.ListListener
import org.kodein.di.erased.instance


const val HISTORY_JUMP_TO_TOP_KEY = "history_jump_to_top"
const val HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY = "history_jump_to_top_action_filter"

class HistoryFragment : KodeinFragment() {

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()

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
        detailViewModel = getViewModel(
            DetailViewModel(
                repo = DetailRepositoryImpl(api = api, detailDao = detailDao)
            )
        )
        return inflater.inflate(R.layout.fragment_history, container, false)
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
            private val postRes: AppCompatTextView = itemView.findViewById(R.id.post_res)
            private val postSize: AppCompatTextView = itemView.findViewById(R.id.post_size)
            private val userAvatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
            private var detail: Detail? = null

            init {
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