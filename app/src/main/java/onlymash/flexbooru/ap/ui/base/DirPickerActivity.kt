package onlymash.flexbooru.ap.ui.base

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import onlymash.flexbooru.ap.common.Settings

abstract class DirPickerActivity : KodeinActivity() {

    val dirPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
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