package com.cyberlogitec.freight9.lib.apistat

import com.cyberlogitec.freight9.common.CurrentUser
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

open class APIStatRequestInterceptor(val currentUser: CurrentUser) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response =
            if (currentUser.user?.token != null)
                chain.proceed(reqest(chain.request()))
            else
                chain.proceed(chain.request())

    private fun reqest(request: Request): Request =
            request.newBuilder()
                    // TODO : stat 에서 auth 연동 시  comment 해제
                    //.addHeader("Authorization","Bearer ${currentUser.user?.token}")
                    .addHeader("Content-Type", "application/json")
                    .url(request.url)
                    .build()

}