package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Commodity(
        val commodityContainer: List<CommodityContainer>,
        val commodityDangerous: List<CommodityDangerou>,
        val commodityMeasurement: CommodityMeasurement,
        val commodityNumberOfPackages: String,
        val commodityReferenceNo: CommodityReferenceNo,
        val commoditySequenceNumber: String,
        val goodsDescription: String,
        val harmonizedSystemCode: String,
        val id: Int,
        val packageTypeCode: String,
        val packageTypeDescription: String,
        val scheduleBnumber: String
): Serializable

data class CommodityContainer(
        val commodityContainerNumber: String,
        val commodityContainerNumberOfPackages: String,
        val commodityContainerVolume: String,
        val commodityContainerVolumeUnit: String,
        val commodityContainerWeight: String,
        val commodityContainerWeightUnit: String,
        val id: Int
):Serializable

data class CommodityDangerou(
        val concentrationOfAcid: String,
        val dangerousContainerNetVolume: String,
        val dangerousContainerNetVolumeUnit: String,
        val dangerousContainerNetWeight: String,
        val dangerousContainerNetWeightUnit: String,
        val dangerousContainerNumber: String,
        val dangerousGoodsAdditionalInfo: String,
        val dangerousGoodsGasFlag: String,
        val dangerousGoodsInhalantHazardFlag: String,
        val dangerousGoodsLiquidFlag: String,
        val dangerousGoodsMarinePollutantFlag: String,
        val dangerousGoodsNetVolume: String,
        val dangerousGoodsNetVolumeUnit: String,
        val dangerousGoodsNetWeight: String,
        val dangerousGoodsNetWeightUnit: String,
        val dangerousGoodsNonMarinePollutantFlag: String,
        val dangerousGoodsSevereMarinePollutantFlag: String,
        val dangerousGoodsSolidFlag: String,
        val dangerousNumberOfPackages: String,
        val emergencyContactName: String,
        val emergencyContactTelephone: String,
        val emsNumber: String,
        val flashPoint: String,
        val flashPointUnit: String,
        val handlingInstructions: String,
        val hazdousMaterialPlacard: String,
        val id: Int,
        val imdgCode: String,
        val imdgSubCode: String,
        val imdgVersion: String,
        val limitedQuantitiesFlag: String,
        val packagingInfo: String,
        val packingGroupCode: String,
        val properShippingName: String,
        val radiactiveGoodsAdditionalInfo: String,
        val radioactivity: String,
        val regulatoryInfo: String,
        val technicalName: String,
        val transportEmergencyCardNumber: String,
        val undgNo: String
):Serializable

data class CommodityMeasurement(
        val commodityGrossVolume: String,
        val commodityGrossVolumeUnit: String,
        val commodityGrossWeight: String,
        val commodityGrossWeightUnit: String,
        val id: Int
):Serializable

data class CommodityReferenceNo(
        val canadianCargoControlNo: String,
        val customsExportDUCR: String,
        val exportLicenseExpiryDate: String,
        val exportLicenseIssueDate: String,
        val exportLicenseNo: String,
        val id: Int,
        val orderNo: String,
        val stockKeepingUnitNo: String,
        val vehicleIdentificationNo: String
):Serializable