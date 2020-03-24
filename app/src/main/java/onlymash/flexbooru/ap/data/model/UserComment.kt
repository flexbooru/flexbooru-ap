package onlymash.flexbooru.ap.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserComment(
    @SerialName("id")
    val id: Int,
    @SerialName("user_avatar")
    val userAvatar: String? = null,
    @SerialName("user_name")
    val userName: String
)