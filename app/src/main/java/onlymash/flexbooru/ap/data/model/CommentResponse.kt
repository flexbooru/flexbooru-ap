package onlymash.flexbooru.ap.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    @SerialName("comments")
    val comments: List<Comment> = emptyList()
)

@Serializable
data class Comment(
    @SerialName("comment")
    val comment: CommentData,
    @SerialName("user")
    val user: UserComment
)