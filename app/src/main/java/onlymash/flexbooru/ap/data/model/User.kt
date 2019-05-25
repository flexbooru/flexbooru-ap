package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "users",
    indices = [(Index(value = ["user_id"], unique = true))]
)
data class User(

    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int,

    @ColumnInfo(name = "username")
    @SerializedName("username")
    val username: String,

    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @ColumnInfo(name = "token")
    @SerializedName("token")
    val token: String,

    @ColumnInfo(name = "jvwall_block_erotic")
    @SerializedName("jvwall_block_erotic")
    val jvwallBlockErotic: Boolean,

    @ColumnInfo(name = "success")
    @SerializedName("success")
    val success: Boolean
)