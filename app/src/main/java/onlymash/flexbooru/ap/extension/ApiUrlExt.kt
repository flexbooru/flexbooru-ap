package onlymash.flexbooru.ap.extension

import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.SearchType

fun Search.getPostsUrl(page: Int): HttpUrl {
    val builder = HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/view_posts/$page")
        .addQueryParameter("lang", "en")
        .addQueryParameter("type", "json")
        .addQueryParameter("posts_per_page", limit.toString())
        .addQueryParameter("order_by", order)
        .addQueryParameter("ldate", dateRange.toString())
        .addQueryParameter("aspect", aspect)
        .addQueryParameter("token", token)
    if (deniedTags.isNotEmpty()) {
        builder.addQueryParameter("denied_tags", deniedTags)
    }
    if (color.isNotEmpty()) {
        builder.addQueryParameter("color", color)
    } else if (query.isNotEmpty() && type == SearchType.NORMAL) {
        builder.addQueryParameter("search_tag", query)
    }
    when (type) {
        SearchType.FAVORITE -> builder.addQueryParameter("stars_by", userId.toString())
        SearchType.UPLOADED -> builder.addQueryParameter("user", uploaderId.toString())
        else -> {}
    }
    if (extJpg) {
        builder.addQueryParameter("ext_jpg", "jpg")
    }
    if (extPng) {
        builder.addQueryParameter("ext_png", "png")
    }
    if (extGif) {
        builder.addQueryParameter("ext_gif", "gif")
    }
    return builder.build()
}

fun getPostDetailUrl(
    scheme: String,
    host: String,
    postId: Int,
    token: String): HttpUrl {

    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/view_post/$postId")
        .addQueryParameter("lang", "en")
        .addQueryParameter("type", "json")
        .addQueryParameter("token", token)
        .build()
}

fun getLoginUrl(scheme: String, host: String): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("login/submit")
        .build()
}

fun getVoteUrl(scheme: String, host: String): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/vote")
        .build()
}

fun getSuggestionUrl(scheme: String, host: String): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/autocomplete_tag")
        .build()
}

fun getCommentsUrl(
    scheme: String,
    host: String,
    postId: Int,
    token: String): HttpUrl {

    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("api/v2/posts/$postId/comments")
        .addQueryParameter("lang", "en")
        .addQueryParameter("token", token)
        .build()
}

fun getCreateCommentUrl(
    scheme: String,
    host: String,
    postId: Int): HttpUrl {

    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("pictures/add_comment/$postId")
        .addQueryParameter("lang", "en")
        .build()
}

fun getAllCommentsUrl(
    scheme: String,
    host: String,
    page: Int,
    token: String): HttpUrl {

    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("api/v2/comments")
        .addQueryParameter("page_number", page.toString())
        .addQueryParameter("lang", "en")
        .addQueryParameter("token", token)
        .build()
}

fun getLogoutUrl(scheme: String, host: String, token: String): HttpUrl {
    return HttpUrl.Builder()
        .scheme(scheme)
        .host(host)
        .addPathSegments("login/logout")
        .addQueryParameter("token", token)
        .build()
}