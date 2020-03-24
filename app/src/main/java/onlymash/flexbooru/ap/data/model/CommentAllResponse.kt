package onlymash.flexbooru.ap.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CommentAllResponse(
    @SerialName("comments")
    val comments: List<CommentAll> = emptyList(),
    @SerialName("page_number")
    val pageNumber: Int,
    @SerialName("posts_per_page")
    val postsPerPage: Int,
    @SerialName("response_posts_count")
    val responsePostsCount: Int
)

@Serializable
data class CommentAll(
    @SerialName("comment")
    val comment: CommentData,
    @SerialName("post")
    val post: Post,
    @SerialName("user")
    val user: UserComment
)