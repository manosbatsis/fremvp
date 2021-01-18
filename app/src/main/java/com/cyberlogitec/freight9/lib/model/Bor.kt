package com.cyberlogitec.freight9.lib.model

import com.cyberlogitec.freight9.lib.apistat.PostServiceRouteResponse
import com.cyberlogitec.freight9.lib.apitrade.PostServiceRouteRequest
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList

class Bor {
    var item: BorList
    lateinit var detailList: OrderTradeOfferDetail
    var routeList: List<PostServiceRouteResponse>


    constructor() {
        this.item = BorList()
        this.routeList = ArrayList()
    }

    fun getWeekPriceVolumeList(): ArrayList<OfferWeekVolume> {
        var weekList: ArrayList<OfferWeekVolume> = ArrayList()
        //week로 정렬
        if(!::detailList.isInitialized)
            return weekList

        for(data in detailList.offerLineItems.sortedBy { it.baseYearWeek }) {
            var item = OfferWeekVolume()
            item.price = data.offerPrice
            item.period = data.baseYearWeek
            item.volume = data.offerRemainderQty

            weekList.add(item)
        }


        return weekList
    }

    fun getServiceRouteReques(): PostServiceRouteRequest {
        lateinit var route:PostServiceRouteRequest

        var data = detailList
        if(null != data){
            route = PostServiceRouteRequest("","", item.locPolCd, item.locPodCd)

        }
        return route

    }
    fun getRouteList(): RouteDataList {
        var list = RouteDataList()

        var max = detailList.offerRoutes.maxBy { it.offerRegSeq }

        for(x in 1 ..(max?.offerRegSeq ?: 0 )){
            var porcode = ""
            var polcode = ""
            var podcode = ""
            var delcode = ""
            var porName = ""
            var polName = ""
            var podName = ""
            var delName = ""

            for(data in detailList.offerRoutes.filter { it.offerRegSeq == x }){
                when(data.locationTypeCode){
                    "01" -> {porcode = data.locationCode
                        porName = data.locationName}
                    "02" -> {polcode = data.locationCode
                        polName = data.locationName}
                    "03" -> {podcode = data.locationCode
                        podName = data.locationName}
                    "04" -> {delcode = data.locationCode
                        delName = data.locationName}
                }
            }

            list.add(RouteData(porcode, porName, polcode, polName, podcode, podName, delcode, delName))

        }

        /*for(data in checkNotNull(detailList.offerRoutes)){
        }*/

        return list

    }


    inner class OfferWeekVolume (
            var period: String = "",
            var price: Float = 0f,
            var volume: Int = 0
    )
}
