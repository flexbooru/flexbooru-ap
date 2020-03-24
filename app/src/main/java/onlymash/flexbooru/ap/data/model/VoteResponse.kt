package onlymash.flexbooru.ap.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class VoteResponse(
    @SerialName("score_n")
    val scoreN: Int
)