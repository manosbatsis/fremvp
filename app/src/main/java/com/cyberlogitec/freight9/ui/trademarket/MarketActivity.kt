package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.BuildConfig.WS_URL
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_TRADE_MARKET
import com.cyberlogitec.freight9.config.StompStatusCode.CONNECTED
import com.cyberlogitec.freight9.config.StompStatusCode.DISCONNECTED
import com.cyberlogitec.freight9.config.StompStatusCode.RECEIVING
import com.cyberlogitec.freight9.lib.apitrade.PostMarketOfferListRequest
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.service.*
import com.cyberlogitec.freight9.lib.ui.split.*
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF_EXPANDED
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.lib.util.Intents.Companion.GOTO_EVENT_SERVICE_RUN
import com.cyberlogitec.freight9.lib.util.Intents.Companion.GOTO_MESSAGE_BOX
import com.cyberlogitec.freight9.lib.util.Intents.Companion.OFFER_ITEM
import com.cyberlogitec.freight9.ui.buyorder.BuyOrderActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.routeselect.select.RouteSelectActivity
import com.cyberlogitec.freight9.ui.sellorder.SellOrderActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.OfferData
import com.github.mikephil.charting.data.OfferDataSet
import com.github.mikephil.charting.data.OfferEntry
import com.github.mikephil.charting.formatter.OfferWeekAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IOfferDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_market.*
import kotlinx.android.synthetic.main.item_market_split_title.view.*
import kotlinx.android.synthetic.main.split_market_drag_top.*
import kotlinx.android.synthetic.main.split_market_popup.*
import kotlinx.android.synthetic.main.split_market_view.*
import kotlinx.android.synthetic.main.split_popup_drag_top.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@RequiresActivityViewModel(value = MarketViewModel::class)
class MarketActivity : BaseActivity<MarketViewModel>(), OnChartValueSelectedListener {

    private lateinit var chartData: ArrayList<MarketOfferList>
    private lateinit var offerRequestFilter: PostMarketOfferListRequest
    private lateinit var prevFrag: MarketOfferPreviewFragment
    private lateinit var tfRegular: Typeface
    private lateinit var tfLight: Typeface
    private lateinit var tfBold: Typeface
    private lateinit var tfExtraBold: Typeface
    private lateinit var splitScreen: SplitScreen
    private lateinit var splitPopup: SplitPopup
    private var splitDisplayCategory = SplitDisplayCategory.LiveDealPrice

    private lateinit var bottomsheetbehavior : CustomBottomSheetBehavior<FrameLayout>

    private var carrierList = ArrayList<Carrier>()
    private var containerList = ArrayList<Container>()
    private var paymentList = ArrayList<Payment>()

    private var selectedRoute: MarketRoute = MarketRoute(null,"","","","")

    private var isInitChart: Boolean = false

    private var stompUrl: String = ""

    private var gson: Gson = Gson()
    private lateinit var timerObs: Disposable
    private var onAir: Boolean = false

    private val adapter by lazy {
        SplitViewPagerAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_market)
        (application as App).component.inject(this)

        window.statusBarColor = getColor(R.color.color_1d1d1d)

