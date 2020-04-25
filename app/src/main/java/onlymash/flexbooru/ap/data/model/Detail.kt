package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Entity(
    tableName = "details",
    indices = [(Index(value = ["id"], unique = true))]
)
@Serializable
data class Detail(

    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    @Transient
    var uid: Long = 0,

    @ColumnInfo(name = "id")
    @SerialName("id")
    val id: Int,

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

    @ColumnInfo(name = "favorite_folder")
    @SerialName("favorite_folder")
    val favoriteFolder: String? = null,

    @ColumnInfo(name = "file_url")
    @SerialName("file_url")
    val fileUrl: String,

    @ColumnInfo(name = "height")
    @SerialName("height")
    val height: Int,

    @ColumnInfo(name = "is_favorites")
    @SerialName("is_favorites")
    val isFavorites: Boolean = false,

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
    var scoreNumber: Int,

    @ColumnInfo(name = "size")
    @SerialName("size")
    val size: Int,

    @ColumnInfo(name = "small_preview")
    @SerialName("small_preview")
    val smallPreview: String,

    @ColumnInfo(name = "spoiler")
    @SerialName("spoiler")
    val spoiler: Boolean,

    @ColumnInfo(name = "star_it")
    @SerialName("star_it")
    var starIt: Boolean = false,

    @ColumnInfo(name = "status")
    @SerialName("status")
    val status: Int,

    @ColumnInfo(name = "tags")
    @SerialName("tags")
    val tags: List<String>,

    @ColumnInfo(name = "tags_full")
    @SerialName("tags_full")
    val tagsFull: List<TagsFull>,

    @ColumnInfo(name = "user_avatar")
    @SerialName("user_avatar")
    val userAvatar: String? = null,

    @ColumnInfo(name = "user_id")
    @SerialName("user_id")
    val userId: Int,

    @ColumnInfo(name = "user_name")
    @SerialName("user_name")
    val userName: String,

    @ColumnInfo(name = "width")
    @SerialName("width")
    val width: Int
)

@Serializable
data class TagsFull(

    @SerialName("name")
    val name: String,

    @SerialName("num")
    val num: Int,

    @SerialName("type")
    val type: Int
)