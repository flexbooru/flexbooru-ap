package onlymash.flexbooru.ap.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import onlymash.flexbooru.ap.data.model.TagsFull

class MyConverters {

    @TypeConverter
    fun fromIntListToJson(list: List<Int>): String = Json.encodeToString(list)

    @TypeConverter
    fun fromJsonToIntList(json: String): List<Int> = Json.decodeFromString(json)

    @TypeConverter
    fun fromStringListToJson(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun fromJsonToStringList(json: String): List<String> = Json.decodeFromString(json)

    @TypeConverter
    fun fromTagsFullListToJson(tagsFull: List<TagsFull>): String = Json.encodeToString(tagsFull)

    @TypeConverter
    fun fromJsonToTagsFullList(json: String): List<TagsFull> = Json.decodeFromString(json)
}