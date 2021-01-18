package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class MasterContractWithInventory(
        val commonDataName: String?,
        val commonErrorCode: String?,
        val commonErrorMessage: String?,
        val commonTreatCode: String?,
        val commonTreatMessage: String?,
        val id: Long?,
        val masterContractNumber: String?,
        val paymentPlanCode: String?,
        val carrierCode: String?,
        val deleteYn: String? = "0",
        val rdTermCode: String?,
        val paymentTermCode: String?,
        val serviceLaneCode: String?,
        val masterContractRoutes: List<MasterContractRoute>,
        val masterContractLineItems: List<MasterContractLineItem>,
        val masterContractCargoTypes: List<MasterContractCargoTypes>,
        val masterContractCarriers: List<MasterContractCarriers>,
        val inventory: InventoryDetails
) : Serializable
{
        data class MasterContractRoute(
                val commonDataName: String?,
                val commonErrorCode: String?,
                val commonErrorMessage: String?,
                val commonTreatCode: String?,
                val commonTreatMessage: String?,
                val id: Long?,
                val masterContractNumber: String?,
                val regSeq: Int? = 0,
                val locationCode: String = "",
                val locationTypeCode: String = "",
                val deleteYn: String? = "0",
                val locationName: String = ""
        ) : Serializable

        data class MasterContractLineItem(
                val commonDataName: String?,
                val commonErrorCode: String?,
                val commonErrorMessage: String?,
                val commonTreatCode: String?,
                val commonTreatMessage: String?,
                val id: Long?,
                val masterContractNumber: String?,
                val baseYearWeek: String?,
                val qty: Float? = 0.0F,
                val deleteYn: String? = "0",
                val masterContractPrices: List<MasterContractPrice>
        ) : Serializable
        {
                data class MasterContractPrice(
                        val commonDataName: String?,
                        val commonErrorCode: String?,
                        val commonErrorMessage: String?,
                        val commonTreatCode: String?,
                        val commonTreatMessage: String?,
                        val id: Long?,
                        val masterContractNumber: String?,
                        val baseYearWeek: String?,
                        val containerTypeCode: String?,
                        val containerSizeCode: String?,
                        val price: Float? = 0.0F,
                        val deleteYn: String? = "0"
                ) : Serializable
        }

        data class MasterContractCargoTypes(
                val commonDataName: String?,
                val commonErrorCode: String,
                val commonErrorMessage: String?,
                val commonTreatCode: String?,
                val commonTreatMessage: String,
                val id: Long?,
                val masterContractNumber: String?,
                val cargoTypeCode: String?,
                val deleteYn: String? = "0"
        ) : Serializable

        data class MasterContractCarriers(
                val commonDataName: String?,
                val commonErrorCode: String?,
                val commonErrorMessage: String?,
                val commonTreatCode: String?,
                val commonTreatMessage: String?,
                val id: Long?,
                val masterContractNumber: String?,
                val carrierCode: String?,
                val deleteYn: String? = "0"
        ) : Serializable
}
