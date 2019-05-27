package onlymash.flexbooru.ap.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.*
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.data.repository.local.LocalRepositoryImpl
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.BaseActivity
import onlymash.flexbooru.ap.ui.dialog.InfoDialog
import onlymash.flexbooru.ap.ui.dialog.TagsDialog
import onlymash.flexbooru.ap.ui.fragment.DetailFragment
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_ACTION_FILTER_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_QUERY_KEY
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.ui.viewmodel.LocalPostViewModel
import onlymash.flexbooru.ap.widget.DismissFrameLayout
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.Exception

const val CURRENT_POSITION_KEY = "current_position"
const val FROM_WHERE_KEY = "from_where"
const val FROM_POSTS = 0
const val FROM_HISTORY = 1

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00

class DetailActivity : BaseActivity() {

    companion object {
        private const val TAG = "DetailActivity"
        fun startDetailActivity(context: Context, fromWhere: Int, query: String = "", position: Int) {
            context.startActivity(Intent(context, DetailActivity::class.java).apply {
                putExtra(FROM_WHERE_KEY, fromWhere)
                putExtra(QUERY_KEY, query)
                putExtra(CURRENT_POSITION_KEY, position)
            })
        }
        fun startDetailActivityWithTransition(
            activity: Activity,
            fromWhere: Int,
            query: String = "",
            position: Int,
            view: View,
            transitionName: String
        ) {
            val intent = Intent(activity, DetailActivity::class.java).apply {
                putExtra(FROM_WHERE_KEY, fromWhere)
                putExtra(QUERY_KEY, query)
                putExtra(CURRENT_POSITION_KEY, position)
            }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, transitionName)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private val postDao by instance<PostDao>()

    private lateinit var localPostViewModel: LocalPostViewModel
    private lateinit var localDetailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter
    private lateinit var colorDrawable: ColorDrawable

    private var fromWhere = FROM_POSTS
    private var pos = 0
    private var query = ""
    private val posts: MutableList<Post> = mutableListOf()
    private val details: MutableList<Detail> = mutableListOf()
    private var currentPostId = 0
    private val allDetails: MutableList<Detail> = mutableListOf()

    val onDismissListener = object : DismissFrameLayout.OnDismissListener {
        override fun onStart() {
            colorDrawable.alpha = ALPHA_MIN
        }

        override fun onProgress(progress: Float) {

        }

        override fun onDismiss() {
            finishAfterTransition()
        }

        override fun onCancel() {
            colorDrawable.alpha = ALPHA_MAX
        }
    }

    override fun finishAfterTransition() {
        if (fromWhere == FROM_POSTS) {
            toolbar_container.toVisibility(false)
            bottom_bar_container.toVisibility(false)
            colorDrawable.alpha = ALPHA_MIN
        }
        super.finishAfterTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.activity_detail)
        intent?.apply {
            fromWhere = getIntExtra(FROM_WHERE_KEY, FROM_POSTS)
            query = getStringExtra(QUERY_KEY) ?: ""
            pos = getIntExtra(CURRENT_POSITION_KEY, 0)
        }
        postponeEnterTransition()
        initView()
        initViewModel()
    }
    private fun initView() {
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        detailAdapter = DetailAdapter(supportFragmentManager)
        posts_pager.background = colorDrawable
        posts_pager.adapter = detailAdapter
        toolbar.apply {
            setNavigationOnClickListener {
                onBackPressed()
            }
            setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.action_set_as -> setAs()
                    R.id.action_share_link -> shareLink()
                    R.id.action_send_file -> sendFile()
                    R.id.action_open_in_browser -> openInBrowser()
                }
                true
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(posts_pager) { _, insets ->
            toolbar_container.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbar_container.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                0
            )
            space_nav_bar.minimumHeight = insets.systemWindowInsetBottom
            insets
        }
        posts_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                pos = position
                when (fromWhere) {
                    FROM_POSTS -> currentPostId = posts[position].id
                    FROM_HISTORY -> currentPostId = details[position].id
                }
                toolbar.title = "Post $currentPostId"
                initVoteIcon()
                val intent = Intent(JUMP_TO_POSITION_ACTION_FILTER_KEY).apply {
                    putExtra(JUMP_TO_POSITION_QUERY_KEY, query)
                    putExtra(JUMP_TO_POSITION_KEY, position)
                }
                sendBroadcast(intent)
            }
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
        post_tags.setOnClickListener {
            TagsDialog.newInstance(currentPostId).show(supportFragmentManager, "tags")
        }
        post_info.setOnClickListener {
            InfoDialog.newInstance(currentPostId).show(supportFragmentManager, "info")
        }
        post_save.setOnClickListener {
            saveFile()
        }
        if (!Settings.isPro) {
            ad_view.toVisibility(true)
            val adBuilder = AdRequest.Builder()
                .addTestDevice("3ABFE34D41CFAFB2402160AC2B4B2DFE")
            ad_view.loadAd(adBuilder.build())
        }
    }

    private fun initViewModel() {
        if (fromWhere == FROM_POSTS) {
            localPostViewModel = getViewModel(object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return LocalPostViewModel(LocalRepositoryImpl(postDao)) as T
                }
            })
            localPostViewModel.posts.observe(this, Observer {
                posts.clear()
                posts.addAll(it)
                detailAdapter.notifyDataSetChanged()
                posts_pager.setCurrentItem(pos, false)
                toolbar.title = "Post ${it[pos].id}"
                localPostViewModel.posts.removeObservers(this)
                startPostponedEnterTransition()
            })
            localPostViewModel.load(query)
        }

        localDetailViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DetailViewModel(
                    repo = DetailRepositoryImpl(api = api, detailDao = detailDao),
                    scheme = Settings.scheme,
                    host = Settings.hostname
                ) as T
            }
        })
        localDetailViewModel.details.observe(this, Observer {
            allDetails.clear()
            allDetails.addAll(it)
            if (fromWhere == FROM_HISTORY && details.isEmpty()) {
                toolbar.title = "Post ${it[pos].id}"
                details.addAll(it)
                detailAdapter.notifyDataSetChanged()
                posts_pager.setCurrentItem(pos, false)
                startPostponedEnterTransition()
            }
            initVoteIcon()
        })
        localDetailViewModel.loadAll()
        localDetailViewModel.voteResult.observe(this, Observer {
            when (it) {
                is NetResult.Success -> {

                }
                is NetResult.Error -> {
                    Toast.makeText(this, it.errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        })
        post_vote.setOnClickListener {
            val token = Settings.userToken
            if (token.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                val postId = currentPostId
                if (postId <= 0) return@setOnClickListener
                val detail = getCurrentDetail(postId) ?: return@setOnClickListener
                val vote = if (detail.starIt) 0 else 9
                localDetailViewModel.vote(vote, token, detail)
            }
        }
    }

    private fun shareLink() {
        startActivity(Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getWebLink())
            },
            getString(R.string.share_via)
        ))
    }

    private fun openInBrowser() {
        launchUrl(getWebLink())
    }

    private fun getWebLink(): String {
        return "${Settings.scheme}://${Settings.hostname}/pictures/view_post/$currentPostId?lang=en"
    }

    private fun getCurrentDetail(postId: Int): Detail? {
        var detail: Detail? = null
        allDetails.forEach {
            if (it.id == postId) {
                detail = it
                return@forEach
            }
        }
        return detail
    }

    private fun setAs() {
        val url = getDetailUrl() ?: return
        lifecycleScope.launch {
            val shareFile = getFile(url)
            if (shareFile != null) {
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_ATTACH_DATA).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_MIME_TYPES, url.getMimeType())
                        data = getUriForFile(shareFile)
                    },
                    getString(R.string.share_via)
                ))
            }
        }
    }

    private fun sendFile() {
        val url = getDetailUrl() ?: return
        lifecycleScope.launch {
            val shareFile = getFile(url)
            if (shareFile != null) {
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_STREAM, getUriForFile(shareFile))
                        type = url.getMimeType()
                    },
                    getString(R.string.share_via)
                ))
            }
        }
    }

    private fun saveFile() {
        val url = getDetailUrl() ?: return
        val fileName = url.fileName()
        val uri = getSaveUri(fileName) ?: return
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                val os = contentResolver.openOutputStream(uri)
                var `is`: FileInputStream? = null
                try {
                    val file = downloadFile(url)
                    `is` = FileInputStream(file)
                    `is`.copyToOS(os)
                    true
                } catch (_: Exception) {
                    false
                } finally {
                    `is`?.safeCloseQuietly()
                    os?.safeCloseQuietly()
                }
            }
            if (success) {
                val docId = DocumentsContract.getDocumentId(uri)
                Toast.makeText(this@DetailActivity, docId, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDetailUrl(): String? {
        val postId = currentPostId
        if (postId <= 0) return null
        return getCurrentDetail(postId)?.getDetailUrl()
    }

    private suspend fun getFile(url: String): File? {
        return withContext(Dispatchers.IO) {
            val desFile = File(externalCacheDir, url.fileName())
            val os = FileOutputStream(desFile)
            var `is`: FileInputStream? = null
            try {
                val file = downloadFile(url)
                `is` = FileInputStream(file)
                `is`.copyToOS(os)
                desFile
            } catch (_: Exception) {
                null
            } finally {
                `is`?.safeCloseQuietly()
                os.safeCloseQuietly()
            }
        }
    }

    private fun downloadFile(url: String): File {
        return GlideApp.with(this)
            .downloadOnly()
            .load(url)
            .submit()
            .get()
    }

    private fun initVoteIcon() {
        if (isVoted(currentPostId)) {
            post_vote.setImageDrawable(
                ContextCompat.getDrawable(
                    this@DetailActivity,
                    R.drawable.ic_star_24dp
                )
            )
        } else {
            post_vote.setImageDrawable(
                ContextCompat.getDrawable(
                    this@DetailActivity,
                    R.drawable.ic_star_border_24dp
                )
            )
        }
    }

    private fun isVoted(postId: Int): Boolean {
        val index = allDetails.indexOfFirst { it.id == postId && it.starIt }
        return index > -1
    }

    private fun initWindow() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        showBar()
    }

    internal fun setVisibility() {
        when (toolbar.visibility) {
            View.VISIBLE -> {
                hideBar()
                toolbar.visibility = View.GONE
                bottom_bar_container.visibility = View.GONE
                shadow.visibility = View.GONE
            }
            else -> {
                showBar()
                toolbar.visibility = View.VISIBLE
                bottom_bar_container.visibility = View.VISIBLE
                shadow.visibility = View.VISIBLE
            }
        }
    }

    private fun showBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = uiFlags
    }

    private fun hideBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE
        window.decorView.systemUiVisibility = uiFlags
    }

    inner class DetailAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment =
            DetailFragment.newInstance(
                if (fromWhere == FROM_POSTS)
                    posts[position].id
                else
                    details[position].id
            )

        override fun getCount(): Int =
            if (fromWhere == FROM_POSTS)
                posts.size
            else
                details.size
    }
}
