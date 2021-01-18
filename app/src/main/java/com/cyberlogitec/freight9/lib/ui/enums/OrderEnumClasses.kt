package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_40FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_40FTHC
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_45FTHC
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_EMPTY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_REEFER
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_SOC
import com.cyberlogitec.freight9.config.ContainerType

/**
 * Order 에서 참조하는 enum class
 */

//----------------------------------------------------------------------------------------------
/**
 * interface 함수 호출 시 구분자
 */
enum class ListenerKind {
    LISTENER_SUM,
    LISTENER_VOLUME,
    LISTENER_KEYPAD,
    LISTENER_INPUTNO_MODE
}

//----------------------------------------------------------------------------------------------
/**
 * Recycler view UI를 변경하기 위한 checkbox UI 구분자
 */
enum class BatchInputMode {
    ModeBatchInputNo,
    ModeBatchInputYes,
    ModeBatchInputMax
}

enum class WholePartialMode {
    ModeWhole,
    ModePartial
}

//----------------------------------------------------------------------------------------------
/**
 * Container type 에 대한 enum class
 */
enum class ContainerItemTypes constructor(
        val index: Int = 0,
        val containerTypeCode: String,          // 01=Dry, 02=Reefer, 03=Empty, 04=SOC
        val containerSizeCode: String,          // 01=20ft, 02=40ft, 03=40ftHC, 04=45ftHC
        val nameId: Int,
        val nameShortId: Int,
        val isT: Boolean         // T or F
){
    D_TYPE_20(0, CONTAINER_TYPE_CODE_DRY, CONTAINER_SIZE_CODE_20FT, R.string.container_full_20, R.string.container_size_20_abbrev, true),
    D_TYPE_40(0, CONTAINER_TYPE_CODE_DRY, CONTAINER_SIZE_CODE_40FT, R.string.container_full_40, R.string.container_size_40_abbrev, false),
    D_TYPE_40_HC(0, CONTAINER_TYPE_CODE_DRY, CONTAINER_SIZE_CODE_40FTHC, R.string.container_full_40hc, R.string.container_size_40hc_abbrev, false),
    D_TYPE_45_HC(0, CONTAINER_TYPE_CODE_DRY, CONTAINER_SIZE_CODE_45FTHC, R.string.container_full_45hc, R.string.container_size_45hc_abbrev, false),
    R_TYPE_20(1, CONTAINER_TYPE_CODE_REEFER, CONTAINER_SIZE_CODE_20FT, R.string.container_rf_20, R.string.container_size_20_abbrev, true),
    R_TYPE_40(1, CONTAINER_TYPE_CODE_REEFER, CONTAINER_SIZE_CODE_40FT, R.string.container_rf_40, R.string.container_size_40_abbrev, false),
    R_TYPE_40_HC(1, CONTAINER_TYPE_CODE_REEFER, CONTAINER_SIZE_CODE_40FTHC, R.string.container_rf_40hc, R.string.container_size_40hc_abbrev, false),
    R_TYPE_45_HC(1, CONTAINER_TYPE_CODE_REEFER, CONTAINER_SIZE_CODE_45FTHC, R.string.container_rf_45hc, R.string.container_size_45hc_abbrev, false),
    E_TYPE_20(2, CONTAINER_TYPE_CODE_EMPTY, CONTAINER_SIZE_CODE_20FT, R.string.container_empty_20, R.string.container_size_20_abbrev, true),
    E_TYPE_40(2, CONTAINER_TYPE_CODE_EMPTY, CONTAINER_SIZE_CODE_40FT, R.string.container_empty_40, R.string.container_size_40_abbrev, false),
    E_TYPE_40_HC(2, CONTAINER_TYPE_CODE_EMPTY, CONTAINER_SIZE_CODE_40FTHC, R.string.container_empty_40hc, R.string.container_size_40hc_abbrev, false),
    E_TYPE_45_HC(2, CONTAINER_TYPE_CODE_EMPTY, CONTAINER_SIZE_CODE_45FTHC, R.string.container_empty_45hc, R.string.container_size_45hc_abbrev, false),
    S_TYPE_20(3, CONTAINER_TYPE_CODE_SOC, CONTAINER_SIZE_CODE_20FT, R.string.container_soc_20, R.string.container_size_20_abbrev, true),
    S_TYPE_40(3, CONTAINER_TYPE_CODE_SOC, CONTAINER_SIZE_CODE_40FT, R.string.container_soc_40, R.string.container_size_40_abbrev, false),
    S_TYPE_40_HC(3, CONTAINER_TYPE_CODE_SOC, CONTAINER_SIZE_CODE_40FTHC, R.string.container_soc_40hc, R.string.container_size_40hc_abbrev, false),
    S_TYPE_45_HC(3, CONTAINER_TYPE_CODE_SOC, CONTAINER_SIZE_CODE_45FTHC, R.string.container_soc_45hc, R.string.container_size_45hc_abbrev, false);
    companion object {
        fun getContainerItemType(containerTypeCode: String, containerSizeCode: String): ContainerItemTypes? {
            for (containerItemType in values()) {
                if (containerItemType.containerTypeCode == containerTypeCode
                        && containerItemType.containerSizeCode == containerSizeCode) {
                    return containerItemType
                }
            }
            return D_TYPE_20
        }
    }
}

