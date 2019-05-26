package onlymash.flexbooru.ap.extension

import java.io.*
import java.nio.charset.Charset

private const val EOF = -1
private const val DEFAULT_BUFFER_SIZE = 1024 * 8

private val UTF_8 = Charset.forName("UTF-8")

fun Closeable.safeCloseQuietly() {
    try {
        close()
    } catch (_: IOException) {
        // Ignore
    }

}

@Throws(IOException::class)
fun InputStream.copyToOS(os: OutputStream?): Long {
    if (os == null) return 0
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var count: Long = 0
    var n: Int = read(buffer)
    while (n != EOF) {
        os.write(buffer, 0, n)
        count += n.toLong()
        n = read(buffer)
    }
    return count
}

@Throws(IOException::class)
fun InputStream.toString(charsetName: String?): String? {
    if (charsetName == null) return null
    val os = ByteArrayOutputStream()
    copyToOS(os)
    return os.toString(charsetName)
}

@Throws(IOException::class)
fun InputStream.toByteArray(): ByteArray? {
    val os = ByteArrayOutputStream()
    copyToOS(os)
    return os.toByteArray()
}