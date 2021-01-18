package com.cyberlogitec.freight9.lib.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ArrayListIntervalDataTypeConverter {
    private val gson = Gson()
    private val type: Type = object : TypeToken<ArrayList<MarketIndexList.Item?>?>() {}.type

    @TypeConverter
    fun stringToArrayListData(json: String?): ArrayList<MarketIndexList.Item?>? {
        return gson.fromJson<ArrayList<MarketIndexList.Item?>>(json, type)
    }

    @TypeConverter
    fun arrayListToString(nestedData: ArrayList<MarketIndexList.Item?>?): String? {
        return gson.toJson(nestedData, type)
    }
}