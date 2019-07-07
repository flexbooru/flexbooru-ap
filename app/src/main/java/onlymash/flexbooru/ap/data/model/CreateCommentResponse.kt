package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName


data class CreateCommentResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("html")
    val html: String? = null
)