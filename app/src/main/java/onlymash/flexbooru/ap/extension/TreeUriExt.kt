package onlymash.flexbooru.ap.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import okio.IOException
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import java.util.*

fun Activity.getSaveUri(fileName: String): Uri? = getFileUri("save", fileName)

fun Activity.getDownloadUri(fileName: String): Uri? = getFileUri("download", fileName)

fun ContentResolver.getFileUriByDocId(docId: String): Uri? {
    val treeUri = getTreeUri() ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
}

fun ContentResolver.getTreeUri(): Uri? {
    val permissions = persistedUriPermissions
    val index = permissions.indexOfFirst { permission ->
        permission.isReadPermission && permission.isWritePermission
    }
    if (index < 0) {
        return null
    }
    return permissions[index].uri
}

private fun Activity.getFileUri(dirName: String, fileName: String): Uri? {
    val treeUri = contentResolver.getTreeUri()
    if (treeUri == null) {
        openDocumentTree()
        return null
    }
    val treeDir = DocumentFile.fromTreeUri(this, treeUri)
    if (treeDir == null || !treeDir.exists() || treeDir.isFile ||
        !treeDir.canRead() || !treeDir.canWrite()) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        openDocumentTree()
        return null
    }
    val treeId = DocumentsContract.getTreeDocumentId(treeUri)
    val dirId = getDocumentFileId(treeId, dirName)
    val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirId) ?: return null
    val dir = DocumentFile.fromSingleUri(this, dirUri)
    var tmpUri: Uri? = null
    try {
        if (dir == null || !dir.exists()) {
            tmpUri = treeDir.createDirectory(dirName)?.uri ?: return null
        } else if (dir.isFile) {
            dir.delete()
            tmpUri = treeDir.createDirectory(dirName)?.uri ?: return null
        }
    } catch (_: Exception) {
        return null
    }
    if (tmpUri != null && tmpUri != dirUri) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        openDocumentTree()
        return null
    }
    val fileId= getDocumentFileId(dirId, fileName)
    var fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileId)
    var file = DocumentFile.fromSingleUri(this, fileUri)
    try {
        if (file == null || !file.exists()) {
            fileUri = DocumentsContract.createDocument(
                contentResolver,
                dirUri,
                fileName.getMimeType(),
                fileName
            ) ?: return null
        } else if (file.isDirectory) {
            file.delete()
            fileUri = DocumentsContract.createDocument(
                contentResolver,
                dirUri,
                fileName.getMimeType(),
                fileName
            ) ?: return null
        }
    } catch (_: Exception) {
        return null
    }
    file = DocumentFile.fromSingleUri(this, fileUri)
    if (file == null || !file.exists() || !file.canWrite()) {
        return null
    }
    return fileUri
}

fun Activity.openDocumentTree() {
    try {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            },
            REQUEST_CODE_OPEN_DIRECTORY)
    } catch (_: ActivityNotFoundException) {}
}

private fun getDocumentFileId(prentId: String, fileName: String): String {
    return if (prentId.endsWith(":")) {
        prentId + fileName
    } else {
        "$prentId/$fileName"
    }
}

private fun closeQuietly(closeable: AutoCloseable?) {
    closeable ?: return
    try {
        closeable.close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (_: Exception) { }
}

fun String.getMimeType(): String {
    var extension = this.fileExt()
    // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
    extension = extension.toLowerCase(Locale.getDefault())
    if (extension == "3ga") {
        return "audio/3gpp"
    } else if (extension == "js") {
        // Missing from the map :(.
        return "text/javascript"
    }
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
}

fun String.fileExt(): String {
    val start = lastIndexOf('.') + 1
    val end = indexOfFirst { it == '?' }
    return if (end > start) {
        substring(start, end)
    } else {
        substring(start)
    }
}

fun String.fileName(): String =
    substring(lastIndexOf('/') + 1)
        .decode()
        .replace("?_", "_")
        .replace("?", "_")
        .replace("!", "")
        .replace(":", "_")
        .replace("\"","_")

fun String.decode(): String = Uri.decode(this)