package onlymash.flexbooru.ap.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.ap.BuildConfig
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.copyText
import onlymash.flexbooru.ap.extension.launchUrl
import onlymash.flexbooru.ap.extension.openAppInMarket
import onlymash.flexbooru.ap.ui.CopyrightActivity

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)
        findPreference<Preference>("about_app_version")?.summary = BuildConfig.VERSION_NAME
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "about_author_website" -> {
                context?.launchUrl("https://blog.fiepi.com")
            }
            "about_author_email" -> {
                val email = "mailto:im@fiepi.me"
                context?.startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_feedback_github" -> {
                context?.launchUrl("https://github.com/flexbooru/flexbooru-ap/issues")
            }
            "about_feedback_telegram" -> {
                context?.launchUrl("https://t.me/Flexbooru")
            }
            "about_feedback_email" -> {
                val email = "mailto:feedback@fiepi.me"
                context?.startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SENDTO
                    data = email.toUri()
                }, getString(R.string.share_via)))
            }
            "about_donation_paypal" -> {
                context?.launchUrl("https://www.paypal.me/fiepi")
            }
            "about_donation_alipay" -> {
                val text = "im@fiepi.com"
                context?.copyText(text)
                view?.let { Snackbar.make(it, getString(R.string.placeholder_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_donation_btc" -> {
                val text = "bc1qxanfk3hc853787a9ctm28x9ff0pvcyy6vpmgpz"
                context?.copyText(text)
                view?.let { Snackbar.make(it, getString(R.string.placeholder_copy_text, text), Snackbar.LENGTH_LONG).show() }
            }
            "about_app_rate" -> {
                context?.run {
                    openAppInMarket(applicationContext.packageName)
                }
            }
            "about_app_translation" -> {
                context?.launchUrl("https://crowdin.com/project/flexbooru")
            }
            "about_licenses" -> {
                context?.run {
                    startActivity(Intent(this, CopyrightActivity::class.java))
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}