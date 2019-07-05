package onlymash.flexbooru.ap.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.setupwizardlib.view.NavigationBar
import kotlinx.android.synthetic.main.activity_setup_wizard.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.SCHEME_HTTP
import onlymash.flexbooru.ap.common.SCHEME_HTTPS
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.extension.toVisibility

class SetupWizardActivity : AppCompatActivity() {

    private val hosts = arrayOf("anime-pictures.net")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_wizard)
        setup.apply {
            setHeaderText(R.string.set_up_server)
            navigationBar.apply {
                backButton.toVisibility(false)
                setNavigationBarListener(object : NavigationBar.NavigationBarListener {
                    override fun onNavigateBack() {
                        onBackPressed()
                    }
                    override fun onNavigateNext() {
                        val host = host_edit.text?.toString() ?: ""
                        when {
                            host in hosts -> {
                                Settings.hostname = host
                                Settings.isOpenSetupWizard = false
                                startActivity(Intent(this@SetupWizardActivity, MainActivity::class.java))
                                finish()
                            }
                            host.isEmpty() -> host_edit.error = getString(R.string.msg_host_cannot_be_empty)
                            else -> host_edit.error = getString(R.string.msg_unsupported_host)
                        }
                    }
                })
            }
        }
        scheme_group.setOnCheckedChangeListener { _, checkedId ->
            Settings.scheme = when (checkedId) {
                R.id.scheme_https -> SCHEME_HTTPS
                else -> SCHEME_HTTP
            }
        }
    }
}
