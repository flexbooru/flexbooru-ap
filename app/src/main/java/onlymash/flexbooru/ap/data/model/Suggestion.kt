package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName

data class Suggestion(
    @SerializedName("tags_list")
    val tagsList: List<Tag> = emptyList()
)

data class Tag(
    @SerializedName("c")
    val c: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("t")
    val t: String,
    @SerializedName("t2")
    val t2: String?
)