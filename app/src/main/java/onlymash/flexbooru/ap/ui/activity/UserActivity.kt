package onlymash.flexbooru.ap.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.SearchType
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.databinding.ActivityUserBinding
import onlymash.flexbooru.ap.extension.getLogoutUrl
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.extension.setupInsets
import org.kodein.di.instance
import kotlin.Exception

private const val USER_ID_KEY = "user_id"
private const val USERNAME_KEY = "username"
private const val AVATAR_URL_KEY = "avatar_url"

class UserActivity : KodeinActivity() {

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

    private val binding by viewBinding(ActivityUserBinding::inflate)

    private var user: User? = null
    private val api by instance<Api>()
    private val postDao by instance<PostDao>()
    private val detailDao by instance<DetailDao>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets { insets ->
            binding.layoutAppBar.containerToolbar.minimumHeight =
                binding.layoutAppBar.toolbar.minimumHeight + insets.top
            binding.scrollContainer.updatePadding(bottom = insets.bottom)
        }
        setSupportActionBar(binding.layoutAppBar.toolbar)
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
                .into(binding.userAvatar)
        }
        binding.userId.text = userId.toString()
        binding.username.text = name
        binding.votesButton.setOnClickListener {
            SearchActivity.startSearchActivity(
                context = this,
                query = "stars_by:$name",
                userId = userId,
                searchType = SearchType.FAVORITE
            )
        }
        binding.postsButton.setOnClickListener {
            SearchActivity.startSearchActivity(
                context = this,
                query = "user:$name",
                uploaderId = userId,
                searchType = SearchType.UPLOADED
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
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
                            binding.layoutProgress.progressBar.isVisible = true
                                lifecycleScope.launch {
                                if (logout(user)) {
                                    Settings.userUid = -1L
                                    Settings.userToken = ""
                                    finish()
                                } else {
                                    binding.layoutProgress.progressBar.isVisible = false
                                }
                            }
                        }
                    }
                    .setNegativeButton(R.string.dialog_no, null)
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun logout(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                try {
                    api.logout(url = getLogoutUrl(token = user.token))
                } catch (_: Exception) {}
                UserManager.deleteByUid(user.uid)
                postDao.deleteAll()
                detailDao.deleteAll()
                true
            }catch (_: Exception) {
                false
            }
        }
    }
}
