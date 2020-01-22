package onlymash.flexbooru.ap.data.repository.comment

import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.model.CommentAll

interface CommentAllRepository {

    fun getComments(
        scope: CoroutineScope,
        token: String
    ): Listing<CommentAll>
}