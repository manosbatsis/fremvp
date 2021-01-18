package com.cyberlogitec.freight9.ui.inventory

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_ALL
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_INVENTORY
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.rx.RxBus
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.inventory.InventoryDetailPopupActivity.Companion.INVENTORY_DETAIL
import com.cyberlogitec.freight9.ui.inventory.InventoryDetailPopupActivity.Companion.INVENTORY_DETAILS
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_inventory_detail.*
import kotlinx.android.synthetic.main.appbar_inventory_detail.*
import kotlinx.android.synthetic.main.body_inventory_detail.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.item_inventory_detail.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_inventory.view.*
import retrofit2.Response
import timber.log.Timber
import java.io.Serializable
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = InventoryViewModel::class)
class InventoryDetailActivity : BaseActivity<InventoryViewModel>() {

    private var selectedPolIndex = 0
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    // default : inStockAmt == 0F 인 항목 안보여줌
    private var isHidePeriodThatOwnedVolumeIs0 = true

    // RouteFilterActivity 로 부터 전달받은 inventoryList
    private lateinit var inventoryList: InventoryList
    private lateinit var masterContractWithInventory: MasterContractWithInventory

    /**
     * inventory detail adapter
     */
    private val inventoryDetailAdapter by lazy {
        InventoryDetailRecyclerAdapter()
                .apply {
                    onClickItem = {
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_OBJECT, it))
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_inventory_detail)
        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_inventory, isEnableNavi = true, menuType = MenuType.DEFAULT, title = getString(R.string.your_inventory_title))

        registViewModelOutputs()
        initView()
        initData()
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    /**
     * View model 의 output interface 처리
     */
    private fun registViewModelOutputs() {

        /**
         * inventory jump popup 에서 다른 menu 로 이동 시 inventory 진입 화면 finish
         */
        viewModel.rxEventFinish
                .bindToLifecycle(this)
                .subscribe { finishKind ->
                    Timber.d("f9: rxEventFinish = $finishKind")
                    if (finishKind == FINISH_INVENTORY || finishKind == FINISH_ALL) finish()
                }

        /**
         * not implementation
         * internet 연결 유무에 따른 처리
         */
        viewModel.rxEventInternet
                .bindToLifecycle(this)
                .subscribe { isConnectedToInternet ->
                    Timber.d("f9: rxEventInternet = $isConnectedToInternet")
                }

        /**
         * not implementation
         * network status에 따른 처리
         */
        viewModel.rxEventNetwork
                .bindToLifecycle(this)
                .subscribe { networkStatus ->
                    Timber.d("f9: rxEventNetwork = ${networkStatus.toJson()}")
                }

        /**
         * onResume 시 refresh
         */
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        // inventoryList 요청 : load from intent
                        requestInventory()
                    }
                }

        viewModel.outPuts.onSuccessRequestInventory()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        inventoryList = it
                        // requestMasterContract 은 viewModel 에서 호출됨
                    }
                }

        viewModel.outPuts.onSuccessRequestMasterContractWithInventory()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        processMasterContractWithInventory(it)
                    }
                }

        viewModel.outPuts.onSuccessRequestPortNm()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        tv_inventory_detail_change_pol_name.text = it
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        if (::masterContractWithInventory.isInitialized) {
                            when (parameterClick) {
                                ParameterClick.CLICK_CONDITION_DETAIL -> {
                                    showDetailConditionPopup(root_inventory_detail, viewModel, masterContractWithInventory, false, true)
                                }
                                ParameterClick.CLICK_WHOLE_ROUTE -> {
                                    val routeDataList = makeRouteDataList(masterContractWithInventory.masterContractRoutes)
                                    val borList = BorList()
                                    borList.locPolCd = inventoryList.polCode
                                    borList.locPolNm = inventoryList.polName
                                    borList.locPodCd = inventoryList.podCode
                                    borList.locPodNm = inventoryList.podName
                                    showWholeRoutePopup(root_inventory_detail, routeDataList, borList)
                                }
                                ParameterClick.CLICK_PRICE_TABLE -> {
                                    showPriceTablePopup(root_inventory_detail, masterContractWithInventory, false)
                                }
                                ParameterClick.CLICK_INVENTORY_VALUATION -> {
                                    showInventoryValuationPopup(root_inventory_detail, inventoryDetailList)
                                }
                                ParameterClick.CLICK_TITLE_LEFT -> {
                                    Timber.d("f9: toolbar_left_btn clcick")
                                    onBackPressed()
                                }
                                ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                    Timber.d("f9: startActivity(MenuActivity)")
                                    startMenuActivityWithFinish(MenuItem.MENUITEM_YOUR_INVENTORY, MenuActivity::class.java)
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                ParameterAny.ANY_VIEW_CHK -> {
                                    val isChecked = second as Boolean
                                    isHidePeriodThatOwnedVolumeIs0 = isChecked
                                    setInventoryDetailRecyclerData(isChecked)
                                }
                                ParameterAny.ANY_ITEM_OBJECT -> {
                                    val inventoryDetail = second as InventoryDetails.InventoryDetail
                                    showInventoryDetailPopup(inventoryDetail)
                                }
                                ParameterAny.ANY_JUMP_TO_OTHERS -> {
                                    val goto = second as Goto
                                    when (goto) {
                                        Goto.GO_MORE -> {
                                            showToast("GO_MORE : Pending")
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                Timber.d("f9: TODO sharePermissionCheck()")
                                            } else {
                                                Timber.d("f9: TODO sharePermissionCheck()")
                                            }
                                        }
                                        Goto.GO_MAKE_A_SELL_OFFER -> {
                                            // Has Intents.OFFER extra
                                            RxActivityResult(this@InventoryDetailActivity)
                                                    .start(Intent(this@InventoryDetailActivity, SofWizardActivity::class.java)
                                                            .putExtra(Intents.MSTR_CTRK_NR, masterContractWithInventory.masterContractNumber)
                                                            .putExtra(Intents.OFFER, Offer())
                                                            .putExtra(Intents.OFFER_DISCARD, false)
                                                            .putExtra(Intents.OFFER_BY_MADE_CONDITION, false)
                                                            .putExtra(Intents.OFFER_MAKE_STEP, false))
                                                    .subscribe(
                                                            { result ->
                                                                RxBus.publish(RxBusEvent(RxBusEvent.EVENT_FINISH, FINISH_INVENTORY))
                                                            },
                                                            { throwable ->
                                                                viewModel.error.onNext(throwable)
                                                                finish()
                                                            }
                                                    )
                                        }
                                        Goto.GO_POL_CHANGE -> {
                                            // MVP 에서 제외
                                            //showPolChangeDialog()
                                        }
                                        else -> {  }
                                    }
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        //------------------------------------------------------------------------------------------

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }
    }

    /**
     * activity view init
     */
    private fun initView() {

        // TODO : Init view
        iv_carrier_more.visibility = View.INVISIBLE // For MVP : View.VISIBLE
        //sv_inventory_body_detail_root.scrollTo(0, 0)
    }

    /**
     * activity data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
    }

    /**
     * request inventory item by intent
     */
    private fun requestInventory() = viewModel.inPuts.requestInventory(intent)

