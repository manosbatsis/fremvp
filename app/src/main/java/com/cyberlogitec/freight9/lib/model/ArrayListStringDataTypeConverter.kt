package com.cyberlogitec.freight9.lib.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ArrayListStringDataTypeConverter {
    private val gson = Gson()
    private val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type

    @TypeConverter
    fun stringToArrayListData(json: String?): ArrayList<String?>? {
        return gson.fromJson<ArrayList<String?>>(json, type)
    }

    @TypeConverter
    fun arrayListToString(nestedData: ArrayList<String?>?): String? {
        return gson.toJson(nestedData, type)
    }
}