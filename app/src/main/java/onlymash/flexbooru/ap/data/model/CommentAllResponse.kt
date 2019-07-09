package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName


data class CommentAllResponse(
    @SerializedName("comments")
    val comments: List<CommentAll>,
    @SerializedName("page_number")
    val pageNumber: Int,
    @SerializedName("posts_per_page")
    val postsPerPage: Int,
    @SerializedName("response_posts_count")
    val responsePostsCount: Int
)

data class CommentAll(
    @SerializedName("comment")
    val comment: CommentData,
    @SerializedName("post")
    val post: Post,
    @SerializedName("user")
    val user: UserComment
)