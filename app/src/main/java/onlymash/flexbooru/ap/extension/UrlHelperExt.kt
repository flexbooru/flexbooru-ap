package onlymash.flexbooru.ap.extension

import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.Search

fun Search.getPostsUrl(page: Int): HttpUrl {
    val builder = HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/view_posts/$page")
        .addQueryParameter("lang", "en")
        .addQueryParameter("type", "json")
        .addQueryParameter("posts_per_page", limit.toString())
        .addQueryParameter("order_by", "date")
        .addQueryParameter("ldate", "0")
    if (query.isNotEmpty()) {
        builder.addQueryParameter("search_tag", query)
    }
    return builder.build()
}

fun getPostDetailUrl(scheme: String, host: String, postId: Int): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/view_post/$postId")
        .addQueryParameter("lang", "en")
        .addQueryParameter("type", "json")
        .build()
}