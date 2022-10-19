package onlymash.flexbooru.ap.data.repository.comment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.NetResult
import retrofit2.HttpException

class CommentRepositoryImpl(private val api: Api) : CommentRepository {

    override suspend fun getComments(url: HttpUrl): NetResult<List<Comment>> {
        return withContext(Dispatchers.IO) {
            try {
                val data = api.getComments(url = url)
                NetResult.Success(data.comments)
            } catch (e: Exception) {
                if (e is HttpException) {
                    NetResult.Error("code: ${e.code()}")
                } else {
                    NetResult.Error(e.message.toString())
                }
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
                val response = api.createComment(url = url, text = text, token = token)
                if (response.success) {
                    NetResult.Success(true)
                } else {
                    NetResult.Error("Failed")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}