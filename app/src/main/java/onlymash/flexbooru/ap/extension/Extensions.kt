package onlymash.flexbooru.ap.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.StaticLayout
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.postDelayed
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.SETTINGS_GRID_WIDTH_BIG
import onlymash.flexbooru.ap.common.SETTINGS_GRID_WIDTH_SMALL
import onlymash.flexbooru.ap.common.Settings
import kotlin.math.roundToInt

/**
 * An extension to `postponeEnterTransition` which will resume after a timeout.
 */
fun Activity.postponeEnterTransition(timeout: Long) {
    postponeEnterTransition()
    window.decorView.postDelayed(timeout) {
        startPostponedEnterTransition()
    }
}

/**
 * Calculated the widest line in a [StaticLayout].
 */
fun StaticLayout.textWidth(): Int {
    var width = 0f
    for (i in 0 until lineCount) {
        width = width.coerceAtLeast(getLineWidth(i))
    }
    return width.toInt()
}

/**
 * Linearly interpolate between two values.
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

fun Context.openAppInMarket(packageName: String) {
    try {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
                getString(R.string.share_via)))
    } catch (_: ActivityNotFoundException) { }
}

fun Context.safeOpenIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {}
}


private val gridWidthResId: Int
    get() = when (Settings.gridWidthString) {
        SETTINGS_GRID_WIDTH_SMALL -> R.dimen.grid_width_small
        SETTINGS_GRID_WIDTH_BIG -> R.dimen.grid_width_big
        else -> R.dimen.grid_width_normal
    }

fun Activity.getSanCount(): Int {
    val itemWidth = resources.getDimensionPixelSize(gridWidthResId)
    val count = (windowWidth.toFloat() / itemWidth.toFloat()).roundToInt()
    return if (count < 1) 1 else count
}

fun Window.showSystemBars() {
    WindowCompat.getInsetsController(this, decorView).show(WindowInsetsCompat.Type.systemBars())
}

fun Window.hideSystemBars() {
    WindowCompat.getInsetsController(this, decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}


@Suppress("DEPRECATION")
inline val Window.isStatusBarShown: Boolean
    get() {
        val flags = attributes.flags
        val newFlags = flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        return flags == newFlags
    }


inline val Activity.windowHeight: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.height() - insets.bottom - insets.top
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view).getInsets(
                WindowInsetsCompat.Type.systemBars())
            resources.displayMetrics.heightPixels - insets.bottom - insets.top
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(dm)
            dm.heightPixels
        }
    }

inline val Activity.windowWidth: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.width() - insets.left - insets.right
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets, view).getInsets(
                WindowInsetsCompat.Type.systemBars())
            resources.displayMetrics.widthPixels - insets.left - insets.right
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(dm)
            dm.widthPixels
        }
    }