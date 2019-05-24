package onlymash.flexbooru.ap.receiver

import android.content.*
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.getMimeType

class DownloadNotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val uri = intent.data
            if (uri == null || uri.scheme != ContentResolver.SCHEME_CONTENT) return
            val newIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                setDataAndType(uri, uri.toString().getMimeType())
            }
            try {
                context.startActivity(
                    Intent.createChooser(
                        newIntent,
                        context.getString(R.string.share_via)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) {}
        }
    }
}