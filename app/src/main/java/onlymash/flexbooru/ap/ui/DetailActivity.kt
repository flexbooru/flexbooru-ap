package onlymash.flexbooru.ap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_detail.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.repository.local.LocalRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.adapter.DetailAdapter
import onlymash.flexbooru.ap.ui.base.KodeinActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        intent?.apply {
            query = getStringExtra(QUERY_KEY) ?: ""
            pos = getIntExtra(CURRENT_POSITION_KEY, 0)
        }
        detailAdapter = DetailAdapter(supportFragmentManager, lifecycle)
        posts_pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        posts_pager.adapter = detailAdapter
        localPostViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LocalPostViewModel(LocalRepositoryImpl(postDao)) as T
            }
        })
        localPostViewModel.posts.observe(this, Observer {
            detailAdapter.updateData(it)
            posts_pager.currentItem = pos
        })
        localPostViewModel.load(query)
    }
}
