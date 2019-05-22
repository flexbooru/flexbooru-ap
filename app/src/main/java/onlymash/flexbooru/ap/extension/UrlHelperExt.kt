package onlymash.flexbooru.ap.extension

import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.Search

fun Search.getPostsUrl(page: Int): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/view_posts/$page")
        .addQueryParameter("lang", "en")
        .addQueryParameter("type", "json")
        .addQueryParameter("posts_per_page", limit.toString())
        .addQueryParameter("order_by", "date")
        .addQueryParameter("ldate", "0")
        .build()
}