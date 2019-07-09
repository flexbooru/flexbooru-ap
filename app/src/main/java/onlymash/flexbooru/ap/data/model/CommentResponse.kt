package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName


data class CommentResponse(
    @SerializedName("comments")
    val comments: List<Comment> = emptyList()
)

data class Comment(
    @SerializedName("comment")
    val comment: CommentData,
    @SerializedName("user")
    val user: UserComment
)