package onlymash.flexbooru.ap.data.api

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.ap.common.USER_AGENT_KEY
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.PostResponse
import onlymash.flexbooru.ap.extension.getUserAgent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface Api {

    companion object {
        operator fun invoke(): Api {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("Api", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
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
                    .addInterceptor(logger)
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
    fun getPosts(@Url url: HttpUrl): Call<PostResponse>

    @GET
    fun getDetail(@Url url: HttpUrl): Call<Detail>
}