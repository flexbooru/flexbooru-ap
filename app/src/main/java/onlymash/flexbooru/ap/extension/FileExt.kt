package onlymash.flexbooru.ap.extension

import java.io.*


fun Closeable.safeCloseQuietly() {
    try {
        close()
    } catch (_: IOException) {
        // Ignore
    }

}

fun InputStream.copyTo(out: OutputStream?): Long {
    if (out == null) return 0
    return copyTo(out, DEFAULT_BUFFER_SIZE)
}