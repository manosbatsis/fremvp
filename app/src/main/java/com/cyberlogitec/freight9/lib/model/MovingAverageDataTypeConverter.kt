package com.cyberlogitec.freight9.lib.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MovingAverageDataTypeConverter {
    private val gson = Gson()
    private val type: Type = object : TypeToken<ArrayList<MovingAverage?>?>() {}.type

    @TypeConverter
    fun stringToMovingAverageData(json: String?): ArrayList<MovingAverage?>? {
        return gson.fromJson<ArrayList<MovingAverage?>>(json, type)
    }

    @TypeConverter
    fun movingAverageDataToString(nestedData: ArrayList<MovingAverage?>?): String? {
        return gson.toJson(nestedData, type)
    }
}