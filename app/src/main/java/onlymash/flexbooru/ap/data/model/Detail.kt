package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "details",
    indices = [(Index(value = ["id"], unique = true))]
)
data class Detail(

    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0,

    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int,

    @ColumnInfo(name = "big_preview")
    @SerializedName("big_preview")
    val bigPreview: String,

    @ColumnInfo(name = "color")
    @SerializedName("color")
    val color: List<Int>,

    @ColumnInfo(name = "download_count")
    @SerializedName("download_count")
    val downloadCount: Int,

    @ColumnInfo(name = "erotics")
    @SerializedName("erotics")
    val erotics: Int,

    @ColumnInfo(name = "ext")
    @SerializedName("ext")
    val ext: String,

    @ColumnInfo(name = "favorite_folder")
    @SerializedName("favorite_folder")
    val favoriteFolder: String?,

    @ColumnInfo(name = "file_url")
    @SerializedName("file_url")
    val fileUrl: String,

    @ColumnInfo(name = "height")
    @SerializedName("height")
    val height: Int,

    @ColumnInfo(name = "is_favorites")
    @SerializedName("is_favorites")
    val isFavorites: Boolean,

    @ColumnInfo(name = "md5")
    @SerializedName("md5")
    val md5: String,

    @ColumnInfo(name = "md5_pixels")
    @SerializedName("md5_pixels")
    val md5Pixels: String,

    @ColumnInfo(name = "medium_preview")
    @SerializedName("medium_preview")
    val mediumPreview: String,

    @ColumnInfo(name = "pubtime")
    @SerializedName("pubtime")
    val pubtime: String,

    @ColumnInfo(name = "score")
    @SerializedName("score")
    val score: Int,

    @ColumnInfo(name = "score_number")
    @SerializedName("score_number")
    var scoreNumber: Int,

    @ColumnInfo(name = "size")
    @SerializedName("size")
    val size: Int,

    @ColumnInfo(name = "small_preview")
    @SerializedName("small_preview")
    val smallPreview: String,

    @ColumnInfo(name = "spoiler")
    @SerializedName("spoiler")
    val spoiler: Boolean,

    @ColumnInfo(name = "star_it")
    @SerializedName("star_it")
    var starIt: Boolean,

    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: Int,

    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    val tags: List<String>,

    @ColumnInfo(name = "tags_full")
    @SerializedName("tags_full")
    val tagsFull: List<TagsFull>,

    @ColumnInfo(name = "user_avatar")
    @SerializedName("user_avatar")
    val userAvatar: String?,

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int,

    @ColumnInfo(name = "user_name")
    @SerializedName("user_name")
    val userName: String,

    @ColumnInfo(name = "width")
    @SerializedName("width")
    val width: Int
)

data class TagsFull(

    @SerializedName("name")
    val name: String,

    @SerializedName("num")
    val num: Int,

    @SerializedName("type")
    val type: Int
)