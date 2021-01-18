package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_TRADE_MARKET
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.startMenuActivity
import com.cyberlogitec.freight9.ui.buyorder.BuyOrderActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.sellorder.SellOrderActivity
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.cyberlogitec.freight9.ui.trademarket.MarketOfferPreviewFragment
import com.cyberlogitec.freight9.ui.trademarket.MarketOfferRouteFragment
import com.github.mikephil.charting.utils.Utils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_market_watchweek.*
import kotlinx.android.synthetic.main.act_market_watchweek.ll_container_body_prev
import kotlinx.android.synthetic.main.act_market_watchweek.ll_container_body_route
import kotlinx.android.synthetic.main.popup_chart_info.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.toolbar_left_btn
import kotlinx.android.synthetic.main.toolbar_common.view.toolbar_right_btn
import kotlinx.android.synthetic.main.toolbar_watch_deal.*
import kotlinx.android.synthetic.main.toolbar_watch_deal.view.*
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


@RequiresActivityViewModel(value = MarketWatchViewModel::class)
class MarketWatchWeekActivity : BaseActivity<MarketWatchViewModel>() {


    private lateinit var chartRequest: PostMarketWatchProductWeekDetailChartListRequest
    private var pageList: ArrayList<String> = ArrayList()
    private lateinit var requestFilter: PostMarketWatchChartRequest
    private lateinit var selectedBaseWeek: String
    private lateinit var carrierList: ArrayList<Carrier>
    private lateinit var paymentList: ArrayList<Payment>
    private lateinit var chartList: ArrayList<MarketWatchChartList.WeekItems>
    var baseDate = Calendar.getInstance()
    private var gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_market_watchweek)
        (application as App).component.inject(this)

        window.statusBarColor = getColor(R.color.color_1d1d1d)

        initToolbar()

        pageList.addAll(resources.getStringArray(R.array.tab_deal_chart))
        requestFilter = intent.extras["filter"] as PostMarketWatchChartRequest
        selectedBaseWeek = intent.extras["week"] as String
        carrierList = intent.extras["carriers"] as ArrayList<Carrier>
        paymentList = intent.extras["payment"] as ArrayList<Payment>
        chartList = intent.extras["chartLists"] as ArrayList<MarketWatchChartList.WeekItems>
        chartRequest = PostMarketWatchProductWeekDetailChartListRequest(requestFilter.companyCodes, requestFilter.containerTypeCode, requestFilter.marketTypeCode, requestFilter.paymentTermCode,requestFilter.pol,requestFilter.pod,selectedBaseWeek,requestFilter.rDTermCode,"daily","")
        baseDate.set(Calendar.YEAR,selectedBaseWeek.substring(0,4).toInt())
        baseDate.set(Calendar.WEEK_OF_YEAR, selectedBaseWeek.substring(4,6).toInt())
        initListener()
        initView()
        initDetailRouteView()
        initRoute()
        initFragment()
