package onlymash.flexbooru.ap.data.repository.comment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.NetResult

class CommentRepositoryImpl(private val api: Api) : CommentRepository {

    override suspend fun getComments(url: HttpUrl): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getComments(url)
                if (response.isSuccessful) {
                    val comments = response.body()?.comments ?: emptyList()
                    NetResult.Success(comments)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }

    override suspend fun createComment(
        url: HttpUrl,
        text: String,
        token: String
    ): NetResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createComment(url, text, token)
                val success = response.body()?.success ?: false
                if (success) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}