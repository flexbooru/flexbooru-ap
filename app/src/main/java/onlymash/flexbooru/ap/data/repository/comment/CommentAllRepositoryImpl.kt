package onlymash.flexbooru.ap.data.repository.comment

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll
import java.util.concurrent.Executor

class CommentAllRepositoryImpl(
    private val api: Api,
    private val networkExecutor: Executor
) : CommentAllRepository {

    @MainThread
    override fun getComments(
        scope: CoroutineScope,
        scheme: String,
        host: String,
        token: String
    ): Listing<CommentAll> {
        val sourceFactory = CommentAllDataSourceFactory(
            scope = scope,
            api = api,
            scheme = scheme,
            host = host,
            token = token
        )
        val livePagedList = sourceFactory.toLiveData(
            config = Config(
                pageSize = 30,
                enablePlaceholders = true
            ),
            fetchExecutor = networkExecutor)
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