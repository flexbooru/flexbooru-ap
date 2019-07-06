package onlymash.flexbooru.ap.data.repository.comment

import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.getCommentsUrl
import java.util.concurrent.Executor

class CommentPagingRepositoryImpl(
    private val repo: CommentRepository,
    private val networkExecutor: Executor
) : CommentPagingRepository {

    override fun getComments(
        scope: CoroutineScope,
        scheme: String,
        host: String,
        postId: Int,
        token: String
    ): Listing<Comment> {
        val sourceFactory = CommentDataSourceFactory(
            scope = scope,
            repo = repo,
            url = getCommentsUrl(
                scheme = scheme,
                host = host,
                postId = postId,
                token = token
            )
        )
        val livePagedList = sourceFactory.toLiveData(
            pageSize = 100,
            fetchExecutor = networkExecutor
        )
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