package com.cyberlogitec.freight9.ui.selloffer

import android.os.Bundle
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.ContractRoute
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.route.RouteGridView
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_route_grid.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofRouteGridVm::class)
class SofRouteGridAct : BaseActivity<SofRouteGridVm>() {

    var routeDataList: RouteDataList = RouteDataList()
    private var isGridView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_route_grid)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {


        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                title = getString(R.string.selloffer_whole_route),
                isEnableNavi=false)
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.masterContractRoutes?.let { routes ->
                        makeRouteData(routes)
                    }
                }
    }

    private fun setListener() {
        // on click toolbar right button
        toolbar_common.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }
    }

    private fun makeRouteData(routes: List<ContractRoute>) {

        val firstPol = routes.sortedBy { it.regSeq }
                .find { it.locationTypeCode == LocationTypeCode.POL }
        val firstPod = routes.sortedBy { it.regSeq }
                .find { it.locationTypeCode == LocationTypeCode.POD }

        val offerRoutesMap = routes
                .sortedBy { offerRoute -> offerRoute.regSeq }
                .groupBy { offerRoute -> offerRoute.regSeq }
        for (data in offerRoutesMap) {
            // data.key : offerRegSeq
            var porCode = ""
            var polCode = ""
            var podCode = ""
            var delCode = ""
            var porCodeName = ""
            var polCodeName = ""
            var podCodeName = ""
            var delCodeName = ""
            for (routeDatas in data.value) {
                when (routeDatas.locationTypeCode) {
                    ConstantTradeOffer.LOCATION_TYPE_CODE_POR -> {
                        porCode = routeDatas.locationCode
                        porCodeName = routeDatas.locationName
                    }
                    ConstantTradeOffer.LOCATION_TYPE_CODE_POL -> {
                        polCode = routeDatas.locationCode
                        polCodeName = routeDatas.locationName
                    }
                    ConstantTradeOffer.LOCATION_TYPE_CODE_POD -> {
                        podCode = routeDatas.locationCode
                        podCodeName = routeDatas.locationName
                    }
                    ConstantTradeOffer.LOCATION_TYPE_CODE_DEL -> {
                        delCode = routeDatas.locationCode
                        delCodeName = routeDatas.locationName
                    }
                }
            }
            routeDataList.add(RouteData(porCode, porCodeName, polCode, polCodeName,
                    podCode, podCodeName, delCode, delCodeName))
        }
        makeGridView(firstPol, firstPod, routeDataList)
    }

    private fun makeGridView(firstPol: ContractRoute?, firstPod: ContractRoute?, routeDataList: RouteDataList) {
        if (isGridView()) return
        gv_whole_route_grid_view.resetView()   //재사용시 view 초기화 시켜줘야함
        gv_whole_route_grid_view.mPol = firstPol?.locationCode ?: "CNSHAL"
        gv_whole_route_grid_view.mPod = firstPod?.locationCode ?: "CNSHAD"
        gv_whole_route_grid_view.mViewType = RouteGridView.GridViewType.SELL_OFFER
        gv_whole_route_grid_view.setData(routeDataList)
        setGridView(true)
    }

    private fun isGridView(): Boolean {
        return isGridView
    }

    private fun setGridView(isGridView: Boolean) {
        this.isGridView = isGridView
    }
}