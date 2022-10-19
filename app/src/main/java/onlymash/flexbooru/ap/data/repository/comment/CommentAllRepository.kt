package onlymash.flexbooru.ap.data.repository.comment

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import onlymash.flexbooru.ap.data.model.CommentAll

interface CommentAllRepository {

    fun getComments(token: String): Flow<PagingData<CommentAll>>
}