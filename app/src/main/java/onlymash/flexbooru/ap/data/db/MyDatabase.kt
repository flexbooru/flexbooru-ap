package onlymash.flexbooru.ap.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import onlymash.flexbooru.ap.data.db.dao.*
import onlymash.flexbooru.ap.data.model.*

@Database(
    entities = [
        Post::class,
        Detail::class,
        User::class,
        TagFilter::class,
        TagBlacklist::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(MyConverters::class)
abstract class MyDatabase : RoomDatabase() {

    companion object {
        private var instance: MyDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context): MyDatabase = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }
        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context, MyDatabase::class.java, "database.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addMigrations(
                        MyMigration(1, 2),
                        MyMigration(2, 3)
                    )
                    .setQueryExecutor(Dispatchers.IO.asExecutor())
                    .setTransactionExecutor(Dispatchers.IO.asExecutor())
                    .build()
    }

    abstract fun postDao(): PostDao

    abstract fun detailDao(): DetailDao

    abstract fun userDao(): UserDao

    abstract fun tagFilterDao(): TagFilterDao

    abstract fun tagBlacklistDao(): TagBlacklistDao
}