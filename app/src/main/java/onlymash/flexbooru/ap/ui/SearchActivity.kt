package onlymash.flexbooru.ap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.floating_action_button.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.COLOR_KEY
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.common.SEARCH_TYPE_KEY
import onlymash.flexbooru.ap.common.USER_ID_KEY
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_ACTION_FILTER_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_QUERY_KEY

class SearchActivity : KodeinActivity() {

    companion object {
        fun startSearchActivity(
            context: Context,
            query: String = "",
            searchType: SearchType = SearchType.NORMAL,
            userId: Int = -1,
            color: String = ""
        ) {
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                .apply {
                    putExtra(QUERY_KEY, query)
                    putExtra(SEARCH_TYPE_KEY, searchType)
                    putExtra(USER_ID_KEY, userId)
                    putExtra(COLOR_KEY, color)
                }
            )
        }
    }

    private var query = ""
    private var searchType = SearchType.NORMAL
    private var userId: Int = -1
    private var color: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        intent?.apply {
            query = getStringExtra(QUERY_KEY) ?: ""
            searchType = getSerializableExtra(SEARCH_TYPE_KEY) as? SearchType ?: SearchType.NORMAL
            userId = getIntExtra(USER_ID_KEY, -1)
            color = getStringExtra(COLOR_KEY) ?: ""
        }

        findNavController(R.id.search_nav_host_fragment).setGraph(
            R.navigation.search_navigation,
            Bundle().apply {
                putString(QUERY_KEY, query)
                putSerializable(SEARCH_TYPE_KEY, searchType)
                putInt(USER_ID_KEY, userId)
                putString(COLOR_KEY, color)
            }
        )
        fab.setOnClickListener {
            sendBroadcast(
                Intent(JUMP_TO_TOP_ACTION_FILTER_KEY)
                    .putExtra(JUMP_TO_TOP_KEY, true)
                    .putExtra(JUMP_TO_TOP_QUERY_KEY, query))
        }
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_posts)
            subtitle = query
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.posts, menu)
        val item = menu?.findItem(R.id.action_search) ?: return true
        val searchView = item.actionView as SearchView
        initSearchView(searchView)
        return true
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.queryHint = getString(R.string.search_posts_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) return false
                return true
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) return false
                search(query)
                return true
            }
        })
    }

    private fun search(query: String) {
        findNavController(R.id.search_nav_host_fragment).navigate(
            R.id.nav_search,
            Bundle().apply {
                putString(QUERY_KEY, query)
            }
        )
    }
}
