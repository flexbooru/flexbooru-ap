package onlymash.flexbooru.ap.data.repository.comment

import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.model.Comment

interface CommentPagingRepository {

    fun getComments(
        scope: CoroutineScope,
        scheme: String,
        host: String,
        postId: Int,
        token: String
    ): Listing<Comment>
}