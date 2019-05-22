package onlymash.flexbooru.ap.data.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import onlymash.flexbooru.ap.data.model.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(posts: List<Post>)

    @Query("SELECT * FROM `posts` WHERE `query` = :query ORDER BY `index_in_response` ASC")
    fun getPosts(query: String) : DataSource.Factory<Int, Post>

    @Query("SELECT * FROM `posts` WHERE `query` = :query ORDER BY `index_in_response` ASC")
    fun getPostsList(query: String) : List<Post>

    @Query("DELETE FROM `posts` WHERE `query` = :query")
    fun deletePosts(query: String)

    @Query("SELECT MAX(`index_in_response`) + 1 FROM `posts` WHERE `query` = :query")
    fun getNextIndex(query: String): Int

    @Query("SELECT * FROM `posts` WHERE `query` = :query ORDER BY `uid` ASC")
    fun getPostsLiveData(query: String) : LiveData<List<Post>>
}