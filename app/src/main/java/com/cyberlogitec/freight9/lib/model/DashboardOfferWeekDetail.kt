package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class DashboardOfferWeekDetail (
        val aggDealAmt: Float = 0.0F,
        val aggDealPrice: Float = 0.0F,
        val aggDealQty: Int = 0,
        val aggOfferQty: Int = 0,
        val aggLeftAmt: Float = 0.0F,
        val aggLeftPrice: Float = 0.0F,
        val aggLeftQty: Int = 0,
        val baseYearWeek: String,
        val offerChangeSeq: Int,
        val offerNumber: String,
        val tradeClosing: String?,
        val dealLog: List<LineItemDealHistoryLog>?
) : Serializable
{
    data class LineItemDealHistoryLog (
            val eventTimestamp: String?,
            val referenceEventNumber: String?,
            val referenceEventChangeSeq: Int = 0,
            val dealQty: Int = 0,
            val dealPrice: Int = 0,
            val dealAmt: Int = 0,
            val offerQty: Int = 0,
            val leftQty: Int = 0,
            val leftPrice: Int = 0,
            val leftAmt: Int = 0
    ) : Serializable
}