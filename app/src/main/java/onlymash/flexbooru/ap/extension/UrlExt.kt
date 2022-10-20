package onlymash.flexbooru.ap.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.Post

fun Post.getPreviewUrl(): String {
    return when (Settings.previewSize) {
        FILE_SIZE_SMALL -> smallPreview
        FILE_SIZE_MEDIUM -> mediumPreview
        else -> bigPreview
    }
}

fun Detail.getPreviewUrl(): String {
    return when (Settings.previewSize) {
        FILE_SIZE_SMALL -> smallPreview
        FILE_SIZE_MEDIUM -> mediumPreview
        else -> bigPreview
    }
}

fun Detail.getDetailUrl(): String {
    return when (Settings.detailSize) {
        FILE_SIZE_MEDIUM -> mediumPreview
        FILE_SIZE_BIG -> bigPreview
        FILE_SIZE_ORIGIN -> fileUrl.toEncodedUrl()
        else -> smallPreview
    }
}

private fun getCustomTabsIntent(context: Context): CustomTabsIntent {
    return CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.colorToolbar))
            .build())
        .build()
}

fun Context.launchUrl(uri: Uri) = try {
    getCustomTabsIntent(this).launchUrl(this, uri)
} catch (e: ActivityNotFoundException) { e.printStackTrace() }

fun Context.launchUrl(url: String) = this.launchUrl(Uri.parse(url))

fun String.toEncodedUrl(): String =
    this.replace(" ", "%20")
        .replace("?", "%3F")