package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Party(
        val carrierInfo: CarrierInfo,
        val consigneeInfo: ConsigneeInfo,
        val consignorInfo: ConsignorInfo,
        val consolidatorInfo: ConsolidatorInfo,
        val exporterInfo : PartyInfoWrapper,
        val contractPartyInfo : PartyInfoWrapper,
        val freightPayerInfo : PartyInfoWrapper,
        val freightForwarderInfo : PartyInfoWrapper,
        val godsOwnerInfo : PartyInfoWrapper,
        val requestorInfo : PartyInfoWrapper,
        val importerInfo : PartyInfoWrapper,
        val messageRecipientInfo : PartyInfoWrapper,
        val firstNotifyPartyInfo : PartyInfoWrapper,
        val secondNotifyPartyInfo : PartyInfoWrapper,
        val thirdNotifyPartyInfo : PartyInfoWrapper,
        val bookingPartyInfo : PartyInfoWrapper,
        val bookingOfficeInfo : PartyInfoWrapper,
        val CorrespondencePartyInfo : PartyInfoWrapper,
        val shipToInfo : PartyInfoWrapper,
        val supplierInfo : PartyInfoWrapper,
        val billOfLadingRecipientInfo : PartyInfoWrapper,
        val orderOfShipperPartyInfo : PartyInfoWrapper,
        val id: Int
): Serializable

data class CarrierInfo(
        val id: Int,
        val partyCharge: PartyCharge,
        val partyInfo: PartyInfo
):Serializable

data class ConsignorInfo(
        val id: Int,
        val partyCharge: PartyCharge,
        val partyInfo: PartyInfo
):Serializable

data class ConsigneeInfo(
        val id: Int,
        val partyCharge: PartyCharge,
        val partyInfo: PartyInfo
):Serializable

data class ConsolidatorInfo(
        val id: Int,
        val partyCharge: PartyCharge,
        val partyInfo: PartyInfo
):Serializable

data class PartyInfoWrapper(
        val id: Int,
        val partyCharge: PartyCharge,
        val partyInfo: PartyInfo
):Serializable



data class PartyCharge(
        val chargeTypeCode: String,
        val id: Int,
        val partyPaymentLocationCode: String,
        val partyPaymentLocationCountryCode: String,
        val partyPaymentLocationCountryName: String,
        val partyPaymentLocationName: String,
        val paymentLocationState: String,
        val prepaidCollectIndicator: String
):Serializable

data class PartyInfo(
        val id: Int,
        val partyAddress1: String,
        val partyAddress2: String,
        val partyAddress3: String,
        val partyAddress4: String,
        val partyCode: String,
        val partyContactEmail: String,
        val partyContactFax: String,
        val partyContactName: String,
        val partyContactTelephone: String,
        val partyContactType: String,
        val partyCountryCode: String,
        val partyName: String,
        val partyPostalCode: String
):Serializable