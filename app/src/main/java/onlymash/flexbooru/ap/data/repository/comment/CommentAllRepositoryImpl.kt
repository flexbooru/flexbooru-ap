package onlymash.flexbooru.ap.data.repository.comment

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll

class CommentAllRepositoryImpl(
    private val api: Api
) : CommentAllRepository {

    @MainThread
    override fun getComments(
        scope: CoroutineScope,
        token: String
    ): Listing<CommentAll> {
        val sourceFactory = CommentAllDataSourceFactory(
            scope = scope,
            api = api,
            token = token
        )
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 30,
                enablePlaceholders = true
            ),
            fetchExecutor = Dispatchers.IO.asExecutor())
        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.initialLoad }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState },
            retry = { sourceFactory.sourceLiveData.value?.retryAllFailed() },
            refresh = { sourceFactory.sourceLiveData.value?.invalidate() },
            refreshState = refreshState
        )
    }
}