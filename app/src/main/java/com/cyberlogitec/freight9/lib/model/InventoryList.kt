package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class InventoryList(
        val eventType: String?,
        val timestamp: String?,
        val errorCode: String?,
        val errorMessage: String?,
        val memo: String?,
        val userId: String?,
        val carrierCode: String?,
        val carrierCount: Int?,
        val minQty: String?,
        val maxQty: String?,
        val minYearWeek: String?,
        val maxYearWeek: String?,
        val polCount: Int?,
        val podCount: Int?,
        val polName: String?,
        val podName: String?,
        val polCode: String?,
        val podCode: String?,
        val inventoryNumber: String?,
        val masterContractNumber: String?,
        val ownerCompanyCode: String?,
        val inventoryChangeSeq: Int?,
        var inventoryDetails: InventoryDetails? // Detail 조회 후 assign
) : Serializable
