package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Entity(
    tableName = "posts",
    indices = [(Index(value = ["id", "query"], unique = true))]
)
@Serializable
data class Post(
    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var uid: Long = 0,

    @ColumnInfo(name = "query")
    @Transient
    var query: String = "",

    @ColumnInfo(name = "index_in_response")
    @Transient
    var indexInResponse: Int = -1,

    @ColumnInfo(name = "id")
    @SerialName("id")
    val id: Int,

    @ColumnInfo(name = "width")
    @SerialName("width")
    val width: Int,

    @ColumnInfo(name = "height")
    @SerialName("height")
    val height: Int,

    @ColumnInfo(name = "small_preview")
    @SerialName("small_preview")
    val smallPreview: String,

    @ColumnInfo(name = "big_preview")
    @SerialName("big_preview")
    val bigPreview: String,

    @ColumnInfo(name = "color")
    @SerialName("color")
    val color: List<Int>,

    @ColumnInfo(name = "download_count")
    @SerialName("download_count")
    val downloadCount: Int,

    @ColumnInfo(name = "erotics")
    @SerialName("erotics")
    val erotics: Int,

    @ColumnInfo(name = "ext")
    @SerialName("ext")
    val ext: String,

    @ColumnInfo(name = "md5")
    @SerialName("md5")
    val md5: String,

    @ColumnInfo(name = "md5_pixels")
    @SerialName("md5_pixels")
    val md5Pixels: String,

    @ColumnInfo(name = "medium_preview")
    @SerialName("medium_preview")
    val mediumPreview: String,

    @ColumnInfo(name = "pubtime")
    @SerialName("pubtime")
    val pubtime: String,

    @ColumnInfo(name = "score")
    @SerialName("score")
    val score: Int,

    @ColumnInfo(name = "score_number")
    @SerialName("score_number")
    val scoreNumber: Int,

    @ColumnInfo(name = "size")
    @SerialName("size")
    val size: Int,

    @ColumnInfo(name = "spoiler")
    @SerialName("spoiler")
    val spoiler: Boolean,

    @ColumnInfo(name = "status")
    @SerialName("status")
    val status: Int
)