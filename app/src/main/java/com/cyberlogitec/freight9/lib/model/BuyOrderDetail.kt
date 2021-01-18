package com.cyberlogitec.freight9.lib.model

import com.cyberlogitec.freight9.lib.ui.enums.ContainerItemTypes
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.google.gson.annotations.SerializedName

/**
 * --------------------------------------------------------------------------
 * http://www.amock.io/api/ycmin/buyorder/order/detail/OFR-HLC-20191107-S03
 * http://www.amock.io/api/ycmin/buyorder/order/detail/OFR-HLC-20191107-S02
 * http://www.amock.io/api/ycmin/buyorder/order/detail/OFR-HLC-20191107-S04
 * http://www.amock.io/api/ycmin/buyorder/order/detail/OFR-HLC-20191107-S01
 * --------------------------------------------------------------------------
 * https://testapi.io/api/freight9/buyorder/order/detail/OFR-HLC-20191107-S03
 * https://testapi.io/api/freight9/buyorder/order/detail/OFR-HLC-20191107-S02
 * https://testapi.io/api/freight9/buyorder/order/detail/OFR-HLC-20191107-S04
 * https://testapi.io/api/freight9/buyorder/order/detail/OFR-HLC-20191107-S01
 * --------------------------------------------------------------------------
 */
data class BuyOrderDetail(
        @SerializedName("balAmt")
        var balAmt: Float = 0f,

        @SerializedName("balDt")
        var balDt: String? = null,

        @SerializedName("balRto")
        var balRto: String? = null,

        @SerializedName("bseInvnNr")
        var bseInvnNr: String? = null,

        @SerializedName("bseWeekFmDt")
        var bseWeekFmDt: String? = null,

        @SerializedName("bseYw")
        var bseYw: String? = null,

        @SerializedName("creUsrId")
        var creUsrId: String? = null,

        @SerializedName("dataOwnrPtrId")
        var dataOwnrPtrId: String? = null,

        @SerializedName("delTrmCd")
        var delTrmCd: String? = null,

        @SerializedName("depSeq")
        var depSeq: String? = null,

        @SerializedName("frtPrceMeasCd")
        var frtPrceMeasCd: String? = null,

        @SerializedName("frtPymtCd")
        var frtPymtCd: String? = null,

        @SerializedName("iniPymtAmt")
        var iniPymtAmt: Float = 0f,

        @SerializedName("iniPymtDt")
        var iniPymtDt: String? = null,

        @SerializedName("iniPymtRto")
        var iniPymtRto: String? = null,

        @SerializedName("invnChngCd")
        var invnChngCd: String? = null,

        @SerializedName("invnNr")
        var invnNr: String? = null,

        @SerializedName("invnSeq")
        var invnSeq: String? = null,

        @SerializedName("midTrmPymtAmt")
        var midTrmPymtAmt: Float = 0f,

        @SerializedName("midTrmPymtDt")
        var midTrmPymtDt: String? = null,

        @SerializedName("midTrmPymtRto")
        var midTrmPymtRto: String? = null,

        @SerializedName("mstrCtrkNr")
        var mstrCtrkNr: String? = null,

        @SerializedName("n20ftCgoPrce")
        var n20ftCgoPrce: Float = 0f,

        @SerializedName("n40ftCgoPrce")
        var n40ftCgoPrce: Float = 0f,

        @SerializedName("n40ftHighCubcCgoPrce")
        var n40ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("n45ftHighCubcCgoPrce")
        var n45ftHighCubcCgoPrce: Float = 0f,

        @SerializedName("oferGrpNr")
        var oferGrpNr: String? = null,

        @SerializedName("oferNr")
        var oferNr: String? = null,

        @SerializedName("oferWeekAmt")
        var oferWeekAmt: Float = 0f,

        @SerializedName("oferWeekQty")
        var oferWeekQty: Float = 0f,

        @SerializedName("ordGrpNr")
        var ordGrpNr: String? = null,

        @SerializedName("ordCtrkNr")
        var ordCtrkNr: String? = null,

        @SerializedName("prcsStsCd")
        var prcsStsCd: String? = null,

        @SerializedName("ptrOferNr")
        var ptrOferNr: String? = null,

        @SerializedName("rcvTrmCd")
        var rcvTrmCd: String? = null,

        @SerializedName("unusQty")
        var unusQty: Float = 1f,

        @SerializedName("updUsrId")
        var updUsrId: String? = null,

        @SerializedName("ordWeekQty")
        var ordWeekQty: Float = 0f,

        @SerializedName("ordWeekTtlAmt")
        var ordWeekTtlAmt: Float = 0f,

        @SerializedName("buyDataOwnrPtrId")
        var buyDataOwnrPtrId: String? = null,

        @SerializedName("cryrCd")
        var cryrCd: String? = null,

        // for filter
        @Transient
        var rdTerm: RdTermItemTypes,

        // for filter : 주차별 nXXftXXPrce 를 가지고 있는 containerItemType들
        @Transient
        var containerItemTypes: List<ContainerItemTypes>
)

