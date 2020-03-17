package onlymash.flexbooru.ap.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_tag_blacklist.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.data.model.TagBlacklist
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.diffcallback.TagBlacklistDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.TagBlacklistViewModel
import onlymash.flexbooru.ap.widget.ListListener
import org.kodein.di.erased.instance

class TagBlacklistFragment : KodeinFragment() {

    private val tagsBlacklist: MutableList<TagBlacklist> = mutableListOf()

    private val tagBlacklistDao by instance<TagBlacklistDao>()
    private lateinit var tagBlacklistViewModel: TagBlacklistViewModel
    private lateinit var tagBlacklistAdapter: TagBlacklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagBlacklistViewModel = getViewModel(TagBlacklistViewModel(tagBlacklistDao))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tag_blacklist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagBlacklistAdapter = TagBlacklistAdapter()
        tags_blacklist_list.apply {
            setOnApplyWindowInsetsListener(ListListener)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = tagBlacklistAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
        tagBlacklistViewModel.tags.observe(this.viewLifecycleOwner, Observer {
            val oldItems = mutableListOf<TagBlacklist>()
            oldItems.addAll(tagsBlacklist)
            tagsBlacklist.clear()
            tagsBlacklist.addAll(it)
            val result = DiffUtil.calculateDiff(TagBlacklistDiffCallback(oldItems, tagsBlacklist))
            result.dispatchUpdatesTo(tagBlacklistAdapter)
        })
        tagBlacklistViewModel.loadAll()
    }

    inner class TagBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TagBlacklistViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tag_blacklist, parent, false))

        override fun getItemCount(): Int = tagsBlacklist.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TagBlacklistViewHolder).bind(tagsBlacklist[position])
        }

        inner class TagBlacklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val name: AppCompatTextView = itemView.findViewById(R.id.name)
            private val actionMenuView: ActionMenuView = itemView.findViewById(R.id.menu_view)

            private var tag: TagBlacklist? = null

            init {
                MenuInflater(itemView.context).inflate(R.menu.tag_filter_item, actionMenuView.menu)
                actionMenuView.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_copy -> tag?.let {
                            itemView.context.copyText(it.name)
                        }
                        R.id.action_delete -> tag?.let {
                            tagBlacklistViewModel.delete(it)
                        }
                    }
                    true
                }
            }

            fun bind(data: TagBlacklist) {
                tag = data
                name.text = data.name
            }
        }
    }
}