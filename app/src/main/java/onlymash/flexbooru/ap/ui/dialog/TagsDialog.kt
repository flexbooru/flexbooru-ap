package onlymash.flexbooru.ap.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.data.db.dao.TagFilterDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.TagBlacklist
import onlymash.flexbooru.ap.data.model.TagFilter
import onlymash.flexbooru.ap.data.model.TagsFull
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.databinding.FastRecyclerviewBinding
import onlymash.flexbooru.ap.databinding.ItemTagBinding
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.activity.SearchActivity
import onlymash.flexbooru.ap.ui.base.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import org.kodein.di.erased.instance

class TagsDialog : BaseBottomSheetDialogFragment() {

    companion object {
        fun newInstance(postId: Int): TagsDialog {
            return TagsDialog().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID_KEY, postId)
                }
            }
        }
    }

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private val tagFilterDao by instance<TagFilterDao>()
    private val tagBlacklistDao by instance<TagBlacklistDao>()
    private var _binding: FastRecyclerviewBinding? = null
    private val binding get() = _binding!!

    private var postId = -1
    private var detail: Detail? = null

    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var tagAdapter: TagAdapter
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getInt(POST_ID_KEY, -1) ?: -1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        _binding = FastRecyclerviewBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }
        })
        tagAdapter = TagAdapter()
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = tagAdapter
        }
        detailViewModel = getViewModel(DetailViewModel(repo = DetailRepositoryImpl(api = api, detailDao = detailDao)))
        detailViewModel.detail.observe(this, Observer {
            if (it is NetResult.Success) {
                detail = it.data
                tagAdapter.notifyDataSetChanged()
            }
        })
        if (postId > -1) {
            detailViewModel.load(postId, Settings.userToken)
        } else {
            dismiss()
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    inner class TagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = detail?.tagsFull?.size ?: 0

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TagViewHolder).bind(detail?.tagsFull?.get(position))
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder = TagViewHolder(parent)

        inner class TagViewHolder(binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {

            constructor(parent: ViewGroup): this(parent.viewBinding(ItemTagBinding::inflate))

            private val tagDot = binding.tagDot
            private val tagName = binding.tagName
            private val tagCount = binding.tagCount
            private val tagAddToFilter = binding.tagAddToFilter
            private val tagAddToBlacklist = binding.tagAddToBlacklist

            private var tagFull: TagsFull? = null

            init {
                itemView.setOnClickListener {
                    tagFull?.name?.let { query ->
                        SearchActivity.startSearchActivity(itemView.context, query)
                    }
                }
                itemView.setOnLongClickListener {
                    itemView.context.copyText(tagFull?.name)
                    true
                }
                tagAddToFilter.setOnClickListener {
                    tagFull?.name?.let { name ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            tagFilterDao.insert(TagFilter(name = name))
                        }
                    }
                }
                tagAddToBlacklist.setOnClickListener {
                    tagFull?.name?.let { name ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            tagBlacklistDao.insert(TagBlacklist(name = name))
                        }
                    }
                }
            }

            fun bind(tag: TagsFull?) {
                tagFull = tag
                if (tag == null) return
                tagName.text = tag.name
                tagCount.text = tag.num.toString()
                val colorId = when (tag.type) {
                    1 -> R.color.color_tag_1_character
                    2 -> R.color.color_tag_2_reference
                    3 -> R.color.color_tag_3_copyright_product
                    4 -> R.color.color_tag_4_author
                    5 -> R.color.color_tag_5_copyright_game
                    6 -> R.color.color_tag_6_copyright_other
                    7 -> R.color.color_tag_7_object
                    else -> R.color.color_tag_0_unknown
                }
                tagDot.setColorFilter(ContextCompat.getColor(itemView.context, colorId))
            }
        }
    }
}