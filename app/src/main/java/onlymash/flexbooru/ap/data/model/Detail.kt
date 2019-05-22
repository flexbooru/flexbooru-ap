package onlymash.flexbooru.ap.data.model
import com.google.gson.annotations.SerializedName


data class Detail(
    @SerializedName("big_preview")
    val bigPreview: String,
    @SerializedName("color")
    val color: List<Int>,
    @SerializedName("download_count")
    val downloadCount: Int,
    @SerializedName("erotics")
    val erotics: Int,
    @SerializedName("ext")
    val ext: String,
    @SerializedName("favorite_folder")
    val favoriteFolder: String,
    @SerializedName("file_url")
    val fileUrl: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_favorites")
    val isFavorites: Boolean,
    @SerializedName("md5")
    val md5: String,
    @SerializedName("md5_pixels")
    val md5Pixels: String,
    @SerializedName("medium_preview")
    val mediumPreview: String,
    @SerializedName("pubtime")
    val pubtime: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("score_number")
    val scoreNumber: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("small_preview")
    val smallPreview: String,
    @SerializedName("spoiler")
    val spoiler: Boolean,
    @SerializedName("star_it")
    val starIt: Boolean,
    @SerializedName("status")
    val status: Int,
    @SerializedName("tags")
    val tags: List<String>,
    @SerializedName("tags_full")
    val tagsFull: List<TagsFull>,
    @SerializedName("user_avatar")
    val userAvatar: String,
    @SerializedName("user_favorite_folders")
    val userFavoriteFolders: Any,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
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