package onlymash.flexbooru.ap.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import onlymash.flexbooru.ap.BuildConfig
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.launchUrl
import onlymash.flexbooru.ap.extension.openAppInMarket
import onlymash.flexbooru.ap.ui.activity.CopyrightActivity
import onlymash.flexbooru.ap.extension.setupBottomPadding

class AboutFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setupBottomPadding()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)
        findPreference<Preference>("about_app_version")?.summary = BuildConfig.VERSION_NAME
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
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
            "about_app_rate" -> {
                context?.run {
                    openAppInMarket(applicationContext.packageName)
                }
            }
            "about_app_translation" -> {
                context?.launchUrl("https://crowdin.com/project/flexbooru-ap")
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