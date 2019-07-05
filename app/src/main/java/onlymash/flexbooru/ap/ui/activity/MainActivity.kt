package onlymash.flexbooru.ap.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.database.MatrixCursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.drawer_tags.*
import kotlinx.android.synthetic.main.floating_action_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.db.dao.TagBlacklistDao
import onlymash.flexbooru.ap.data.db.dao.TagFilterDao
import onlymash.flexbooru.ap.data.model.Tag
import onlymash.flexbooru.ap.data.model.TagBlacklist
import onlymash.flexbooru.ap.data.model.TagFilter
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.PostActivity
import onlymash.flexbooru.ap.ui.diffcallback.TagFilterDiffCallback
import onlymash.flexbooru.ap.ui.fragment.*
import onlymash.flexbooru.ap.ui.viewmodel.SuggestionViewModel
import onlymash.flexbooru.ap.ui.viewmodel.TagFilterViewModel
import org.kodein.di.erased.instance

private const val SUGGESTION_FOR_NORMAL = 0
private const val SUGGESTION_FOR_FILTER = 1

class MainActivity : PostActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val detailDao by instance<DetailDao>()
    private val tagFilterDao by instance<TagFilterDao>()
    private val tagBlacklistDao by instance<TagBlacklistDao>()
    private val api by instance<Api>()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var suggestionViewModel: SuggestionViewModel
    private lateinit var tagFilterViewModel: TagFilterViewModel

    private val suggestions: MutableList<String> = mutableListOf()
    private val tagsFilter: MutableList<TagFilter> = mutableListOf()

    private val tagsSearch: MutableList<String> = mutableListOf()

    private var suggestionFor = SUGGESTION_FOR_NORMAL

    private var suggestionAdapter: CursorAdapter? = null
    private var tagFilterSuggestionAdapter: CursorAdapter? = null
    private lateinit var tagFilterAdapter: TagFilterAdapter

    @IdRes
    private var currentFragmentId = R.id.nav_posts

    private var user: User? = null

    private lateinit var headerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp.registerOnSharedPreferenceChangeListener(this)
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_posts,
                R.id.nav_history,
                R.id.nav_tags_blacklist,
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
        suggestionViewModel = getViewModel(SuggestionViewModel(SuggestionRepositoryImpl(api)))
        suggestionViewModel.tags.observe(this, Observer {
            handleSuggestions(it)
        })
        initTagFilter()
    }

    private fun initTagFilter() {
        ViewCompat.setOnApplyWindowInsetsListener(nav_view_tags) { _, insets ->
            (nav_view_tags.layoutParams as DrawerLayout.LayoutParams).topMargin = insets.systemWindowInsetTop
            insets
        }
        toolbar_drawer.apply {
            setNavigationOnClickListener {
                drawer_layout.closeDrawer(GravityCompat.END)
            }
            menu?.findItem(R.id.action_add_tag_filter)?.let {
                val searchView = it.actionView as SearchView
                initTagFilterSearchView(searchView)
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_clear_all -> {
                        if (tagsFilter.isNotEmpty()) {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle(R.string.tags_filter_clear_all)
                                .setMessage(R.string.tags_filter_clear_all_content)
                                .setPositiveButton(R.string.dialog_ok) { _, _ ->
                                    tagFilterViewModel.deleteAll()
                                    tagsSearch.clear()
                                }
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .create()
                                .show()
                        }
                    }
                }
                true
            }
        }
        tagFilterAdapter = TagFilterAdapter()
        tags_filter_list.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = tagFilterAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, RecyclerView.VERTICAL))
        }
        tagFilterViewModel = getViewModel(TagFilterViewModel(tagFilterDao))
        tagFilterViewModel.tags.observe(this, Observer {
            handleTagFilter(it)
        })
        tagFilterViewModel.loadAll()
        fab_search.setOnClickListener {
            if (tagsSearch.isEmpty()) {
                return@setOnClickListener
            }
            var query = ""
            tagsSearch.forEach {
                query = if (query.isEmpty()) {
                    it
                } else {
                    "$query&&$it"
                }
            }
            query = query.trim()
            if (query.isNotEmpty()) {
                search(query)
            }
        }
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
            R.id.nav_tags_blacklist -> {
                menuInflater.inflate(R.menu.tag_blacklist, menu)
                val item = menu?.findItem(R.id.action_search) ?: return true
                val searchView = item.actionView as SearchView
                initSearchView(searchView)
            }
            R.id.nav_about -> menuInflater.inflate(R.menu.about, menu)
            else -> menu?.clear()
        }
        return true
    }

    private fun initTagFilterSearchView(searchView: SearchView) {
        tagFilterSuggestionAdapter = SimpleCursorAdapter(
            this,
            R.layout.item_suggestion_tag_filter,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            0
        )
        searchView.apply {
            queryHint = getString(R.string.add_tag_filter_hint)
            val icon = findViewById<ImageView>(androidx.appcompat.R.id.search_button)
            icon.setImageResource(R.drawable.ic_playlist_add_24dp)
            TooltipCompat.setTooltipText(icon, getString(R.string.action_add_tag_filter))
            suggestionsAdapter = tagFilterSuggestionAdapter
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionClick(position: Int): Boolean {
                    tagFilterViewModel.create(suggestions[position])
                    return true
                }
                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }
            })
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) return false
                    suggestionFor = SUGGESTION_FOR_FILTER
                    fetchSuggestions(newText)
                    return true
                }
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query.isNullOrEmpty()) return false
                    tagFilterViewModel.create(query)
                    return true
                }
            })
        }
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
            if (currentFragmentId == R.id.nav_tags_blacklist) {
                queryHint = getString(R.string.tags_blacklist_add_hint)
                val icon = findViewById<ImageView>(androidx.appcompat.R.id.search_button)
                icon.setImageResource(R.drawable.ic_playlist_add_24dp)
                TooltipCompat.setTooltipText(icon, getString(R.string.tags_blacklist_add))
            } else {
                queryHint = getString(R.string.search_posts_hint)
            }
            suggestionsAdapter = suggestionAdapter
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionClick(position: Int): Boolean {
                    val tag = suggestions[position]
                    when (currentFragmentId) {
                        R.id.nav_posts -> search(tag)
                        R.id.nav_tags_blacklist -> lifecycleScope.launch(Dispatchers.IO) {
                            tagBlacklistDao.insert(TagBlacklist(name = tag))
                        }
                    }
                    return true
                }
                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }
            })
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) return false
                    suggestionFor = SUGGESTION_FOR_NORMAL
                    fetchSuggestions(newText)
                    return true
                }
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query.isNullOrEmpty()) return false
                    when (currentFragmentId) {
                        R.id.nav_posts -> search(query)
                        R.id.nav_tags_blacklist -> lifecycleScope.launch(Dispatchers.IO) {
                            tagBlacklistDao.insert(TagBlacklist(name = query))
                        }
                    }
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
        when (suggestionFor) {
            SUGGESTION_FOR_NORMAL -> suggestionAdapter?.swapCursor(cursor)
            SUGGESTION_FOR_FILTER -> tagFilterSuggestionAdapter?.swapCursor(cursor)
        }
    }

    private fun handleTagFilter(tags: List<TagFilter>) {
        val oldItems = mutableListOf<TagFilter>()
        oldItems.addAll(tagsFilter)
        tagsFilter.clear()
        tagsFilter.addAll(tags)
        val result = DiffUtil.calculateDiff(TagFilterDiffCallback(oldItems, tagsFilter))
        result.dispatchUpdatesTo(tagFilterAdapter)
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

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            drawer_layout.isDrawerOpen(GravityCompat.END) -> drawer_layout.closeDrawer(GravityCompat.END)
            else -> super.onBackPressed()
        }
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

    inner class TagFilterAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = tagsFilter.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as TagFilterViewHolder).bind(tagsFilter[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TagFilterViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag_filter, parent, false))

        inner class TagFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val name: AppCompatTextView = itemView.findViewById(R.id.name)
            private val actionMenuView: ActionMenuView = itemView.findViewById(R.id.menu_view)

            private var tagFilter: TagFilter? = null

            init {
                MenuInflater(itemView.context).inflate(R.menu.tag_filter_item, actionMenuView.menu)
                actionMenuView.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_copy -> tagFilter?.let {
                            copyText(it.name)
                        }
                        R.id.action_delete -> tagFilter?.let {
                            if (itemView.isSelected) {
                                tagsSearch.remove(it.name)
                            }
                            tagFilterViewModel.delete(it)
                        }
                    }
                    true
                }
                itemView.setOnClickListener {
                    tagFilter?.let { tag ->
                        val isSelected = itemView.isSelected
                        if (isSelected) {
                            tagsSearch.remove(tag.name)
                        } else {
                            tagsSearch.add(tag.name)
                        }
                        itemView.isSelected = !isSelected
                    }
                }
            }

            fun bind(tag: TagFilter) {
                tagFilter = tag
                name.text = tag.name
                itemView.isSelected = tag.name in tagsSearch
            }
        }
    }
}