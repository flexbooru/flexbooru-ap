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
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.receiver.DownloadNotificationClickReceiver
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.extension.*
import onlymash.flexbooru.ap.okhttp.OkHttpDownloader
import onlymash.flexbooru.ap.okhttp.ProgressInterceptor
import onlymash.flexbooru.ap.ui.base.DirPickerActivity
import java.io.IOException
import java.io.InputStream

const val INPUT_DATA_KEY = "input_data"
private const val DOWNLOAD_URL_KEY = "url"
private const val DOWNLOAD_POST_ID_KEY = "post_id"
private const val DOWNLOAD_DOC_ID_KEY = "doc_id"

class DownloadWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    companion object {
        fun download(activity: DirPickerActivity, detail: Detail) {
            val uri = activity.getDownloadUri(detail.fileUrl.fileName()) ?: return
            val docId = DocumentsContract.getDocumentId(uri) ?: return
            val data = workDataOf(
                DOWNLOAD_URL_KEY to detail.fileUrl.toEncodedUrl(),
                DOWNLOAD_DOC_ID_KEY to docId,
                DOWNLOAD_POST_ID_KEY to detail.id
            )
            runWork(data)
        }
        fun runWork(data: Data) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(App.app).enqueue(work)
        }
    }

    override fun doWork(): Result {

        val url = inputData.getString(DOWNLOAD_URL_KEY)
        val postId = inputData.getInt(DOWNLOAD_POST_ID_KEY, -1)
        val docId = inputData.getString(DOWNLOAD_DOC_ID_KEY)

        if (url.isNullOrEmpty() || postId < 0 || docId.isNullOrEmpty()) {
            return Result.failure()
        }

        val desUri = applicationContext.contentResolver.getFileUriByDocId(docId) ?: return Result.failure()

        val channelId = applicationContext.packageName + ".download"

        val title = applicationContext.getString(R.string.placeholder_post_id, postId)

        val notificationManager = getNotificationManager(
            channelId,
            applicationContext.getString(R.string.action_download))

        val downloadingNotificationBuilder = getDownloadingNotificationBuilder(
            title = title,
            url = url,
            channelId = channelId)

        ProgressInterceptor.bindUrlWithInterval(
            url = url,
            interval = 1000L
        ) { progress ->
            downloadingNotificationBuilder.setProgress(100, progress, false)
            notificationManager.notify(postId, downloadingNotificationBuilder.build())
        }

        var `is`: InputStream? = null
        val os = applicationContext.contentResolver.openOutputStream(desUri)
        try {
            `is` = OkHttpDownloader(applicationContext)
                .load(url).body.source().inputStream()
            `is`.copyTo(os)
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
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_complete))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    private fun getDownloadErrorNotificationBuilder(title: String, channelId: String): NotificationCompat.Builder {
        val intent = Intent(applicationContext, DownloadNotificationClickReceiver::class.java)
        intent.putExtra(INPUT_DATA_KEY, inputData.toByteArray())
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                applicationContext,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.msg_download_failed))
            .setOngoing(false)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.stat_sys_download,
                applicationContext.getString(R.string.retry),
                pendingIntent
            )
    }
}