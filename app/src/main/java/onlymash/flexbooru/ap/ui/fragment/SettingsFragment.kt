package onlymash.flexbooru.ap.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.PREFERENCE_NAME

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREFERENCE_NAME
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.pref_settings, "settings_screen")
    }
}