package onlymash.flexbooru.ap.common

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.kodein.di.generic.instance


const val SCHEME_KEY = "scheme"
const val HOST_NAME_KEY = "hostname"

const val SETTINGS_NIGHT_MODE = "settings_night_mode"
private const val SETTINGS_NIGHT_MODE_SYSTEM = "system"
private const val SETTINGS_NIGHT_MODE_ON = "on"
private const val SETTINGS_NIGHT_MODE_OFF = "off"

object Settings {

    private val sp by App.app.instance<SharedPreferences>()

    var scheme: String
        get() = sp.getString(SCHEME_KEY, "https") ?: "https"
        set(value) = sp.edit().putString(SCHEME_KEY, value).apply()

    var hostname: String
        get() = sp.getString(HOST_NAME_KEY, "anime-pictures.net") ?: "anime-pictures.net"
        set(value) = sp.edit().putString(HOST_NAME_KEY, value).apply()

    private val nightModeString: String
        get() = sp.getString(SETTINGS_NIGHT_MODE, SETTINGS_NIGHT_MODE_SYSTEM) ?: SETTINGS_NIGHT_MODE_SYSTEM

    @AppCompatDelegate.NightMode
    val nightMode: Int
        get() = when (nightModeString) {
            SETTINGS_NIGHT_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
            SETTINGS_NIGHT_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
}