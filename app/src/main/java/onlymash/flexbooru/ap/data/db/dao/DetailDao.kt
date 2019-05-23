package onlymash.flexbooru.ap.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import onlymash.flexbooru.ap.data.model.Detail

@Dao
interface DetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(details: List<Detail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(detail: Detail)

    @Query("SELECT * FROM `details` ORDER BY `uid` ASC")
    fun getAllDetails() : List<Detail>

    @Query("SELECT * FROM `details` ORDER BY `uid` ASC")
    fun getAllDetailsLivaData() : LiveData<List<Detail>>

    @Query("SELECT * FROM `details` WHERE `id` = :id")
    fun getDetailById(id: Int) : Detail

    @Query("DELETE FROM `details` WHERE `id` = :id")
    fun deleteDetailById(id: Int)

    @Delete
    fun delete(detail: Detail)

    @Query("DELETE FROM `details`")
    fun deleteAll()
}