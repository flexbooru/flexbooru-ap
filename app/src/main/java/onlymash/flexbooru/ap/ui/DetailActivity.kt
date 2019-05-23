package onlymash.flexbooru.ap.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.data.repository.local.LocalRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.adapter.DetailAdapter
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.dialog.TagsDialog
import onlymash.flexbooru.ap.ui.viewmodel.LocalPostViewModel
import org.kodein.di.generic.instance

const val CURRENT_POSITION_KEY = "current_position"

class DetailActivity : KodeinActivity() {

    companion object {
        private const val TAG = "DetailActivity"
        fun startDetailActivity(context: Context, query: String, position: Int) {
            context.startActivity(Intent(context, DetailActivity::class.java).apply {
                putExtra(QUERY_KEY, query)
                putExtra(CURRENT_POSITION_KEY, position)
            })
        }
    }

    private val postDao by instance<PostDao>()
    private lateinit var localPostViewModel: LocalPostViewModel
    private lateinit var detailAdapter: DetailAdapter
    private var pos = 0
    private var query = ""
    private var posts: List<Post> = listOf()
    private var currentPostId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.activity_detail)
        intent?.apply {
            query = getStringExtra(QUERY_KEY) ?: ""
            pos = getIntExtra(CURRENT_POSITION_KEY, 0)
        }
        initView()
        initViewModel()
    }
    private fun initView() {
        detailAdapter = DetailAdapter(supportFragmentManager, lifecycle)
        posts_pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        posts_pager.adapter = detailAdapter
        toolbar.setNavigationOnClickListener {
            onBackPressed()
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
        posts_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPostId = posts[position].id
                toolbar.title = "Post $currentPostId"
            }
        })
        post_tags.setOnClickListener {
            TagsDialog.newInstance(currentPostId).show(supportFragmentManager, "tags")
        }
        post_info.setOnClickListener {

        }
    }

    private fun initViewModel() {
        localPostViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LocalPostViewModel(LocalRepositoryImpl(postDao)) as T
            }
        })
        localPostViewModel.posts.observe(this, Observer {
            posts = it
            detailAdapter.updateData(it)
            posts_pager.setCurrentItem(pos, false)
            toolbar.title = "Post ${it[pos].id}"
        })
        localPostViewModel.load(query)
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
}
