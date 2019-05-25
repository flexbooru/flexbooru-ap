package onlymash.flexbooru.ap.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.app_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.glide.GlideApp

class UserActivity : AppCompatActivity() {

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_account)
        }
        user = UserManager.getUserByUid(Settings.userUid)
        user?.let { user ->
            GlideApp.with(this)
                .load(user.avatarUrl)
                .centerCrop()
                .placeholder(ContextCompat.getDrawable(this, R.drawable.avatar_user))
                .into(user_avatar)
            user_id.text = user.userId.toString()
            username.text = user.username
            fav_action_button.setOnClickListener {
                SearchActivity.startSearchActivity(
                    context = this,
                    query = "stars_by:${user.username}",
                    userId = user.userId,
                    searchType = SearchType.FAVORITE
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.user_logout)
                    .setMessage(R.string.user_logout_content)
                    .setPositiveButton(R.string.dialog_yes) { _, _ ->
                        user?.let { user ->
                            UserManager.deleteByUid(user.uid)
                        }
                        Settings.userUid = -1L
                        finish()
                    }
                    .setNegativeButton(R.string.dialog_no, null)
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
