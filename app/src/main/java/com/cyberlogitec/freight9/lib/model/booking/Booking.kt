package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Booking(
        val bookingContact: BookingContact,
        val bookingDate: BookingDate,
        val bookingLocation: BookingLocation,
        val bookingReferenceNo: BookingReferenceNo,
        val bookingSpecialCargo: BookingSpecialCargo,
        val deliveryTerm: String,
        val id: Int,
        val messageFunctionCode: String,
        val messageNumber: String = "-",
        val receiveTerm: String
):Serializable

data class BookingContact(
        val contactEmail: String,
        val contactFax: String,
        val contactName: String,
        val contactTelephone: String,
        val id: Int
):Serializable

data class BookingDate(
        val documentDate: String,
        val documentClosingTime: String,
        val earlistDepartureDate: String,
        val id: Int,
        val latestDeliveryDate: String,
        val shipmentDate: String,
        val onBoardDate: String
):Serializable

data class BookingLocation(
        val deliveryLocationCode: String = "",
        val deliveryLocationName: String = "",
        val id: Int,
        val portOfReceiptLocationCode: String = "",
        val portOfReceiptLocationName: String = "",
        val prohibitedTSLocationCode: String = "",
        val prohibitedTSLocationName: String = "",
        val requestedTSLocationCode: String = "",
        val requestedTSLocationName: String = ""
):Serializable

data class BookingReferenceNo(
        val agentReferenceNo: String = "",
        val billOfLadingNo: String = "-",
        val bookingReferenceNo: String = "",
        val consigneeReferenceNo: String = "",
        val contractLineItemNo: String = "",
        val contractNo: String = "",
        val exportReferenceNo: String,
        val exportLicenceNo: String = "",
        val forwarderReferenceNo: String = "",
        val invoiceNo: String,
        val id: Int,
        val mutuallyDefinedReferenceNo: String = "",
        val orderNo: String,
        val shipperIdentifyingNo: String = "",
        val tariffNo: String = "",
        val vehicleIdentificationNo: String = ""
):Serializable

data class BookingSpecialCargo(
        val dangerousCargoIndicator: String,
        val enviromentalPollutantCargoIndicator: String,
        val id: Int,
        val nonContainerizedIndicator: String,
        val reeferCargoIndicator: String
):Serializable