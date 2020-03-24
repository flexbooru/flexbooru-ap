package onlymash.flexbooru.ap.data.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Suggestion(
    @SerialName("tags_list")
    val tagsList: List<Tag> = emptyList()
)

@Serializable
data class Tag(
    @SerialName("c")
    val c: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("t")
    val t: String,
    @SerialName("t2")
    val t2: String?
)