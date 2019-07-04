package onlymash.flexbooru.ap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tags_blacklist",
    indices = [(Index(value = ["name"], unique = true))]
)
data class TagBlacklist(

    @ColumnInfo(name = "uid")
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String
)