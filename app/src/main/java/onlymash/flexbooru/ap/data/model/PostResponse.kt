package onlymash.flexbooru.ap.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    @SerialName("max_pages")
    val maxPages: Int,
    @SerialName("page_number")
    val pageNumber: Int,
    @SerialName("posts")
    val posts: List<Post>,
    @SerialName("posts_count")
    val postsCount: Int,
    @SerialName("posts_per_page")
    val postsPerPage: Int,
    @SerialName("response_posts_count")
    val responsePostsCount: Int
)