package onlymash.flexbooru.ap.ui

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    private val postDao by instance<PostDao>()
    private lateinit var localPostViewModel: LocalPostViewModel
    private var pos = 0
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        intent?.apply {
            query = getStringExtra(QUERY_KEY) ?: ""
            pos = getIntExtra(CURRENT_POSITION_KEY, 0)
        }
        localPostViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LocalPostViewModel(LocalRepositoryImpl(postDao)) as T
            }
        })
        localPostViewModel.posts.observe(this, Observer {
            posts_pager.adapter = DetailAdapter(it, supportFragmentManager, lifecycle)
            posts_pager.currentItem = pos
        })
        localPostViewModel.load(query)
    }
}
