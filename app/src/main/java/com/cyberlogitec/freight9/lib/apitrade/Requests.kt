package com.cyberlogitec.freight9.lib.apitrade

import com.google.gson.annotations.SerializedName

/**
 * Created by Omjoon on 2017. 6. 7..
 */

data class PostImageBody(val file : String?)

data class PostFavoriteTopicBody(@SerializedName("topic_id") val topicId : Long)

/**
 * dataOwnrPtrId = "HLC"
 * mstrCtrkNr = "MST-F9_HLC-201908-01"
 * ipolCd = "JPTYO"
 * ipodCd = "CAVAN"
 */
data class PostServiceRouteRequest(
        @SerializedName("dataOwnrPtrId")
        val dataOwnrPtrId : String?,
        @SerializedName("mstrCtrkNr")
        val mstrCtrkNr : String?,
        @SerializedName("ipolCd")
        val ipolCd : String?,
        @SerializedName("ipodCd")
        val ipodCd : String?
)

data class PostInventoryInfoRequest(
        @SerializedName("dataOwnrPtrId")
        val dataOwnrPtrId : String?,
        @SerializedName("ipolCd")
        val ipolCd : String?,
        @SerializedName("ipodCd")
        val ipodCd : String?
)

data class PostPortRouteRequest(
        @SerializedName("ipolCd")
        val ipolCd : String?,
        @SerializedName("ipodCd")
        val ipodCd : String?
)

//data class PostBofDraftFirstSave(
//        @SerializedName("mstrCtrk")
//        val mstrCtrk : BofContract?,
//
//        @SerializedName("laneDetail")
//        val laneDetail : List<BofLaneDetail>?
//)

data class PostInventoryListRequest(
        val polCode: String?,
        val podCode: String?,
        val firstPaymentRatio: Float?,
        val carrierCode: String?,
        val rdTermCode: String?,
        val paymentTermCode: String?,
        val ownerCompanyCode: String?            // optional
) {
        constructor(polCode: String, podCode: String): this(polCode, podCode, 0.0F,
                "", "", "","")

        constructor(): this("", "", 0.0F,
                "", "", "","")
}

data class PostMarketOfferListRequest(
        @Transient
        @SerializedName("id")
        var id : String?,
        @SerializedName("marketTypeCode")
        var marketTypeCode : String?,

        @SerializedName("companyCodes")
        var companyCodes : ArrayList<String>?,

        @SerializedName("rdtermCode")
        var rDTermCode : String?,

        @SerializedName("paymentTermCode")
        var paymentTermCode : String?,

        @SerializedName("pol")
        var pol : String = "",
        @SerializedName("pod")
        var pod : String = "",

        @Transient
        @SerializedName("containerSizeCode")
        var containerSizeCode : String?,

        @SerializedName("containerTypeCode")
        var containerTypeCode : String?
)

/***************************************************************************************************
 * /list/newServicelaneDueDate
 **************************************************************************************************/
data class PostNewServicelaneDueDateRequest(
        @SerializedName("cryrCd")
        val cryrCd : String?,

        @SerializedName("svceLaneCd")
        val svceLaneCd : String?,

        @SerializedName("basePortCd")
        val basePortCd : String?,

        @SerializedName("basePortRotSeq")
        val basePortRotSeq : String?,

        @SerializedName("baseYearWeekArrList")
        val baseYearWeekArrList : List<BaseYearWeek>?,

        @SerializedName("portRotSeqArrList")
        val portRotSeqArrList : List<PortRotSeq>?
)

data class BaseYearWeek(
        @SerializedName("baseYearWeek")
        val baseYearWeek : String?
)

data class PortRotSeq(
        @SerializedName("portRotSeq")
        val portRotSeq : String?
)
