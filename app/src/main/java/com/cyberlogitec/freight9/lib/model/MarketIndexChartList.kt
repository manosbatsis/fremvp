package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostMarketIndexChartRequest(
        @SerializedName("idxSubject")
        var idxSubject : String?,

        @SerializedName("idxCategory")
        var idxCategory : String?,

        @SerializedName("idxCd")
        var idxCd : String?,

        @SerializedName("interval")
        var interval : String?
):Serializable

data class MarketIndexChartList(

        @SerializedName("idxSubject")
        var idxSubject : String?,

        @SerializedName("idxCategory")
        var idxCategory : String?,

        @SerializedName("idxCd")
        var idxCd : String?,

        @SerializedName("idxNm")
        var idxNm : String?,

        @SerializedName("interval")
        var interval : String?,


        @SerializedName("Cell")
        var weekItems: ArrayList<Items>

):Serializable
{
        data class Items(
                @SerializedName("intervalTimestamp")
                var intervalStamp: String,

                @SerializedName("xAxis")
                var xAxis: String,

                @SerializedName("value")
                var value: Double,

                @SerializedName("volume")
                var volume: Int,

                @SerializedName("changeValue")
                var changeValue: Double,

                @SerializedName("changeRate")
                var changeRate: Double,

                @Transient
                var xVal: Float

        ):Serializable
}






