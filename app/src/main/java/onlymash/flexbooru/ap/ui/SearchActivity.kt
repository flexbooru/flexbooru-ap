package onlymash.flexbooru.ap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.floating_action_button.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.QUERY_KEY
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_ACTION_FILTER_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_KEY
import onlymash.flexbooru.ap.ui.fragment.JUMP_TO_TOP_QUERY_KEY

class SearchActivity : KodeinActivity() {

    companion object {
        fun startSearchActivity(context: Context, query: String) {
            context.startActivity(
                Intent(context, SearchActivity::class.java)
                .putExtra(QUERY_KEY, query)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val query = intent?.getStringExtra(QUERY_KEY) ?: ""
        findNavController(R.id.search_nav_host_fragment).setGraph(
            R.navigation.search_navigation,
            Bundle().apply {
                putString(QUERY_KEY, query)
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
}
