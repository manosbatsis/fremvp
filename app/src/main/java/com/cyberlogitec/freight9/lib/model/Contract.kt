package com.cyberlogitec.freight9.lib.model

import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_CYCY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Contract (
        @SerializedName("@id")
        var id0: Int ? = null,
        var commonDataName: String? = null,
        var commonErrorCode: String? = null,
        var commonErrorMessage: String? = null,
        var commonTreatCode: String? = null,
        var commonTreatMessage: String? = null,
        var id: Int? = null,
        var masterContractNumber: String = "",
        var paymentPlanCode: String = PAYMENT_PLANCODE_PS,
        var carrierCode: String = "",
        var deleteYn: String = "0",
        var rdTermCode: String = OFFER_RD_TERM_CODE_CYCY,                  // CY-CY
        var paymentTermCode: String = OFFER_PAYMENT_TERM_CODE_PPD,         // PPD
        var serviceLaneCode: String = "01",
        var masterContractLineItems: List<ContractLineItem>? = null,
        var masterContractCargoTypes: List<ContractCargoType>? = null,
        var masterContractCarriers: List<ContractCarrier>? = null,
        var masterContractRoutes: List<ContractRoute>? = null,
        var inventory: ContractInventory? = null,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Local Properties for Offer (copy from member info)
        var tradeCompanyCode: String = "USR00",
        var tradeRoleCode: String = "001",
        var offerPaymentPlanCode: String = PAYMENT_PLANCODE_PS,
        var offerPaymentTermCode: String = OFFER_PAYMENT_TERM_CODE_PPD

) : Serializable

data class ContractLineItem (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var masterContractNumber: String,
        var baseYearWeek: String,
        var qty: Int,
        var deleteYn: String,
        var masterContractPrices: List<ContractPrice>?,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Local Properties for Offer (copy from inventory)
        var isChecked: Boolean = false,
        var blink: Boolean = false,
        var focused: Boolean = false,
        var remainderQty: Int = 0,
        var offerQty: Int = 0,
        var costPrice: Float = 0.0f,
        var offerPrice: Int = 0
) : Serializable

data class ContractPrice (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var masterContractNumber: String,
        var baseYearWeek: String,
        var containerTypeCode: String = CONTAINER_TYPE_CODE_DRY,
        var containerSizeCode: String = CONTAINER_SIZE_CODE_20FT,
        var price: Float,
        var deleteYn: String
) : Serializable

data class ContractCargoType (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var masterContractNumber: String,
        var cargoTypeCode: String,
        var deleteYn: String
) : Serializable

data class ContractCarrier (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var masterContractNumber: String,
        var carrierCode: String,
        var deleteYn: String
) : Serializable

data class ContractInventory (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var inventoryNumber: String,
        var inventoryChangeSeq: Int,
        var ownerCompanyCode: String,
        var masterContractNumber: String,
        var inventoryEventTypeCode: String,
        var deleteYn: String,

        var inventoryDetails: List<ContractInventoryDetail>?
) : Serializable

data class ContractInventoryDetail (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var inventoryNumber: String,
        var inventoryChangeSeq: Int,
        var regSeq: Int,
        var initialQty: Int,
        var initialOnMarketQty: Int,
        var inOutQty: Int,
        var inOutOnMarketQty: Int,
        var remainderOnMarketQty: Int,
        var remainderConfirmedQty: Int,
        var accumulatedInOnMarketQty: Int,
        var accumulatedOutOnMarketQty: Int,
        var accumulatedInConfirmedQty: Int,
        var accumulatedOutQty: Int,
        var inOutPrice: Int,
        var inventoryStatusCode: String,
        var baseYearWeek: String,
        var memo: String?,
        var deleteYn: String,

        var inventoryTransactions: List<ContractInventoryTransaction>?

) : Serializable

data class ContractInventoryTransaction (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var inventoryNumber: String,
        var regSeq: Int,
        var regSecondSeq: Int,
        var initialQty: Int,
        var inOutQty: Int,
        var remainderConfirmedQty: Int,
        var inOutPrice: Int,
        var inventoryDetailStatusCode: String,
        var referenceNumber: String,
        var referenceChangeSeq: String,
        var referenceSecondNumber: String,
        var referenceSecondChangeSeq: String,
        var inOutTypeCode: String,
        var referenceThirdNumber: String,
        var referenceThirdChangeSeq: String,
        var deleteYn: String,
        var memo: String?
) : Serializable

data class ContractRoute (
        @SerializedName("@id")
        var id0: Int,
        var commonDataName: String?,
        var commonErrorCode: String?,
        var commonErrorMessage: String?,
        var commonTreatCode: String?,
        var commonTreatMessage: String?,
        var id: Int,
        var masterContractNumber: String,
        var regSeq: Int,
        var locationCode: String,
        var locationName: String,
        var locationTypeCode: String,
        var deleteYn: String

) : Serializable
