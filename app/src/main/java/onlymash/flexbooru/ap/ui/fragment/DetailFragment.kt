package onlymash.flexbooru.ap.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.decoder.CustomDecoder
import onlymash.flexbooru.ap.decoder.CustomRegionDecoder
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.DetailActivity
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import org.kodein.di.generic.instance
import java.io.File
import java.util.concurrent.Executor

const val POST_ID_KEY = "post_id"

class DetailFragment : KodeinFragment() {

    companion object {
        private const val TAG = "DetailFragment"
        fun newInstance(postId: Int): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID_KEY, postId)
                }
            }
        }
    }

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private val ioExecutor by instance<Executor>()
    private val picasso by instance<Picasso>()

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
                    repo = DetailRepositoryImpl(api = api, detailDao = detailDao),
                    scheme = scheme,
                    host = host
                ) as T
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val subsamplingScaleImageView = view.findViewById<SubsamplingScaleImageView>(R.id.post_image)
        subsamplingScaleImageView.apply {
            setOnClickListener {
                (requireActivity() as DetailActivity).setVisibility()
            }
            setExecutor(ioExecutor)
            setBitmapDecoderFactory{
                CustomDecoder(picasso)
            }
            setRegionDecoderFactory {
                CustomRegionDecoder()
            }
        }
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        detailViewModel.detail.observe(this, Observer {
            when (it) {
                is NetResult.Error -> {
                    Log.w(TAG, it.errorMsg)
                }
                is NetResult.Success -> {
                    GlideApp.with(requireContext())
                        .downloadOnly()
                        .load(it.data.fileUrl)
                        .into(object : CustomTarget<File>() {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                                subsamplingScaleImageView.setImage(ImageSource.uri(resource.toUri()))
                                progressBar.visibility = View.GONE
                            }
                        })
                }
                else -> {
                    Log.w(TAG, "unknown data")
                }
            }
        })
        detailViewModel.load(postId)
    }
}