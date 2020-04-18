package onlymash.flexbooru.ap.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class MyMigration(startVersion: Int, endVersion: Int): Migration(startVersion, endVersion) {
    override fun migrate(database: SupportSQLiteDatabase) {
        when {
            startVersion == 1 && endVersion == 2 -> {
                database.execSQL("CREATE TABLE IF NOT EXISTS `tags_filter` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX `index_tags_filter_name` ON `tags_filter` (`name`)")
            }
            startVersion == 2 && endVersion == 3 -> {
                database.execSQL("CREATE TABLE IF NOT EXISTS `tags_blacklist` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX `index_tags_blacklist_name` ON `tags_blacklist` (`name`)")
            }
        }
    }
}