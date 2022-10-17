package onlymash.flexbooru.ap.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.PREFERENCE_NAME
import onlymash.flexbooru.ap.common.SETTINGS_PATH_KEY
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.extension.decode
import onlymash.flexbooru.ap.extension.getTreeUri
import onlymash.flexbooru.ap.extension.openDocumentTree
import onlymash.flexbooru.ap.widget.ListListener

private const val PATH_KEY = "settings_download_path"

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setOnApplyWindowInsetsListener(ListListener)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.apply {
            sharedPreferencesName = PREFERENCE_NAME
            sharedPreferencesMode = Context.MODE_PRIVATE
        }
        setPreferencesFromResource(R.xml.pref_settings, "settings_screen")
        Settings.pathString = context?.contentResolver?.getTreeUri()?.toString()?.decode()
        initPathSummary()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    private fun initPathSummary() {
        var pathSummary = Settings.pathString
        if (pathSummary.isNullOrEmpty()) {
            pathSummary = getString(R.string.settings_unset)
        }
        findPreference<Preference>(PATH_KEY)?.summary = pathSummary
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == PATH_KEY) {
            activity?.openDocumentTree()
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SETTINGS_PATH_KEY -> initPathSummary()
        }
    }
}