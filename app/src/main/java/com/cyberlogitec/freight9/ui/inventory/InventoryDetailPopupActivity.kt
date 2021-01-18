package com.cyberlogitec.freight9.ui.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.InventoryDetails
import com.cyberlogitec.freight9.lib.model.MasterContractWithInventory
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_ALL
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_INVENTORY
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.split.*
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.inventory.chartview.DecoView
import com.cyberlogitec.freight9.ui.inventory.chartview.charts.SeriesItem
import com.cyberlogitec.freight9.ui.inventory.chartview.events.DecoEvent
import com.cyberlogitec.freight9.ui.inventory.tooltipview.ClosePolicy
import com.cyberlogitec.freight9.ui.inventory.tooltipview.Tooltip
import com.cyberlogitec.freight9.ui.trademarket.MarketCounterOfferListFragment
import com.cyberlogitec.freight9.ui.trademarket.MarketOfferPreviewFragment
import com.cyberlogitec.freight9.ui.trademarket.MarketOfferRouteFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.popup_inventory_detail.*
import kotlinx.android.synthetic.main.popup_inventory_detail_jump.view.*
import kotlinx.android.synthetic.main.split_market_popup.*
import kotlinx.android.synthetic.main.split_popup_drag_top.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs


@RequiresActivityViewModel(value = InventoryViewModel::class)
class InventoryDetailPopupActivity : BaseActivity<InventoryViewModel>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private lateinit var masterContractWithInventory: MasterContractWithInventory
    private lateinit var inventoryDetail: InventoryDetails.InventoryDetail

    private lateinit var mPrevFrag: MarketOfferPreviewFragment
    private lateinit var splitPopup: SplitPopup

    private var inStockAmt: Float = 0.0F
    private var onMarketAmt: Float = 0.0F
    private var bookedAmt: Float = 0.0F       // TODO : 아직 없음
    private var soldAmt: Float = 0.0F
    private var expiredAmt: Float = 0.0F

    private var splitDisplayCategory = SplitDisplayCategory.TradeMarketCounterOfferList
    private var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    private var isExpiredAmt = false

    companion object {
        const val INVENTORY_DETAILS = "inventory_details"
        const val INVENTORY_DETAIL = "inventory_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.popup_inventory_detail)
        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        currencyFormat.minimumFractionDigits = 0
        masterContractWithInventory = intent.getSerializableExtra(INVENTORY_DETAILS) as MasterContractWithInventory
        inventoryDetail = intent.getSerializableExtra(INVENTORY_DETAIL) as InventoryDetails.InventoryDetail

        //isExpiredAmt = inventoryDetail.remainderQty!!.toInt() == 0
        isExpiredAmt = inventoryDetail.baseYearWeek!! < getTodayYearWeekNumber()
        if (!isExpiredAmt) {
            initSplitPopupLayout()
        }

        registViewModelOutputs()
        initView()
        setData()
        setListener()
    }

    /**
     * bottom sheet init
     */
    private fun initSplitPopupLayout() {
        initSplitPopup(SplitDisplayCategory.TradeMarketCounterOfferList, SplitConst.SPLIT_UI_HALF_EXPANDED, getString(R.string.counter_offers))

        mPrevFrag = MarketOfferPreviewFragment.newInstance(viewModel)
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_prev, mPrevFrag)
                .commit()
        supportFragmentManager.beginTransaction().replace(R.id.ll_container_body, MarketCounterOfferListFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_route, MarketOfferRouteFragment.newInstance(viewModel))
                .commit()
    }

    private fun initSplitPopup(splitDisplayCategory: SplitDisplayCategory, isCollapsed: Int, title: String) {
        //bottom_sheet_frameout_popup.visibility = View.GONE
        this.splitDisplayCategory = splitDisplayCategory
        splitPopup = SplitPopup(SplitUiData(this, splitDisplayCategory, bottom_sheet_frameout_popup, isCollapsed, title),
                ::receiveSplitPopupViewEvent)
        val dm: DisplayMetrics = applicationContext.resources.displayMetrics
        splitPopup.setHalfExpandRatio(SplitConst.SPLIT_TITLE_HEIGHT_80.toPx().toFloat()/dm.heightPixels)
    }

    private fun changeSplitPopupTitle(displayCategory: SplitDisplayCategory) {

        splitPopup.changeTitle(displayCategory)
    }

    private fun receiveSplitPopupViewEvent(splitUiReceiveData: SplitUiReceiveData) {
        when(splitUiReceiveData.state) {
            BottomSheetBehavior.STATE_DRAGGING -> {
                tooltip?.dismiss()
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                setToolTip()
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                if (getBottomState() != BottomSheetBehavior.STATE_COLLAPSED) {
                    setToolTip()
                }
            }
        }
        setBottomState(splitUiReceiveData.state)
    }

    private fun getBottomState() = bottomSheetState

    private fun setBottomState(state: Int) {
        if (state == BottomSheetBehavior.STATE_SETTLING
                || state == BottomSheetBehavior.STATE_DRAGGING) {
            return
        }
        bottomSheetState = state
    }

    /**
     * bottom sheet 에서 close(X) 처리
     */
    private fun changeBackSplitPopup() {
        if(ll_container_body.visibility == View.INVISIBLE) {
            ll_container_body_prev.visibility=View.INVISIBLE
            ll_container_body.visibility=View.VISIBLE
            ll_container_body_route.visibility=View.INVISIBLE
            if(splitPopup.bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                initSplitPopup(SplitDisplayCategory.TradeMarketCounterOfferList, SplitConst.SPLIT_UI_ZERO, "")
            }
        } else {
            initSplitPopup(SplitDisplayCategory.TradeMarketCounterOfferList, SplitConst.SPLIT_UI_ZERO, "")
        }
    }

    private var standbyTooltipRelease = false
    override fun onBackPressed() {
        if (!removeTooltip()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removePopup()
    }

    /**
     * remove tooltip ui
     */
    private fun removeTooltip() : Boolean {
        var removedTooltip = false
        if (tooltip != null) {
            // tooltip 이 해제된 후 finish 하도록 한다
            standbyTooltipRelease = true
            tooltip?.dismiss()
            removedTooltip = true
        }
        return removedTooltip
    }

    /**
     * View model 의 output interface 처리
     */
    private fun registViewModelOutputs() {

        /**
         * 다른 menu 로 이동 시 화면 finish
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

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when (parameterClick) {
                            ParameterClick.CLICK_FINISH -> {
                                removeTooltip()
                                finish()
                            }
                            ParameterClick.CLICK_CLOSE -> {
                                changeBackSplitPopup()
                            }
                            else -> {  }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                ParameterAny.ANY_JUMP_TO_OTHERS -> {
                                    val goto = second as Goto
                                    showInventoryDetailJumpPopup(goto)
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        viewModel.outPuts.viewSplitPopupDetail()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: viewSplitPopupDetail")
                    ll_container_body_prev.visibility = View.VISIBLE
                    ll_container_body.visibility = View.INVISIBLE
                    ll_container_body_route.visibility = View.INVISIBLE
                    changeSplitPopupTitle(SplitDisplayCategory.TradeMarketOfferPrev)
                }

        viewModel.outPuts.viewSplitPopupRoute()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: viewSplitPopupRoute")
                    ll_container_body_prev.visibility = View.INVISIBLE
                    ll_container_body.visibility = View.INVISIBLE
                    ll_container_body_route.visibility = View.VISIBLE
                    changeSplitPopupTitle(SplitDisplayCategory.TradeMarketOfferRoute)
                }
    }

    /**
     * activity view init
     */
    private fun initView() {

    }

    /**
     * activity data init, set listener
     */
    private fun setData() {
        with(inventoryDetail) {

            // In-Stock, on Market, Booked, Sold, Expired UI에 표시될 수량 계산
            inStockAmt = if (isExpiredAmt) 0.0F else remainderConfirmedQty ?: 0.0F
            onMarketAmt = remainderOnMarketQty ?: 0.0F
            soldAmt = accumulatedOutOnMarketQty ?: 0.0F
            expiredAmt = (remainderConfirmedQty ?: 0.0F) + onMarketAmt

            tv_inventory_detail_popup_title.text = getWeekFull(baseYearWeek)
            // TODO : 아직 값 없음
            val f9MarketValue = 0.0F
            // TODO : 아직 값 없음
            val unitCostValue = 0.0F
            tv_inventory_detail_popup_desc_f9_value.text = currencyFormat.format(f9MarketValue.toInt())
            tv_inventory_detail_popup_desc_cost_value.text = currencyFormat.format(unitCostValue.toInt())
            val diffValue = (f9MarketValue - unitCostValue).toInt()
            var diffSymbol = if (diffValue < 0) "- " else "+ "
            val diffAbsValue = abs(diffValue)
            if (diffValue > 0F) { diffSymbol = "+" }
            tv_inventory_detail_popup_desc_f9_value_diff.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"

            // 이미 만료된 물량
            var resourceStringId = R.string.your_inventory_on_market
            if (isExpiredAmt) {
                tv_inventory_detail_popup_desc_f9_value.text = "-"
                tv_inventory_detail_popup_desc_f9_value_diff.visibility = View.GONE
                resourceStringId = R.string.your_inventory_expired
            }
            tv_inventory_detail_popup_graph_index_on_market_expired.text = getString(resourceStringId)
            tv_inventory_detail_popup_buttons_onmarket_expired_title.text = getString(resourceStringId)

            tv_inventory_detail_popup_buttons_instock.text = getConvertedTeuValue(inStockAmt.toInt(), true, false)
            tv_inventory_detail_popup_buttons_onmarket_expired.text =
                    if (isExpiredAmt) getConvertedTeuValue(expiredAmt.toInt(), true, false)
                    else getConvertedTeuValue(onMarketAmt.toInt(), true, false)
            tv_inventory_detail_popup_buttons_booked.text = getConvertedTeuValue(bookedAmt.toInt(), true, false)
            tv_inventory_detail_popup_buttons_sold.text = getConvertedTeuValue(soldAmt.toInt(), true, false)
        }
        setGraph()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        fl_inventory_detail_popup.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_FINISH)
        }

        cv_inventory_detail_popup.setSafeOnClickListener {
            // Do nothing
        }

        cv_inventory_detail_buttons_instock.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_IN_STOCK))
        }

        cv_inventory_detail_buttons_onmarket_expired.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS,
                    if (isExpiredAmt) Goto.GO_EXPIRED else Goto.GO_ON_MARKET))
        }

        cv_inventory_detail_buttons_booked.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_BOOKED))
        }

        cv_inventory_detail_buttons_sold.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_SOLD))
        }

        btn_split_popup_close.setOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CLOSE)
        }
    }

    /**
     * circular graph init & set
     */
    private fun setGraph() {
        createTracks()
        setupEvents()
    }

    private var mSeriesSold: Int = 0
    private var mSeriesBooked: Int = 0
    private var mSeriesOnMarketExpired: Int = 0
    private var mSeriesInStock: Int = 0
    private var mBackIndexPie: Int = 0

    private val colorSold = Color.parseColor("#f6f2fd")
    private val colorBooked = Color.parseColor("#d2bff7")
    private val colorOnMarketExpired = Color.parseColor("#a47fef")
    private val colorInStock = Color.parseColor("#4a00e0")
    private val colorBackground = Color.parseColor("#ffffff")

    /**
     * circular graph 생성
     */
    private fun createTracks() {

        val decoView =  dv_inventory_detail as DecoView

        decoView.executeReset()
        decoView.deleteAll()
        // rotateAngle = 0 : 상단에서 시작, rotateAngle = 180 : 하단에서 시작
        decoView.configureAngles(360, 0)

        var mSeriesMaxPie = if (isExpiredAmt) {
            inStockAmt + expiredAmt + bookedAmt + soldAmt
        } else {
            inStockAmt + onMarketAmt + bookedAmt + soldAmt
        }
        // 예외처리 추가
        mSeriesMaxPie = if(mSeriesMaxPie == 0.0F) 0.1F else mSeriesMaxPie
        Timber.d("f9: mSeriesMaxPie : $mSeriesMaxPie")

        val circleInset = 15.toDp()

        val seriesBack = SeriesItem.Builder(colorBackground)
                .setRange(0.0F, mSeriesMaxPie, mSeriesMaxPie)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_PIE)
                .setInset(PointF(circleInset, circleInset))
                .build()
        mBackIndexPie = decoView.addSeries(seriesBack)

        val seriesSold = SeriesItem.Builder(colorSold)
                .setRange(0.0F, mSeriesMaxPie, 0.0F)
                .setInitialVisibility(false)
                .setLineWidth(3.toDp())
                .setCapRounded(false)
                .setShowPointWhenEmpty(false)
                .build()
        mSeriesSold = decoView.addSeries(seriesSold)

        val seriesBooked = SeriesItem.Builder(colorBooked)
                .setRange(0.0F, mSeriesMaxPie, 0.0F)
                .setInitialVisibility(false)
                .setLineWidth(5.toDp())
                .setCapRounded(false)
                .setShowPointWhenEmpty(false)
                .build()
        mSeriesBooked = decoView.addSeries(seriesBooked)

        val seriesOnMarketExpired = SeriesItem.Builder(colorOnMarketExpired)
                .setRange(0.0F, mSeriesMaxPie, 0.0F)
                .setInitialVisibility(false)
                .setLineWidth(10.toDp())
                .setCapRounded(false)
                .setShowPointWhenEmpty(false)
                .build()
        mSeriesOnMarketExpired = decoView.addSeries(seriesOnMarketExpired)

        val seriesInStock = SeriesItem.Builder(colorInStock)
                .setRange(0.0F, mSeriesMaxPie, 0.0F)
                .setInitialVisibility(false)
                .setLineWidth(15.toDp())
                .setCapRounded(false)
                .setShowPointWhenEmpty(false)
                .build()
        mSeriesInStock = decoView.addSeries(seriesInStock)
    }

    /**
     * circular graph view setup (index 별)
     */
    private fun setupEvents() {
        val decoView =  dv_inventory_detail as DecoView
        check(!(decoView.isEmpty)) { "Unable to add events to empty View" }

        decoView.executeReset()

        with(inventoryDetail) {
            //------------------------------------------------------------------------------------------
            // sold 가 soldAmt 만큼 이동
            //------------------------------------------------------------------------------------------
            addAnimation(decoView, mSeriesSold, soldAmt, 1000, colorBackground)

            //------------------------------------------------------------------------------------------
            // booked 가 bookedAmt 만큼 이동
            //------------------------------------------------------------------------------------------
            addAnimation(decoView, mSeriesBooked, bookedAmt, 1000, colorBackground)
            // sold 는 bookedAmt+soldAmt 만큼 이동
            decoView.addEvent(DecoEvent.Builder(bookedAmt + soldAmt)
                    .setIndex(mSeriesSold)
                    .setDelay(1000)
                    .setDuration(1000)
                    .build())

            //------------------------------------------------------------------------------------------
            // on market 이 onMarketOrExpiredAmt 만큼 이동
            //------------------------------------------------------------------------------------------
            val onMarketOrExpiredAmt = if (isExpiredAmt) expiredAmt else onMarketAmt
            addAnimation(decoView, mSeriesOnMarketExpired, onMarketOrExpiredAmt, 1000, colorBackground)
            // booked 는 on market + booked 만큼 이동
            decoView.addEvent(DecoEvent.Builder(onMarketOrExpiredAmt + bookedAmt)
                    .setIndex(mSeriesBooked)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build())
            // sold 는 on market + booked + sold 만큼 이동
            decoView.addEvent(DecoEvent.Builder(onMarketOrExpiredAmt + bookedAmt + soldAmt)
                    .setIndex(mSeriesSold)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build())

            //------------------------------------------------------------------------------------------
            // In stock 이 inStockAmt 만큼 이동
            //------------------------------------------------------------------------------------------
            addAnimation(decoView, mSeriesInStock, inStockAmt, 1000, colorBackground)
            // on market 은 in stock + on market 만큼 이동
            decoView.addEvent(DecoEvent.Builder(inStockAmt + onMarketOrExpiredAmt)
                    .setIndex(mSeriesOnMarketExpired)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build())
            // booked 는 In stock + on market + booked 만큼 이동
            decoView.addEvent(DecoEvent.Builder(inStockAmt + onMarketOrExpiredAmt + bookedAmt)
                    .setIndex(mSeriesBooked)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build())
            // sold 는 In stock + on market + booked + sold 만큼 이동
            decoView.addEvent(DecoEvent.Builder(inStockAmt + onMarketOrExpiredAmt + bookedAmt + soldAmt)
                    .setIndex(mSeriesSold)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build())
        }
        //------------------------------------------------------------------------------------------
        // END
        decoView.addEvent(DecoEvent.Builder(DecoEvent.EventType.EVENT_COLOR_CHANGE, colorBackground)
                .setIndex(mBackIndexPie)
                .setDelay(1000)
                .setDuration(1000)
                .setListener(object : DecoEvent.ExecuteEventListener {
                    override fun onEventStart(event: DecoEvent) {}
                    override fun onEventEnd(event: DecoEvent) {
                        if (!isExpiredAmt) setToolTip()
                    }
                })
                .build())
    }

    /**
     * circular graph andmation 처리
     */
    private fun addAnimation(arcView: DecoView, series: Int, moveTo: Float, delay: Long, color: Int) {
        val listener = object : DecoEvent.ExecuteEventListener {
            override fun onEventStart(event: DecoEvent) {
                arcView.addEvent(DecoEvent.Builder(DecoEvent.EventType.EVENT_COLOR_CHANGE, color)
                        .setIndex(mBackIndexPie)
                        .setDuration(1000)
                        .build())
            }
            override fun onEventEnd(event: DecoEvent) {
                arcView.addEvent(DecoEvent.Builder(DecoEvent.EventType.EVENT_COLOR_CHANGE, color)
                        .setIndex(mBackIndexPie)
                        .setDuration(1000)
                        .build())
            }
        }
        arcView.addEvent(DecoEvent.Builder(moveTo)
                .setIndex(series)
                .setDelay(delay)
                .setDuration(1000)
                .setListener(listener)
                .build())
    }

    /**
     * "Booking here" tooltip ui 설정 및 표시
     */
    private var tooltip: Tooltip? = null
    private fun setToolTip() {
        tooltip?.dismiss()
        cv_inventory_detail_buttons_instock.post {
            tooltip = Tooltip.Builder(this)
                    .anchor(cv_inventory_detail_buttons_instock, 0, -3, true)
                    .closePolicy(ClosePolicy.TOUCH_NONE)
                    .text(getString(R.string.your_inventory_booking_here))
                    .overlay(false)
                    .create()
            tooltip
                    ?.doOnTouch { clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, Goto.GO_IN_STOCK)) }
                    ?.doOnPrepare { }
                    ?.doOnHidden {
                        tooltip = null
                        if (standbyTooltipRelease) finish()
                    }
                    ?.doOnFailure { }
                    ?.doOnShown { standbyTooltipRelease = false }
                    ?.show(rl_popup_inventory_detail, Tooltip.Gravity.TOP, false)
        }
    }

    /**
     * 다른 메뉴로 이동하기 위한 popup show
     */
    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showInventoryDetailJumpPopup(goto: Goto) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPopup = inflater.inflate(R.layout.popup_inventory_detail_jump, null)
        popupWindow = InventoryDetailJumpPopup(viewPopup, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true, viewModel)
        (popupWindow as InventoryDetailJumpPopup).initValue(masterContractWithInventory, inventoryDetail, goto)
        (popupWindow as InventoryDetailJumpPopup).show(viewPopup.rl_popup_inventory_detail_jump)
        (popupWindow as InventoryDetailJumpPopup).isOutsideTouchable = true
        (popupWindow as InventoryDetailJumpPopup).setOnDismissListener { removePopup() }
        viewPopup.iv_inventory_detail_jump_popup_close.setOnClickListener { removePopup() }
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    enum class Goto {
        GO_IN_STOCK,
        GO_ON_MARKET,
        GO_EXPIRED,
        GO_BOOKED,
        GO_SOLD
    }
}

