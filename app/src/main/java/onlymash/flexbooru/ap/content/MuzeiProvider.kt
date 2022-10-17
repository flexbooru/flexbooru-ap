package onlymash.flexbooru.ap.content

import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import onlymash.flexbooru.ap.common.App
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.dao.DetailDao
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.extension.getPostDetailUrl
import onlymash.flexbooru.ap.extension.toEncodedUrl
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.worker.MuzeiArtWorker
import org.kodein.di.instance
import java.io.FileInputStream
import java.io.InputStream

class MuzeiProvider : MuzeiArtProvider() {

    override fun onLoadRequested(initial: Boolean) {
        MuzeiArtWorker.enqueueLoad()
    }

    override fun openFile(artwork: Artwork): InputStream {
        val app = context?.applicationContext ?: return super.openFile(artwork)
        if (app is App) {
            val api by app.instance<Api>()
            val detailDao by app.instance<DetailDao>()
            val postId = artwork.token?.toInt() ?: -1
            if (postId < 0) {
                return super.openFile(artwork)
            }
            var detail: Detail? = detailDao.getDetailById(postId)
            if (detail == null) {
                try {
                    val response = api.getDetailNoSuspend(
                        url = getPostDetailUrl(
                            postId = postId,
                            token = Settings.userToken
                        )
                    ).execute()
                    if (response.isSuccessful) {
                        detail = response.body()?.also {
                            detailDao.insert(it)
                        }
                    }
                } catch (_: Exception) {}
            }
            val url = detail?.fileUrl?.toEncodedUrl() ?: return super.openFile(artwork)
            val file = try {
                GlideApp.with(app)
                    .downloadOnly()
                    .load(url.toUri())
                    .submit()
                    .get()
            } catch (_: Exception) {
                null
            }
            return if (file != null && file.exists())
                FileInputStream(file)
            else
                super.openFile(artwork)
        } else {
            return super.openFile(artwork)
        }
    }
}
