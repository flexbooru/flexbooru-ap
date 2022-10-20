package onlymash.flexbooru.ap.data.repository.post

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.model.Post

class PostRepositoryImpl(
    private val db: MyDatabase,
    private val api: Api
) : PostRepository {

    override fun getPosts(search: Search): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = search.limit,
                initialLoadSize = search.limit,
                enablePlaceholders = true
            ),
            remoteMediator = PostRemoteMediator(api, search, db),
            initialKey = 0
        ) {
            db.postDao().getPosts(search.query)
        }.flow
    }
}