package onlymash.flexbooru.ap.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.TagsFull
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.SearchActivity
import onlymash.flexbooru.ap.ui.base.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import org.kodein.di.generic.instance

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

    private var postId = -1
    private var detail: Detail? = null

    private lateinit var scheme: String
    private lateinit var host: String
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var tagAdapter: TagAdapter
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getInt(POST_ID_KEY, -1) ?: -1
        scheme = Settings.scheme
        host = Settings.hostname
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.dialog_tags, null)
        tagAdapter = TagAdapter()
        view.findViewById<RecyclerView>(R.id.tags_list).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = tagAdapter
        }
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
        detailViewModel.detail.observe(this, Observer {
            if (it is NetResult.Success) {
                detail = it.data
                tagAdapter.notifyDataSetChanged()
            }
        })
        if (postId > -1) {
            detailViewModel.load(postId)
        } else {
            dismiss()
        }
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }
        })
        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    inner class TagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = detail?.tagsFull?.size ?: 0

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TagViewHolder).bind(detail?.tagsFull?.get(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TagViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag, parent, false))

        inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val tagDot: AppCompatImageView = itemView.findViewById(R.id.tag_dot)
            private val tagName: AppCompatTextView = itemView.findViewById(R.id.tag_name)
            private val tagCount: AppCompatTextView = itemView.findViewById(R.id.tag_count)

            var tagFull: TagsFull? = null

            init {
                itemView.setOnClickListener {
                    val query = tagFull?.name ?: return@setOnClickListener
                    SearchActivity.startSearchActivity(itemView.context, query)
                }
                itemView.setOnLongClickListener {
                    itemView.context.copyText(tagFull?.name)
                    true
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