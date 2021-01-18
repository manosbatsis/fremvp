package com.cyberlogitec.freight9.lib.apibooking

import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboard
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET

interface APIBookingService {

    object EndPoint {
        const val baseUrl = BuildConfig.SERVER_BOOKING_URL
        const val wsUrl = BuildConfig.WS_URL
        //fun wsUrl(topicId: Long?) = wsUrl + "ws/chat/" + topicId + "/"
    }
    @GET("api/f9/bkg/dashboard")
    fun getBookingDashboardList()
    : Single<Response<BookingDashboard>>
}