//    private fun requestInventoryDetail(inventoryNumber: String, inventoryChangeSeq: Int) = viewModel.inPuts
//            .requestInventoryDetail(Pair(inventoryNumber, inventoryChangeSeq))

    /**
     * request port name
     */
    private fun requestPortNm(locationCode: String) = viewModel.inPuts.requestPortNm(locationCode)

    /**
     * activity data init, set listener
     */
    private fun setData() {
        with(inventoryList) {
            iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
            tv_carrier_name.text = getCarrierCode(carrierCode)

            tv_pol_name.text = polCode
            tv_pol_count.text = polCount.getCodeCount()
            tv_pol_desc.text = polName

            tv_pod_name.text = podCode
            tv_pod_count.text = podCount.getCodeCount()
            tv_pod_desc.text = podName
        }

        // paymentTermCode 에 따른 POL, POD 결정
        val isPPD = masterContractWithInventory.paymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
        val masterContractRoutes = masterContractWithInventory.masterContractRoutes
                .filter { it.regSeq == 1 }
                .sortedBy { it.locationTypeCode }
        // POL, POD 첫번째 항목의 locationCode
        val locationCode = if (isPPD) {
            masterContractRoutes.filter { it.locationTypeCode == ConstantTradeOffer.LOCATION_TYPE_CODE_POL }[0].locationCode
        }
        else {
            masterContractRoutes.filter { it.locationTypeCode == ConstantTradeOffer.LOCATION_TYPE_CODE_POD }[0].locationCode
        }
        tv_inventory_detail_change_pol.text =
                if (isPPD) getString(R.string.buy_order_payplan_pol)
                else getString(R.string.buy_order_payplan_pod)
        tv_inventory_detail_change_pol_code.text = locationCode
        // locationCode 에 대한 Name 요청(DB)
        requestPortNm(locationCode)

        /**
         * TODO : 현재 표시할 값 없음
         */
        val f9MarketValue = 0.0f
        val totalCostValue = 0.0f
        tv_inventory_detail_f9_market_value.text = currencyFormat.format(f9MarketValue.toInt())
        tv_inventory_detail_total_cost_value.text = currencyFormat.format(totalCostValue.toInt())
        val diffValue = (f9MarketValue - totalCostValue).toInt()
        var diffSymbol = if (diffValue < 0) "- " else "+ "
        val diffAbsValue = Math.abs(diffValue)
        if (diffValue > 0F) {
            diffSymbol = "+"
        }
        tv_inventory_detail_estimate_profit_value.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"

        setListener()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        //------------------------------------------------------------------------------------------
        // wait click event (toolbar left button)
        toolbar_inventory.toolbar_left_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
        }

        //------------------------------------------------------------------------------------------
        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_inventory.toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        //------------------------------------------------------------------------------------------
        // MORE button
        iv_carrier_more.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_MORE))
        }

        //------------------------------------------------------------------------------------------
        // MAKE A SELL OFFER button
        btn_inventory_detail_make_sell_offer.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_MAKE_A_SELL_OFFER))
        }

        iv_inventory_detail_pol_change.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_POL_CHANGE))
        }

        //------------------------------------------------------------------------------------------
        // view detail
        tv_inventory_detail_view_detail.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_INVENTORY_VALUATION)
        }

        //------------------------------------------------------------------------------------------
        // detail condition / whole route / price table : Show Popup
        tv_link_condition_detail.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CONDITION_DETAIL)
        }

        tv_link_whole_route.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_WHOLE_ROUTE)
        }

        tv_price_table.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_PRICE_TABLE)
        }

        //------------------------------------------------------------------------------------------
        // detail condition / whole route / price table : Show Popup
        chk_inventory_detail_hide.setOnCheckedChangeListener { _, isChecked ->
            clickViewParameterAny(Pair(ParameterAny.ANY_VIEW_CHK, isChecked))
        }
    }

    /**
     * inventory detail popup : Popup per yearweek
     */
    private fun showInventoryDetailPopup(inventoryDetail: InventoryDetails.InventoryDetail) {
        startActivity(Intent(this, InventoryDetailPopupActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(INVENTORY_DETAILS, masterContractWithInventory)
                .putExtra(INVENTORY_DETAIL, inventoryDetail)
        )
    }

    /**
     * not used
     */
    @SuppressLint("InflateParams")
    private fun showPolChangeDialog() {
        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(false)
        dialog.setContentView(view)

        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                selectedPolIndex = index
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            tv_inventory_detail_change_pol_code.text = inventoryDetailPolChangeList[selectedPolIndex].locPolCd
            tv_inventory_detail_change_pol_name.text = inventoryDetailPolChangeList[selectedPolIndex].locPolName

            // TODO : 변경된 POL 에 대한 setInventoryDetailRecyclerData()

        }

        view.picker.setItems(inventoryPolSpinList)
        view.picker.index = selectedPolIndex

        dialog.show()
    }

    /**
     * ui card 의 "whole route" 선택 시 보여질 popup에 표시될 route data 추출
     */
    private fun makeRouteDataList(masterContractWithInventoryContractRoutes: List<MasterContractWithInventory.MasterContractRoute>)
            : RouteDataList {
        val routeDataList = RouteDataList()
        val routesMap = masterContractWithInventoryContractRoutes
                .sortedBy { it.regSeq }
                .groupBy { it.regSeq }
        for (data in routesMap) {
            // data.key : regSeq
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
        return routeDataList
    }

    /**
     * Inventory detail Recycler view init
     */
    private fun inventoryDetailRecyclerViewInit() {
        recycler_inventory_detail.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(this@InventoryDetailActivity)
            adapter = this@InventoryDetailActivity.inventoryDetailAdapter
        }
        setInventoryDetailRecyclerData()
    }

    /**
     * recycler adapter data 추출 및 inventory detail ui 구성
     * hide 선택 시 detail item 이 과거주차인 경우 안보여준다
     */
    private fun setInventoryDetailRecyclerData(isHidePeriodThatOwnedVolumeIs0: Boolean = true) {
        val textColor = ContextCompat.getColor(this@InventoryDetailActivity,
                if (isHidePeriodThatOwnedVolumeIs0) R.color.color_333333 else R.color.color_c7c7c7)
        tv_view_hide_selected_hide.setTextColor(textColor)
        tv_view_hide_selected_0.setTextColor(textColor)
        tv_view_hide_selected_owned_volume.setTextColor(textColor)

        val typeface = ResourcesCompat.getFont(this@InventoryDetailActivity,
                if (isHidePeriodThatOwnedVolumeIs0) R.font.opensans_bold else R.font.opensans_regular)
        val typefacecenter = ResourcesCompat.getFont(this@InventoryDetailActivity, R.font.opensans_bold)
        tv_view_hide_selected_hide.typeface = typeface
        tv_view_hide_selected_0.typeface = typefacecenter
        tv_view_hide_selected_owned_volume.typeface = typeface

        this.isHidePeriodThatOwnedVolumeIs0 = isHidePeriodThatOwnedVolumeIs0
        var inventoryDetail = masterContractWithInventory
                .inventory
                .inventoryDetails
                ?.sortedBy { it.baseYearWeek }
        inventoryDetail =
                // 현재 주차 이후의 항목들만. 이전 주차는 안보여줌
                if (isHidePeriodThatOwnedVolumeIs0) inventoryDetail!!.filter { it.baseYearWeek!! >= getTodayYearWeekNumber() }
                else inventoryDetail!!

        btn_inventory_detail_make_sell_offer.isEnabled = inventoryDetail.isNotEmpty()

        inventoryDetailAdapter.setData(inventoryDetail)
        inventoryDetailAdapter.notifyDataSetChanged()
    }

    /**
     * Inventory Detail Recycler view adapter : Period, Volume
     */
    private class InventoryDetailRecyclerAdapter : RecyclerView.Adapter<InventoryDetailRecyclerAdapter.ViewHolder>() {

        val datas = mutableListOf<InventoryDetails.InventoryDetail>()
        var onClickItem: (InventoryDetails.InventoryDetail) -> Unit = {}

        fun setData(data: List<InventoryDetails.InventoryDetail>) {
            this.datas.clear()
            this.datas.addAll(data)
        }

//        fun getData(): List<InventoryDetails.InventoryDetail> {
//            return this.datas
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_detail, parent, false))

        override fun getItemCount(): Int {
            return datas.size
        }

        /**
         * Owned : remainderQty + remainderOnMarketQty
         * Total : accumulatedInConfirmedQty
         * ExpiredAmt : 이미 만료된 물량이기 때문에 in stock은 무조건 0, F9 market value 안나옴
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with (datas[position]) {
                    val isExpired = baseYearWeek!! < getTodayYearWeekNumber()
                    tv_inventory_detail_week.text = context.getWeek(baseYearWeek)
                    tv_inventory_detail_weekof.text = baseYearWeek.getBeginDateOfWeekNumber()
                    // Expired 인 경우 inStock = 0.0F
                    val ownedAmt = (if (isExpired) 0.0F else remainderConfirmedQty ?: 0.0F) +
                            (if (isExpired) 0.0F else remainderOnMarketQty ?: 0.0F)
                    val totalAmt = accumulatedInConfirmedQty ?: 0.0F
                    pv_inventory_detail_hgraph.progress = ownedAmt / totalAmt * 100.0F
                    pv_inventory_detail_hgraph.progressAnimate()
                    tv_inventory_detail_volume.text = context.getString(R.string.your_inventory_detail_owned_total_value,
                            context.getConvertedTeuValue(ownedAmt.toInt()),
                            context.getConvertedTeuValue(totalAmt.toInt()))
                    iv_inventory_detail_go_popup.setSafeOnClickListener { onClickItem(this) }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    /**
     * Inventory detail adapter 에서 사용될 data class
     */
    private var inventoryDetailList = mutableListOf<InventoryDetailItem>()
    data class InventoryDetailItem(
            var dataOwnerPointerId: String = "",         // "HLC"
            var inventoryNumber: String = "",                 // "HLC_MST_20190827_1"
            var masterContractNumber: String = "",            // "MST-F9_HLC-201908-01"
            var polCd: String = "",                  // "JPUKB"
            var polName: String = "",                // "KOBE"
            var bseYw: String = "",                  // "201903"
            var bseWeekFmDt: String = "",           // "20190113"
            var f9MarketValue: Float = 0F,
            var unitCostValue: Float = 0F,
            var inStockAmt: Float = 0F,
            var expiredAmt: Float = 0F,
            var onMarketAmt: Float = 0F,
            var bookedAmt: Float = 0F,
            var soldAmt: Float = 0F,
            var counterOffersCnt: Int = 0
    ) : Serializable

    /**
     * not used
     * MVP에서는 사용하지 않음
     * Inventory detail Spin control 에서 사용될 data class
     */
    private var inventoryPolSpinList = mutableListOf<TextItem>()
    private var inventoryDetailPolChangeList = mutableListOf<InventoryPolChange>()
    data class InventoryPolChange(
            var locPolCd: String = "",
            var locPolName: String = ""
    )

    enum class Goto {
        GO_MAKE_A_SELL_OFFER,
        GO_VIEW_DETAIL,
        GO_POL_CHANGE,
        GO_MORE
    }

    /**
     * inventory detail info 설정 및 recycler view init
     */
    private fun processMasterContractWithInventory(response: Response<MasterContractWithInventory>) {
        // InventoryDetailWithMaster
        Timber.d("f9: onSuccessRequestMasterContractWithInventory : it = $response")
        if (response.isSuccessful) {
            masterContractWithInventory = response.body() as MasterContractWithInventory
            setData()
            inventoryDetailRecyclerViewInit()
        } else {
            showToast("Fail get Offer Detail(Http)\n" + response.errorBody())
            finish()
            return
        }
    }
}