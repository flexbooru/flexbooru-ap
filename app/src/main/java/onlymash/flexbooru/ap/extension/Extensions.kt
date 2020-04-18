package onlymash.flexbooru.ap.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.StaticLayout
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
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

fun Activity.getWindowWidth(): Int {
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Activity.getSanCount(): Int {
    val itemWidth = resources.getDimensionPixelSize(gridWidthResId)
    val count = (getWindowWidth().toFloat() / itemWidth.toFloat()).roundToInt()
    return if (count < 1) 1 else count
}

fun View.toFullscreenStable() {
    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}

fun View.toFullscreenImmersive() {
    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE
}


inline var Window.isShowBar: Boolean
    get() = (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0
    set(value) {
        if (value) {
            decorView.toFullscreenStable()
        } else {
            decorView.toFullscreenImmersive()
        }
    }
