package onlymash.flexbooru.ap.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseList
import kotlinx.serialization.stringify
import onlymash.flexbooru.ap.data.model.TagsFull

class MyConverters {

    @TypeConverter
    fun fromIntListToJson(list: List<Int>): String = Json.stringify(list)

    @TypeConverter
    fun fromJsonToIntList(json: String): List<Int> = Json.parseList(json)

    @TypeConverter
    fun fromStringListToJson(list: List<String>): String = Json.stringify(list)

    @TypeConverter
    fun fromJsonToStringList(json: String): List<String> = Json.parseList(json)

    @TypeConverter
    fun fromTagsFullListToJson(tagsFull: List<TagsFull>): String = Json.stringify(tagsFull)

    @TypeConverter
    fun fromJsonToTagsFullList(json: String): List<TagsFull> = Json.parseList(json)
}