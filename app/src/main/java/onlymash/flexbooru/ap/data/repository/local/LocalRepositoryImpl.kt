package onlymash.flexbooru.ap.data.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.db.dao.PostDao
import onlymash.flexbooru.ap.data.model.Post

class LocalRepositoryImpl(private val dao: PostDao) : LocalRepository {

    override suspend fun getLocalPosts(query: String): List<Post> {
        return withContext(Dispatchers.IO) {
            dao.getPostsList(query)
        }
    }

}