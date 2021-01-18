package com.cyberlogitec.freight9.lib.ui.route

import android.view.View
import java.util.*

class RouteDataList {
    var dataList: ArrayList<RouteData>

    var porList: ArrayList<RouteItemData>
    var polList: ArrayList<RouteItemData>
    var podList: ArrayList<RouteItemData>
    var delList: ArrayList<RouteItemData>


    constructor() {
        this.dataList = ArrayList()
        this.porList = ArrayList()
        this.polList = ArrayList()
        this.podList = ArrayList()
        this.delList = ArrayList()
    }

    fun add(data: RouteData) {
        this.dataList.add(data)
        makeData(data)
    }
    fun clone(): RouteDataList {
        var list = RouteDataList()
        for(data in this.dataList)
            list.add(RouteData(data.porCd,data.porNm,data.polCd,data.polNm,data.podCd,data.podNm,data.delCd,data.delNm))
        return list
    }
    fun clear() {
        this.dataList.clear()
        this.porList.clear()
        this.polList.clear()
        this.podList.clear()
        this.delList.clear()
    }

    private fun makeData(data: RouteData) {
        //mPol pod를 만들어서 적재
        makePors(data)
        makePols(data)
        makePods(data)
        makeDels(data)
    }
    private fun makePors(data: RouteData) {
        var isContain : Boolean = false
        for(por in porList) {
            if(data.porCd.equals(por.code)) {
                isContain = true
                break
            }
        }
        if(!isContain)
            porList.add(RouteItemData(data.porCd, data.porNm, data.polCd))
    }

    private fun makePols(data: RouteData) {
        var isContain : Boolean = false
        for(pol in polList) {
            if(data.polCd.equals(pol.code)) {
                isContain = true
                break
            }
        }
        if(!isContain)
            polList.add(RouteItemData(data.polCd,data.polNm,data.polCd))
    }
    private fun makePods(data: RouteData) {
        var isContain : Boolean = false
        for(pod in podList) {
            if(data.podCd.equals(pod.code)) {
                isContain = true
                break
            }
        }
        if(!isContain)
            podList.add(RouteItemData(data.podCd,data.podNm,data.podCd))
    }
    private fun makeDels(data: RouteData) {
        var isContain : Boolean = false
        for(del in delList) {
            if(data.delCd.equals(del.code)) {
                isContain = true
                break
            }
        }
        if(!isContain)
            delList.add(RouteItemData(data.delCd,data.delNm,data.podCd))
    }


    fun getPors(parentCode: String): ArrayList<RouteItemData> {
        var list:ArrayList<RouteItemData> = ArrayList()

        for(data in porList) {
            if(dataList.filter{ it.polCd == parentCode}.any { it.porCd == data.code })
                list.add(data)
        }
        return list
    }
    fun getPols(code: String): ArrayList<RouteItemData> {
        var list:ArrayList<RouteItemData> = ArrayList()

        for(data in polList) {
            if(data.code.equals(code))
                list.add(data)
        }
        return list
    }

    fun getPods(code: String): ArrayList<RouteItemData> {
        var list:ArrayList<RouteItemData> = ArrayList()

        for(data in podList) {
            if(data.code.equals(code))
                list.add(data)
        }
        return list
    }
    fun getDels(parentCode: String): ArrayList<RouteItemData> {
        var list:ArrayList<RouteItemData> = ArrayList()

        for(data in delList) {
            if(dataList.filter{ it.podCd == parentCode}.any { it.delCd == data.code })
                list.add(data)
        }
        return list
    }
    fun getPor(code: String): List<RouteItemData> {
        return porList.filter { it.code == code }
    }
    fun getPol(code: String): List<RouteItemData> {
        return polList.filter { it.code == code }
    }
    fun getPod(code: String): List<RouteItemData> {
        return podList.filter { it.code == code }
    }

    fun getDel(code: String): List<RouteItemData> {

        return delList.filter { it.code == code }

    }


    inner class RouteItemData (
            var code: String? = "",
            var name: String? = "",
            var parentCode: String? = "") {

        var view: View? = null

    }
}
