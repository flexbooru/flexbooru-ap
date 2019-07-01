package onlymash.flexbooru.ap.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.ap.data.model.TagFilter

@Dao
interface TagFilterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tagFilter: TagFilter): Long

    @Query("SELECT * FROM `tags_filter` ORDER BY `name` ASC")
    fun getAll(): List<TagFilter>

    @Query("SELECT * FROM `tags_filter` ORDER BY `name` ASC")
    fun getAllLiveData(): LiveData<List<TagFilter>>

    @Delete
    fun delete(tagFilter: TagFilter)

    @Query("DELETE FROM `tags_filter` WHERE `uid` = :uid")
    fun deleteByUid(uid: Long)

    @Query("DELETE FROM `tags_filter`")
    fun deleteAll()
}