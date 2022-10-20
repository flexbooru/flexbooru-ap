package onlymash.flexbooru.ap.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.databinding.DialogInfoBinding
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.SearchActivity
import onlymash.flexbooru.ap.ui.activity.UserActivity
import onlymash.flexbooru.ap.ui.base.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ap.ui.base.DirPickerActivity
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.worker.DownloadWorker
import org.kodein.di.instance

class InfoDialog : BaseBottomSheetDialogFragment() {
    companion object {
        fun newInstance(postId: Int): InfoDialog {
            return InfoDialog().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID_KEY, postId)
                }
            }
        }
    }

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private var _binding: DialogInfoBinding? = null
    private val binding get() = _binding!!

    private var postId = -1

    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getInt(POST_ID_KEY, -1) ?: -1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        _binding = DialogInfoBinding.inflate(layoutInflater)
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
        detailViewModel = getViewModel(DetailViewModel(
            repo = DetailRepositoryImpl(api = api, detailDao = detailDao)
        ))
        detailViewModel.detail.observe(this, Observer {
            if (it is NetResult.Success) {
                initView(it.data)
            }
        })
        if (postId > -1) {
            detailViewModel.load(postId, Settings.userToken)
        } else {
            dismiss()
        }
        return dialog
    }

    private fun initView(detail: Detail) {
        detail.userAvatar?.let { avatarUrl ->
            context?.let { context ->
                GlideApp.with(context)
                    .load(avatarUrl)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar_user))
                    .into(binding.userAvatar)
            }
        }
        val red = detail.color[0]
        val green = detail.color[1]
        val blue = detail.color[2]
        val color = Color.rgb(red, green, blue)
        binding.apply {
            userName.text = detail.userName
            userId.text = detail.userId.toString()
            pubDate.text = detail.pubtime.formatDate()
            postResSize.text = getString(R.string.placeholder_post_res_size,
                detail.width, detail.height, Formatter.formatFileSize(context, detail.size.toLong()))
            postUrlDownload.setOnClickListener {
                (activity as? DirPickerActivity)?.let {
                    DownloadWorker.download(it, detail)
                }
            }
            postUrlOpen.setOnClickListener {
                activity?.apply {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        data = Uri.parse(detail.fileUrl.toEncodedUrl())
                    }
                    safeOpenIntent(intent)
                }
            }
            postColorValue.apply {
                text = getString(R.string.placeholder_post_color, red, green, blue)
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
            }
            colorContainer.setOnClickListener {
                activity?.let {
                    val colorQuery = "${red}_${green}_${blue}_30"
                    SearchActivity.startSearchActivity(context = it, query = colorQuery, color = colorQuery)
                }
            }
            userContainer.setOnClickListener {
                activity?.let {
                    UserActivity.startUserActivity(
                        context = it,
                        userId = detail.userId,
                        username = detail.userName,
                        avatarUrl = detail.userAvatar)
                }
            }
            postUrlContainer.setOnLongClickListener {
                context?.copyText(detail.fileUrl.toEncodedUrl())
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}