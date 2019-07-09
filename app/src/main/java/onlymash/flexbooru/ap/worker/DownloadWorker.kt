package onlymash.flexbooru.ap.worker

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.work.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.receiver.DownloadNotificationClickReceiver
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.okhttp.OkHttpDownloader
import onlymash.flexbooru.ap.okhttp.ProgressInterceptor
import onlymash.flexbooru.ap.okhttp.ProgressListener
import java.io.IOException
import java.io.InputStream

private const val DOWNLOAD_URL_KEY = "url"
private const val DOWNLOAD_POST_ID_KEY = "post_id"
private const val DOWNLOAD_DOC_ID_KEY = "doc_id"

class DownloadWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        fun download(activity: Activity, detail: Detail) {
            val uri = activity.getDownloadUri(detail.fileUrl.fileName()) ?: return
            val docId = DocumentsContract.getDocumentId(uri) ?: return
            val workerManager = WorkManager.getInstance(App.app)
            workerManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                            DOWNLOAD_URL_KEY to detail.fileUrl,
                            DOWNLOAD_DOC_ID_KEY to docId,
                            DOWNLOAD_POST_ID_KEY to detail.id
                        )
                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result {

        val urlString = inputData.getString(DOWNLOAD_URL_KEY)
        val postId = inputData.getInt(DOWNLOAD_POST_ID_KEY, -1)
        val docId = inputData.getString(DOWNLOAD_DOC_ID_KEY)

        if (urlString.isNullOrEmpty() || postId < 0 || docId.isNullOrEmpty()) {
            return Result.failure()
        }

        val url = urlString.toHttpUrlOrNull().toString()

        val desUri = getFileUriByDocId(docId) ?: return Result.failure()

        val channelId = applicationContext.packageName + ".download"

        val title = applicationContext.getString(R.string.placeholder_post_id, postId)

        val notificationManager = getNotificationManager(
            channelId,
            applicationContext.getString(R.string.action_download))

        val downloadingNotificationBuilder = getDownloadingNotificationBuilder(
            title = title,
            url = url,
            channelId = channelId)

        var startTime = 0L
        var elapsedTime = 400L

        ProgressInterceptor.addListener(
            url,
            object : ProgressListener {
                override fun onProgress(progress: Int) {
                    if (elapsedTime >= 400L) {
                        downloadingNotificationBuilder.setProgress(100, progress, false)
                        notificationManager.notify(postId, downloadingNotificationBuilder.build())
                        startTime = System.currentTimeMillis()
                        elapsedTime = 0L
                    } else {
                        elapsedTime = System.currentTimeMillis() - startTime
                    }
                }
            })

        var `is`: InputStream? = null
        val os = applicationContext.contentResolver.openOutputStream(desUri)
        try {
            `is` = OkHttpDownloader(applicationContext)
                .load(url).body?.source()?.inputStream()
            `is`?.copyTo(os)
        } catch (_: IOException) {
            notificationManager.notify(
                postId,
                getDownloadErrorNotificationBuilder(
                    title,
                    channelId
                ).build()
            )
            return Result.failure()
        } finally {
            ProgressInterceptor.removeListener(url)
            `is`?.safeCloseQuietly()
            os?.safeCloseQuietly()
        }
        notificationManager.notify(
            postId,
            getDownloadedNotificationBuilder(
                title = title,
                channelId = channelId,
                desUri = desUri
            ).build()
        )
        return Result.success()
    }

    private fun getNotificationManager(channelId: String, channelName: String): NotificationManager {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return notificationManager
    }

    private fun getDownloadingNotificationBuilder(title: String, url: String, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(title)
            .setContentText(url)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(false)
    }

    private fun getDownloadedNotificationBuilder(title: String, channelId: String, desUri: Uri): NotificationCompat.Builder {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
        intent.data = desUri
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, System.currentTimeMillis().toInt(), intent, 0)
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_complete))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    private fun getDownloadErrorNotificationBuilder(title: String, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_failed))
            .setOngoing(false)
            .setAutoCancel(true)
    }
}