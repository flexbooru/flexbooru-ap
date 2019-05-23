package onlymash.flexbooru.ap.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import onlymash.flexbooru.ap.data.model.TagsFull

class MyConverters {

    @TypeConverter
    fun fromIntListToJson(list: List<Int>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromJsonToIntList(json: String): List<Int> =
        Gson().fromJson<List<Int>>(json, object : TypeToken<List<Int>>(){}.type)

    @TypeConverter
    fun fromStringListToJson(list: List<String>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromJsonToStringList(json: String): List<String> =
        Gson().fromJson<List<String>>(json, object : TypeToken<List<String>>(){}.type)

    @TypeConverter
    fun fromTagsFullListToJson(tagsFull: List<TagsFull>): String =
        Gson().toJson(tagsFull)

    @TypeConverter
    fun fromJsonToTagsFullList(json: String): List<TagsFull> =
        Gson().fromJson<List<TagsFull>>(json, object : TypeToken<List<TagsFull>>(){}.type)
}