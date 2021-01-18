package com.cyberlogitec.freight9.lib.apistat

import com.google.gson.annotations.SerializedName


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

/***************************************************************************************************
 * /list/summary, weeklist
 **************************************************************************************************/

data class PostDashboardSummaryListRequest(
        @SerializedName("userId")
        val userId: String?,                    // "USR00"
        @SerializedName("offerTypeCode")
        val offerTypeCode: String?,             // "S"(Sell) or "B"(Buy)
        @SerializedName("polCode")
        val polCode: String?,                   // "KRPUS"
        @SerializedName("podCode")
        val podCode: String?,                   // "GBSOU"
        @SerializedName("baseYearWeek")
        val baseYearWeek: String?               // ["202032","202016"] > "202032" 로 변경
)

data class PostDashboardWeekListRequest(
        @SerializedName("userId")
        val userId: String?,                    // "USR00"
        @SerializedName("offerTypeCode")
        val offerTypeCode: String?,             // "S"(Sell) or "B"(Buy)
        @SerializedName("polCode")
        val polCode: String?,                   // "KRPUS"
        @SerializedName("podCode")
        val podCode: String?                    // "GBSOU"
)

/***************************************************************************************************
 * /list/summary/rtelist
 **************************************************************************************************/

data class PostDashboardRouteListRequest(
        @SerializedName("userId")
        val userId: String?,                    // "USR00"
        @SerializedName("offerTypeCode")
        val offerTypeCode: String?              // "S"(Sell) or "B"(Buy)
)

/**************************************************************************************************/
