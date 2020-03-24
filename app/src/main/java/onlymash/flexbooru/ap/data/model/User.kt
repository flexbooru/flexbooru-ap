package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
    tableName = "users",
    indices = [(Index(value = ["user_id"], unique = true))]
)
@Serializable
data class User(

    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var uid: Long = 0L,

    @ColumnInfo(name = "user_id")
    @SerialName("user_id")
    val userId: Int,

    @ColumnInfo(name = "username")
    @SerialName("username")
    val username: String,

    @ColumnInfo(name = "avatar_url")
    @SerialName("avatar_url")
    val avatarUrl: String?,

    @ColumnInfo(name = "token")
    @SerialName("token")
    val token: String,

    @ColumnInfo(name = "jvwall_block_erotic")
    @SerialName("jvwall_block_erotic")
    val jvwallBlockErotic: Boolean,

    @ColumnInfo(name = "success")
    @SerialName("success")
    val success: Boolean
)