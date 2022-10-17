package onlymash.flexbooru.ap.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import onlymash.flexbooru.ap.R
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.common.HOST
import onlymash.flexbooru.ap.common.SCHEME_HTTPS
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.db.dao.PostDao
import org.kodein.di.instance

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
        val postDao by App.app.instance<PostDao>()
        val query = Settings.muzeiQuery
        val posts = postDao.getPostsLimit(query, Settings.muzeiLimit)
        val attributionString = applicationContext.getString(R.string.muzei_attribution)
        val providerClient = ProviderContract.getProviderClient(applicationContext, applicationContext.packageName + ".muzei")
        providerClient.setArtwork(posts.map {  post ->
            Artwork(
                token = post.id.toString(),
                title = applicationContext.getString(R.string.placeholder_post_id, post.id),
                byline = query,
                attribution = attributionString,
                webUri = "$SCHEME_HTTPS://$HOST/pictures/view_post/${post.id}?lang=en".toUri(),
                persistentUri = "$SCHEME_HTTPS://$HOST".toUri(),
            )
        })
        return Result.success()
    }
}