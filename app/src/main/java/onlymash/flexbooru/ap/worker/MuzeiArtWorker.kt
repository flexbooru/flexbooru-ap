package onlymash.flexbooru.ap.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.db.dao.PostDao
import org.kodein.di.erased.instance

class MuzeiArtWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {

        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance(App.app)
            workManager.enqueue(OneTimeWorkRequestBuilder<MuzeiArtWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build())
        }
    }

    override suspend fun doWork(): Result {
        val postDao: PostDao by App.app.instance()
        val query = Settings.muzeiQuery
        val scheme = Settings.scheme
        val host = Settings.hostname
        val posts = postDao.getPostsLimit(query, Settings.muzeiLimit)
        val attributionString = applicationContext.getString(R.string.muzei_attribution)
        val providerClient = ProviderContract.getProviderClient(applicationContext, applicationContext.packageName + ".muzei")
        providerClient.setArtwork(posts.map {  post ->
            Artwork().apply {
                token = post.id.toString()
                title = applicationContext.getString(R.string.placeholder_post_id, post.id)
                byline = query
                attribution = attributionString
                webUri = "$scheme://$host/pictures/view_post/${post.id}?lang=en".toUri()
                persistentUri = "$scheme://$host".toUri()
            }
        })
        return Result.success()
    }
}