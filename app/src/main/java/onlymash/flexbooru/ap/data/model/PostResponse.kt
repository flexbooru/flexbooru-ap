package onlymash.flexbooru.ap.data.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("max_pages")
    val maxPages: Int,
    @SerializedName("page_number")
    val pageNumber: Int,
    @SerializedName("posts")
    val posts: List<Post>,
    @SerializedName("posts_count")
    val postsCount: Int,
    @SerializedName("posts_per_page")
    val postsPerPage: Int,
    @SerializedName("response_posts_count")
    val responsePostsCount: Int
)