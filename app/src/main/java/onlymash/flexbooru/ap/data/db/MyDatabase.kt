package onlymash.flexbooru.ap.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        private val MIGRATION_1_2 by lazy {
            object : Migration(1,2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `tags_filter` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_tags_filter_name` ON `tags_filter` (`name`)")
                }
            }
        }
        private val MIGRATION_2_3 by lazy {
            object : Migration(2,3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `tags_blacklist` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_tags_blacklist_name` ON `tags_blacklist` (`name`)")
                }
            }
        }
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
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()
    }

    abstract fun postDao(): PostDao

    abstract fun detailDao(): DetailDao

    abstract fun userDao(): UserDao

    abstract fun tagFilterDao(): TagFilterDao

    abstract fun tagBlacklistDao(): TagBlacklistDao
}