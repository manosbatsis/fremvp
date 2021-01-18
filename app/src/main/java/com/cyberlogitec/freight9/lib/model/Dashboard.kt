package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class Dashboard(
        val userId: String,
        val offerTypeCode: String,
        val polCode: String?,
        val podCode: String?,
        val selectedBaseYearWeek: List<String>?,
        val closedStsCount: Int,
        val openStsCount: Int,
        val cell: List<Cell>
) : Serializable
{
    data class Cell(
            val headPolCode: String,
            val headPodCode: String,
            val headPolName: String,
            val headPodName: String,
            val polCount: Int,
            val podCount: Int,
            val offerNumber: String,
            val offerChangeSeq: Int,
            val aggDealQty: Int = 0,
            val aggLeftQty: Int = 0,
            val priceValue: Float = 0.0F,
            val eventTimestamp: String?,
            val referenceEventNumber: String?,
            val referenceEventChangeSeq: Int?,
            val allYn: String?,
            val offerStatus: String,
            val carrierCount: Int?,
            var lineItem: List<LineItem>?,
            val routeItem: List<RouteItem>?,
            val carrierItem: List<CarrierItem>?
    ) : Serializable
    {
        data class RouteItem(
                val podCode: String?,
                val polCode: String?
        ) : Serializable

        data class LineItem(
                val baseYearWeek: String,
                val dealQty: Int = 0,
                val dealPrice: Float = 0.0F,
                val dealAmt: Float = 0.0F,
                val leftQty: Int = 0,
                val leftPrice: Float = 0.0F,
                val leftAmt: Float = 0.0F,
                val lineEventTimestamp: String?,
                val lineReferenceEventNumber: String?,
                val lineReferenceEventChangeSeq: Int?,
                var offerPrices: List<OrderTradeOfferDetail.OfferLineItem.OfferPrice>
        ) : Serializable

        data class CarrierItem(
                val carrierCode: String,
                val carrierName: String
        ) : Serializable
    }
}