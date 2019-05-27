package onlymash.flexbooru.ap.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.SearchActivity
import onlymash.flexbooru.ap.ui.UserActivity
import onlymash.flexbooru.ap.ui.base.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.worker.DownloadWorker
import org.kodein.di.generic.instance

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

    private var postId = -1

    private lateinit var scheme: String
    private lateinit var host: String
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var detailViewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getInt(POST_ID_KEY, -1) ?: -1
        scheme = Settings.scheme
        host = Settings.hostname
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.dialog_info, null)
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
                initView(view, it.data)
            }
        })
        if (postId > -1) {
            detailViewModel.load(postId, Settings.userToken)
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

    private fun initView(view: View, detail: Detail) {
        val context = view.context
        detail.userAvatar?.let {
            GlideApp.with(context)
                .load(it)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.avatar_user))
                .into(view.findViewById(R.id.user_avatar))
        }
        view.findViewById<AppCompatTextView>(R.id.user_name).text = detail.userName
        view.findViewById<AppCompatTextView>(R.id.user_id).text = detail.userId.toString()
        view.findViewById<AppCompatTextView>(R.id.pub_date).text = detail.pubtime.formatDate()
        view.findViewById<AppCompatTextView>(R.id.post_size).text =
            getString(
                R.string.placeholder_post_size,
                Formatter.formatFileSize(context, detail.size.toLong()),
                detail.width,
                detail.height)
        view.findViewById<AppCompatImageView>(R.id.post_url_download).setOnClickListener {
            activity?.let {
                DownloadWorker.download(it, detail)
            }
        }
        view.findViewById<AppCompatImageView>(R.id.post_url_open).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(detail.fileUrl)
            }
            context.safeOpenIntent(intent)
        }
        view.findViewById<AppCompatTextView>(R.id.post_color_value).text =
                getString(
                    R.string.placeholder_post_color,
                    detail.color[0], detail.color[1], detail.color[2])
        view.findViewById<ConstraintLayout>(R.id.color_container).setOnClickListener {
            val color = "${detail.color[0]}_${detail.color[1]}_${detail.color[2]}_30"
            SearchActivity.startSearchActivity(context = context, query = color, color = color)
        }
        view.findViewById<ConstraintLayout>(R.id.user_container).setOnClickListener {
            UserActivity.startUserActivity(
                context = context,
                userId = detail.userId,
                username = detail.userName,
                avatarUrl = detail.userAvatar)
        }
        view.findViewById<ConstraintLayout>(R.id.post_url_container).setOnLongClickListener {
            context.copyText(detail.fileUrl)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}