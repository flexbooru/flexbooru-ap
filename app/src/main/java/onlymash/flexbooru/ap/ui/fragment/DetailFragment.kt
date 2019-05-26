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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.decoder.CustomDecoder
import onlymash.flexbooru.ap.decoder.CustomRegionDecoder
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getDetailUrl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.extension.toVisibility
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.DetailActivity
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import org.kodein.di.generic.instance
import java.io.File
import java.util.concurrent.Executor

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
        val photoView = view.findViewById<PhotoView>(R.id.post_gif)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        detailViewModel.detail.observe(this, Observer {
            when (it) {
                is NetResult.Error -> {
                    Log.w(TAG, it.errorMsg)
                }
                is NetResult.Success -> {
                    val url = it.data.getDetailUrl()
                    if (it.data.ext == ".gif") {
                        subsamplingScaleImageView.toVisibility(false)
                        photoView.toVisibility(true)
                        photoView.setOnClickListener {
                            (activity as? DetailActivity)?.setVisibility()
                        }
                        GlideApp.with(requireContext())
                            .asGif()
                            .load(url)
                            .fitCenter()
                            .addListener(object : RequestListener<GifDrawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<GifDrawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    progressBar.toVisibility(false)
                                    return false
                                }
                                override fun onResourceReady(
                                    resource: GifDrawable?,
                                    model: Any?,
                                    target: Target<GifDrawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    progressBar.toVisibility(false)
                                    return false
                                }
                            })
                            .into(photoView)
                    } else {
                        photoView.toVisibility(false)
                        subsamplingScaleImageView.toVisibility(true)
                        subsamplingScaleImageView.apply {
                            setExecutor(ioExecutor)
                            setBitmapDecoderFactory{
                                CustomDecoder(picasso)
                            }
                            setRegionDecoderFactory {
                                CustomRegionDecoder()
                            }
                            setOnClickListener {
                                (activity as? DetailActivity)?.setVisibility()
                            }
                        }
                        GlideApp.with(requireContext())
                            .downloadOnly()
                            .load(url)
                            .into(object : CustomTarget<File>() {
                                override fun onLoadCleared(placeholder: Drawable?) {}
                                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                                    subsamplingScaleImageView.setImage(ImageSource.uri(resource.toUri()))
                                    progressBar.toVisibility(false)
                                }
                            })
                    }
                }
                else -> {
                    Log.w(TAG, "unknown data")
                }
            }
        })
        detailViewModel.load(postId, Settings.userToken)
    }
}