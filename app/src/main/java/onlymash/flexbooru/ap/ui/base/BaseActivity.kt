package onlymash.flexbooru.ap.ui.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import onlymash.flexbooru.ap.common.REQUEST_CODE_OPEN_DIRECTORY
import onlymash.flexbooru.ap.common.Settings

abstract class BaseActivity : KodeinActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )
            Settings.pathString = Uri.decode(uri.toString())
            Settings.pathTreeId = DocumentsContract.getTreeDocumentId(uri)
            Settings.pathAuthority = uri.authority
        }
    }
}