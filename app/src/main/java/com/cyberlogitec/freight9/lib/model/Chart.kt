package com.cyberlogitec.freight9.lib.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.annotation.Inherited

@Entity(tableName = "market_watch_chart")
data class Chart(
        @PrimaryKey
        @ColumnInfo(name = "storeId")
        @SerializedName("storeId")
        var storeId: String,

        @ColumnInfo(name = "chartType")
        @SerializedName("chartType")
        var chartType: String? = null,

        @ColumnInfo(name = "interVal")
        @SerializedName("interVal")
        var interVal: String? = null,

        @ColumnInfo(name = "intervalList")
        @SerializedName("intervalList")
        var intervalList: ArrayList<String>? = ArrayList(),

        @ColumnInfo(name = "indicator")
        @SerializedName("indicator")
        var indicator: String? = null,

        @ColumnInfo(name = "movingAverage")
        @SerializedName("movingAverage")
        var movingAverage: ArrayList<MovingAverage>? = null


        ):Serializable

data class ChartWrapper(
        var chartSetting: Chart,

        var intervalList: ArrayList<String>
)


