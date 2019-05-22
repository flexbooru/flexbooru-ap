package onlymash.flexbooru.ap.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.fragment_detail.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import org.kodein.di.generic.instance

const val POST_ID_KEY = "post_id"

class DetailFragment : KodeinFragment() {

    companion object {
        fun newInstance(postId: Int): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID_KEY, postId)
                }
            }
        }
    }

    private val api by instance<Api>()

    private lateinit var detailViewModel: DetailViewModel
    private lateinit var scheme: String
    private lateinit var host: String
    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments ?: throw(RuntimeException("arguments is null."))
        postId = args.getInt(POST_ID_KEY)
        scheme = Settings.scheme
        host = Settings.hostname
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_detail, container, false)
        detailViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DetailViewModel(
                    repo = DetailRepositoryImpl(api = api),
                    scheme = scheme,
                    host = host
                ) as T
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailViewModel.detail.observe(this, Observer {
            when (it) {
                is NetResult.Error -> {
                    val msg = it.errorMsg
                    Log.w("Detail", msg)
                }
                is NetResult.Success -> {
                    val detail = it.data
                    val context = requireContext()
                    val photoView = PhotoView(context)
                    photoView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    detail_container.apply {
                        removeAllViews()
                        addView(photoView)
                    }
                    GlideApp.with(context)
                        .load(detail.fileUrl)
                        .into(photoView)
                }
            }
        })
        detailViewModel.load(postId)
    }
}