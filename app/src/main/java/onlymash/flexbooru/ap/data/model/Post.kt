package onlymash.flexbooru.ap.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "posts",
    indices = [(Index(value = ["id", "query"], unique = true))]
)
data class Post(
    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0,

    @ColumnInfo(name = "query")
    var query: String = "",

    @ColumnInfo(name = "index_in_response")
    var indexInResponse: Int = -1,

    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int,

    @ColumnInfo(name = "width")
    @SerializedName("width")
    val width: Int,

    @ColumnInfo(name = "height")
    @SerializedName("height")
    val height: Int,

    @ColumnInfo(name = "small_preview")
    @SerializedName("small_preview")
    val smallPreview: String,

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
    val scoreNumber: Int,

    @ColumnInfo(name = "size")
    @SerializedName("size")
    val size: Int,

    @ColumnInfo(name = "spoiler")
    @SerializedName("spoiler")
    val spoiler: Boolean,

    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: Int
)