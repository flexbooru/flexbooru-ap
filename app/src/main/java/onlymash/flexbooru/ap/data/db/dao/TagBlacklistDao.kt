package onlymash.flexbooru.ap.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.ap.data.model.TagBlacklist

@Dao
interface TagBlacklistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tag: TagBlacklist): Long

    @Query("SELECT * FROM `tags_blacklist` ORDER BY `name` ASC")
    fun getAll(): List<TagBlacklist>

    @Query("SELECT * FROM `tags_blacklist` ORDER BY `name` ASC")
    fun getAllLiveData(): LiveData<List<TagBlacklist>>

    @Delete
    fun delete(tag: TagBlacklist)

    @Query("DELETE FROM `tags_blacklist` WHERE `uid` = :uid")
    fun deleteByUid(uid: Long)

    @Query("DELETE FROM `tags_blacklist`")
    fun deleteAll()
}