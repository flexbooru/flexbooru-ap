package onlymash.flexbooru.ap.data.repository.comment

import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.NetResult

interface CommentRepository {

    suspend fun getComments(url: HttpUrl): NetResult<List<Comment>>

    suspend fun createComment(url: HttpUrl, text: String, token: String): NetResult<Boolean>
}