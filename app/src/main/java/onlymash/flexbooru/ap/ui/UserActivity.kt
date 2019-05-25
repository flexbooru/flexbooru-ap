package onlymash.flexbooru.ap.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.app_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.glide.GlideApp

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_account)
        }
        val user = UserManager.getUserByUid(Settings.userUid) ?: return
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
