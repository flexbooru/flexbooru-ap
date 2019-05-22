package onlymash.flexbooru.ap.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyConverters {

    @TypeConverter
    fun fromIntListToJson(list: List<Int>): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromJsonToIntList(json: String): List<Int> =
        Gson().fromJson<List<Int>>(json, object : TypeToken<List<Int>>(){}.type)
}