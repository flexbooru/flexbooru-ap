package onlymash.flexbooru.ap.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.decoder.CustomDecoder
import onlymash.flexbooru.ap.decoder.CustomRegionDecoder
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.activity.DetailActivity
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.widget.DismissFrameLayout
import org.kodein.di.erased.instance
import java.io.File

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

    private lateinit var detailViewModel: DetailViewModel
    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments ?: throw(RuntimeException("arguments is null."))
        postId = args.getInt(POST_ID_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_detail, container, false)
        detailViewModel = getViewModel(
            DetailViewModel(
                repo = DetailRepositoryImpl(api = api, detailDao = detailDao)
            )
        )
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? DetailActivity)?.let {
            view.findViewById<DismissFrameLayout>(R.id.detail_container)
                .setDismissListener(it.onDismissListener)
        }
        val subsamplingScaleImageView = view.findViewById<SubsamplingScaleImageView>(R.id.post_image)
        val photoView = view.findViewById<PhotoView>(R.id.post_gif)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        detailViewModel.detail.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is NetResult.Error -> {
                    Log.w(TAG, it.errorMsg)
                }
                is NetResult.Success -> {
                    val url = it.data.getDetailUrl()
                    if (it.data.ext == ".gif") {
                        subsamplingScaleImageView.isVisible = false
                        photoView.isVisible = true
                        photoView.setOnClickListener {
                            (activity as? DetailActivity)?.setVisibility()
                        }
                        photoView.transitionName = "post_${it.data.id}"
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
                                    progressBar.isVisible = false
                                    return false
                                }
                                override fun onResourceReady(
                                    resource: GifDrawable?,
                                    model: Any?,
                                    target: Target<GifDrawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    progressBar.isVisible = false
                                    return false
                                }
                            })
                            .into(photoView)
                    } else {
                        photoView.isVisible = false
                        subsamplingScaleImageView.isVisible = true
                        subsamplingScaleImageView.apply {
                            setExecutor(Dispatchers.IO.asExecutor())
                            setBitmapDecoderFactory{
                                CustomDecoder(GlideApp.with(this))
                            }
                            setRegionDecoderFactory {
                                CustomRegionDecoder()
                            }
                            setOnClickListener {
                                (activity as? DetailActivity)?.setVisibility()
                            }
                            transitionName = "post_${it.data.id}"
                        }
                        GlideApp.with(requireContext())
                            .downloadOnly()
                            .load(url)
                            .into(object : CustomTarget<File>() {
                                override fun onLoadCleared(placeholder: Drawable?) {}
                                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                                    subsamplingScaleImageView.setImage(ImageSource.uri(resource.toUri()))
                                    progressBar.isVisible = false
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