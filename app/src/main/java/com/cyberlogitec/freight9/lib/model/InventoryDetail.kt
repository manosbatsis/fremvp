package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class InventoryDetails(
        val commonDataName: String?,
        val commonErrorCode: String?,
        val commonErrorMessage: String?,
        val commonTreatCode: String?,
        val commonTreatMessage: String?,
        val id: Long?,
        val inventoryNumber: String?,
        val inventoryChangeSeq: Int?,
        val ownerCompanyCode: String?,
        val masterContractNumber: String?,
        val inventoryEventTypeCode: String?,
        val deleteYn: String?,
        val offerCarriers: List<OrderTradeOfferDetail.OfferCarrier>?,
        val inventoryDetails: List<InventoryDetail>?
)  : Serializable
{
        data class InventoryDetail(
                val commonDataName: String?,
                val commonErrorCode: String?,
                val commonErrorMessage: String?,
                val commonTreatCode: String?,
                val commonTreatMessage: String?,
                val id: Int?,
                val inventoryNumber: String?,
                val inventoryChangeSeq: Int?,
                val regSeq: Int?,
                val initialQty: Float?,
                val initialOnMarketQty: Float?,
                val inOutQty: Float?,
                val inOutOnMarketQty: Float?,
                val remainderOnMarketQty: Float?,
                val remainderConfirmedQty: Float?,
                val inOutPrice: Float?,
                val inventoryStatusCode: String?,
                val baseYearWeek: String?,
                val memo: String?,
                val accumulatedInOnMarketQty: Float?,
                val accumulatedOutOnMarketQty: Float?,
                val accumulatedInConfirmedQty: Float?,
                val accumulatedOutQty: Float?,
                val deleteYn: String?,
                val inventoryTransactions: List<InventoryTransaction>?  // 이 array는 Sell Order에서는 무관한 값들임
        ) : Serializable
        {
                data class InventoryTransaction(
                        val commonDataName: String?,
                        val commonErrorCode: String?,
                        val commonErrorMessage: String?,
                        val commonTreatCode: String?,
                        val commonTreatMessage: String?,
                        val id: Int?,
                        val inventoryNumber: String?,
                        val inventoryChangeSeq: Int?,
                        val regSeq: Int?,
                        val regSecondSeq: Int?,
                        val initialQty: Float?,
                        val inOutQty: Float?,
                        val remainderQty: Float?,
                        val inOutPrice: Float?,
                        val inventoryDetailStatusCode: String?,
                        val referenceNumber: String?,
                        val referenceChangeSeq: Int?,
                        val referenceSecondNumber: String?,
                        val referenceSecondChangeSeq: Int?,
                        val inOutTypeCode: String?,
                        val referenceThirdNumber: String?,
                        val referenceThirdChangeSeq: Int?,
                        val deleteYn: String?,
                        val memo: String?
                ) : Serializable
        }
}

data class InventoryDetailDummy(
        @SerializedName("dataOwnrPtrId")
        var dataOwnrPtrId: String? = null,

        @SerializedName("invnNr")
        var invnNr: String? = null,

        @SerializedName("frtPrceMeasCd")
        var frtPrceMeasCd: String? = null,

        @SerializedName("bseYw")
        var bseYw: String? = null,

        @SerializedName("mstrCtrkNr")
        var mstrCtrkNr: String? = null,

        @SerializedName("bseWeekFmDt")
        var bseWeekFmDt: String? = null,

        @SerializedName("iniQty")
        var iniQty: Float = 0f,

        @SerializedName("unusQty")
        var unusQty: Float = 0f,

        @SerializedName("mktQty")
        var mktQty: Float = 0f,

        @SerializedName("bkgQty")
        var bkgQty: Float = 0f,

        @SerializedName("soldQty")
        var soldQty: Float = 0f,

        @SerializedName("iniAmt")
        var iniAmt: Float = 0f,

        @SerializedName("unusAmt")
        var unusAmt: Float = 0f,

        @SerializedName("mktAmt")
        var mktAmt: Float = 0f,

        @SerializedName("soldAmt")
        var soldAmt: Float = 0f,

        @SerializedName("n20ftCgoPrce")
        var n20ftCgoPrce: Float = 0f,

        @SerializedName("n40ftCgoPrce")
        var n40ftCgoPrce: Float = 0f,

        @SerializedName("n40ftHighCubcCgoPrce")
        var n40ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("n45ftHighCubcCgoPrce")
        var n45ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("buyN20ftCgoPrce")
        var buyN20ftCgoPrce: Float = 0f,

        @SerializedName("buyN40ftCgoPrce")
        var buyN40ftCgoPrce: Float = 0f,

        @SerializedName("buyN40ftHighCubcCgoPrce")
        var buyN40ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("buyN45ftHighCubcCgoPrce")
        var buyN45ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("ptrOferNr")
        var ptrOferNr: String? = null,

        @SerializedName("cryrCd")
        var cryrCd: String? = null,

        @SerializedName("iniPymtRto")
        var iniPymtRto: Float = 0f,

        @SerializedName("iniPymtDt")
        var iniPymtDt: String? = null,

        @SerializedName("iniPymtAmt")
        var iniPymtAmt: Float = 0f,

        @SerializedName("midTrmPymtRto")
        var midTrmPymtRto: Float = 0f,

        @SerializedName("midTrmPymtDt")
        var midTrmPymtDt: String? = null,

        @SerializedName("midTrmPymtAmt")
        var midTrmPymtAmt: Float = 0f,

        @SerializedName("balRto")
        var balRto: Float = 0f,

        @SerializedName("balDt")
        var balDt: String? = null,

        @SerializedName("balAmt")
        var balAmt: Float = 0f,

        @SerializedName("rcvTrmCd")
        var rcvTrmCd: String? = null,

        @SerializedName("delTrmCd")
        var delTrmCd: String? = null,

        @SerializedName("frtPymtCd")
        var frtPymtCd: String? = null,

        @SerializedName("prcsStsCd")
        var prcsStsCd: String? = null,

        @SerializedName("creUsrId")
        var creUsrId: String? = null,

        @SerializedName("updUsrId")
        var updUsrId: String? = null,

        @SerializedName("depSeq")
        var depSeq: String? = null
)


