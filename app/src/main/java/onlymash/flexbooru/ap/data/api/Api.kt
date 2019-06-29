package onlymash.flexbooru.ap.data.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import onlymash.flexbooru.ap.common.USER_AGENT_KEY
import onlymash.flexbooru.ap.data.model.*
import onlymash.flexbooru.ap.extension.getUserAgent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface Api {

    companion object {
        operator fun invoke(): Api {
            val interceptor = Interceptor { chain ->
                val builder =  chain.request().newBuilder()
                    .removeHeader(USER_AGENT_KEY)
                    .addHeader(USER_AGENT_KEY, getUserAgent())
                chain.proceed(builder.build())
            }
            val client = OkHttpClient.Builder().apply {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
            }
                .build()
            return Retrofit.Builder()
                .baseUrl("https://fiepi.me")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)
        }
    }

    @GET
    suspend fun getPosts(@Url url: HttpUrl): Response<PostResponse>

    @GET
    suspend fun getDetail(@Url url: HttpUrl): Response<Detail>

    @POST
    @FormUrlEncoded
    suspend fun login(@Url url: HttpUrl,
              @Field("login") username: String,
              @Field("password") password: String,
              @Field("time_zone") timeZone: String): Response<User>

    @POST
    @FormUrlEncoded
    suspend fun vote(@Url url: HttpUrl,
             @Field("post") postId: Int,
             @Field("vote") vote: Int = 9, // 9: vote 0: remove vote
             @Field("token") token: String): Response<VoteResponse>

    @POST
    @FormUrlEncoded
    suspend fun getSuggestion(@Url url: HttpUrl,
                              @Field("tag") tag: String,
                              @Field("token") token: String): Response<Suggestion>
}