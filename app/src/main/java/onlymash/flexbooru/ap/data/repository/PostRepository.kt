package onlymash.flexbooru.ap.data.repository

import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.model.Post

interface PostRepository {
    fun getPosts(search: Search): Listing<Post>
}