package onlymash.flexbooru.ap.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun Context.copyText(text: String?) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.primaryClip = ClipData.newPlainText("text", text ?: "")
}