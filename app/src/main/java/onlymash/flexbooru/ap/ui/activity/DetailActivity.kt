package onlymash.flexbooru.ap.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.HOST
import onlymash.flexbooru.ap.common.POST_ID_KEY
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.data.repository.detail.DetailRepositoryImpl
import onlymash.flexbooru.ap.data.repository.local.LocalRepositoryImpl
import onlymash.flexbooru.ap.databinding.ActivityDetailBinding
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.DirPickerActivity
import onlymash.flexbooru.ap.ui.dialog.InfoDialog
import onlymash.flexbooru.ap.ui.dialog.TagsDialog
import onlymash.flexbooru.ap.ui.fragment.DetailFragment
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_ACTION_FILTER_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_POSITION_QUERY_KEY
import onlymash.flexbooru.ap.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ap.ui.viewmodel.LocalPostViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.DismissFrameLayout
import org.kodein.di.instance
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.Exception

const val CURRENT_POSITION_KEY = "current_position"
const val FROM_WHERE_KEY = "from_where"
const val FROM_POSTS = 0
const val FROM_HISTORY = 1
const val FROM_POST = 2

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00

class DetailActivity : DirPickerActivity() {

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
        fun startDetailActivityFromComment(context: Context, postId: Int) {
            context.startActivity(Intent(context, DetailActivity::class.java).apply {
                putExtra(POST_ID_KEY, postId)
            })
        }
    }

    private val api by instance<Api>()
    private val detailDao by instance<DetailDao>()
    private val postDao by instance<PostDao>()
    
    private val binding by viewBinding(ActivityDetailBinding::inflate)
    private val postsPager get() = binding.postsPager
    private val toolbar get() = binding.toolbar
    private val shortcut get() = binding.shortcut
    
    private lateinit var localPostViewModel: LocalPostViewModel
    private lateinit var localDetailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter
    private lateinit var colorDrawable: ColorDrawable

    private var fromWhere = FROM_POSTS
    private var pos = 0
    private var query = ""
    private val posts: MutableList<Post> = mutableListOf()
    private val details: MutableList<Detail> = mutableListOf()
    private var currentPostId = -1
    private val allDetails: MutableList<Detail> = mutableListOf()

    val onDismissListener = object : DismissFrameLayout.OnDismissListener {
        override fun onStart() {
            postsPager.isUserInputEnabled = false
            postsPager
            colorDrawable.alpha = ALPHA_MIN
        }

        override fun onProgress(progress: Float) {

        }

        override fun onDismiss() {
            finishAfterTransition()
        }

        override fun onCancel() {
            postsPager.isUserInputEnabled = true
            colorDrawable.alpha = ALPHA_MAX
        }
    }

    override fun finishAfterTransition() {
        if (fromWhere == FROM_POSTS) {
            binding.toolbarContainer.isVisible = false
            shortcut.bottomBarContainer.isVisible = false
            colorDrawable.alpha = ALPHA_MIN
        }
        super.finishAfterTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.isShowBar = true
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarContainer.minimumHeight = toolbar.height + systemBarInsets.top
            binding.toolbarContainer.updatePadding(
                left = systemBarInsets.left,
                top = systemBarInsets.top,
                right = systemBarInsets.right
            )
            shortcut.spaceNavBar.minimumHeight = systemBarInsets.bottom
            insets
        }
        intent?.apply {
            val url = data
            val postId = getIntExtra(POST_ID_KEY, -1)
            if (url is Uri && url.scheme == "https") {
                fromWhere = FROM_POST
                pos = 0
                currentPostId = url.path?.replace("/pictures/view_post/", "")?.toInt() ?: 0
            } else if (postId > 0) {
                fromWhere = FROM_POST
                pos = 0
                currentPostId = postId
            } else {
                fromWhere = getIntExtra(
                    FROM_WHERE_KEY,
                    FROM_POSTS
                )
                query = getStringExtra(QUERY_KEY) ?: ""
                pos = getIntExtra(CURRENT_POSITION_KEY, 0)
            }
        }
        if (fromWhere == FROM_POSTS) {
            postponeEnterTransition()
        }
        initView()
        initViewModel()
    }

    private fun initView() {
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        detailAdapter = DetailAdapter(supportFragmentManager, lifecycle)
        postsPager.background = colorDrawable
        postsPager.adapter = detailAdapter
        toolbar.apply {
            setNavigationOnClickListener {
                onBackPressed()
            }
            setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.action_comments -> {
                        if (Settings.userToken.isEmpty()) {
                            startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
                        } else if (currentPostId > 0) {
                            CommentActivity.startActivity(this@DetailActivity, currentPostId)
                        }
                    }
                    R.id.action_set_as -> setAs()
                    R.id.action_copy_link -> copyText(getWebLink())
                    R.id.action_share_link -> shareLink()
                    R.id.action_send_file -> sendFile()
                }
                true
            }
            if (fromWhere == FROM_POST) {
                title = getString(R.string.placeholder_post_id, currentPostId)
            }
        }
        postsPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pos = position
                when (fromWhere) {
                    FROM_POSTS -> {
                        currentPostId = posts[position].id
                        val intent = Intent(JUMP_TO_POSITION_ACTION_FILTER_KEY).apply {
                            putExtra(JUMP_TO_POSITION_QUERY_KEY, query)
                            putExtra(JUMP_TO_POSITION_KEY, position)
                        }
                        sendBroadcast(intent)
                    }
                    FROM_HISTORY -> currentPostId = details[position].id
                }
                toolbar.title = getString(R.string.placeholder_post_id, currentPostId)
                initVoteIcon()
            }
        })
        shortcut.postTags.setOnClickListener {
            if (currentPostId > 0) {
                TagsDialog.newInstance(currentPostId).show(supportFragmentManager, "tags")
            }
        }
        shortcut.postInfo.setOnClickListener {
            if (currentPostId > 0) {
                InfoDialog.newInstance(currentPostId).show(supportFragmentManager, "info")
            }
        }
        shortcut.postSave.setOnClickListener { saveFile() }
        TooltipCompat.setTooltipText(shortcut.postTags, shortcut.postTags.contentDescription)
        TooltipCompat.setTooltipText(shortcut.postInfo, shortcut.postInfo.contentDescription)
        TooltipCompat.setTooltipText(shortcut.postSave, shortcut.postSave.contentDescription)
        TooltipCompat.setTooltipText(shortcut.postVote, shortcut.postVote.contentDescription)
    }

    private fun initViewModel() {
        if (fromWhere == FROM_POSTS) {
            localPostViewModel = getViewModel(LocalPostViewModel(LocalRepositoryImpl(postDao)))
            localPostViewModel.posts.observe(this, Observer { data ->
                posts.clear()
                posts.addAll(data)
                detailAdapter.notifyDataSetChanged()
                postsPager.setCurrentItem(pos, false)
                currentPostId = data[pos].id
                toolbar.title = getString(R.string.placeholder_post_id, currentPostId)
                localPostViewModel.posts.removeObservers(this)
                startPostponedEnterTransition()
            })
            localPostViewModel.load(query)
        }

        localDetailViewModel = getViewModel(
            DetailViewModel(
                repo = DetailRepositoryImpl(api = api, detailDao = detailDao)
            )
        )
        localDetailViewModel.details.observe(this, Observer {
            allDetails.clear()
            allDetails.addAll(it)
            if (fromWhere == FROM_HISTORY && details.isEmpty()) {
                currentPostId = it[pos].id
                toolbar.title = getString(R.string.placeholder_post_id, currentPostId)
                details.addAll(it)
                detailAdapter.notifyDataSetChanged()
                postsPager.setCurrentItem(pos, false)
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
        shortcut.postVote.setOnClickListener {
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

    private fun getWebLink(): String {
        return "https://$HOST/pictures/view_post/$currentPostId?lang=en"
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
                    `is`.copyTo(os)
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
                `is`.copyTo(os)
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
        if (currentPostId.isVoted()) {
            shortcut.postVote.setImageDrawable(
                ContextCompat.getDrawable(
                    this@DetailActivity,
                    R.drawable.ic_star_24dp
                )
            )
        } else {
            shortcut.postVote.setImageDrawable(
                ContextCompat.getDrawable(
                    this@DetailActivity,
                    R.drawable.ic_star_border_24dp
                )
            )
        }
    }

    private fun Int.isVoted(): Boolean {
        val index = allDetails.indexOfFirst { it.id == this && it.starIt }
        return index > -1
    }

    internal fun setVisibility() {
        val isVisible = !toolbar.isVisible
        window.isShowBar = isVisible
        toolbar.isVisible = isVisible
        shortcut.bottomBarContainer.isVisible = isVisible
        binding.shadow.isVisible = isVisible
    }

    inner class DetailAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

        override fun createFragment(position: Int): Fragment =
            DetailFragment.newInstance(
                when (fromWhere) {
                    FROM_POSTS -> posts[position].id
                    FROM_HISTORY -> details[position].id
                    else -> currentPostId
                }
            )

        override fun getItemCount(): Int =
            when (fromWhere) {
                FROM_POSTS -> posts.size
                FROM_HISTORY -> details.size
                else -> 1
            }
    }
}
