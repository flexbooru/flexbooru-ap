package onlymash.flexbooru.ap.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.progress_bar.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.data.repository.login.LoginRepositoryImpl
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.viewmodel.LoginViewModel
import onlymash.flexbooru.ap.widget.setupInsets
import org.kodein.di.erased.instance

class LoginActivity : KodeinActivity() {

    private val api by instance<Api>()
    private lateinit var loginViewModel: LoginViewModel

    private var username = ""
    private var password = ""
    private var requesting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupInsets {  }
        loginViewModel = getViewModel(LoginViewModel(LoginRepositoryImpl(api)))
        loginViewModel.loginResult.observe(this, Observer {
            handleResult(it)
        })
        sign_in_button.setOnClickListener {
            attemptSignIn()
        }
    }

    private fun attemptSignIn() {
        if (requesting) return
        username = username_edit.text.toString().trim()
        password = password_edit.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.make(sign_in_button, getString(R.string.msg_login_tip_empty), Snackbar.LENGTH_LONG).show()
            return
        }
        loginViewModel.login(
            username = username,
            password = password
        )
        sign_in_button.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    private fun handleResult(result: NetResult<User>) {
        sign_in_button.visibility = View.VISIBLE
        progress_bar.visibility = View.GONE
        when (result) {
            is NetResult.Success -> {
                Settings.userToken = result.data.token
                Settings.userUid = result.data.uid
                startActivity(Intent(this, UserActivity::class.java))
                finish()
            }
            is NetResult.Error -> {
                error_msg.text = result.errorMsg
            }
        }
    }
}
