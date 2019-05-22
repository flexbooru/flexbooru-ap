package onlymash.flexbooru.ap.data.repository.detail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getPostDetailUrl
import retrofit2.HttpException
import kotlin.Exception

class DetailRepositoryImpl(private val api: Api) : DetailRepository {

    override suspend fun getDetail(scheme: String, host: String, postId: Int): NetResult<Detail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDetail(getPostDetailUrl(scheme, host, postId)).execute()
                if (response.isSuccessful) {
                    val detail = response.body()
                    if (detail == null) {
                        NetResult.Error("Empty")
                    } else {
                        NetResult.Success(detail)
                    }
                } else {
                    NetResult.Error("code: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is HttpException) {
                    NetResult.Error("code: ${e.code()}")
                } else {
                    NetResult.Error(e.message.toString())
                }
            }
        }
    }
}