        viewModel.outPuts.gotoSellOrder()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SellOrderActivity")
                    startActivityForResult(Intent(this, SellOrderActivity::class.java).putExtra(OFFER_ITEM, it),ORDER_REQUEST)
                }
        viewModel.outPuts.gotoBuyOrder()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(BuyOrderActivity")
                    startActivityForResult(Intent(this, BuyOrderActivity::class.java).putExtra(OFFER_ITEM, it), ORDER_REQUEST)
                }

        doEventService(Actions.START_SERVICE)

        initToolbar()

        initRoute()
        initFilter()

        initSplitPopupLayout()
        initSplitView()
        initChart()

        /*ll_filter.setSafeOnClickListener {
            //viewModel.inPuts.requestSellOfferLists(0)
            val items = chartData.filter { it.offerTypeCode == "B" }
            if(!items.isNullOrEmpty()) {

                val data = items[getRandom((items.size - 1).toFloat(), 0f).toInt()]
//            if(ll_container_body.visibility == View.INVISIBLE) {
//                ll_container_body_prev.visibility=View.INVISIBLE
//                ll_container_body.visibility=View.VISIBLE
//                ll_container_body_route.visibility=View.INVISIBLE
//            }
//            initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_EXPANDED, "test")
//            bottomsheetbehavior.setAllowDragging(false)
//            splitPopup.resetRouteTitle()

                val type = "Bid"
                val bor = BorList()
                bor.marketCode = "01"
                bor.locPolCd = offerRequestFilter.pol
                bor.locPodCd = offerRequestFilter.pod
                bor.carrierCodes = offerRequestFilter.companyCodes
                bor.rdTermCode = offerRequestFilter.rDTermCode
                bor.paymentTermCode = offerRequestFilter.paymentTermCode
                bor.containerTypeCode = offerRequestFilter.containerTypeCode
                bor.offerTypeCode = data.offerTypeCode
                bor.baseYearWeek = data.week
                bor.price = data.price

                viewModel.inPuts.requestOrderLists(bor)

                prevFrag.setRequestWeek(data.week)
                if (ll_container_body.visibility == View.INVISIBLE) {
                    ll_container_body_prev.visibility = View.INVISIBLE
                    ll_container_body.visibility = View.VISIBLE
                    ll_container_body_route.visibility = View.INVISIBLE
                }
                initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_EXPANDED, "$type ${this.getWeek(data.week)}")
                bottomsheetbehavior.setAllowDragging(false)
                splitPopup.resetRouteTitle()
                Toast.makeText(this, "select cell ${data.price} , item ${data.price}", Toast.LENGTH_SHORT).show()
                //startActivity(BookingActivity::class.java)
            }
        }*/
        /*ll_filter.setSafeOnClickListener {
            //viewModel.inPuts.requestMarketOfferList(mOfferRequestFilter)
            //startActivity(MarketTestActivity::class.java)

        }*/

        checkGoToMessageBox(intent)
    }

    /*private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }*/

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkGoToMessageBox(intent!!)
        checkGotoEventServiceRun(intent)
    }

    // TODO : Push Noti에서 MarketActivity를 Intent로 실행하면 이곳에서 체크
    //  Extra = GOTO_MESSAGE_BOX / true 인 경우 MessageBox 로 이동? (정책 필요)
    private fun checkGoToMessageBox(intent: Intent) {
        Timber.d("f9: Intent : $intent")
        if (intent.hasExtra(GOTO_MESSAGE_BOX)) {
            if (intent.getBooleanExtra(GOTO_MESSAGE_BOX, false)) {
                // GOTO MessageBox
                viewModel.inPuts.clickToMenuMessageBox(intent)
            }
        }
    }

    // EventService > Splash > MarketActivity (case. MarketActivity exist)
    private fun checkGotoEventServiceRun(intent: Intent) {
        Timber.d("f9: Intent : $intent")
        if (intent.hasExtra(GOTO_EVENT_SERVICE_RUN)) {
            if (intent.getBooleanExtra(GOTO_EVENT_SERVICE_RUN, false)) {
                doEventService(Actions.START_SERVICE)
            }
        }
    }

    private fun initRoute() {

        selectedRoute = MarketRoute(null,"CNSHA","Shanghai, Shanghai","DEHAM","Hamburg, HH")
        viewModel.outPuts.onSuccessLoadRouteFilter()
                .bindToLifecycle(this)
                .subscribe {
                    if (it != null) {
                        updateRoute(it)
                    }else {
                        selectedRoute = MarketRoute(null,"","","","")
                    }
                }
        viewModel.inPuts.loadRouteFilter(Parameter.EVENT)
        //add route selector
        container_current_route.setOnClickListener {
            Timber.d("f9: startActivity(RouteSelectActivity)")
            val intent = Intent(this, RouteSelectActivity::class.java)
            intent.putExtra("fromCode", selectedRoute.fromCode)
            intent.putExtra("fromDetail", selectedRoute.fromDetail)
            intent.putExtra("toCode", selectedRoute.toCode)
            intent.putExtra("toDetail", selectedRoute.toDetail)
            startActivityForResult(intent, PICK_ROUTE_REQUEST)
        }
        updateRoute(selectedRoute)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                PICK_CARRIER_REQUEST -> {
                    carrierList.clear()
                    btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)
                }
                PICK_ROUTE_REQUEST -> {
                    if (data != null) {
                        selectedRoute = MarketRoute(null,
                                data.getStringExtra("selectFromCode"),
                                data.getStringExtra("selectFromDetail"),
                                data.getStringExtra("selectToCode"),
                                data.getStringExtra("selectToDetail"))
                    }
                    storeRouteDB(selectedRoute)
                    updateRoute(selectedRoute)
                }
                ORDER_REQUEST -> {
                    dismissSplitPopup()
                }
            }
        }

    }
    private fun updateRoute(route: MarketRoute) {
        tv_market_pol.text = if(route.fromCode.isNotEmpty()) route.fromCode else "FROM"
        tv_market_pol_detail.text = route.fromDetail
        tv_market_pod.text = if(route.toCode.isNotEmpty()) route.toCode else "TO"
        tv_market_pod_detail.text = route.toDetail
        selectedRoute = route
    }

    private fun storeRouteDB(route: MarketRoute) {
        viewModel.inPuts.storeRouteFilter(route)

    }

    private fun initToolbar() {
        toolbar_common.setBackgroundColor(getColor(R.color.color_1d1d1d))

        // set custom toolbar
        defaultbarInit(toolbar_common, menuType = MenuType.DEFAULT, title = "Trade Market", isEnableNavi = false)

        // wait click event (toolbar left button)
        toolbar_common.toolbar_left_btn.setOnClickListener{
            it?.let {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_common.toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")

            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }

        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivity(MENUITEM_TRADE_MARKET, MenuActivity::class.java)
                }

        viewModel.outPuts.gotoMenuMessageBox()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivityWithIntent(MENUITEM_TRADE_MARKET, MenuActivity::class.java, it)
                }
    }

    private fun initFilter() {

        viewModel.outPuts.refreshCarrierFilter()
                .bindToLifecycle(this)
                .subscribe {
                    carrierList.clear()
                    carrierList.addAll(it)
                    btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)
                    requestOfferChartList()
                }
        viewModel.outPuts.refreshContainerFilter()
                .bindToLifecycle(this)
                .subscribe {
                    containerList.clear()
                    containerList.addAll(it)
                    btn_market_filter_container.text = checkContainerFilterName(containerList)
                }
        viewModel.outPuts.refreshPaymentFilter()
                .bindToLifecycle(this)
                .subscribe {
                    paymentList.clear()
                    paymentList.addAll(it)
                    btn_market_filter_ppd.text = checkPaymentFilterName(paymentList)

                }

        btn_market_filter_carrier.setOnClickListener {
            Timber.d("f9: btn_market_filter_carrier click")
            viewModel.inPuts.clickToCarrierFilter(Parameter.CLICK)

        }
        btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)

        viewModel.outPuts.gotoCarrierFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketCarrierFilterActivity")
                    startActivity(MarketCarrierFilterActivity::class.java)
                }
        btn_market_filter_container.setOnClickListener {
            Timber.d("f9: btn_market_filter_carrier click")
            viewModel.inPuts.clickToContainerFilter(Parameter.CLICK)
        }
        btn_market_filter_container.text = checkContainerFilterName(containerList)
        viewModel.outPuts.gotoContainerFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketContainerFilterActivity")
                    startActivity(MarketContainerFilterActivity::class.java)
                }

        btn_market_filter_ppd.setOnClickListener {
            Timber.d("f9: btn_market_filter_ppd click")
            viewModel.inPuts.clickToPpdFilter(Parameter.CLICK)
        }
        viewModel.outPuts.gotoPpdFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketPpdFilterActivity")
                    startActivity(MarketPpdFilterActivity::class.java)
                }
    }

    private fun checkCarrierFilterName(list: ArrayList<Carrier>): String? {
        var displayCarrier = ""
        var count = 0
        for(data in list) {
            if(data.select!!){
                count++
                if(displayCarrier == "")
                    displayCarrier = data.carriercode
            }
        }
        if(count == list.size)
            displayCarrier = getString(R.string.carrier_all)
        else if(count > 1)
            displayCarrier += " +${count -1}"

        return displayCarrier

    }
    private fun checkContainerFilterName(list: ArrayList<Container>): String? {
        var rdTerm = ""
        var type = ""
        var size = ""

        for(data in list) {
            if(data.selected!!){
                when(data.dataType) {
                    "rdterm" -> {
                        when(data.code) {
                            "YY" -> { rdTerm = resources.getString(R.string.rd_term_type_cycy_abbrev)
                                offerRequestFilter.rDTermCode = "01"}
                            "YD" -> { rdTerm = resources.getString(R.string.rd_term_type_cydoor_abbrev)
                                offerRequestFilter.rDTermCode = "02"}
                            "DY" -> { rdTerm = resources.getString(R.string.rd_term_type_doorcy_abbrev)
                                offerRequestFilter.rDTermCode = "03"}
                            "DD" -> { rdTerm = resources.getString(R.string.rd_term_type_doordoor_abbrev)
                                offerRequestFilter.rDTermCode = "04"}
                        }
                    }
                    "type" -> {
                        when(data.code) {
                            ContainerType.F_TYPE -> { type = resources.getString(R.string.full_container_simple_abbrev)
                                offerRequestFilter.containerTypeCode = "01"}
                            ContainerType.R_TYPE -> { type = resources.getString(R.string.rf_container_simple_abbrev)
                                offerRequestFilter.containerTypeCode = "02"}
                            ContainerType.S_TYPE -> { type = resources.getString(R.string.soc_container_simple_abbrev)
                                offerRequestFilter.containerTypeCode = "03"}
                            ContainerType.E_TYPE -> { type = resources.getString(R.string.empty_container_simple_abbrev)
                                offerRequestFilter.containerTypeCode = "04"}
                        }
                    }
                    "size" -> {size = data.code}
                }
            }
        }
        return "$rdTerm $type"
    }

    private fun checkPaymentFilterName(list: ArrayList<Payment>): CharSequence? {
        var type = ""
        var plan = ""

        for(data in list) {
            if(data.selected!!){
                when(data.dataType) {
                    "type" -> {
                        when(data.paymenttypecode) {
                            "P" -> { type = resources.getString(R.string.filter_payment_type_ppd)
                                offerRequestFilter.paymentTermCode = "01"}
                            "C" -> { type = resources.getString(R.string.filter_payment_type_cct)
                                offerRequestFilter.paymentTermCode = "02"}
                        }
                    }
                    "plan" -> {plan = data.iniPymtRto!!}
                }
            }
        }
        return "$type ($plan%)"
    }
    private fun initSplitPopupLayout() {
        initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_ZERO, "")
        bottomsheetbehavior = BottomSheetBehavior.from<FrameLayout>(bottom_sheet_frameout) as CustomBottomSheetBehavior<FrameLayout>

        prevFrag = MarketOfferPreviewFragment.newInstance(viewModel)
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_prev, prevFrag)
                .commit()
        supportFragmentManager.beginTransaction().replace(R.id.ll_container_body, MarketOfferListFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_route, MarketOfferRouteFragment.newInstance(viewModel))
                .commit()

        viewModel.outPuts.viewSplitPopupDetail()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: viewSplitPopupDetail")
                    ll_container_body_prev.visibility=View.VISIBLE
                    ll_container_body.visibility=View.INVISIBLE
                    ll_container_body_route.visibility=View.INVISIBLE
                    changeSplitPopupTitle(SplitDisplayCategory.TradeMarketOfferPrev)

                }
        viewModel.outPuts.viewSplitPopupRoute()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: viewSplitPopupRoute")
                    ll_container_body_prev.visibility=View.INVISIBLE
                    ll_container_body.visibility=View.INVISIBLE
                    ll_container_body_route.visibility=View.VISIBLE
                    changeSplitPopupTitle(SplitDisplayCategory.TradeMarketOfferRoute)

                }

        btn_split_popup_close.setOnClickListener {
            changeBackSplitPopup()

        }

        textView.setOnClickListener {
            viewModel.inPuts.swipeToOfferDetail(0)
        }
        textView2.setOnClickListener {
            viewModel.inPuts.swipeToOfferRoute(0)
        }
        viewModel.outPuts.refreshSplitPopupDeatil()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: refreshSplitPopupDeatil")
                    splitPopup.setRouteTitle(it.item.cryrCd!!,"${it.item.locPolCd}-${it.item.locPodCd}")
                }

    }
    private fun changeBackSplitPopup() {
        if(ll_container_body.visibility == View.INVISIBLE) {
            ll_container_body_prev.visibility=View.INVISIBLE
            ll_container_body.visibility=View.VISIBLE
            ll_container_body_route.visibility=View.INVISIBLE
            if(splitPopup.bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_ZERO, "")
                bottomsheetbehavior.setAllowDragging(true)
            }
            changeSplitPopupTitle(SplitDisplayCategory.TradeMarketList)
        } else {
            initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_ZERO, "")
            bottomsheetbehavior.setAllowDragging(true)
        }
    }

    private fun dismissSplitPopup() {
        ll_container_body_prev.visibility=View.INVISIBLE
        ll_container_body.visibility=View.VISIBLE
        ll_container_body_route.visibility=View.INVISIBLE
        initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_ZERO, "")
        bottomsheetbehavior.setAllowDragging(true)

    }
    override fun onBackPressed() {
        if(splitPopup.bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {

            changeBackSplitPopup()
        } else {
            super.onBackPressed()
        }
    }

    private fun changeSplitViewTitle(displayCategory: SplitDisplayCategory) {
        splitScreen.changeTitle(displayCategory)
    }

    private fun changeSplitPopupTitle(displayCategory: SplitDisplayCategory) {

        splitPopup.changeTitle(displayCategory)
    }
    private fun initSplitView() {

        adapter.add(resources.getString(R.string.split_market_live_deal_price))
        adapter.add(resources.getString(R.string.split_market_deals_by_voyage_week))
        adapter.add(resources.getString(R.string.split_market_your_inventory))
        adapter.add(resources.getString(R.string.split_market_your_offers_on_market))

        vp_market_split.apply {
            adapter = this@MarketActivity.adapter
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_1, MarketLiveDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_2, MarketWeekDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_3, MarketInventoryFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_4, MarketMyOfferFragment.newInstance(viewModel))
                .commit()

        vp_market_split.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                Timber.d("viewpager scroll state changed")

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Timber.d("viewpager page scrolled")

            }

            override fun onPageSelected(position: Int) {
                Timber.d("viewpager page selected $position")
                changeSplitView(position)
            }
        })
        tabLayout_indicator.setupWithViewPager(vp_market_split)
        initSplitView(SplitDisplayCategory.LiveDealPrice, SplitConst.SPLIT_UI_COLLAPSED)
    }

    private fun changeSplitView(position: Int) {
        ll_container_body_1.visibility = View.INVISIBLE
        ll_container_body_2.visibility = View.INVISIBLE
        ll_container_body_3.visibility = View.INVISIBLE
        ll_container_body_4.visibility = View.INVISIBLE
        when(position) {
            0-> { ll_container_body_1.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.LiveDealPrice)}
            1-> { ll_container_body_2.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.DealsByVoyageWeek)}
            2-> { ll_container_body_3.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourInventory)}
            3-> { ll_container_body_4.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourOffersOnMarket)}
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun initChart() {

        tfRegular = ResourcesCompat.getFont(this, R.font.opensans_regular)!!
        tfLight = ResourcesCompat.getFont(this, R.font.opensans_light)!!
        tfExtraBold = ResourcesCompat.getFont(this, R.font.opensans_extrabold)!!
        tfBold = ResourcesCompat.getFont(this, R.font.opensans_bold)!!

        chart_trade_offer.description.isEnabled = false
        chart_trade_offer.setOnChartValueSelectedListener(this)
        chart_trade_offer.setDrawGridBackground(false)
        chart_trade_offer.setTouchEnabled(true)

        // enable scaling and dragging
        chart_trade_offer.isDragEnabled = true

        //chart.setScaleEnabled(true);
        chart_trade_offer.setPinchZoom(true)

        //chart.setMaxVisibleValueCount(200);
        chart_trade_offer.setBackgroundColor(getColor(R.color.color_0d0d0d))
        chart_trade_offer.axisRight.isEnabled = false

        chart_trade_offer.setDrawGridBackground(false)
        chart_trade_offer.isKeepPositionOnRotation = true
        chart_trade_offer.isEnabled = true
        chart_trade_offer.minOffset = 0f
        chart_trade_offer.extraLeftOffset = 16f
        chart_trade_offer.extraBottomOffset = 10f
        chart_trade_offer.extraTopOffset = 6f

        val l = chart_trade_offer.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.typeface = tfLight
        l.isEnabled = false

        val yl = chart_trade_offer.axisLeft
        yl.textSize = 13f
        yl.typeface = tfBold
        yl.spaceTop = 30f
        yl.spaceBottom = 30f
        yl.setDrawZeroLine(false)
        yl.textColor = getColor(R.color.very_light_pink)
        yl.axisMinimum = 0.0f
        yl.labelCount = 9
        //yl.setXOffset(8f)
        yl.minWidth = 38f
        yl.maxWidth = 38f
        yl.xOffset = 14f

        val xAxisFormatter = OfferWeekAxisValueFormatter(chart_trade_offer)

        val xl = chart_trade_offer.xAxis
        xl.position = XAxis.XAxisPosition.TOP
        xl.typeface = tfExtraBold
        xl.setDrawGridLines(false)
        xl.granularity = 1f // only intervals of 1 day
        xl.valueFormatter = xAxisFormatter
        xl.textColor = getColor(R.color.white)
        xl.textSize = 13f
        xl.subTextSize = 10f
        xl.subTypeface = tfBold
        xl.subTextColor = (getColor(R.color.warm_grey))
        xl.yOffset = 25f


        //mOfferRequestFilter = PostMarketOfferListRequest("HLC", mRoute.fromCode,mRoute.toCode,carrier,"Y","D","A","20ft","P","202001", "1")
        offerRequestFilter = PostMarketOfferListRequest("", "01", getRequestCarrier(), "01","01", selectedRoute.fromCode, selectedRoute.toCode,"01","01")

        viewModel.outPuts.onSuccessRequestMarketOfferList()
                .bindToLifecycle(this)
                .subscribe { it ->
                    Timber.d("f9: request test $it")


                    runOnUiThread {
                        setData(it.matrixWeekPrice, it.qtyUnit)
                        chartData = it.matrixWeekPrice

                    }
                    if(!isInitChart){

                        val cal: Calendar = Calendar.getInstance()
                        cal.add(Calendar.WEEK_OF_YEAR, 4)
                        val toWeek: String = SimpleDateFormat("yyyyww").format(cal.time)
                        val maxData = it.matrixWeekPrice
                                .filter { it.week == toWeek }
                                .maxBy { it.qty }

                        maxData?.let { chart_trade_offer.moveViewTo(4.5f, maxData.price.toFloat(),YAxis.AxisDependency.LEFT) }
                        isInitChart = true
                    }
                    //stomp 연결 시작
                    stompUrl = it.topicName
                    if(::mStompClient.isInitialized && mStompClient.isConnected) {
                        compositeDisposable?.remove(dispTopic)
                        setSubscription(stompUrl)
                    }else {
                        connectStomp(stompUrl)
                    }

                }
    }

    override fun onResume() {
        super.onResume()
        viewModel.inPuts.refreshFilter(Parameter.EVENT)
    }

    private fun requestOfferChartList() {
        offerRequestFilter.pol = selectedRoute.fromCode
        offerRequestFilter.pod = selectedRoute.toCode
        offerRequestFilter.companyCodes = getRequestCarrier()
        //payplan depseq 를 보냄


        viewModel.inPuts.requestMarketOfferList(offerRequestFilter)

    }

    private fun getRequestCarrier() : ArrayList<String> {
        val carrier = ArrayList<String>()
        for(data in carrierList) {
            if(data.select!!)
                carrier.add(data.carriercode)
        }
        return carrier
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData(items: List<MarketOfferList>, qtyUnit: String) {
        //offerData.clear()

        val cal = Calendar.getInstance()
        cal.minimalDaysInFirstWeek = 4
        cal.firstDayOfWeek = Calendar.MONDAY

        val offerList: ArrayList<OfferEntry> = ArrayList()

        //오늘을 기준으로 53주차 data가 보여진다
        //화면에 처음 보여지는 주차는 오늘 주차 + 4주차부터 보여짐
        //없을경우 0으로 채움
        //

        for(x in 1..53){
            val toweek: String = "${cal.weekYear}${String.format("%02d",cal.get(Calendar.WEEK_OF_YEAR))}"
            if(items.filter { toweek.isNotEmpty() && it.week.isNotEmpty() && it.week == toweek }.isNullOrEmpty()){
                offerList.add(OfferEntry(x.toFloat(), 1000f, 990f,
                        0, OfferEntry.OfferType.DUMMY,null))
            }else {

                for(data in items.filter { it.week == toweek }){
                    offerList.add(OfferEntry(x.toFloat(), data.price.toFloat() + 10, data.price.toFloat(),
                            data.qty, if(data.offerTypeCode == "B") OfferEntry.OfferType.BUY_OFFER else OfferEntry.OfferType.SELL_OFFER,data))
                }
            }

            cal.add(Calendar.DAY_OF_YEAR,7)

        }
        val set1 = OfferDataSet(offerList, "")

        set1.setDrawIcons(false)
        set1.setDrawValues(true)

        val dataSets = java.util.ArrayList<IOfferDataSet>()
        dataSets.add(set1) // add the data sets

        val data = OfferData(dataSets)
        data.setDrawValues(true)
        data.setValueTypeface(tfBold)
        data.setValueTextSize(8f)
        data.setValueTextColor(Color.WHITE)

        chart_trade_offer.qtyUnit = qtyUnit
        chart_trade_offer.data = data

    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {

        val  data = e?.data as MarketOfferList
        val type: String = if(h?.offerType == OfferEntry.OfferType.BUY_OFFER) {
            "Bid"
        } else {
            "Ask"
        }
        val bor =  BorList()
        bor.marketCode = "01"
        bor.locPolCd = offerRequestFilter.pol
        bor.locPodCd = offerRequestFilter.pod
        bor.carrierCodes = offerRequestFilter.companyCodes
        bor.rdTermCode = offerRequestFilter.rDTermCode
        bor.paymentTermCode = offerRequestFilter.paymentTermCode
        bor.containerTypeCode = offerRequestFilter.containerTypeCode
        bor.offerTypeCode = data.offerTypeCode
        bor.baseYearWeek = data.week
        bor.price = data.price

        viewModel.inPuts.requestOrderLists(bor)

        prevFrag.setRequestWeek(data.week)
        if(ll_container_body.visibility == View.INVISIBLE) {
            ll_container_body_prev.visibility=View.INVISIBLE
            ll_container_body.visibility=View.VISIBLE
            ll_container_body_route.visibility=View.INVISIBLE
        }
        initSplitPopup(SplitDisplayCategory.TradeMarketList, SplitConst.SPLIT_UI_EXPANDED, "$type ${this.getWeek(data.week)}")
        bottomsheetbehavior.setAllowDragging(false)
        splitPopup.resetRouteTitle()
        Toast.makeText(this, "select cell ${e.y} , item ${data.price}",Toast.LENGTH_SHORT).show()

    }

    override fun onNothingSelected() {}

    private fun initSplitPopup(splitDisplayCategory: SplitDisplayCategory, isCollapsed: Int, title: String) {
        this.splitDisplayCategory = splitDisplayCategory
        splitPopup = SplitPopup(SplitUiData(this, splitDisplayCategory, bottom_sheet_frameout_popup, isCollapsed, title),
                ::receiveSplitPopupViewEvent)

    }

    private var bottomSheetState = BottomSheetBehavior.STATE_HALF_EXPANDED
    // STATE_COLLAPSED(0.0F), STATE_HALF_EXPANDED(0.078), STATE_EXPANDED(1.0)
    private var bottomSheetSlideOffset: Float = SPLIT_SLIDE_HALF_EXPANDED
    private fun initSplitView(splitDisplayCategory: SplitDisplayCategory, isCollapsed: Int) {

        splitScreen = SplitScreen(SplitUiData(this, splitDisplayCategory,
                bottom_sheet_frameout, BottomSheetBehavior.STATE_HALF_EXPANDED, ""),
                ::receiveSplitViewEvent, ::receiveSplitViewSlideOffset)
        val dm: DisplayMetrics = applicationContext.resources.displayMetrics
        splitScreen.setHalfExpandRatio(SplitConst.SPLIT_TITLE_HEIGHT_80.toPx().toFloat()/dm.heightPixels)
    }

    private fun receiveSplitViewSlideOffset(slideOffset: Float) {
        when(getBottomState()) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                if (slideOffset < getBottomSlideOffset()) {
                    bottom_sheet_frameout.setPadding(0, 0, 0, 0)
                }
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED -> {
                if (slideOffset > SPLIT_SLIDE_HALF) {
                    bottom_sheet_frameout.setPadding(0, resources.getDimension(R.dimen.default_title_height).toInt(), 0, 0)
                }
            }
        }
        setBottomSlideOffset(slideOffset)
    }

    private fun getBottomSlideOffset() = bottomSheetSlideOffset

    private fun setBottomSlideOffset(slideOffset: Float) {
        bottomSheetSlideOffset = slideOffset
    }

    private fun receiveSplitViewEvent(splitUiReceiveData: SplitUiReceiveData) {
        when(splitUiReceiveData.state) {
            BottomSheetBehavior.STATE_DRAGGING -> {

            }
            BottomSheetBehavior.STATE_COLLAPSED -> {

            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                if (getBottomState() != BottomSheetBehavior.STATE_COLLAPSED) {

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

    private fun receiveSplitPopupViewEvent(splitUiReceiveData: SplitUiReceiveData) {

        when(splitUiReceiveData.state) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                bottomsheetbehavior.setAllowDragging(true)
            }
        }
    }
    inner class SplitViewPagerAdapter: PagerAdapter() {

        private var dataList: ArrayList<String> = ArrayList()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = layoutInflater.inflate(R.layout.item_market_split_title, container, false)

            view.tv_split_view_title.text = dataList[position]
            container.addView(view)
            return view
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return (view == `object` as View)
        }

        override fun getCount(): Int {
            return dataList.size
        }

        fun add(data:String) {
            dataList.add(data)
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }
    companion object {
        const val PICK_CARRIER_REQUEST = 1
        const val PICK_ROUTE_REQUEST = 2
        const val ORDER_REQUEST = 3

    }


    override fun onPause() {
        super.onPause()
        disconnectStomp()
    }

    /*
    stomp 연결
     */

    private lateinit var mStompClient: StompClient

    private var response: String = ""

    private var compositeDisposable: CompositeDisposable? = null

    private lateinit var dispTopic:Disposable

    private val usrId = "name"

    private fun connectStomp(url: String) {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL)

        val share = SharedPreferenceManager(this)
        val headers: MutableList<StompHeader> = ArrayList()
        headers.add(StompHeader(usrId, share.name))

        headers.add(StompHeader("Authorization", share.token))

//        mStompClient.withServerHeartbeat(5000)
        //mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)

        resetSubscriptions()

        val dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {  setMessage("Stomp connection error",true) }
                .subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {setMessage("Stomp connection opened", true)
                            changeStatusUi(CONNECTED)
                        }
                        LifecycleEvent.Type.ERROR -> {
                            setMessage("Stomp connection error \n${lifecycleEvent.exception}",true)
                            changeStatusUi(DISCONNECTED)
                            if(!this.isDestroyed)
                                doNetworkCheck()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            setMessage("Stomp connection closed", true)
                            resetSubscriptions()
                            changeStatusUi(DISCONNECTED)
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> setMessage("Stomp failed server heartbeat", true)
                    }
                }

        compositeDisposable?.add(dispLifecycle)

        setSubscription(url)

        mStompClient.connect(headers)

    }
    private fun setSubscription(url: String) {

        // Receive greetings
        dispTopic = mStompClient.topic("/topic/${url}")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { t -> setMessage("doOnError $t", true) }
                .subscribe({ topicMessage: StompMessage ->
                    setMessage("Received ${topicMessage.payload}", true)
                    val data: MarketChartOfferList = gson.fromJson(topicMessage.payload, MarketChartOfferList::class.java)
                    runOnUiThread {
                        setData(data.matrixWeekPrice, data.qtyUnit)
                        chartData = data.matrixWeekPrice
                        timerEvent()
                    }
                })
                { throwable: Throwable? -> setMessage("Error on subscribe topic \n${throwable}", true) }

        compositeDisposable?.add(dispTopic)

    }
    private fun resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable!!.dispose()
        }
        compositeDisposable = CompositeDisposable()
    }
    private fun disconnectStomp() {
        if(::mStompClient.isInitialized)
            mStompClient.disconnect()

    }
    private fun setMessage(msg: String, isReset: Boolean) {
        Timber.d("f9: $msg")
        if(isReset)
            response = (msg + "\n")
        else
            response += (msg + "\n")

    }
    private fun timerEvent() {
        if(onAir)
            return
        onAir = true
        changeStatusUi(RECEIVING)
        timerObs = Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onAir = false
                    changeStatusUi(RECEIVING)
                }
    }
    private fun changeStatusUi(code: String){
        when(code){
            CONNECTED -> {v_circle.background = getDrawable(R.drawable.bg_circle_5_purplesh_blue)
                tv_onair.text = getString(R.string.market_stomp_on_air)
                tv_onair.setTextColor(getColor(R.color.color_bfbfbf))
            }
            DISCONNECTED -> {v_circle.background = getDrawable(R.drawable.bg_circle_5_orangey_red)
                tv_onair.text = getString(R.string.market_stomp_disconn)
                tv_onair.setTextColor(getColor(R.color.color_bfbfbf))
            }
            RECEIVING -> {
                tv_onair.text = getString(R.string.market_stomp_on_air)
                if(onAir){
                    v_circle.background = getDrawable(R.drawable.bg_circle_5_140f36)
                    tv_onair.setTextColor(getColor(R.color.color_bfbfbf))
                }else {
                    v_circle.background = getDrawable(R.drawable.bg_circle_5_purplesh_blue)
                    tv_onair.setTextColor(getColor(R.color.color_bfbfbf))
                }
            }
        }

    }

    private fun doEventService(action: Actions) {

        val isRunningService = isRunningService(EventService::class.java)
        Timber.d("f9: Service status : %s", getServiceState(this).name)
        Timber.d("f9: isRunningService : %s", isRunningService)
        if (!isRunningService) {
            setServiceState(this, ServiceState.STOPPED)
        }

        if (action.name == Actions.START_SERVICE.name) {
            Intent(this, EventService::class.java).also {
                it.action = action.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Timber.d("f9: Starting the service in >=26 Mode")
                    startForegroundService(it)
                } else {
                    Timber.d("f9: Starting the service in < 26 Mode")
                    startService(it)
                }
            }
        }
    }
    private lateinit var networkCheckDisp: Disposable
    private fun doNetworkCheck() {
        if(::networkCheckDisp.isInitialized)
            networkCheckDisp.dispose()
        networkCheckDisp = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(checkNetwork()) {
                        connectStomp(stompUrl)
                        networkCheckDisp.dispose()
                    }
                }
    }

    private fun checkNetwork():Boolean {

        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}
