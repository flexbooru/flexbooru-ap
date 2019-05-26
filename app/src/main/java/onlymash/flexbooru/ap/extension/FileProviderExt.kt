package onlymash.flexbooru.ap.extension

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

private const val AUTHORITY = "onlymash.flexbooru.ap.fileprovider"

fun Context.getUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(this, AUTHORITY, file)
}