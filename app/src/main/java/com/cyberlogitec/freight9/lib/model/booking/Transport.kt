package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Transport(
        val carrierSCAC: String,
        val id: Int,
        val transportDate: TransportDate,
        val transportLocation: TransportLocation,
        val transportMeansCode: String,
        val transportMode: String,
        val transportStageCode: String,
        val vesselCountryCode: String,
        val vesselLloydCode: String,
        val vesselName: String,
        val voyageNumber: String
): Serializable

data class TransportDate(
        val estimatedArrivalDate: String,
        val estimatedDepartureDate: String,
        val id: Int
):Serializable

data class TransportLocation(
        val id: Int,
        val portOfDischargeLocationCode: String,
        val portOfDischargeLocationName: String,
        val portOfLoadingLocationCode: String,
        val portOfLoadingLocationName: String
):Serializable

object TransportStageCode {
    const val TRANSPORT_STAGE_PRE = "10"
    const val TRANSPORT_STAGE_MAIN = "20"
    const val TRANSPORT_STAGE_ON = "30"
}