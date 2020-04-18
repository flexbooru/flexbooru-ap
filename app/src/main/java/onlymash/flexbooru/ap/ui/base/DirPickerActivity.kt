package onlymash.flexbooru.ap.ui.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import onlymash.flexbooru.ap.common.REQUEST_CODE_OPEN_DIRECTORY
import onlymash.flexbooru.ap.common.Settings

abstract class DirPickerActivity : KodeinActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.apply {
                persistedUriPermissions.forEach { permission ->
                    if (permission.isWritePermission && permission.uri != uri) {
                        releasePersistableUriPermission(permission.uri, flags)
                    }
                }
                takePersistableUriPermission(uri, flags)
            }
            Settings.pathString = Uri.decode(uri.toString())
        }
    }
}