enum class ContainerSize {
    SIZE_N20, SIZE_N40, SIZE_40HC, SIZE_45HC
}

enum class ContainerName constructor(
        val containerTypeCode: String,
        val nameFullId: Int,
        val nameMiddleId: Int
){
    D_NAME(CONTAINER_TYPE_CODE_DRY, R.string.container_full_name, R.string.container_full_middle_name),
    R_NAME(CONTAINER_TYPE_CODE_REEFER, R.string.container_rf_name, R.string.container_rf_middle_name),
    E_NAME(CONTAINER_TYPE_CODE_EMPTY, R.string.container_empty_name, R.string.container_empty_middle_name),
    S_NAME(CONTAINER_TYPE_CODE_SOC, R.string.container_soc_name, R.string.container_soc_middle_name);
    companion object {
        fun getContainerName(containerTypeCode: String): ContainerName? {
            for (containerName in values()) {
                if (containerName.containerTypeCode == containerTypeCode) {
                    return containerName
                }
            }
            return D_NAME
        }
    }
}

enum class RdTermItemTypes constructor(
        val offerPaymentTermCode: String,
        val rdNameId: Int   // CY-CY, CY-Door, Door-Door, Door-Cy
){
    RD_YY(ConstantTradeOffer.OFFER_RD_TERM_CODE_CYCY, R.string.rd_term_type_cycy),
    RD_YD(ConstantTradeOffer.OFFER_RD_TERM_CODE_CYDOOR, R.string.rd_term_type_cydoor),
    RD_DY(ConstantTradeOffer.OFFER_RD_TERM_CODE_DOORCY, R.string.rd_term_type_doorcy),
    RD_DD(ConstantTradeOffer.OFFER_RD_TERM_CODE_DOORDOOR, R.string.rd_term_type_doordoor);
    companion object {
        fun getRdTermItemType(offerPaymentTermCode: String) : RdTermItemTypes? {
            for (rdTermItemType in values()) {
                if (rdTermItemType.offerPaymentTermCode == offerPaymentTermCode) {
                    return rdTermItemType
                }
            }
            return RD_YY
        }
    }
}

enum class ContainerCard {
    AllContainerClose,
    FullContainer,
    EmptyContainer,
    SOCContainer,
    RFContainer
}

enum class PayPlanEntry {
    PP_BuyOrder,
    PP_BuyOrderDetailCondition,
    PP_SellOrder,
    PP_SellOrderDetailCondition
}
