package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class DashboardOfferHistory(
        val offerNumber: String,
        val offerTypeCode: String,
        val allYn: String,
        val eventCell: List<EventCell>?
) : Serializable
{
    data class EventCell(
            val eventCode: String,  // "01"=offerPlaced, "02"=dealt, "03"=weekExpired,  "04"=allDealt, "05"=offerClosed, "05"=offerCanceled
            val eventName: String,
            val lastEventTimestamp: String,
            val lastEventHost: String,
            val eventLog: List<EventLog>,
            var baseYearWeek: String = "",      // use in Local
            var allYn: String                   // use in Local. same value with DashboardOfferHistory.allYn
    ) : Serializable
    {
        data class EventLog(
                val eventTimestamp: String?,
                val referenceEventNumber: String?,
                val referenceEventNumberChangeSeq: Int?,
                val baseYearWeek: String,
                val dealQty: Float = 0.0F,
                val dealPrice: Float = 0.0F,
                val dealAmt: Float = 0.0F,
                val offerQty: Float = 0.0F,
                val offerPrice: Float = 0.0F,
                val offerAmt: Float = 0.0F,
                val leftQty: Float = 0.0F,
                val leftPrice: Float = 0.0F,
                val leftAmt: Float = 0.0F,
                val tradeClosing: String?,
                var isSelected: Boolean = false     // use in Local
        ) : Serializable
    }
}