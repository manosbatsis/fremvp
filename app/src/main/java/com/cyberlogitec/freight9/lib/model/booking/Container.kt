package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Container(
        val containerDate: ContainerDate?,
        val containerDimension: ContainerDimension?,
        val containerFullEmptyIndicator: String,
        val containerHaulageArrangementCode: String,
        val containerMeasurement: ContainerMeasurement?,
        val containerMovementTypeCode: String,
        val containerNo: String = "-",
        val containerParty: ContainerParty?,
        val containerQuantity: String,
        val containerReefer: ContainerReefer?,
        val containerReferenceNo: ContainerReferenceNo?,
        val containerRemark: ContainerRemark?,
        val containerSupplierCode: String,
        val containerTypeSize: String,
        val id: Int,
        val logicalContainerNumber: String,
        var isExpand: Boolean = false
): Serializable

data class ContainerDate(
        val id: Int,
        val pickupDate: String,
        val plannedPickupDate: String,
        val positioningDate: String,
        val requestedDeliveryDate: String,
        val requestedPositioningDate: String
):Serializable

data class ContainerDimension(
        val containerHeight: String,
        val containerLength: String,
        val containerWidth: String,
        val dimensionUnit: String,
        val id: Int
):Serializable

data class ContainerMeasurement(
        val containerWeight: String,
        val containerWeightUnit: String,
        val airflow: String,
        val airflowUnit: String,
        val carbonDioxideGasLevel: String,
        val containerNetVolume: String,
        val containerNetVolumeUnit: String,
        val containerNetWeight: String,
        val containerNetWeightUnit: String,
        val humidity: String,
        val id: Int,
        val nitrogenGasLevel: String,
        val oxygenGasLevel: String
):Serializable

data class ContainerParty(
        val emptyDispatchParty: EmptyDispatchParty,
        val id: Int,
        val subcontractor: Subcontractor
):Serializable
data class EmptyDispatchParty(
        val id: Int,
        val partyInfo: PartyInfo
):Serializable

data class Subcontractor(
        val id: Int,
        val partyInfo: PartyInfo
):Serializable

data class ContainerReefer(
        val id: Int,
        val nonActiveReeferIndicator: String,
        val reeferContainerTemperature: String,
        val reeferContainerTemperatureUnit: String
):Serializable

data class ContainerReferenceNo(
        val customerLoadReferenceNo: String,
        val id: Int,
        val orderNo: String,
        val vehicleIdentificationNo: String
):Serializable

data class ContainerRemark(
        val canadianCargoControlNo: String,
        val cleanedFlag: String,
        val controlledAtmosphereFlag: String,
        val customsExportDUCR: String,
        val equipmentDescription: String,
        val foodGradeEquipmentFlag: String,
        val fumigationFlag: String,
        val garmentOnHangerFlag: String,
        val gensetFlag: String,
        val heavyWeightFlag: String,
        val humidityFlag: String,
        val id: Int,
        val inTransitColdSterilizationFlag: String,
        val numberOfTemperatureProbes: String,
        val numberOfUSDprobes: String,
        val stowAboveDeckFlag: String,
        val stowBelowDeckFlag: String,
        val superFreezerServiceFlag: String,
        val sweptFlag: String,
        val temperatureControlInstructions: String,
        val temperatureVariance: String,
        val ventClosedFlag: String,
        val ventOpenFlag: String
):Serializable