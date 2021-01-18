package com.cyberlogitec.freight9.lib.apibooking

import com.cyberlogitec.freight9.lib.model.booking.BookingDashboard
import io.reactivex.Single
import retrofit2.Response

interface APIBookingClientType {


    fun getBookingDashboardList(): Single<Response<BookingDashboard>>
}