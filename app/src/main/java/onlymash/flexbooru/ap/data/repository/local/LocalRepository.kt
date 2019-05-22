package onlymash.flexbooru.ap.data.repository.local

import onlymash.flexbooru.ap.data.model.Post

interface LocalRepository {
    suspend fun getLocalPosts(query: String): List<Post>
}