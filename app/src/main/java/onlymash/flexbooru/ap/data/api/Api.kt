package onlymash.flexbooru.ap.data.api

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.ap.BuildConfig
import onlymash.flexbooru.ap.common.USER_AGENT_KEY
import onlymash.flexbooru.ap.data.model.*
import onlymash.flexbooru.ap.extension.userAgent
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface Api {

    companion object {
        operator fun invoke(): Api {
            val builder = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d("Api", message)
                    }
                })
                logger.level = HttpLoggingInterceptor.Level.BASIC
                builder.addInterceptor(logger)
            }
            val contentType = "application/json".toMediaType()
            val json = Json {
                ignoreUnknownKeys = true
            }
            return Retrofit.Builder()
                .baseUrl("https://fiepi.me")
                .client(builder.build())
                .addConverterFactory(
                    json.asConverterFactory(contentType)
                )
                .build()
                .create(Api::class.java)
        }
    }

    @GET
    suspend fun getPosts(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): PostResponse

    @GET
    suspend fun getDetail(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): Detail

    @GET
    fun getDetailNoSuspend(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): Call<Detail>

    @POST
    @FormUrlEncoded
    suspend fun login(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl,
        @Field("login") username: String,
        @Field("password") password: String,
        @Field("time_zone") timeZone: String
    ): User

    @GET
    suspend fun logout(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): Response<ResponseBody>

    @POST
    @FormUrlEncoded
    suspend fun vote(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl,
        @Field("post") postId: Int,
        @Field("vote") vote: Int = 9, // 9: vote 0: remove vote
        @Field("token") token: String
    ): VoteResponse

    @POST
    @FormUrlEncoded
    suspend fun getSuggestion(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl,
        @Field("tag") tag: String,
        @Field("token") token: String
    ): Suggestion

    @GET
    suspend fun getComments(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): CommentResponse

    @POST
    @FormUrlEncoded
    suspend fun createComment(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl,
        @Field("text") text: String,
        @Field("token") token: String
    ): CreateCommentResponse


    @GET
    suspend fun getAllComments(
        @Header(USER_AGENT_KEY) ua: String = userAgent,
        @Url url: HttpUrl
    ): CommentAllResponse
}