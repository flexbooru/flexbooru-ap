package onlymash.flexbooru.ap.ui.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.WindowInsets
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.floating_action_button.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Tag
import onlymash.flexbooru.ap.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.base.PostActivity
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_ACTION_FILTER_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_QUERY_KEY
import onlymash.flexbooru.ap.ui.viewmodel.SuggestionViewModel
import org.kodein.di.erased.instance

class SearchActivity : PostActivity() {

    companion object {
        fun startSearchActivity(
            context: Context,
            query: String = "",
            searchType: SearchType = SearchType.NORMAL,
            userId: Int = -1,
            uploaderId: Int = -1,
            color: String = ""
        ) {
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                .apply {
                    putExtra(QUERY_KEY, query)
                    putExtra(SEARCH_TYPE_KEY, searchType)
                    putExtra(USER_ID_KEY, userId)
                    putExtra(UPLOADER_ID_KEY, uploaderId)
                    putExtra(COLOR_KEY, color)
                }
            )
        }
    }

    override var query = ""
    private var searchType = SearchType.NORMAL
    private var userId: Int = -1
    private var uploaderId: Int = -1
    private var color: String = ""

    private val api by instance<Api>()
    private lateinit var suggestionViewModel: SuggestionViewModel
    private val suggestions: MutableList<String> = mutableListOf()
    private var suggestionAdapter: CursorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        intent?.apply {
            query = getStringExtra(QUERY_KEY) ?: ""
            searchType = getSerializableExtra(SEARCH_TYPE_KEY) as? SearchType ?: SearchType.NORMAL
            userId = getIntExtra(USER_ID_KEY, -1)
            uploaderId = getIntExtra(UPLOADER_ID_KEY, -1)
            color = getStringExtra(COLOR_KEY) ?: ""
        }

        findNavController(R.id.search_nav_host_fragment).setGraph(
            R.navigation.search_navigation,
            Bundle().apply {
                putString(QUERY_KEY, query)
                putSerializable(SEARCH_TYPE_KEY, searchType)
                putInt(USER_ID_KEY, userId)
                putInt(UPLOADER_ID_KEY, uploaderId)
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
        suggestionViewModel = getViewModel(SuggestionViewModel(SuggestionRepositoryImpl(api)))
        suggestionViewModel.tags.observe(this, Observer {
            handleSuggestions(it)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.posts_search, menu)
        val item = menu?.findItem(R.id.action_search) ?: return true
        val searchView = item.actionView as SearchView
        initSearchView(searchView)
        return true
    }

    private fun initSearchView(searchView: SearchView) {
        suggestionAdapter = SimpleCursorAdapter(
            this,
            R.layout.item_suggestion,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            0)
        searchView.apply {
            queryHint = getString(R.string.search_posts_hint)
            suggestionsAdapter = suggestionAdapter
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionClick(position: Int): Boolean {
                    search(suggestions[position])
                    return true
                }
                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }
            })
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) return false
                    fetchSuggestions(newText)
                    return true
                }
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query.isNullOrEmpty()) return false
                    search(query)
                    return true
                }
            })
        }
    }

    override fun applyInsets(insets: WindowInsets) {
        container_toolbar.minimumHeight = toolbar.minimumHeight + insets.systemWindowInsetTop
        val fabMarginBottom = insets.systemWindowInsetBottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
        (fab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = fabMarginBottom
    }

    private fun fetchSuggestions(tag: String) {
        val token = Settings.userToken
        if (token.isEmpty()) return
        suggestionViewModel.fetch(
            tag = tag,
            token = token
        )
    }

    private fun handleSuggestions(tags: List<Tag>) {
        suggestions.clear()
        suggestions.addAll(tags.map {
            it.t.replace("<b>", "")
                .replace("</b>", "")
        })
        val columns = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA)
        val cursor = MatrixCursor(columns)
        suggestions.forEachIndexed { index, suggestion ->
            val tmp = arrayOf(
                index.toString(),
                suggestion,
                suggestion
            )
            cursor.addRow(tmp)
        }
        suggestionAdapter?.swapCursor(cursor)
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
