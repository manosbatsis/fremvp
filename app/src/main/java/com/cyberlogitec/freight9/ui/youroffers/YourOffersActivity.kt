package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.EventInitial.EVENT_INITIAL_PRODUCT
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_ALL
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_OFFERS
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.split.*
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF_EXPANDED
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.inventory.RouteFilterPopup
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.trademarket.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_your_offers.*
import kotlinx.android.synthetic.main.appbar_route_filter.iv_route_filter_all
import kotlinx.android.synthetic.main.appbar_route_filter.ll_route_filter_from
import kotlinx.android.synthetic.main.appbar_route_filter.ll_route_filter_to
import kotlinx.android.synthetic.main.appbar_route_filter.tv_route_filter_from_code
import kotlinx.android.synthetic.main.appbar_route_filter.tv_route_filter_from_name
import kotlinx.android.synthetic.main.appbar_route_filter.tv_route_filter_to_code
import kotlinx.android.synthetic.main.appbar_route_filter.tv_route_filter_to_name
import kotlinx.android.synthetic.main.appbar_your_offers.*
import kotlinx.android.synthetic.main.body_pol_pod_card_offers.view.*
import kotlinx.android.synthetic.main.body_your_offers.*
import kotlinx.android.synthetic.main.item_market_split_title.view.*
import kotlinx.android.synthetic.main.item_your_offers.view.*
import kotlinx.android.synthetic.main.item_your_offers_header.view.*
import kotlinx.android.synthetic.main.split_market_drag_top.*
import kotlinx.android.synthetic.main.split_market_view.*
import kotlinx.android.synthetic.main.toolbar_your_offers.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresActivityViewModel(value = YourOffersViewModel::class)
class YourOffersActivity : BaseActivity<YourOffersViewModel>(),
        SwipeableRecyclerViewTouchListener.SwipeListener{

    private lateinit var dashboardWeekList: List<String>

    private var selectedRouteFromTo: RouteFilterPopup.RouteFromTo = RouteFilterPopup.RouteFromTo.INIT
    private var selectedRouteData: RouteFilterPopup.RouteAdapterData? = null
    private var selectedWeek: String = EmptyString
    // Init value : false !!! (true : onMarket List, false : Closed List)
    private var isOnMarketList: Boolean = false

    private var isInitRequested: Boolean = false

    private val adapter:RecyclerAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    onClickItem = {
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, it))
                    }
                    onClickButton = {
                        requestClickOnMarketAndClosed(it)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    // 재사용 Activity는 onPause(), onNewIntent(), onResume() 순서로 동작
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (it.hasExtra(Intents.YOUR_OFFER_TYPE)) {
                viewModel.inPuts.injectIntent(it)
                // offer list 요청
                initRequestOffers()
            }
        }
    }

    /**
     * activity data init
     */
    private fun initData() {
        selectedRouteFromTo = RouteFilterPopup.RouteFromTo.INIT
        selectedRouteData = null
        selectedWeek = EmptyString
        isInitRequested = false
    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_your_offers,
                menuType = MenuType.DEFAULT,
                title = getString(R.string.menu_your_buy_offers),
                isEnableNavi = false)

        setFabButton(false)
        recyclerViewInit()
        setListener()

        // offer list 요청
        requestOffers(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("f9: onDestroy !!!")
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {

        /**
         * offerType 에 대한 summary list
         */
        viewModel.outPuts.onSuccessRequestOffersSummary()
                .bindToLifecycle(this)
                .subscribe {
                    // offerType : Buy Offers("B") / Sell Offers("S")
                    it?.let { offerValues ->
                        processOffersSummary(offerValues)
                    }
                }

        /**
         * 모든 offer list 를 포함하고 있는 weeklist
         */
        viewModel.outPuts.onSuccessWeekList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { baseYearWeeks ->
                        // Init Week horizontal scroll view
                        if (baseYearWeeks.isNotEmpty()) {
                            makeAndInitWeekOfList(baseYearWeeks)
                        }
                    }
                }

        /**
         * RxBus 에서 전송한 Finish event 처리
         */
        viewModel.rxEventFinish
                .bindToLifecycle(this)
                .subscribe { finishKind ->
                    Timber.d("f9: rxEventFinish = $finishKind")
                    if (finishKind == FINISH_OFFERS || finishKind == FINISH_ALL) finish()
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_FAB -> {
                                moveScrollTop()
                            }
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                startMenuActivity(
                                        if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                                            MenuItem.MENUITEM_YOUR_BUY_OFFERS
                                        } else {
                                            MenuItem.MENUITEM_YOUR_SELL_OFFERS
                                        },
                                        MenuActivity::class.java
                                )
                            }
                            ParameterClick.CLICK_FILTER_FROM -> {
                                requestRouteData(RouteFilterPopup.RouteFromTo.FROM)
                            }
                            ParameterClick.CLICK_FILTER_TO -> {
                                requestRouteData(RouteFilterPopup.RouteFromTo.TO)
                            }
                            ParameterClick.CLICK_FILTER_ALL -> {
                                requestRouteData(RouteFilterPopup.RouteFromTo.ALL)
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
                                ParameterAny.ANY_ON_MARKET_CLOSED -> {
                                    isOnMarketList = second as Boolean
                                    requestOffersFilter()
                                }
                                ParameterAny.ANY_WEEK_TAB_INDEX -> {
                                    clickWeekOfTabProcess(second as Int)
                                }
                                ParameterAny.ANY_ITEM_INDEX -> {
                                    adapter.datas[second as Int].cell?.let { item ->
                                        Timber.d("f9: onClickItem : ${item.toJson()}")
                                        gotoDetail(item)
                                    }
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        // Filtering result
        viewModel.outPuts.onSuccessOffersFilter()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { triple ->
                        processOffersFilter(triple)
                    }
                }

        // Route data result
        viewModel.outPuts.onSuccessRouteData()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { routePair ->
                        showRouteSelectPopup(routePair, ::onRouteSelectClick)
                    }
                }

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

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    Timber.e("f9: error : $it")
                    showToast("Fail (Throwable)\n" + it.message)
                    finish()
                }

        /**
         * RxBus 에서 전송한 Refresh event 처리
         */
        viewModel.rxEventOfferRefresh
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: viewModel.rxEventOfferRefresh")
                    it?.let { message ->
                        // YourOffersDetailActivity에서 Cancel 하고 돌아온 경우
                        Timber.d("f9: result : call Offers refresh by Offer Created!")
                        Timber.d("f9: rxEventOfferRefresh = ${message.toJson()}")
                        initRequestOffers()
                    }
                }
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        // https://medium.com/@ydh0256/android-recyclerview-%EC%9D%98-%EC%B5%9C%EC%83%81%EB%8B%A8%EA%B3%BC-%EC%B5%9C%ED%95%98%EB%8B%A8-%EC%8A%A4%ED%81%AC%EB%A1%A4-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EA%B0%90%EC%A7%80%ED%95%98%EA%B8%B0-f0e5fda34301
        recycler_offer_list.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1)) {
                    // TOP of List
                    setFabButton(false)
                } else if (!recyclerView.canScrollVertically(1)){
                    // End of List
                    // TODO : Do nothing
                } else {
                    // Idle
                    // TODO : Do nothing
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val diffYPosition = dx - dy
                if (diffYPosition > 0) {
                    // Scroll Down
                    setFabButton(true)
                } else {
                    // Scroll Up
                    setFabButton(false)
                }
            }
        })

        fl_fab.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_FAB)
        }

        //------------------------------------------------------------------------------------------
        // From, All, To click
        ll_route_filter_from.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_FILTER_FROM)
        }

        ll_route_filter_to.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_FILTER_TO)
        }

        iv_route_filter_all.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_FILTER_ALL)
        }

        //------------------------------------------------------------------------------------------
        // "ALL" layout
        rl_hsv_weekof_all.setSafeOnClickListener {
            requestClickToWeekOfTab(-1)
        }

        //------------------------------------------------------------------------------------------
        // on click toolbar right button
        toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }
    }

    /**
     * Route filter 초기화
     */
    private fun setRouteFilterInit() {
        tv_route_filter_from_code.text = getString(R.string.from)
        tv_route_filter_from_name.text = EmptyString
        tv_route_filter_to_code.text = getString(R.string.to)
        tv_route_filter_to_name.text = EmptyString
    }

    /**
     * Scroll view 를 top으로 scroll
     */
    private fun moveScrollTop() {
        Handler().postDelayed({
            recycler_offer_list.scrollToPosition(0)
            ab_your_offers.setExpanded(true)
        }, 250)
    }

    /**
     * Fab button 설정
     */
    private fun setFabButton(isVisible: Boolean) {
         fl_fab.visibility = if (isVisible) View.VISIBLE else View.GONE
        // For animation
        /*
        fl_fab.startAnimation(if (isVisible) {
            AnimationUtils.loadAnimation(this, R.anim.fab_open)
        } else {
            AnimationUtils.loadAnimation(this, R.anim.fab_close)
        })
        */
    }

    /**
     * Offer list screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_offer_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@YourOffersActivity.adapter
        }
        recycler_offer_list.addOnItemTouchListener(
            SwipeableRecyclerViewTouchListener(recycler_offer_list, this@YourOffersActivity)
        )
    }
    
    private fun initRequestOffers() {
        Timber.e("f9: initRequestOffers()")
        initData()
        requestOffers(true)
    }

    /**
     * Recycler view item click 시 detail 화면으로 이동
     */
    private fun gotoDetail(cell: Dashboard.Cell) {
        RxActivityResult(this@YourOffersActivity)
                .start(Intent(this, YourOffersDetailActivity::class.java)
                        .putExtra(Intents.YOUR_OFFER_DASHBOARD_ITEM, cell))
                .subscribe(
                        { result ->
                            if (result.isOk) {
                                // YourOffersDetailActivity에서 Cancel 하고 돌아온 경우
                                Timber.d("f9: result : call Offers refresh")
                                initRequestOffers()
                            } else {
                                /*
                                * YourOffersDetailActivity에서 Revise 또는 Back 으로 돌아온 경우
                                * Revise 로 돌아온 경우에는  viewModel.rxEventOfferCreated 에서 initRequestOffers() 호출한다
                                * */
                                Timber.d("f9: result : not call Offers refresh")
                            }
                        },
                        { throwable ->
                            viewModel.error.onNext(throwable)
                        }
                )
    }

    private fun setToolbarTitle(yourOfferType: String) {

        when(yourOfferType) {
            OFFER_TYPE_CODE_BUY -> {
                toolbar_title_text.text = getString(R.string.menu_your_buy_offers)
            }
            OFFER_TYPE_CODE_SELL -> {
                toolbar_title_text.text = getString(R.string.menu_your_sell_offers)
            }
        }
    }

    /***********************************************************************************************
     * Request Offers list to Rx
     */
    private fun requestOffers(isRouteChanged: Boolean = false) {
        viewModel.inPuts.requestOffersSummaryByApi(makeFilterCondition(isRouteChanged))
    }

    /***********************************************************************************************
     * Request filtering to Rx
     */
    private fun requestOffersFilter(isRouteChanged: Boolean = false) {
        viewModel.inPuts.requestOffersFilter(makeFilterCondition(isRouteChanged))
    }

    /**
     * return offers filter condition data class
     */
    private fun makeFilterCondition(isRouteChanged: Boolean = false): FilterCondition {
        val polCode = tv_route_filter_from_code.text.toString()
        val podCode = tv_route_filter_to_code.text.toString()
        val polCodeIsAll = (polCode.compareTo(getString(R.string.all), true) == 0)
                || (polCode.compareTo(getString(R.string.from), true) == 0)
        val podCodeIsAll = (podCode.compareTo(getString(R.string.all), true) == 0)
                || (podCode.compareTo(getString(R.string.to), true) == 0)

        if (isRouteChanged) {
            // "ALL"
            this.selectedWeek = EmptyString
            // "On Market"
            this.isOnMarketList = true
        }

        return FilterCondition(
                this.selectedRouteFromTo,
                this.selectedRouteData,
                this.selectedWeek,
                this.isOnMarketList,
                polCode,
                podCode,
                polCodeIsAll,
                podCodeIsAll,
                isRouteChanged,
                this.isInitRequested
        )
    }

    /***********************************************************************************************
     * Request route datas to Rx
     */
    private fun requestRouteData(routeFromTo: RouteFilterPopup.RouteFromTo) {
        viewModel.inPuts.requestRouteData(Triple(
                routeFromTo,
                tv_route_filter_from_code.text.toString(),
                tv_route_filter_to_code.text.toString()
        ))
    }

    /***********************************************************************************************
     * Request route click week tab to Rx
     */
    private fun requestClickToWeekOfTab(index: Int) {
        clickViewParameterAny(Pair(ParameterAny.ANY_WEEK_TAB_INDEX, index))
    }

    /***********************************************************************************************
     * Request On market, Closed click
     */
    private fun requestClickOnMarketAndClosed(isOnMarketClosed: Boolean) {
        clickViewParameterAny(Pair(ParameterAny.ANY_ON_MARKET_CLOSED, isOnMarketClosed))
    }

    /***********************************************************************************************
     * Process response about Offers summary
     */
    private fun processOffersSummary(offerValues: Pair<String, Dashboard>) {
        // 사용될 변수 최초 초기화
        yourOfferType = offerValues.first
        selectedRouteFromTo = RouteFilterPopup.RouteFromTo.INIT

        Timber.d("f9: offerType : $yourOfferType")

        // 최초 호출에 대한 응답인 경우에만 수행
        if (!isInitRequested) {

            // Title name : Your Buy Offers / Your Sell Offers
            setToolbarTitle(yourOfferType)

            // Init Route Filter
            setRouteFilterInit()

            // Init Recycler Mode , On Market / Closed, refreshListUi
            initOnMarketClosedButtonStatus()

            // Init Split View
            initSplitView()

            isInitRequested = true
        }
    }

    /***********************************************************************************************
     * Process response about Offers filter
     */
    private fun processOffersFilter(triple: Triple<FilterCondition, List<Dashboard.Cell>, Pair<Int, Int>>) {
        val filterCondition = triple.first
        val filteredCellList = triple.second
        // Route 변경한 경우 filter 된 dashBoard list 를 기준으로 WeekList 재구성
        if (filterCondition.isRouteChanged) {
            // "On Market" checked 후 onClickToOnMarket 에서 refreshListUi 한다
            initOnMarketClosedButtonStatus()
        } else {
            refreshListUi(
                    filterCondition,
                    filteredCellList,
                    // On Market , Closed list count
                    triple.third
            )
        }
    }

    /***********************************************************************************************
     * Init Week Horizontal scroll view & Process
     */
    private val weekOfListTagSeq = 9999980
    private fun makeAndInitWeekOfList(baseYearWeeks: List<String>) {

        this.dashboardWeekList = baseYearWeeks

        val childCount = ll_weekof.childCount
        for (childIndex in 0 until childCount) {
            val itemLayout = ll_weekof.getChildAt(childIndex)
            if (itemLayout is LinearLayout) {
                itemLayout.removeAllViews()
            }
        }
        ll_weekof.removeAllViews()

        var findScrollIndex = -1
        val currentYearWeek = getTodayYearWeekNumber()
        for ((index, week) in dashboardWeekList.withIndex()) {
            val llItem = LinearLayout(this)
            llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            llItem.orientation = LinearLayout.VERTICAL
            llItem.gravity = Gravity.BOTTOM
            val margin = 12.toDp().toInt()
            val params = llItem.layoutParams as LinearLayout.LayoutParams
            when (index) {
                dashboardWeekList.lastIndex -> {
                    params.setMargins(margin, 0, 0, 0)
                }
                else -> {
                    params.setMargins(margin, 0, margin, 0)
                }
            }
            llItem.layoutParams = params

            llItem.addView(makeWeekOfTabTextView(week))
            llItem.addView(makeWeekOfTabImageView())
            llItem.tag = weekOfListTagSeq + index
            llItem.setOnClickListener { requestClickToWeekOfTab(index) }
            ll_weekof.addView(llItem)

            // 현재 주차 이후로 첫번째 index
            if (week >= currentYearWeek && findScrollIndex < 0) {
                findScrollIndex = index
            }
        }

        // 현재 주차 이후의 첫번째 Index를 "ALL" 다음에 표시
        Handler().postDelayed({
            val xPos = if (findScrollIndex < 1) {
                0
            } else {
                ll_weekof.getChildAt(findScrollIndex - 1).right + 12.toDp().toInt()
            }
            hsv_weekof.scrollTo(xPos, 0)
        }, 100)

        // 선택한 주차가 없는 경우 "ALL" selected 상태
        if (this.selectedWeek.isEmpty()) {
            setAllTabLayout(true, isRequest = false)
        }
    }

    /**
     * week tab text view create
     */
    private fun makeWeekOfTabTextView(titleWeekOf: String) : TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textView.setTextAppearance(R.style.txt_opensans_r_16_greyishbrown)
        textView.text = if (titleWeekOf.isEmpty()) getString(R.string.route_filter_all) else getWeek(titleWeekOf)
        return textView
    }

    /**
     * week tab 시 처리
     */
    private fun clickWeekOfTabProcess(index: Int) {
        if (index < 0) {
            // "ALL"
            setAllTabLayout(true, isRequest = true)
        } else {
            // NOT "ALL"
            setAllTabLayout(false)
        }
        setOtherTabLayout(index)
    }

    private fun makeWeekOfTabImageView() : View {
        val imageView = View(this)
        val margin = 2.toDp().toInt()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.toDp().toInt())
        params.setMargins(0, margin, 0, 0)
        imageView.layoutParams = params
        imageView.setBackgroundColor(getColor(R.color.color_1d1d1d))
        return imageView
    }

    /**
     * horizontal all tab layout 설정
     */
    private fun setAllTabLayout(isSelected: Boolean, isRequest: Boolean = false) {
        tv_hsv_weekof_all.setTextColor(getColor(if (isSelected) { R.color.colorWhite }
        else { R.color.greyish_brown }))

        tv_hsv_weekof_all.setTextAppearance(if (isSelected) { R.style.txt_opensans_eb_16_white }
        else { R.style.txt_opensans_r_16_greyishbrown })

        iv_hsv_weekof_all.setBackgroundColor(getColor(if (isSelected) { R.color.blue_violet }
        else { R.color.color_1d1d1d }))

        if (isRequest) {
            // "ALL" 인 경우 ""
            selectedWeek = EmptyString
            // request to Rx
            requestOffers()
            Timber.d("f9: click : ALL")
        }
    }

    /**
     * horizontal week tab layout 설정
     */
    private fun setOtherTabLayout(index: Int) {

        val clickTagId = weekOfListTagSeq + index
        val childCount = ll_weekof.childCount
        for (childIndex in 0 until childCount) {
            var selectedIndex = false
            val childView = ll_weekof.getChildAt(childIndex) as LinearLayout
            var textColorValue = R.color.greyish_brown
            var viewColorValue = R.color.color_1d1d1d

            if (childView.tag as Int == clickTagId) {
                // "ex. 202023"
                selectedWeek = dashboardWeekList[index]
                Timber.d("f9: click : $selectedWeek")

                textColorValue = R.color.colorWhite
                viewColorValue = R.color.blue_violet
                selectedIndex = true

                // scroll bar 이동
                childView.parent.requestChildFocus(childView, childView)

                // request to Rx
                requestOffers()
            }
            val subChildCount = childView.childCount
            for (subIndex in 0 until subChildCount) {
                when (val subChildView = childView.getChildAt(subIndex)) {
                    is TextView -> {
                        subChildView.setTextAppearance(if (selectedIndex) R.style.txt_opensans_eb_16_white
                        else R.style.txt_opensans_r_16_greyishbrown)
                        subChildView.setTextColor(getColor(textColorValue))
                    }
                    is View -> {
                        subChildView.setBackgroundColor(getColor(viewColorValue))
                    }
                }
            }
        }
    }

    /***********************************************************************************************
     * Init On Market, Closed Button UI
     */
    private fun initOnMarketClosedButtonStatus() {
        isOnMarketList = false
        adapter.setIsOnMarketList(isOnMarketList)
        requestClickOnMarketAndClosed(true)
    }

    /***********************************************************************************************
     * Refresh RecyclerView & Init ScrllView Position
     */
    private fun refreshListUi(
            filterCondition: FilterCondition,
            filteredCellList: List<Dashboard.Cell>,
            pair: Pair<Int, Int>) {
        // Do filter list & refresh RecyclerView
        val itemList = mutableListOf<RecyclerAdapter.Item>()
        val header = RecyclerAdapter.Item.Builder()
                .type(RecyclerAdapter.HEADER)
                .headerItem(RecyclerAdapter.HeaderItem(pair.first, pair.second, filterCondition.isOnMarketList))
                .build()
        itemList.add(header)
        for (cell in filteredCellList) {
            val content = RecyclerAdapter.Item.Builder()
                    .type(RecyclerAdapter.CONTENT)
                    .cellItem(cell)
                    .build()
            itemList.add(content)
        }
        val footer = RecyclerAdapter.Item.Builder()
                .type(RecyclerAdapter.FOOTER)
                .footerItem(null)
                .build()
        itemList.add(footer)

        adapter.setData(this.selectedWeek, itemList)
        adapter.notifyDataSetChanged()

        // NestedScrollView move to top position
        moveScrollTop()
    }

    /***********************************************************************************************
     * Selected Routes from Route Select Popup : Request Filtering
     */
    private fun onRouteSelectClick(position: Int, routeFromTo: RouteFilterPopup.RouteFromTo,
                                   routeData: RouteFilterPopup.RouteAdapterData) {
        Timber.d("f9: onRouteSelectClick - position = $position")
        this.selectedRouteFromTo = routeFromTo
        this.selectedRouteData = routeData

        selectedRouteData?.let { route ->
            when (selectedRouteFromTo) {
                RouteFilterPopup.RouteFromTo.FROM -> {
                    tv_route_filter_from_code.text =
                            if (route.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                            else route.polOrPorPortCode
                    tv_route_filter_from_name.text = route.polOrPorPortName
                }
                RouteFilterPopup.RouteFromTo.ALL -> {
                    tv_route_filter_from_code.text =
                            if (route.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                            else route.polOrPorPortCode
                    tv_route_filter_from_name.text = route.polOrPorPortName
                    tv_route_filter_to_code.text =
                            if (route.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                            else route.podOrDelPortCode
                    tv_route_filter_to_name.text = route.podOrDelPortName
                }
                RouteFilterPopup.RouteFromTo.TO -> {
                    tv_route_filter_to_code.text =
                            if (route.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                            else route.podOrDelPortCode
                    tv_route_filter_to_name.text = route.podOrDelPortName
                }
                else -> { }
            }
        }

        // request to Rx
        requestOffers(true)
    }

    /***********************************************************************************************
     * Bottom SplitView Init & handling
     */
    private lateinit var splitScreen: SplitScreen
    private var bottomSheetState = BottomSheetBehavior.STATE_HALF_EXPANDED
    // STATE_COLLAPSED(0.0F), STATE_HALF_EXPANDED(0.078), STATE_EXPANDED(1.0)
    private var bottomSheetSlideOffset: Float = SPLIT_SLIDE_HALF_EXPANDED
    private val splitAdapter by lazy {
        SplitViewPagerAdapter()
    }

    private fun initSplitView() {

        splitAdapter.dataList.clear()
        splitAdapter.add(resources.getString(R.string.split_market_your_inventory))
        splitAdapter.add(resources.getString(R.string.split_market_live_deal_price))
        splitAdapter.add(resources.getString(R.string.split_market_deals_by_voyage_week))
        splitAdapter.add(resources.getString(R.string.split_market_all_offers_on_market))

        vp_market_split.apply {
            adapter = this@YourOffersActivity.splitAdapter
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_1, MarketInventoryFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_2, MarketLiveDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_3, MarketWeekDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_4, MarketAllOfferFragment.newInstance(viewModel))
                .commit()

        vp_market_split.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                Timber.d("f9: viewpager scroll state changed")

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Timber.d("f9: viewpager page scrolled")

            }

            override fun onPageSelected(position: Int) {
                Timber.d("f9: viewpager page selected $position")
                changeSplitView(position)
            }
        })
        tabLayout_indicator.setupWithViewPager(vp_market_split)
        initSplitView(SplitDisplayCategory.YourInventory)
        changeSplitView(0)
    }

    private fun initSplitView(splitDisplayCategory: SplitDisplayCategory) {
        bottom_sheet_frameout.visibility = View.VISIBLE
        bottom_sheet_frameout.setPadding(0, 0, 0, 0)
        setBottomState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        setBottomSlideOffset(SPLIT_SLIDE_HALF_EXPANDED)
        splitScreen = SplitScreen(SplitUiData(this, splitDisplayCategory,
                bottom_sheet_frameout, BottomSheetBehavior.STATE_HALF_EXPANDED, EmptyString),
                ::receiveSplitViewEvent, ::receiveSplitViewSlideOffset)
        val dm: DisplayMetrics = applicationContext.resources.displayMetrics
        splitScreen.setHalfExpandRatio(SplitConst.SPLIT_TITLE_HEIGHT_80.toPx().toFloat()/dm.heightPixels)
    }

    private fun changeSplitView(position: Int) {
        ll_container_body_1.visibility = View.INVISIBLE
        ll_container_body_2.visibility = View.INVISIBLE
        ll_container_body_3.visibility = View.INVISIBLE
        ll_container_body_4.visibility = View.INVISIBLE
        when(position) {
            0-> { ll_container_body_1.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourInventory)}
            1-> { ll_container_body_2.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.LiveDealPrice)}
            2-> { ll_container_body_3.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.DealsByVoyageWeek)}
            3-> { ll_container_body_4.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.AllOffersOnMarket)}
        }
    }

    private fun changeSplitViewTitle(displayCategory: SplitDisplayCategory) {
        splitScreen.changeTitle(displayCategory)
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
                setFabButton(false)
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                // Do nothing : fl_fab.show()
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                if (getBottomState() != BottomSheetBehavior.STATE_COLLAPSED) {
                    // Do nothing : fl_fab.show()
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

    inner class SplitViewPagerAdapter: PagerAdapter() {
        var dataList: ArrayList<String> = ArrayList<String>()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = layoutInflater.inflate(R.layout.item_market_split_title, container, false)

            view.tv_split_view_title.text = dataList.get(position)
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

    /***********************************************************************************************
     * RecyclerAdapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        lateinit var context: Context
        lateinit var selectedWeek: String
        private lateinit var tfRegular: Typeface
        private lateinit var tfExtraBold: Typeface

        var datas: MutableList<Item> = ArrayList()
        var onClickItem: (Int) -> Unit = { _: Int -> }
        var onClickButton: (Boolean) -> Unit = { _: Boolean -> }
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var isOnMarketList: Boolean = false

        fun setData(selectedWeek: String, datas: MutableList<Item>) {
            this.selectedWeek = selectedWeek
            this.datas.clear()
            this.datas = datas
        }

        fun setIsOnMarketList(isOnMarketList: Boolean) {
            this.isOnMarketList = isOnMarketList
        }

        override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            ResourcesCompat.getFont(context, R.font.opensans_regular)?.let{ tfRegular = it }
            ResourcesCompat.getFont(context, R.font.opensans_extrabold)?.let { tfExtraBold = it }

            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            var holder: RecyclerView.ViewHolder? = null
            when(type) {
                HEADER -> {
                    holder = HeaderViewHolder(inflater.inflate(R.layout.item_your_offers_header, parent, false))
                }
                CONTENT -> {
                    holder = ContentViewHolder(inflater.inflate(R.layout.item_your_offers, parent, false))
                }
                FOOTER -> {
                    holder = FooterViewHolder(inflater.inflate(R.layout.item_your_offers_footer, parent, false))
                }
            }
            return holder ?: throw IllegalStateException("Item type unspecified.")
        }

        override fun getItemCount(): Int = datas.size

        override fun getItemViewType(position: Int) = datas[position].type

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                val item = datas[holder.bindingAdapterPosition]
                when(item.type) {
                    HEADER -> {
                        bindHeader(holder as HeaderViewHolder, position)
                    }
                    CONTENT -> {
                        bindContent(holder as ContentViewHolder, position)
                    }
                    FOOTER -> {
                        bindFooter(holder as FooterViewHolder, position)
                    }
                }
            }
        }

        private fun bindHeader(holder: HeaderViewHolder, position: Int) {
            with(holder.itemView) {
                datas[position].headerItem?.let { item ->
                    setButtonOffersCount(holder, item)
                    setOnMarketClosedButtonStatus(holder, item.isOnMarketClosed)

                    btn_on_market.setSafeOnClickListener {
                        setOnMarketClosedButtonStatus(holder, true)
                        onClickButton(true)
                    }

                    btn_closed.setSafeOnClickListener {
                        setOnMarketClosedButtonStatus(holder, false)
                        onClickButton(false)
                    }
                }
            }
        }

        private fun bindContent(holder: ContentViewHolder, position: Int) {
            with(holder.itemView) {
                datas[position].cell?.let { item ->

                    val dealQty = item.aggDealQty
                    val leftQty = item.aggLeftQty

                    pv_hgraph.progress = if (dealQty == 0) {
                        0.0F
                    } else {
                        (dealQty.toFloat() / (dealQty.toFloat() + leftQty.toFloat())) * 100.0F
                    }

                    tv_dealt_volume.text = "$dealQty T"
                    tv_left_volume.text = "$leftQty T"

                    // Offered at, Last Dealt at : 체크 순서 의미 있음
                    val dateTime = item.eventTimestamp?.getMessageDeliveryDateTime() ?: EmptyString
                    val referenceEventNumber = item.referenceEventNumber ?: EmptyString
                    val dateTimeResId = if (referenceEventNumber.isNotEmpty()) {
                        // "F" 로 시작하면 Offered at XXX
                        if (referenceEventNumber.startsWith(EVENT_INITIAL_PRODUCT, true)) {
                            R.string.your_offers_offered_at
                        } else {
                            R.string.your_offers_last_dealt_at
                        }
                    } else -1

                    if (dateTimeResId > -1 && dateTime.isNotEmpty()) {
                        tv_dealt_at_date_time.text = context.getString(dateTimeResId, dateTime)
                    } else {
                        tv_dealt_at_date_time.text = dateTime
                    }

                    tv_cost_title.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                        context.getString(R.string.cost_value)
                    } else {
                        context.getString(R.string.sales)
                    }

                    tv_cost_value.text = currencyFormat.format(item.priceValue.toLong())

                    tv_pol_code.text = item.headPolCode
                    tv_pol_count.text = item.polCount.getCodeCount()
                    tv_pol_name.text = item.headPolName

                    tv_pod_code.text = item.headPodCode
                    tv_pod_count.text = item.podCount.getCodeCount()
                    tv_pod_name.text = item.headPodName

                    setSafeOnClickListener { onClickItem(position) }
                }
            }
        }

        private fun bindFooter(holder: FooterViewHolder, position: Int) {
            // Do Nothing
        }

        class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class Item(val type: Int, val headerItem: HeaderItem?, val cell: Dashboard.Cell?, val footerItem: Any?) {
            data class Builder(
                    private var type: Int = 0,
                    private var headerItem: HeaderItem? = null,
                    private var cell: Dashboard.Cell? = null,
                    private var footerItem: Any? = null) {

                fun type(type: Int) = apply { this.type = type }
                fun headerItem(headerItem: HeaderItem?) = apply { this.headerItem = headerItem }
                fun cellItem(cell: Dashboard.Cell?) = apply { this.cell = cell }
                fun footerItem(footerItem: Any?) = apply { this.footerItem = footerItem }
                fun build() = Item(type, headerItem, cell, footerItem)
            }
        }

        /***********************************************************************************************
         * Set count at On Market, Closed button (By Rx)
         */
        private fun setButtonOffersCount(holder: HeaderViewHolder, headerItem: HeaderItem) {
            with(holder.itemView) {
                btn_closed.text = context.getString(R.string.your_offers_closed_button, headerItem.closedCount)
                btn_on_market.text = context.getString(R.string.your_offers_on_market_button, headerItem.onMarketCount)
            }
        }

        /***********************************************************************************************
         * Set Status On Market, Closed Button UI
         */
        private fun setOnMarketClosedButtonStatus(holder: HeaderViewHolder, isOnMarketClosed: Boolean): Boolean {
            val isSameStatus = isOnMarketList == isOnMarketClosed
            // 상태가 다른 경우에만 처리
            if (!isSameStatus) {
                with(holder.itemView) {

                    setIsOnMarketList(isOnMarketClosed)

                    btn_closed.setTextColor(context.getColor(
                            if (isOnMarketClosed) {
                                R.color.greyish_brown
                            } else {
                                R.color.white
                            }))

                    btn_closed.background = context.getDrawable(
                            if (isOnMarketClosed) {
                                R.drawable.bg_round_corner_dashboard_pale_right_bottom
                            } else {
                                R.drawable.bg_round_corner_dashboard_greyish_right_bottom
                            })

                    btn_closed.typeface =
                            if (isOnMarketClosed) {
                                tfRegular
                            } else {
                                tfExtraBold
                            }

                    btn_on_market.setTextColor(context.getColor(
                            if (isOnMarketClosed) {
                                R.color.white
                            } else {
                                R.color.greyish_brown
                            }))

                    btn_on_market.background = context.getDrawable(
                            if (isOnMarketClosed) {
                                R.drawable.bg_round_corner_dashboard_greyish_left_bottom
                            } else {
                                R.drawable.bg_round_corner_dashboard_pale_left_bottom
                            })

                    btn_on_market.typeface =
                            if (isOnMarketClosed) {
                                tfExtraBold
                            } else {
                                tfRegular
                            }
                }
            }
            return isSameStatus
        }

        companion object {
            const val HEADER = 0
            const val CONTENT = 1
            const val FOOTER = 2
        }

        class HeaderItem(
                var onMarketCount: Int,
                var closedCount: Int,
                var isOnMarketClosed: Boolean)
    }

    /***********************************************************************************************
     * Swipe overrides & fun
     * left : week 별 detail list
     * right : whole route
     */
    override fun canSwipeLeft(position: Int): Boolean = position > 0

    override fun canSwipeRight(position: Int): Boolean = position > 0

    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        // detail preview 화면 보여줌
        Timber.d("f9: onDismissedBySwipeLeft : ${reverseSortedPositions[0]}")
        goSwipeActivity(0, adapter.datas[reverseSortedPositions[0]].cell)
    }

    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        // detail route 화면 보여줌
        Timber.d("f9: onDismissedBySwipeRight : ${reverseSortedPositions[0]}")
        goSwipeActivity(1, adapter.datas[reverseSortedPositions[0]].cell)
    }

    private fun goSwipeActivity(index: Int, cell: Dashboard.Cell?) {
        cell?.let {
            startActivity(Intent(this, YourOffersSwipeActivity::class.java)
                    .putExtra(Intents.YOUR_OFFER_DASHBOARD_ITEM, cell)
                    .putExtra(Intents.YOUR_OFFER_FRAG_INDEX, index))
        }
    }

    /***********************************************************************************************
     * Filter conditions
     */
    data class FilterCondition(
            var selectedRouteFromTo: RouteFilterPopup.RouteFromTo = RouteFilterPopup.RouteFromTo.INIT,
            var selectedRouteData: RouteFilterPopup.RouteAdapterData? = null,
            var selectedWeek: String = EmptyString,
            var isOnMarketList: Boolean = true,
            var polCode: String = EmptyString,
            var podCode: String = EmptyString,
            var polCodeIsAll: Boolean = false,
            var podCodeIsAll: Boolean = false,
            var isRouteChanged: Boolean = false,
            var isInitRequested: Boolean = false
    )

    companion object {
        var yourOfferType: String = OFFER_TYPE_CODE_BUY
    }
}
