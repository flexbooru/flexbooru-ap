package onlymash.flexbooru.ap.ui

import android.content.Context
import android.content.Intent
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

private const val USER_ID_KEY = "user_id"
private const val USERNAME_KEY = "username"
private const val AVATAR_URL_KEY = "avatar_url"

class UserActivity : AppCompatActivity() {

    companion object {
        fun startUserActivity(
            context: Context,
            userId: Int,
            username: String,
            avatarUrl: String?) {
            context.startActivity(Intent(context, UserActivity::class.java).apply {
                putExtra(USER_ID_KEY, userId)
                putExtra(USERNAME_KEY, username)
                putExtra(AVATAR_URL_KEY, avatarUrl)
            })
        }
    }

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_account)
        }
        var userId = -1
        var name = ""
        var avatarUrl: String? = null
        intent?.let {
            userId = it.getIntExtra(USER_ID_KEY, -1)
            name = it.getStringExtra(USERNAME_KEY) ?: ""
            avatarUrl = it.getStringExtra(AVATAR_URL_KEY) ?: ""
        }
        if (userId < 0) {
            user = UserManager.getUserByUid(Settings.userUid)
            user?.let {
                userId = it.userId
                name = it.username
                avatarUrl = it.avatarUrl ?: ""
            }
        }
        if (!avatarUrl.isNullOrEmpty()) {
            GlideApp.with(this)
                .load(avatarUrl)
                .centerCrop()
                .placeholder(ContextCompat.getDrawable(this, R.drawable.avatar_user))
                .into(user_avatar)
        }
        user_id.text = userId.toString()
        username.text = name
        votes_button.setOnClickListener {
            SearchActivity.startSearchActivity(
                context = this,
                query = "stars_by:$name",
                userId = userId,
                searchType = SearchType.FAVORITE
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (user != null) {
            menuInflater.inflate(R.menu.user, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.user_logout)
                    .setMessage(R.string.user_logout_content)
                    .setPositiveButton(R.string.dialog_yes) { _, _ ->
                        user?.let { user ->
                            UserManager.deleteByUid(user.uid)
                        }
                        Settings.userToken = ""
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
