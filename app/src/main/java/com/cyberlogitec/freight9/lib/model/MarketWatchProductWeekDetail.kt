package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostMarketWatchProductWeekDetailChartListRequest(
        @SerializedName("carrierCodes")
        var companyCodes : ArrayList<String>?,

        @SerializedName("containerTypeCode")
        var containerTypeCode : String?,

        @SerializedName("marketTypeCode")
        var marketTypeCode : String?,

        @SerializedName("paymentTermCode")
        var paymentTermCode : String?,

        @SerializedName("polCode")
        var pol : String = "",

        @SerializedName("podCode")
        var pod : String = "",

        @SerializedName("baseYearWeek")
        var baseYearWeek : String = "",

        @SerializedName("rdTermCode")
        var rDTermCode : String?,

        @SerializedName("interval")
        var interval : String?,

        @SerializedName("offerTypeCode")
        var offerTypeCode : String?
):Serializable

data class MarketWatchProductWeekDetailChartList(

        /*@SerializedName("topicName")
        var topicName: String,*/

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

        @SerializedName("baseYearWeek")
        var baseYearWeek: String,

        @SerializedName("interval")
        var interval: String,

        @SerializedName("Cell")
        var cells: ArrayList<WeekItems>

)
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

                @SerializedName("intervalTimestamp")
                var intervalTimestamp: String,

                @Transient
                var xVal: Float

        )
}

data class MarketWatchDealHistory(

        @SerializedName("companyCodes")
        var companyCodes : ArrayList<String>?,

        @SerializedName("marketTypeCode")
        var marketTypeCode: String,

        @SerializedName("rdTermCode")
        var rdtermCode: String,

        @SerializedName("containerTypeCode")
        var containerTypeCode: String,

        @SerializedName("paymentTermCode")
        var paymentTermCode: String,

        @SerializedName("polCd")
        var pol: String,

        @SerializedName("podCd")
        var pod: String,

        @SerializedName("qtyUnit")
        var qtyUnit: String = "T",

        @SerializedName("baseYearWeek")
        var baseYearWeek: String,

        @SerializedName("Cell")
        var cells: ArrayList<MarketWatchDealHistoryItems>

):Serializable

data class MarketWatchDealHistoryItems(
        var idx: Double,

        var timestamp: String,

        var referenceEventNumber: String,

        var referenceEventChangeNumber: Double,
        var dealQty: Double,
        var dealPrice: Double,
        var dealAmt: Double,
        var priceChange: Double,
        var priceRate: Double

):Serializable

data class MarketWatchDealBidAsk(

        @SerializedName("companyCodes")
        var companyCodes : ArrayList<String>?,

        @SerializedName("marketTypeCode")
        var marketTypeCode: String,

        @SerializedName("offerTypeCode")
        var offerTypeCode: String,

        @SerializedName("rdTermCode")
        var rdtermCode: String,

        @SerializedName("containerTypeCode")
        var containerTypeCode: String,

        @SerializedName("paymentTermCode")
        var paymentTermCode: String,

        @SerializedName("polCd")
        var pol: String,

        @SerializedName("podCd")
        var pod: String,

        @SerializedName("baseYearWeek")
        var baseYearWeek: String,

        @SerializedName("Cell")
        var cells: ArrayList<MarketWatchDealBidAskItems>

):Serializable

data class MarketWatchDealBidAskItems(
        @SerializedName("tradeOfferNumber")
        var tradeOfferNumber: String,
        @SerializedName("tradeOfferChangeSeq")
        var tradeOfferChangeSeq: Double,
        @SerializedName("carrierCode")
        var companyCd: String = "",
        @SerializedName("carrierCount")
        var carrierCount: Double,
        @SerializedName("headPolName")
        var headPolNm: String,
        @SerializedName("headPodName")
        var headPodNm: String,
        @SerializedName("headPolCode")
        var headPolCd: String,
        @SerializedName("headPodCode")
        var headPodCd: String,
        @SerializedName("polCount")
        var polCount: Int,
        @SerializedName("podCount")
        var podCount: Int,
        @SerializedName("avgQty")
        var avgQty: Double,
        @SerializedName("avgPrice")
        var avgPrice: Double,
        @SerializedName("allYn")
        var allYn: String,
        @SerializedName("minYearWeek")
        var minYearWeek: String,
        @SerializedName("maxYearWeek")
        var maxYearWeek: String,
        @SerializedName("minPrice")
        var minPrice: Double,
        @SerializedName("maxPrice")
        var maxPrice: Double


):Serializable





