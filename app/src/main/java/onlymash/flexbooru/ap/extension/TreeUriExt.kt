package onlymash.flexbooru.ap.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.*
import java.util.*

fun Activity.getSaveUri(fileName: String): Uri? =
    getUri("save", fileName)

fun Activity.getDownloadUri(fileName: String): Uri? =
    getUri("download", fileName)

fun getFileUriByDocId(docId: String): Uri? {
    val treeId = Settings.pathTreeId ?: return null
    val authority = Settings.pathAuthority ?: return null
    val treeUri = DocumentsContract.buildTreeDocumentUri(authority, treeId) ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
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

private fun Activity.getUri(dirName: String, fileName: String): Uri? {
    val basePath = Settings.pathString
    val treeId = Settings.pathTreeId
    val authority = Settings.pathAuthority
    if (basePath == null || !basePath.startsWith(ContentResolver.SCHEME_CONTENT) ||
        treeId.isNullOrEmpty() || authority.isNullOrEmpty()) {
        openDocumentTree()
        return null
    }
    val treeUri = DocumentsContract.buildTreeDocumentUri(authority, treeId)
    val treeDir = DocumentFile.fromTreeUri(this, treeUri)
    if (treeDir == null || !treeDir.canWrite()) {
        Toast.makeText(this, getString(R.string.msg_path_denied), Toast.LENGTH_LONG).show()
        openDocumentTree()
        return null
    }
    var dirDocId = findChildrenDocIdByFilename(treeUri, dirName)
    if (dirDocId == null) {
        treeDir.createFile(DocumentsContract.Document.MIME_TYPE_DIR, dirName)
        dirDocId = findChildrenDocIdByFilename(treeUri, dirName) ?: return null
    }
    val fileDocId = if (dirDocId.endsWith(":")) {
        dirDocId + fileName
    } else {
        "$dirDocId/$fileName"
    }
    val dirDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirDocId)
    var fileDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, fileDocId)
    val fileDoc = DocumentFile.fromSingleUri(this, fileDocUri)
    if (fileDoc == null || !fileDoc.exists()) {
        fileDocUri = DocumentsContract.createDocument(
            contentResolver,
            dirDocUri,
            fileName.getMimeType(),
            fileName
        )
    }
    return fileDocUri
}

private fun closeQuietly(closeable: AutoCloseable?) {
    closeable ?: return
    try {
        closeable.close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (_: Exception) { }
}

private fun Context.findChildrenDocIdByFilename(treeUri: Uri, filename: String): String? {
    var docId: String? = null
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        treeUri,
        DocumentsContract.getTreeDocumentId(treeUri)
    )
    val childrenCursor = contentResolver.query(
        childrenUri,
        arrayOf(
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID
        ),
        null,
        null,
        null
    )
    try {
        childrenCursor?.let {
            while (it.moveToNext()) {
                if ( childrenCursor.getString(0) == filename) {
                    docId = childrenCursor.getString(1)
                    break
                }
            }
        }
    } finally {
        closeQuietly(childrenCursor)
    }
    return docId
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
        .replace("?_", "_")
        .replace("?", "_")
        .replace("!", "")
        .replace(":", "_")
        .replace("\"","_")

fun String.decode(): String = Uri.decode(this)