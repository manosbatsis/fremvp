package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class OrderTradeOfferDetail(
        val txType: String?,
        val txDealProcessType: Int?,
        val dealSuccessYn: String?,
        val dealErrorCode: String?,
        val dealErrorMessage: String?,
        val dealSkipYn: String?,
        val dealSkipMessage: String?,
        val id: Long = 0L,
        var masterContractNumber: String?,
        val offerNumber: String,
        val offerChangeSeq: Int = 0,
        val tradeMarketTypeCode: String,            // 01=Ocean, 02=Air
        var offerTypeCode: String,                  // S=Sell, B=Buy
        val offerTransactionTypeCode: String,       // 01=New, 02=Modify, 03=Cancel
        var tradeCompanyCode: String,               // 정의된 Membership Code에 따른다
        var tradeRoleCode: String,                  // 001=Carrier, 002=Forwarder, 003=NVOCC, 004=Cargo Owner, 005= Trade System
        val allYn: String,                          // 1=Whole, 0=Partial
        var referenceOfferNumber: String,           // Product에서 채번된 OFFER NUMBER로 TRADE에서는 별도의 OFFER NUMBER로 채번하여 관리하고 상대방의 키는 참조정보로 관리한다
        val referenceOfferChangeSeq: Int,           // 0
        var referenceOfferNumberOfCurrentProduct: String,
        val referenceOfferChangeSeqOfCurrentProduct: Int,           // 0
        val offerDate: String,                      // "20200423090323700000"
        val offerMainStatusCode: String,            // "39"
        val expireYn: String,                       // Y = 1, N = 0
        val virtualGroupAgreementYn: String,        // Y = 1, N = 0
        val whatIfAgreementYn: String,              // Y = 1, N = 0
        val virtualGroupOfferYn: String,            // Y = 1, N = 0
        val offerRejectCode: String,                // "0"
        val timeInForceCode: String,                // 0=장구분동안유효(장전시간외, 정규장, 시간외등하나의장구분동안만유효), 1=취소할때까지유효(GTC), 2=At the Opening (OPG), 3=전부체결안되면전체수량취소(IOC), 4=체결될수있는만큼체결되고잔량취소(FOK), 5=정해진시간까지유효
        val effectiveDate: String?,
        val deleteDate: String?,
        val employeeNumber: String,                 // "USR92"
        val inputSource: String?,
        val dealNumber: String,                     // "D202005121606332297130003467"
        val dealChangeSeq: Int,                     // 0
        val deleteYn: String,                       // "0"
        val offerRdTermCode: String,                // 01=CY-CY, 02=CY-DOOR, 03=DOOR-CY, 04=DOOR-DOOR
        val offerPaymentTermCode: String,           // 01=PPD, 02=CCT
        val offerSimulationFactors: List<OfferSimulationFactor>,
        var offerLineItems: List<OfferLineItem>,
        val offerRoutes: List<OfferRoute>,
        var offerSelections: List<OfferSelection>,
        var offerCarriers: List<OfferCarrier>
) : Serializable
{
    data class OfferSimulationFactor(
            val factorId: Int?,                      // 001=Week, 002=Route, 003=Price, 004=Volume
            val minValue: Float? = 0.0F,
            val maxValue: Float? = 0.0F
    ) : Serializable

    data class OfferLineItem(
            val commonDataName: String,
            val commonErrorCode: String,
            val commonErrorMessage: String,
            val commonTreatCode: String,
            val commonTreatMessage: String,
            val dealSuccessYn: String,
            val dealErrorCode: String,
            val dealErrorMessage: String,
            val dealSkipYn: String,
            val dealSkipMessage: String,
            val dealQty: Int = 0,
            val dealPrice: Int = 0,
            val dealAmt: Int = 0,
            val id: Long = 0L,
            val offerNumber: String,
            val offerChangeSeq: Int = 0,
            val offerItemSeq: Int = 0,
            val baseYearWeek: String,               // yyyyww (6자리)
            var offerQty: Int = 0,                   // 단위 : TEU
            var offerRemainderQty: Int = 0,
            val offerPrice: Float,                  // 소수점 두자리까지
            val tradeContainerTypeCode: String,   // 01=Dry, 02=Reefer, 03=Empty, 04=SOC
            val tradeContainerSizeCode: String,   // 01=20ft, 02=40ft, 03=40ftHC, 04=45ftHC
            val firstPaymentRatio: Float,
            val middlePaymentRatio: Float,
            val balancedPaymentRatio: Float,
            val deleteYn: String,
            val offerPrices: List<OfferPrice>,
            @Transient
            var checked: Boolean = false
    ) : Serializable
    {
        data class OfferPrice(
                val containerTypeOrder: Int = 0,
                val containerSizeOrder: Int = 0,
                val id: Long = 0L,
                val offerNumber: String,
                val offerChangeSeq: Int = 0,
                val offerItemSeq: Int = 0,
                val deleteYn: String,
                val containerTypeCode: String,      // 01=Dry, 02=Reefer, 03=Empty, 04=SOC
                val containerSizeCode: String,      // 01=20ft, 02=40ft, 03=40ftHC, 04=45ftHC
                val offerPrice: Float                // 소수점 두자리까지
        ) : Serializable
    }

    data class OfferRoute(
            val id: Long = 0L,
            val offerNumber: String,
            val offerChangeSeq: Int = 0,
            val deleteYn: String,
            val offerRegSeq: Int,
            val locationCode: String,               // TRADE 를 위한 Container가 채류한 LCC/SCC Location Code Location Division Code
            val locationTypeCode: String,           // 01=POR, 02=POL, 03=POD, 04=DEL
            val locationName: String                // locationCode 에 대한 Name
    ) : Serializable

    data class OfferSelection(
//            val id: Long = 0L,
//            val offerNumber: String,
//            val offerChangeSeq: Int = 0,
//            val deleteYn: String,
            val selectOfferNumber: String,
            val selectOfferChangeSeq: Long
    ) : Serializable

    data class OfferCarrier(
            val offerCarrierCode: String
    ) : Serializable
}