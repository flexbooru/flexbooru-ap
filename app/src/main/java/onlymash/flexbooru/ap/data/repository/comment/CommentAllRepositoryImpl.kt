package onlymash.flexbooru.ap.data.repository.comment

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll

class CommentAllRepositoryImpl(
    private val api: Api
) : CommentAllRepository {

    override fun getComments(token: String): Flow<PagingData<CommentAll>> {
        return Pager(
            config = PagingConfig(
                pageSize = CommentAllSource.PAGE_SIZE,
                enablePlaceholders = true
            ),
            initialKey = 0
        ) {
            CommentAllSource(api, token)
        }.flow
    }
}