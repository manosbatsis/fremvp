package com.cyberlogitec.freight9.lib.apibooking

import com.cyberlogitec.freight9.lib.model.booking.BookingDashboard
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboardItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response


class APIBookingClient(val apiBookingService: APIBookingService) : APIBookingClientType {

    override fun getBookingDashboardList()
            : Single<Response<BookingDashboard>> =
            apiBookingService.getBookingDashboardList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
}