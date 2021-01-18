package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostMarketWatchChartRequest(
        @SerializedName("companyCodes")
        var companyCodes : ArrayList<String>?,

        @SerializedName("containerTypeCode")
        var containerTypeCode : String?,

        @SerializedName("marketTypeCode")
        var marketTypeCode : String?,

        @SerializedName("paymentTermCode")
        var paymentTermCode : String?,

        @SerializedName("polCode")
        var pol : String = "",

        @SerializedName("pol_detail")
        var polDetail : String = "",

        @SerializedName("podCode")
        var pod : String = "",

        @SerializedName("pod_detail")
        var podDetail : String = "",

        @SerializedName("rdTermCode")
        var rDTermCode : String?
):Serializable

data class MarketWatchChartList(

        @SerializedName("topicName")
        var topicName: String,

        @SerializedName("marketTypeCode")
        var marketTypeCode: String,

        @SerializedName("rdTermCode")
        var rdtermCode: String,

        @SerializedName("containerTypeCode")
        var containerTypeCode: String,

        @SerializedName("paymentTermCode")
        var paymentTermCode: String,

        @SerializedName("pol")
        var pol: String,

        @SerializedName("pod")
        var pod: String,

        @SerializedName("qtyUnit")
        var qtyUnit: String = "T",


        @SerializedName("Cell")
        var weekItems: ArrayList<WeekItems>

):Serializable
{
        data class WeekItems(
                @SerializedName("baseYearWeek")
                var baseYearWeek: String,

                @SerializedName("status")
                var status: String,

                @SerializedName("open")
                var open: Double,

                @SerializedName("low")
                var low: Double,

                @SerializedName("high")
                var high: Double,

                @SerializedName("close")
                var close: Double,

                @SerializedName("volume")
                var volume: Double,

                @SerializedName("changeValue")
                var changeValue: Double,

                @SerializedName("changeRate")
                var changeRate: Double,

                @SerializedName("latestEventDate")
                var latestEventTimeStamp: String

        ):Serializable
}






