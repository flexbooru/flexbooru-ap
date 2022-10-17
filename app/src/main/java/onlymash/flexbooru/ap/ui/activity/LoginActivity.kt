package onlymash.flexbooru.ap.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.data.repository.login.LoginRepositoryImpl
import onlymash.flexbooru.ap.databinding.ActivityLoginBinding
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.extension.launchUrl
import onlymash.flexbooru.ap.ui.base.KodeinActivity
import onlymash.flexbooru.ap.ui.viewmodel.LoginViewModel
import onlymash.flexbooru.ap.viewbinding.viewBinding
import onlymash.flexbooru.ap.widget.setupInsets
import org.kodein.di.instance

class LoginActivity : KodeinActivity() {

    private val api by instance<Api>()

    private val binding by viewBinding(ActivityLoginBinding::inflate)
    private val progressBar get() = binding.progress.progressBar
    private val errorMsg get() = binding.errorMsg
    private val signIn get() = binding.signInButton
    private val register get() = binding.register
    private val usernameEdit get() = binding.usernameEdit
    private val passwordEdit get() = binding.passwordEdit

    private lateinit var loginViewModel: LoginViewModel

    private var username = ""
    private var password = ""
    private var requesting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets {  }
        loginViewModel = getViewModel(LoginViewModel(LoginRepositoryImpl(api)))
        loginViewModel.loginResult.observe(this, Observer { handleResult(it) })
        signIn.setOnClickListener { attemptSignIn() }
        register.setOnClickListener { launchUrl("https://anime-pictures.net/login/view_register") }
    }

    private fun attemptSignIn() {
        if (requesting) return
        username = usernameEdit.text.toString().trim()
        password = passwordEdit.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.msg_login_tip_empty), Snackbar.LENGTH_LONG).show()
            return
        }
        loginViewModel.login(
            username = username,
            password = password
        )
        signIn.isVisible = false
        progressBar.isVisible = true
    }

    private fun handleResult(result: NetResult<User>) {
        signIn.isVisible = true
        progressBar.isVisible = false
        when (result) {
            is NetResult.Success -> {
                Settings.userToken = result.data.token
                Settings.userUid = result.data.uid
                startActivity(Intent(this, UserActivity::class.java))
                finish()
            }
            is NetResult.Error -> {
                errorMsg.text = result.errorMsg
            }
        }
    }
}
