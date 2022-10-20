package onlymash.flexbooru.ap.okhttp

import okhttp3.Interceptor
import okhttp3.Response

import java.io.IOException

class ProgressInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()
        val listener = LISTENERS_MAP[url] ?: return response
        return response.newBuilder()
            .body(ProgressResponseBody(response.body, listener))
            .build()
    }

    companion object {

        private val LISTENERS_MAP: MutableMap<String, ProgressListener> = hashMapOf()

        //注册下载监听
        fun addListener(url: String, listener: ProgressListener) {
            LISTENERS_MAP[url] = listener
        }

        //取消注册下载监听
        fun removeListener(url: String) {
            LISTENERS_MAP.remove(url)
        }

        fun bindUrlWithInterval(url: String, interval: Long, callback: (Int) -> Unit) {
            var startTime = 0L
            var elapsedTime = interval
            addListener(url, object : ProgressListener {
                override fun onUpdate(bytesRead: Long, contentLength: Long, done: Boolean) {
                    val progress = 100 * bytesRead / contentLength
                    if (elapsedTime >= interval) {
                        callback.invoke(progress.toInt())
                        startTime = System.currentTimeMillis()
                        elapsedTime = 0L
                    } else {
                        elapsedTime = System.currentTimeMillis() - startTime
                    }
                }
            })
        }
    }
}