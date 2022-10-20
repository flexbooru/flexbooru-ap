package onlymash.flexbooru.ap.common

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.kodein.di.instance


const val SETTINGS_NIGHT_MODE_KEY = "settings_night_mode"
private const val SETTINGS_NIGHT_MODE_ON = "on"
private const val SETTINGS_NIGHT_MODE_OFF = "off"
private const val SETTINGS_NIGHT_MODE_SYSTEM = "system"
private const val SETTINGS_NIGHT_MODE_BATTERY = "battery"

const val SETTINGS_PAGE_LIMIT_KEY = "settings_page_limit"
const val SETTINGS_MUZEI_LIMIT_KEY = "settings_muzei_limit"
const val SETTINGS_PATH_KEY = "settings_path"

private const val SETTINGS_PREVIEW_SIZE_KEY = "settings_preview_size"
private const val SETTINGS_DETAIL_SIZE_KEY = "settings_detail_size"
const val FILE_SIZE_SMALL = "small"
const val FILE_SIZE_MEDIUM = "medium"
const val FILE_SIZE_BIG = "big"
const val FILE_SIZE_ORIGIN = "origin"

const val USER_UID_KEY = "user_uid"
const val USER_TOKEN_KEY = "token"

const val SETTINGS_GRID_WIDTH_KEY = "settings_grid_width"
const val SETTINGS_GRID_WIDTH_SMALL = "small"
const val SETTINGS_GRID_WIDTH_NORMAL = "normal"
const val SETTINGS_GRID_WIDTH_BIG = "big"

private const val OPEN_SETUP_WIZARD_KEY = "open_setup_wizard"

private const val MUZEI_QUERY_KEY = "muzei_query"

object Settings {

    private val sp: SharedPreferences by App.app.instance()

    private val nightModeString: String
        get() = sp.getString(SETTINGS_NIGHT_MODE_KEY, SETTINGS_NIGHT_MODE_SYSTEM) ?: SETTINGS_NIGHT_MODE_SYSTEM

    @AppCompatDelegate.NightMode
    val nightMode: Int
        get() = when (nightModeString) {
            SETTINGS_NIGHT_MODE_ON -> AppCompatDelegate.MODE_NIGHT_YES
            SETTINGS_NIGHT_MODE_OFF -> AppCompatDelegate.MODE_NIGHT_NO
            SETTINGS_NIGHT_MODE_BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

    val pageLimit: Int
        get() = sp.getString(SETTINGS_PAGE_LIMIT_KEY, "20")?.toInt() ?: 20

    val muzeiLimit: Int
        get() = sp.getString(SETTINGS_MUZEI_LIMIT_KEY, "10")?.toInt() ?: 10

    var pathString: String?
        get() = sp.getString(SETTINGS_PATH_KEY, "")
        set(value) = sp.edit().putString(SETTINGS_PATH_KEY, value).apply()

    val previewSize: String
        get() = sp.getString(SETTINGS_PREVIEW_SIZE_KEY, FILE_SIZE_MEDIUM) ?: FILE_SIZE_MEDIUM

    val detailSize: String
        get() = sp.getString(SETTINGS_DETAIL_SIZE_KEY, FILE_SIZE_BIG) ?: FILE_SIZE_BIG

    var userUid: Long
        get() = sp.getLong(USER_UID_KEY, -1L)
        set(value) = sp.edit().putLong(USER_UID_KEY, value).apply()

    var userToken: String
        get() = sp.getString(USER_TOKEN_KEY, "") ?: ""
        set(value) = sp.edit().putString(USER_TOKEN_KEY, value).apply()

    val gridWidthString: String
        get() = sp.getString(SETTINGS_GRID_WIDTH_KEY, SETTINGS_GRID_WIDTH_NORMAL) ?: SETTINGS_GRID_WIDTH_NORMAL

    var isOpenSetupWizard: Boolean
        get() = sp.getBoolean(OPEN_SETUP_WIZARD_KEY, true)
        set(value) = sp.edit().putBoolean(OPEN_SETUP_WIZARD_KEY, value).apply()

    var muzeiQuery: String
        get() = sp.getString(MUZEI_QUERY_KEY, "") ?: ""
        set(value) = sp.edit().putString(MUZEI_QUERY_KEY, value).apply()
}