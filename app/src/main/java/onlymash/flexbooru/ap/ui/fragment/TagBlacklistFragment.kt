package onlymash.flexbooru.ap.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.data.model.TagBlacklist
import onlymash.flexbooru.ap.databinding.FragmentTagBlacklistBinding
import onlymash.flexbooru.ap.databinding.ItemTagBlacklistBinding
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.diffcallback.TagBlacklistDiffCallback
import onlymash.flexbooru.ap.ui.viewmodel.TagBlacklistViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.extension.setupBottomPadding
import org.kodein.di.instance

class TagBlacklistFragment : KodeinFragment() {

    private var _binding: FragmentTagBlacklistBinding? = null
    private val binding get() = _binding!!

    private val tagsBlacklist: MutableList<TagBlacklist> = mutableListOf()
    private val tagBlacklistDao by instance<TagBlacklistDao>()
    private lateinit var tagBlacklistViewModel: TagBlacklistViewModel
    private lateinit var tagBlacklistAdapter: TagBlacklistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tagBlacklistViewModel = getViewModel(TagBlacklistViewModel(tagBlacklistDao))
        _binding = FragmentTagBlacklistBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagBlacklistAdapter = TagBlacklistAdapter()
        binding.tagsBlacklistList.apply {
            setupBottomPadding()
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    inner class TagBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = TagBlacklistViewHolder(parent)

        override fun getItemCount(): Int = tagsBlacklist.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TagBlacklistViewHolder).bind(tagsBlacklist[position])
        }

        inner class TagBlacklistViewHolder(binding: ItemTagBlacklistBinding) : RecyclerView.ViewHolder(binding.root) {

            constructor(parent: ViewGroup): this(parent.viewBinding(ItemTagBlacklistBinding::inflate))

            private val name = binding.name
            private val actionMenuView = binding.menuView

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