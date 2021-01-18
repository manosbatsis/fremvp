package com.cyberlogitec.freight9.lib.model

import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.LOCATION_TYPE_CODE_POL
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_CYCY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import java.io.Serializable


data class Offer (
        var offerNumber: String = Constant.EmptyString,               // "PXXXXX"
        val referenceOfferNumber: String = Constant.EmptyString,      // "FXXXXX"
        var offerChangeSeq: Long = 0L,
        var referenceOfferChangeSeq: Long = 0L,
        var masterContractNumber: String = Constant.EmptyString,
        var tradeMarketTypeCode: String = "01",
        var offerTypeCode: String = Constant.EmptyString,
        var offerTransactionTypeCode: String = "01",
        var tradeCompanyCode: String = "USR00",
        var tradeRoleCode: String = "001",
        var allYn: String = "0",
        var expireYn: String = "0",
        var virtualGroupAgreementYn: String = "0",
        var whatIfAgreementYn: String = "0",
        var virtualGroupOfferYn: String = "0",
        var timeInForceCode: String = "0",
        var offerRdTermCode: String = OFFER_RD_TERM_CODE_CYCY,         // CY-CY
        var offerPaymentTermCode: String = OFFER_PAYMENT_TERM_CODE_PPD,      // PPD
        var offerLineItems: List<OfferLineItem>? = null,
        var offerRoutes: List<OfferRoute>? = null,
        var offerCarriers: List<OfferCarrier>? = null,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Missed Properties from Contract
        var offerPaymentPlanCode: String = PAYMENT_PLANCODE_PS,
        var serviceLaneCode: String? = null,
        var serviceLaneName: String? = null,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // local Property (bof recently)
        var isChecked: Boolean = false

) : Serializable

data class OfferLineItem (
        var baseYearWeek: String = Constant.EmptyString,
        var offerQty: Int = 0,
        var offerPrice: Int = 0,
        var tradeContainerTypeCode: String = CONTAINER_TYPE_CODE_DRY,
        var tradeContainerSizeCode: String = CONTAINER_SIZE_CODE_20FT,
        var firstPaymentRatio: Float = 0.0f,
        var middlePaymentRatio: Float = 0.0f,
        var balancedPaymentRatio: Float = 0.0f,
        var offerPrices: List<OfferPrice>? = null,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // local propertity
        var costPrice: Int = 0,
        var isChecked: Boolean = false,
        var blink: Boolean = false,
        var focused: Boolean = false

) : Serializable

data class OfferPrice (
        var containerTypeCode:String = CONTAINER_TYPE_CODE_DRY,
        var containerSizeCode:String = CONTAINER_SIZE_CODE_20FT,
        var offerPrice: Float = 0.0f
) : Serializable

data class OfferRoute (
        var offerRegSeq: Int = 0,
        var locationCode: String = Constant.EmptyString,
        var locationTypeCode: String = LOCATION_TYPE_CODE_POL,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // local property
        var locationName: String = Constant.EmptyString
) : Serializable

data class OfferCarrier (
        var offerCarrierCode: String = Constant.EmptyString,

        ////////////////////////////////////////////////////////////////////////////////////////////
        // local Property (bof carrier filter)
        var offerCarrierName: String? = null,
        var isChecked: Boolean = true
) : Serializable