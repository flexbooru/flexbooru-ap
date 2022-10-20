package onlymash.flexbooru.ap.data.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.model.Post

interface PostRepository {
    fun getPosts(search: Search): Flow<PagingData<Post>>
}