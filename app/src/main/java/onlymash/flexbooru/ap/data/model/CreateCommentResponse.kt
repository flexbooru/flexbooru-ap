package onlymash.flexbooru.ap.data.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class CreateCommentResponse(

    @SerialName("success")
    val success: Boolean,

    @SerialName("html")
    val html: String? = null
)