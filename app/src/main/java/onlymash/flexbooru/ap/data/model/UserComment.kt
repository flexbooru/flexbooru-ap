package onlymash.flexbooru.ap.data.model

import com.google.gson.annotations.SerializedName

data class UserComment(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_avatar")
    val userAvatar: String? = null,
    @SerializedName("user_name")
    val userName: String
)