//        checkTabBidAsk(SimpleDateFormat("yyyyww").format(baseDate.time))


    }

    private fun initListener() {
        viewModel.outPuts.onSuccessLoadChartSetting()
                .bindToLifecycle(this)
                .subscribe {
                    when(it.interVal){
                        getString(R.string.market_watch_chart_1day) -> {chartRequest.interval = "daily"}
                        getString(R.string.market_watch_chart_1week) -> {chartRequest.interval = "weekly"}
                        getString(R.string.market_watch_chart_1month) -> {chartRequest.interval = "monthly"}
                    }
                    requestWeekDetailChartList()

                }
        viewModel.outPuts.viewSplitPopupDetail()
                .bindToLifecycle(this)
                .subscribe {
                    ll_container_body_prev.visibility=View.VISIBLE
                    ll_container_body_route.visibility=View.INVISIBLE
                    changeSelectTab(textView)
                    showDetailRoute(true)
                }
        viewModel.outPuts.viewSplitPopupRoute()
                .bindToLifecycle(this)
                .subscribe {
                    ll_container_body_prev.visibility=View.INVISIBLE
                    ll_container_body_route.visibility=View.VISIBLE
                    changeSelectTab(textView2)
                    showDetailRoute(true)
                }

        viewModel.outPuts.gotoSellOrder()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityForResult(Intent(this, SellOrderActivity::class.java).putExtra(Intents.OFFER_ITEM, it), MarketActivity.ORDER_REQUEST)
                }
        viewModel.outPuts.gotoBuyOrder()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityForResult(Intent(this, BuyOrderActivity::class.java).putExtra(Intents.OFFER_ITEM, it), MarketActivity.ORDER_REQUEST)
                }
        viewModel.outPuts.onSuccessRequestWeekChartList()
                .bindToLifecycle(this)
                .subscribe {
                    //stomp 연결 시작
                    if(::mStompClient.isInitialized && mStompClient.isConnected) {
                        compositeDisposable?.remove(dispTopic)
                        setSubscription(makeStompUrl(chartRequest))
                    }else {
                        connectStomp(makeStompUrl(chartRequest))
                    }
                }
    }
    private fun requestWeekDetailChartList() {
        viewModel.inPuts.requestWeekDetailChartList(chartRequest)
        viewModel.inPuts.requestWeekDealHistory(chartRequest)
        viewModel.inPuts.requestBid(chartRequest)
        val req = chartRequest.copy()
        req.offerTypeCode = OFFER_TYPE_CODE_SELL
        viewModel.inPuts.requestAsk(req)

        if(::mStompClient.isInitialized && mStompClient.isConnected) {
            compositeDisposable?.remove(dispTopic)
            setSubscription(makeStompUrl(chartRequest))
        }else {
            connectStomp(makeStompUrl(chartRequest))
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun initView() {

        val sdf = SimpleDateFormat("ww, yyyy")
        val weekFormat = SimpleDateFormat("yyyyww")
        iv_arror_left.setOnClickListener {
            if(chartList.minBy { it.baseYearWeek }!!.baseYearWeek == weekFormat.format(baseDate.time))
                return@setOnClickListener
            baseDate.add(Calendar.WEEK_OF_YEAR, -1)
            tv_baseweek.text = "Week ${sdf.format(baseDate.time)}"
            viewModel.inPuts.changeBaseWeek(weekFormat.format(baseDate.time))
            chartRequest.baseYearWeek = weekFormat.format(baseDate.time)
            requestWeekDetailChartList()
        }
        iv_arror_right.setOnClickListener {
            if(chartList.maxBy { it.baseYearWeek }!!.baseYearWeek == weekFormat.format(baseDate.time))
                return@setOnClickListener
            baseDate.add(Calendar.WEEK_OF_YEAR, +1)
            tv_baseweek.text = "Week ${sdf.format(baseDate.time)}"
            viewModel.inPuts.changeBaseWeek(weekFormat.format(baseDate.time))
            chartRequest.baseYearWeek = weekFormat.format(baseDate.time)
            requestWeekDetailChartList()
        }
        baseDate.set(Calendar.DAY_OF_WEEK, 7)
        tv_baseweek.text = "Week ${sdf.format(baseDate.time)}"




    }
//    private fun checkTabBidAsk(baseWeek: String) {
//
//        var data = chartList.filter { it.baseYearWeek.equals(baseWeek) }.firstOrNull()
//        if(data != null) {
//            when(data.status) {
//                "Trading Closed" -> {
//                    // bid, ask 를 없앤다
//                    pageList.remove(resources.getString(R.string.market_watch_deal_bid))
//                    pageList.remove(resources.getString(R.string.market_watch_deal_ask))
//                }
//                else ->  {
//                    // bid, ask를 보여준다
//                    if(!pageList.contains(resources.getString(R.string.market_watch_deal_bid)))
//                        pageList.add(resources.getString(R.string.market_watch_deal_bid))
//                    if(!pageList.contains(resources.getString(R.string.market_watch_deal_ask)))
//                        pageList.add(resources.getString(R.string.market_watch_deal_ask))
//
//                }
//            }
//        }else {
//            pageList.remove(resources.getString(R.string.market_watch_deal_bid))
//            pageList.remove(resources.getString(R.string.market_watch_deal_ask))
//        }
//        viewPager2.adapter!!.notifyDataSetChanged()
//        //Tab Width 변경
//        for (i in pageList.indices) {
//            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
//            val params = layout.layoutParams as LinearLayout.LayoutParams
//            params.weight = 0f
//            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
//            layout.layoutParams = params
//        }
//
//    }

    private fun initDetailRouteView() {

        iv_arror_back.setSafeOnClickListener {
            showDetailRoute(false)
            viewModel.refreshSplitPopupDeatil.onNext(Bor())
        }

        textView.setOnClickListener {
            viewModel.inPuts.swipeToOfferDetail(0)
            changeSelectTab(it as TextView)
        }
        textView2.setOnClickListener {
            viewModel.inPuts.swipeToOfferRoute(0)
            changeSelectTab(it as TextView)
        }
        showDetailRoute(false)
    }

    private fun changeSelectTab(view: TextView?) {
        when(view) {
            textView -> {
                textView!!.typeface = ResourcesCompat.getFont(this, R.font.opensans_extrabold)
                textView!!.setTextColor(getColor(R.color.white))
                textView2!!.typeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
                textView2!!.setTextColor(getColor(R.color.greyish_brown))
            }
            textView2 -> {
                textView!!.typeface = ResourcesCompat.getFont(this, R.font.opensans_regular)
                textView!!.setTextColor(getColor(R.color.greyish_brown))
                textView2!!.typeface = ResourcesCompat.getFont(this, R.font.opensans_extrabold)
                textView2!!.setTextColor(getColor(R.color.white))
            }
        }
    }

    private fun initRoute() {

        tv_pol_cd.text = requestFilter.pol
        tv_pod_cd.text = requestFilter.pod

    }

    private fun initToolbar() {
        toolbar_market_watch_week.setBackgroundColor(getColor(R.color.color_1d1d1d))

        defaultbarInit(toolbar_market_watch_week,menuType = MenuType.CROSS)
        toolbar_market_watch_week.setBackgroundColor(getColor(R.color.color_1d1d1d))
        toolbar_market_watch_week.toolbar_left_btn.visibility = View.INVISIBLE

        // wait click event (toolbar left button)
        toolbar_market_watch_week.toolbar_left_btn.setOnClickListener{
            it?.let {
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_market_watch_week.toolbar_right_btn.setOnClickListener {
            onBackPressed()
        }

        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivity(MENUITEM_TRADE_MARKET, MenuActivity::class.java)
                }

        toolbar_market_watch_week.iv_information.setOnClickListener {
            showFilterDetailPopup()
        }

    }
    private fun initFragment() {

        //Attach Viewpager to tablayout
        viewPager2.adapter = ViewPagerAdapter(pageList,this)
        viewPager2.isUserInputEnabled = false
        viewPager2.offscreenPageLimit = 3


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let { unselectedTab ->
                    ResourcesCompat.getFont(unselectedTab.parent!!.context, R.font.opensans_regular).also {
                        it?.let { setTabTitleTypeface(unselectedTab.position, it) }
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { selectedTab ->
                    ResourcesCompat.getFont(selectedTab.parent!!.context, R.font.opensans_extrabold).also {
                        it?.let { setTabTitleTypeface(selectedTab.position, it) }
                    }
                }
            }
        })

        TabLayoutMediator(tabLayout, viewPager2, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            (viewPager2.adapter as ViewPagerAdapter).also {
                tab.text = it.pageList[position]
            }
        }).attach()

        //Tab Width 변경
        for (i in pageList.indices) {
            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }

        //init offerdetail/route fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_prev, MarketOfferPreviewFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_route, MarketOfferRouteFragment.newInstance(viewModel))
                .commit()
    }

    override fun onBackPressed() {
        if(ll_detail_route.visibility == View.VISIBLE)
            showDetailRoute(false)
        else
            super.onBackPressed()
    }

    private fun showDetailRoute(isShow: Boolean) {
        when(isShow) {
            true -> {ll_detail_route.visibility = View.VISIBLE}
            false -> {ll_detail_route.visibility = View.INVISIBLE}
        }

    }

    private fun showFilterDetailPopup() {

        val mView : View = layoutInflater.inflate(R.layout.popup_chart_info, null)
        val mPopupWindow = PopupWindow(mView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false)

        mPopupWindow.setBackgroundDrawable(getDrawable(R.drawable.bg_round_corner_8_purpley_blue))
        mPopupWindow.isOutsideTouchable = true
        //data setting
        mView.tv_pol.text = requestFilter.pol
        mView.tv_pol_detail.text = requestFilter.polDetail
        mView.tv_pod.text = requestFilter.pod
        mView.tv_pod_detail.text = requestFilter.podDetail

        //carriers
        mView.tv_carrier.text = checkCarrierFilterName(carrierList)
        when(requestFilter.rDTermCode) {
            "01" -> { mView.tv_rdterm.text = getString(R.string.rd_term_type_cycy)}
            "02" -> { mView.tv_rdterm.text = getString(R.string.rd_term_type_cydoor)}
            "03" -> { mView.tv_rdterm.text = getString(R.string.rd_term_type_doorcy)}
            "04" -> { mView.tv_rdterm.text = getString(R.string.rd_term_type_doordoor)}
        }
        when(requestFilter.containerTypeCode) {
            "01" -> { mView.tv_container_type.text = getString(R.string.full_container)}
            "02" -> { mView.tv_container_type.text = getString(R.string.rf_container)}
            "03" -> { mView.tv_container_type.text = getString(R.string.soc_container)}
            "04" -> { mView.tv_container_type.text = getString(R.string.empty_container)}
        }
        when(requestFilter.paymentTermCode) {
            "01" -> { mView.tv_container_type.text = getString(R.string.full_container)}
        }

        mView.tv_pay_plan.text = checkPaymentFilterName(paymentList)


        //data setting end

        mPopupWindow.showAtLocation(window.decorView.rootView,Gravity.TOP, 0, Utils.convertDpToPixel(70f).toInt())

        val container: View
        container = (if (mPopupWindow.background == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPopupWindow.contentView.parent
            } else {
                mPopupWindow.contentView
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPopupWindow.contentView.parent.parent
            } else {
                mPopupWindow.contentView.parent
            }
        }) as View
        val context: Context = mPopupWindow.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.95f
        wm.updateViewLayout(container, p)
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

    private fun checkPaymentFilterName(list: ArrayList<Payment>): CharSequence? {
        var type = ""
        var plan = ""

        for(data in list) {
            if(data.selected!!){
                when(data.dataType) {
                    "type" -> {
                        when(data.paymenttypecode) {
                            "P" -> { type = resources.getString(R.string.payplan_prepaid)
                                requestFilter.paymentTermCode = "01"}
                            "C" -> { type = resources.getString(R.string.payplan_collect)
                                requestFilter.paymentTermCode = "02"}
                        }
                    }
                    "plan" -> {plan = "${data.iniPymtRto!!}% - ${data.midTrmPymtRto!!}% - ${data.balRto!!}%"}
                }
            }
        }
        return "$type ($plan)"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                MarketWatchActivity.PICK_CHART_SETTING_REQUEST -> {
                    if (data != null) {
                        when(viewModel) {
                            is MarketWatchViewModel -> {
                                val setting = data.getSerializableExtra(Intents.MARKET_WATCH_CHART_SETTING) as Chart
                                viewModel.inPuts.storeChartSetting(setting)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setTabTitleTypeface(position: Int, type: Typeface){
        val tabLayout : LinearLayout = ((tabLayout.getChildAt(0) as ViewGroup)).getChildAt(position) as LinearLayout
        val tabTextView: TextView = tabLayout.getChildAt(1) as TextView
        tabTextView.typeface = type
    }

    inner class ViewPagerAdapter(val pageList: ArrayList<String>, fragment: FragmentActivity) : FragmentStateAdapter(fragment){
        private var fragments = mutableListOf<Fragment>()
        override fun getItemCount(): Int = pageList.size
        override fun createFragment(position: Int): Fragment {
            when(pageList[position]){
                resources.getString(R.string.market_watch_deal_chart)->return MarketWatchDealFragment(viewModel)
                resources.getString(R.string.market_watch_deal_history)->return MarketWatchDealHistoryFragment(viewModel)
                resources.getString(R.string.market_watch_deal_bid)->{
                    val fragment = MarketWatchOfferListFragment(viewModel)
                    fragment.setOfferUiType(OFFER_TYPE_CODE_BUY)
                    fragments.add(fragment)
                    return fragment
                }
                resources.getString(R.string.market_watch_deal_ask)->{
                    val fragment = MarketWatchOfferListFragment(viewModel)
                    fragment.setOfferUiType(OFFER_TYPE_CODE_SELL)
                    fragments.add(fragment)
                    return fragment
                }

                else->{
                    val fragment = MarketWatchDealFragment(viewModel)
                    fragments.add(fragment)
                    return fragment
                }
            }
        }
    }

    /*
    stomp 연결
     */

    private lateinit var mStompClient: StompClient

    private var response: String = ""

    private var compositeDisposable: CompositeDisposable? = null

    private lateinit var dispTopic: Disposable
    private lateinit var dispTopic2: Disposable

    private val usrId = "name"

    private fun connectStomp(url: String) {
        Timber.d("connect stomp $url")
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.WS_URL_WATCH)

        val share = SharedPreferenceManager(this)
        val headers: MutableList<StompHeader> = ArrayList()
        headers.add(StompHeader(usrId, share.name))

        headers.add(StompHeader("Authorization", share.token))

//        mStompClient.withServerHeartbeat(5000)
//        mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)

        resetSubscriptions()

        val dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {  setMessage("Stomp connection error",true) }
                .subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {setMessage("Stomp connection opened", true)
                        }
                        LifecycleEvent.Type.ERROR -> {
                            setMessage("Stomp connection error \n${lifecycleEvent.exception}",true)
                            if(!this.isDestroyed)
                                doNetworkCheck()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            setMessage("Stomp connection closed", true)
                            resetSubscriptions()
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> setMessage("Stomp failed server heartbeat", true)
                    }
                }

        compositeDisposable?.add(dispLifecycle)

        setSubscription(url)

        mStompClient.connect(headers)
    }
    private fun setSubscription(url: String) {
        Timber.d("setsubscription $url")
        // Receive greetings
        dispTopic = mStompClient.topic("/topic/${url}")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { t -> setMessage("doOnError $t", true) }
                .subscribe({ topicMessage: StompMessage ->
                    setMessage("Received ${topicMessage.payload}", true)
                    val data: MarketWatchProductWeekDetailChartList = gson.fromJson(topicMessage.payload, MarketWatchProductWeekDetailChartList::class.java)
                    runOnUiThread {
                     //여기서 ui update
                        viewModel.onSuccessRequestWeekChartList.onNext(data)
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
    private lateinit var networkCheckDisp: Disposable
    private fun doNetworkCheck() {
        if(::networkCheckDisp.isInitialized)
            networkCheckDisp.dispose()
        networkCheckDisp = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(checkNetwork()) {
                        connectStomp(makeStompUrl(chartRequest))
                        networkCheckDisp.dispose()
                    }
                }
    }
    private fun checkNetwork():Boolean {

        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun makeStompUrl(filter: PostMarketWatchProductWeekDetailChartListRequest): String {
        var url = ""
        url = "${filter.containerTypeCode}_${filter.marketTypeCode}_${filter.paymentTermCode}_${filter.pol}_${filter.pod}_${filter.baseYearWeek}_${filter.rDTermCode}_${filter.interval}"
        return url
    }
    private fun makeStompUrl2(filter: PostMarketWatchProductWeekDetailChartListRequest): String {
        var url = ""
        url = "${filter.containerTypeCode}_${filter.marketTypeCode}_${filter.paymentTermCode}_${filter.pol}_${filter.pod}_${filter.rDTermCode}"
        return url
    }
}
