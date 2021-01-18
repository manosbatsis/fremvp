package com.cyberlogitec.freight9.lib.ui.route


class RouteData(     //mPor -> mPol(port) -> mPod(port) -> del  mPod,mPol
                 val porCd: String?,
                 val porNm: String?,
                 val polCd: String?,
                 val polNm: String?,
                 val podCd: String?,
                 val podNm: String?,
                 val delCd: String?,
                 val delNm: String?) {

    override fun toString(): String {
        return "$porCd/$porNm/$polCd/$polNm/$podCd/$podNm/$delCd/$delNm"
    }
}
