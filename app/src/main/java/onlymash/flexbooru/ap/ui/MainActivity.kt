package onlymash.flexbooru.ap.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.floating_action_button.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.SETTINGS_NIGHT_MODE_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import org.kodein.di.generic.instance

class MainActivity : KodeinActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()

    private lateinit var appBarConfiguration: AppBarConfiguration

    @IdRes
    private var currentFragmentId = R.id.nav_posts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp.registerOnSharedPreferenceChangeListener(this)
        setSupportActionBar(toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_posts,
                R.id.nav_settings
            ),
            drawer_layout
        )
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentId = destination.id
            val lp = toolbar.layoutParams as AppBarLayout.LayoutParams
            if (destination.id == R.id.nav_posts) {
                lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                fab.visibility = View.VISIBLE
            } else {
                lp.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                fab.visibility = View.GONE
            }
            delegate.invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (currentFragmentId) {
            R.id.nav_posts -> menuInflater.inflate(R.menu.fragment_post, menu)
            R.id.nav_settings -> menuInflater.inflate(R.menu.fragment_settings, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_posts -> Log.w("onOptionsItemSelected", "Posts")
            R.id.action_settings -> Log.w("onOptionsItemSelected", "Settings")
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }
}