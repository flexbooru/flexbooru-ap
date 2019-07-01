package onlymash.flexbooru.ap.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.database.MatrixCursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.floating_action_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Tag
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.PostActivity
import onlymash.flexbooru.ap.ui.fragment.*
import onlymash.flexbooru.ap.ui.viewmodel.SuggestionViewModel
import org.kodein.di.erased.instance

class MainActivity : PostActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val detailDao by instance<DetailDao>()
    private val api by instance<Api>()

    private lateinit var suggestionViewModel: SuggestionViewModel

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val suggestions: MutableList<String> = mutableListOf()

    private var suggestionAdapter: CursorAdapter? = null

    @IdRes
    private var currentFragmentId = R.id.nav_posts

    private var user: User? = null

    private lateinit var headerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp.registerOnSharedPreferenceChangeListener(this)
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_posts,
                R.id.nav_history,
                R.id.nav_settings,
                R.id.nav_about
            ),
            drawer_layout
        )
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentId = destination.id
            val lp = toolbar.layoutParams as AppBarLayout.LayoutParams
            when (currentFragmentId) {
                R.id.nav_posts -> {
                    lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    fab.visibility = View.VISIBLE
                }
                else -> {
                    lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                    fab.visibility = View.GONE
                }
            }
            delegate.invalidateOptionsMenu()
        }
        fab.setOnClickListener {
            when (currentFragmentId) {
                R.id.nav_posts -> {
                    sendBroadcast(
                        Intent(JUMP_TO_TOP_ACTION_FILTER_KEY)
                            .putExtra(JUMP_TO_TOP_KEY, true)
                            .putExtra(JUMP_TO_TOP_QUERY_KEY, "")
                    )
                }
                R.id.nav_history -> {
                    sendBroadcast(
                        Intent(HISTORY_JUMP_TO_TOP_ACTION_FILTER_KEY)
                            .putExtra(HISTORY_JUMP_TO_TOP_KEY, true)
                    )
                }
            }
        }
        headerView = nav_view.getHeaderView(0)
        loadUser()
        headerView.setOnClickListener {
            if (user == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, UserActivity::class.java))
            }
        }
        if (Settings.hostname == "fiepi.com") {
            navController.navigate(R.id.nav_settings)
            Toast.makeText(
                this,
                getString(R.string.msg_you_must_first_set_your_host),
                Toast.LENGTH_LONG
            ).show()
        }
        suggestionViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SuggestionViewModel(SuggestionRepositoryImpl(api)) as T
            }
        })
        suggestionViewModel.tags.observe(this, Observer {
            handleSuggestions(it)
        })
    }

    private fun loadUser() {
        lifecycleScope.launch {
            user = withContext(Dispatchers.IO) {
                UserManager.getUserByUid(Settings.userUid)
            }
            when (val user = user) {
                null -> {
                    headerView.apply {
                        findViewById<CircleImageView>(R.id.user_avatar).setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                        findViewById<AppCompatTextView>(R.id.user_name).setText(R.string.nav_header_login)
                        findViewById<AppCompatTextView>(R.id.user_id).text = ""
                    }
                }
                else -> {
                    headerView.apply {
                        GlideApp.with(this@MainActivity)
                            .load(user.avatarUrl)
                            .placeholder(ContextCompat.getDrawable(this@MainActivity, R.drawable.avatar_user))
                            .into(findViewById<CircleImageView>(R.id.user_avatar))
                        findViewById<AppCompatTextView>(R.id.user_name).text = user.username
                        findViewById<AppCompatTextView>(R.id.user_id).text = user.userId.toString()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        when (currentFragmentId) {
            R.id.nav_posts -> {
                menuInflater.inflate(R.menu.posts, menu)
                val item = menu?.findItem(R.id.action_search) ?: return true
                val searchView = item.actionView as SearchView
                initSearchView(searchView)
            }
            R.id.nav_history -> menuInflater.inflate(R.menu.history, menu)
            R.id.nav_about -> menuInflater.inflate(R.menu.about, menu)
            else -> menu?.clear()
        }
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
                    searchView.setQuery(suggestions[position], false)
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

    private fun fetchSuggestions(tag: String) {
        val token = Settings.userToken
        if (token.isEmpty()) return
        suggestionViewModel.fetch(
            scheme = Settings.scheme,
            host = Settings.hostname,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_all_history -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.history_clear_all)
                    .setMessage(R.string.history_clear_all_content)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            detailDao.deleteAll()
                        }
                    }
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .create()
                    .show()
            }
            R.id.action_donation -> findNavController(R.id.nav_host_fragment).navigate(R.id.nav_purchase)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SETTINGS_NIGHT_MODE_KEY -> AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
            USER_UID_KEY -> loadUser()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun search(query: String) {
        findNavController(R.id.nav_host_fragment).navigate(
            R.id.nav_search,
            Bundle().apply {
                putString(QUERY_KEY, query)
            }
        )
    }
}