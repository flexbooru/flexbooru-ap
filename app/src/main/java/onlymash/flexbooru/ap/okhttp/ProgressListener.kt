package onlymash.flexbooru.ap.okhttp

interface ProgressListener {
    fun onUpdate(
        bytesRead: Long,
        contentLength: Long,
        done: Boolean
    )
}