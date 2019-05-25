package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName


data class VoteResponse(
    @SerializedName("score_n")
    val scoreN: Int
)