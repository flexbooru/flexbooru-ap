package onlymash.flexbooru.ap.data.db.dao

import androidx.room.*
import onlymash.flexbooru.ap.data.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Query("SELECT * FROM `users` ORDER BY `uid` ASC")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM `users` WHERE `uid` = :uid")
    fun getUserByUid(uid: Long): User?

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM `users` WHERE `uid` = :uid")
    fun deleteByUid(uid: Long)
}