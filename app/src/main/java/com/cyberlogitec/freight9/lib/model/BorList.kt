package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BorList(
        @SerializedName("offerNumber")
        var offerNumber: String ? = null,

        @SerializedName("offerChangeSeq")
        var offerChangeSeq: Long ? = 0,

        @SerializedName("marketCode")
        var marketCode: String ? = null,

        @SerializedName("wholeYn")
        var wholeYn: String ? = "",

        @SerializedName("baseYearWeek")
        var baseYearWeek: String ? = null,

        @SerializedName("minYearWeek")
        var minYearWeek: String ? = null,

        @SerializedName("maxYearWeek")
        var maxYearWeek: String ? = null,

        @SerializedName("price")
        var price: Double ? = 0.0,

        @SerializedName("minPrice")
        var minPrice: Double ? = 0.0,

        @SerializedName("maxPrice")
        var maxPrice: Double ? = 0.0,

        @SerializedName("remainderQty")
        var remainderQty: Long ? = 0,

        @SerializedName("containerTypeCode")
        var containerTypeCode: String ? = null,

        @SerializedName("paymentTermCode")
        var paymentTermCode: String ? = null,

        @SerializedName("rdTermCode")
        var rdTermCode: String ? = null,

        @SerializedName("offerTypeCode")
        var offerTypeCode: String ? = null,

        @SerializedName("polCode")
        var locPolCd: String ? = null,

        @SerializedName("podCode")
        var locPodCd: String ? = null,

        @SerializedName("carrierCode")
        var cryrCd: String ? = null,

        @Transient
        var cryrName: String? = "",

        @SerializedName("carrierCount")
        var carrierCount: Int ? = 0,

        @SerializedName("polCount")
        var locPolCnt: Int ? = 0,

        @SerializedName("podCount")
        var locPodCnt: Int ? = 0,

        @SerializedName("polName")
        var locPolNm: String ? = null,

        @SerializedName("podName")
        var locPodNm: String ? = null,

        @SerializedName("carrierCodes")
        var carrierCodes: ArrayList<String>? = null,

        @SerializedName("referenceOfferNumber")
        var referenceOfferNumber: String ? = null,

        @SerializedName("referenceOfferChangeSeq")
        var referenceOfferChangeSeq: Long ? = 0,

        @SerializedName("userId")
        var userId: String ? = null,

        @SerializedName("ownerCompanyCode")
        var ownerCompanyCode: String ? = null,

        @Transient
        var mstrCtrkNr: String ? = null,

        @Transient
        var oferGrpNr: String ? = null

) : Serializable {
    constructor(it: MarketWatchDealBidAsk?, item: MarketWatchDealBidAskItems?) : this(){
            if(it != null) {
                    this.marketCode = it.marketTypeCode
                    this.containerTypeCode = it.containerTypeCode
                    this.paymentTermCode = it.paymentTermCode
                    this.rdTermCode = it.rdtermCode
                    this.offerTypeCode = it.offerTypeCode
            }
            if (item != null) {
                    this.offerNumber = item.tradeOfferNumber
                    this.referenceOfferNumber = item.tradeOfferNumber
                    this.offerChangeSeq = item.tradeOfferChangeSeq.toLong()
                    this.wholeYn = item.allYn
                    this.cryrCd = item.companyCd
                    this.carrierCount = item.carrierCount.toInt()
                    this.locPolCd = item.headPolCd
                    this.locPolNm = item.headPolNm
                    this.locPolCnt = item.polCount
                    this.locPodCd = item.headPodCd
                    this.locPodNm = item.headPodNm
                    this.locPodCnt = item.podCount

            }
    }
}
