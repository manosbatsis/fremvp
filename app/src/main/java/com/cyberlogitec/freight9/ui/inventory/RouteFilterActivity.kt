package com.cyberlogitec.freight9.ui.inventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.lib.apitrade.PostInventoryListRequest
import com.cyberlogitec.freight9.lib.model.InventoryList
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_ALL
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_INVENTORY
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.split.*
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF_EXPANDED
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.lib.util.Intents.Companion.INVENTORY_LIST
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.trademarket.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_route_filter.*
import kotlinx.android.synthetic.main.body_route_filter.*
import kotlinx.android.synthetic.main.item_market_split_title.view.*
import kotlinx.android.synthetic.main.item_route_filter_result.view.*
import kotlinx.android.synthetic.main.split_market_drag_top.*
import kotlinx.android.synthetic.main.split_market_view.*
import kotlinx.android.synthetic.main.toolbar_inventory.view.*
import kotlinx.android.synthetic.main.toolbar_route_filter.*
import timber.log.Timber


@RequiresActivityViewModel(value = RouteFilterViewModel::class)
class RouteFilterActivity : BaseActivity<RouteFilterViewModel>(),
        SwipeableRecyclerViewTouchListener.SwipeListener {

    private var onSwipeItemLeft: (Int) -> Unit = {}
    private var onSwipeItemRight: (Int) -> Unit = {}

    private lateinit var splitScreen: SplitScreen
    //private lateinit var bottomsheetbehavior : CustomBottomSheetBehavior<FrameLayout>

    private var inventoryLists: MutableList<InventoryList> = mutableListOf()
    private var filterLists: MutableList<InventoryList> = mutableListOf()

    private var polCode: String = EmptyString
    private var podCode: String = EmptyString

    /**
     * inventory list adapter
     */
    private val routeFilterResultAdapter by lazy {
        RouteFilterResultAdapter()
                .apply {
                    onClickItem = { clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, it)) }
                    onSwipeItemLeft = { clickViewParameterAny(Pair(ParameterAny.ANY_SWIPE_LEFT, it)) }
                    onSwipeItemRight = { clickViewParameterAny(Pair(ParameterAny.ANY_SWIPE_RIGHT, it)) }
                }
    }

    /**
     * not used
     * MVP 에서는 사용하지 않음
     * bottom sheet pager adapter
     */
    private val splitAdapter by lazy {
        SplitViewPagerAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_route_filter)
        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        // set custom toolbar
        defaultbarInit(toolbar_route_filter, isEnableNavi = false, menuType = MenuType.DEFAULT, title = getString(R.string.your_inventory_title))

        registViewModelOutputs()
        initView()
        initData()
        initSplitView()
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
                        initView()
                        requestInventoryDatas()
                    }
                }

        viewModel.outPuts.onSuccessRequestInventoryList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            inventoryLists.clear()
                            inventoryLists = (it.body() as List<InventoryList>).toMutableList()
                            inventoryLists.sortedBy { inventoryList -> inventoryList.minYearWeek }
                            routeFilterRecyclerviewInit(inventoryLists)
                        } else {
                            showToast("Fail Inventory list(Http)\n" + it.errorBody())
                            finish()
                        }
                    }
                }

        viewModel.outPuts.onSuccessRequestPolPodList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            filterLists.clear()
                            filterLists = (it.body() as List<InventoryList>).toMutableList()
                        } else {
                            showToast("Fail Pol, Pod list(Http)\n" + it.errorBody())
                            finish()
                        }
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            // wait click event (toolbar left button)
                            ParameterClick.CLICK_TITLE_LEFT -> {
                                Timber.d("f9: toolbar_left_btn clcick")
                                onBackPressed()
                            }
                            // emit event to viewModel -> show drawer menu
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                Timber.d("f9: startActivity(MenuActivity)")
                                startMenuActivity(MenuItem.MENUITEM_YOUR_INVENTORY, MenuActivity::class.java)
                            }
                            // go to Market
                            ParameterClick.CLICK_JUMP_TO_OTHERS -> {
                                startActivityWithFinish(Intent(this, MarketActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                            }
                            // Filter layout click
                            ParameterClick.CLICK_OTHERS -> {
                                /**
                                 * TODO : 추후 전달 예정
                                 */
                                showToast("추후 전달 예정")
                            }
                            ParameterClick.CLICK_FILTER_FROM -> {
                                val routeFromTo = RouteFilterPopup.RouteFromTo.FROM
                                showRouteSelectPopup(Pair(routeFromTo, makeRouteAdapterDatas(routeFromTo)), ::onRouteSelectClick)
                            }
                            ParameterClick.CLICK_FILTER_TO -> {
                                val routeFromTo = RouteFilterPopup.RouteFromTo.TO
                                showRouteSelectPopup(Pair(routeFromTo, makeRouteAdapterDatas(routeFromTo)), ::onRouteSelectClick)
                            }
                            ParameterClick.CLICK_FILTER_ALL -> {
                                val routeFromTo = RouteFilterPopup.RouteFromTo.ALL
                                showRouteSelectPopup(Pair(routeFromTo, makeRouteAdapterDatas(routeFromTo)), ::onRouteSelectClick)
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
                                ParameterAny.ANY_SWIPE_LEFT -> {
                                    val leftIndex = second as Int
                                    showContractPreviewPopup(true, routeFilterResultAdapter.datas[leftIndex])
                                }
                                ParameterAny.ANY_SWIPE_RIGHT -> {
                                    val rightIndex = second as Int
                                    showContractPreviewPopup(false, routeFilterResultAdapter.datas[rightIndex])
                                }
                                ParameterAny.ANY_ITEM_INDEX -> {
                                    val index = second as Int
                                    Timber.d("f9: gotoClickItem : ${routeFilterResultAdapter.datas[index]}")
                                    startActivity(
                                            Intent(this@RouteFilterActivity, InventoryDetailActivity::class.java)
                                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    .putExtra(INVENTORY_LIST, (routeFilterResultAdapter.datas[index]) as InventoryList)
                                    )
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

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    override fun onBackPressed() {
        clickViewParameterClick(ParameterClick.CLICK_JUMP_TO_OTHERS)
        super.onBackPressed()
    }

    /**
     * activity view init
     */
    private fun initView() {
        tv_route_filter_from_code.text = getString(R.string.from)
        tv_route_filter_from_name.text = EmptyString
        tv_route_filter_to_code.text = getString(R.string.to)
        tv_route_filter_to_name.text = EmptyString
    }

    /**
     * activity data init
     */
    private fun initData() {
        setData()
    }

    /**
     * activity data init, set listener
     */
    private fun setData() {
        makeAndInitWeekOfList()
        setListener()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        // wait click event (toolbar left button)
        toolbar_route_filter.toolbar_left_btn.setSafeOnClickListener{
            it.let {
                clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_route_filter.toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
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
        // Filter layout click
        ll_route_filter_filter.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_OTHERS)
        }
    }

    /**
     * pol, pod 선택 후 Request body 구성하고 Inventory list 요청
     */
    private fun requestInventoryDatas(polCode: String = EmptyString, podCode: String = EmptyString) {
        this.polCode = polCode
        this.podCode = podCode
        viewModel.inPuts.requestInventoryList(PostInventoryListRequest(polCode, podCode))
    }

    /**
     * Make Route filter result datas (inventory list)
     */
    @Suppress("UNCHECKED_CAST")
    private fun routeFilterRecyclerviewInit(inventoryLists: List<Any?>? = null) {
        recycler_route_filter_result.apply {
            layoutManager = LinearLayoutManager(this@RouteFilterActivity)
            adapter = this@RouteFilterActivity.routeFilterResultAdapter
        }

        val touchListener = SwipeableRecyclerViewTouchListener(
                recycler_route_filter_result,
                this@RouteFilterActivity
        )

        recycler_route_filter_result.addOnItemTouchListener(touchListener)

        routeFilterResultAdapter.datas.clear()
        inventoryLists?.let { list ->
            routeFilterResultAdapter.datas.addAll(list as MutableList<InventoryList>)
        }
        routeFilterResultAdapter.notifyDataSetChanged()
    }

    /**
     * not used
     * MVP 에서는 left, right swipe 기능 사용하지 않음 (InventoryContractPreviewPopup 표시)
     */
    override fun canSwipeLeft(position: Int): Boolean = false
    override fun canSwipeRight(position: Int): Boolean = false
    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        onSwipeItemLeft(reverseSortedPositions[0])
    }
    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        onSwipeItemRight(reverseSortedPositions[0])
    }

    /**
     * not used
     * MVP 에서는 ALL 만 존재함
     * WeekOf Horizontal Scroll view
     */
    private var weekof_list_tag_seq = 9999980
    private var weekOfList = listOf(EmptyString/*, "201905", "201906", "201907", "201908", "201909", "201910", "201911", "201912"*/)
    private fun makeAndInitWeekOfList() {
        for ((index, weekOf) in weekOfList.withIndex()) {
            val ll_item = LinearLayout(this)
            ll_item.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            ll_item.orientation = LinearLayout.VERTICAL
            ll_item.gravity = Gravity.BOTTOM
            val margin = 12.toDp().toInt()
            val params = ll_item.layoutParams as LinearLayout.LayoutParams
            when (index) {
                0 -> { params.setMargins(0, 0, margin, 0) }
                weekOfList.lastIndex -> { params.setMargins(margin, 0, 0, 0) }
                else -> { params.setMargins(margin, 0, margin, 0) }
            }
            ll_item.layoutParams = params

            ll_item.addView(makeWeekOfTabTextView(weekOf))
            ll_item.addView(makeWeekOfTabImageView())
            ll_item.tag = weekof_list_tag_seq + index
            ll_route_filter_weekof.addView(ll_item)
        }
        clickWeekOfTabProcess( 0)
    }

    /**
     * not used
     * MVP 에서는 ALL 만 존재함
     */
    private fun makeWeekOfTabTextView(titleWeekOf: String) : TextView {
        val textview = TextView(this)
        textview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textview.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textview.layoutParams = params
        textview.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textview.setTextAppearance(R.style.txt_opensans_r_16_greyishbrown)
        textview.text = if (titleWeekOf.isEmpty()) getString(R.string.route_filter_all) else getWeek(titleWeekOf)
        return textview
    }

    /**
     * not used
     * MVP 에서는 ALL 만 존재함
     */
    private fun clickWeekOfTabProcess(index: Int) {
        /**
         * TODO : weekOf tab list click 시 index 에 대한 처리될 내용 추가되어야 함
         *  하단 Recycler View refresh (index에 해당되는 WeekOf 의 List)
         */
        val clickTagId = weekof_list_tag_seq + index
        val childCount = ll_route_filter_weekof.childCount
        for (childIndex in 0 until childCount) {
            var selectedIndex = false
            val childView = ll_route_filter_weekof.getChildAt(childIndex) as LinearLayout
            var textColorValue = R.color.greyish_brown
            var viewColorValue = R.color.color_1d1d1d
            if (childView.tag as Int == clickTagId) {
                showToast("click : " +
                        if (weekOfList[index].isEmpty()) getString(R.string.route_filter_all)
                        else getWeek(weekOfList[index]))
                textColorValue = R.color.colorWhite
                viewColorValue = R.color.blue_violet
                selectedIndex = true
                // scroll bar 이동
                childView.parent.requestChildFocus(childView, childView)
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

    /**
     * not used
     * MVP 에서는 ALL 만 존재함
     */
    private fun makeWeekOfTabImageView() : View {
        val imageview = View(this)
        val margin = 2.toDp().toInt()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5.toDp().toInt())
        params.setMargins(0, margin, 0, 0)
        imageview.layoutParams = params
        imageview.setBackgroundColor(getColor(R.color.color_1d1d1d))
        return imageview
    }

    /**
     * Route Filter Result adapter
     */
    private class RouteFilterResultAdapter : RecyclerView.Adapter<RouteFilterResultAdapter.ViewHolder>() {

        lateinit var context: Context
        // TODO : etd 오름차순으로 정렬되어 있는 상태이어야 한다
        val datas = mutableListOf<Any>()
        var onClickItem: (Int) -> Unit = { _: Int -> }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_route_filter_result, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {

                when (datas[position]) {
                    is InventoryList -> {
                        val data = datas[position] as InventoryList
                        tv_filter_item_category.visibility = View.GONE

                        iv_filter_carrier_logo.setImageResource(data.carrierCode.getCarrierIcon(false))
                        tv_filter_carrier_name.text = context.getCarrierCode(data.carrierCode)

                        tv_filter_container_amount.text = "${data.minQty?.toFloat()?.toInt()}-${data.maxQty?.toFloat()?.toInt()}"

                        tv_filter_pol_code.text = data.polCode
                        tv_filter_pol_count.text = data.polCount.getCodeCount()
                        tv_filter_pol_name.text = data.polName

                        tv_filter_pod_code.text = data.podCode
                        tv_filter_pod_count.text = data.podCount.getCodeCount()
                        tv_filter_pod_name.text = data.podName

                        tv_filter_weekof.text = "${context.getWeek(data.minYearWeek)}-${context.getWeek(data.maxYearWeek)}"

                        iv_filter_period_whole.visibility = View.INVISIBLE
                    }
                    is RouteFilterResult -> {
                        val data = datas[position] as RouteFilterResult
                        val etdDate = data.etdDate
                        var showEtd = true
                        if (position > 0) {
                            showEtd = (datas[position-1] as RouteFilterResult).etdDate != etdDate
                        }
                        tv_filter_item_category.visibility = if (showEtd) View.VISIBLE else View.GONE
                        tv_filter_item_category.text = data.etdDate

                        iv_filter_carrier_logo.setImageResource(data.cryrCd.getCarrierIcon(false))
                        tv_filter_carrier_name.text = context.getCarrierCode(data.cryrCd)

                        tv_filter_container_amount.text = "${data.minAmount}-${data.maxAmount}"

                        tv_filter_pol_code.text = data.polCode
                        tv_filter_pol_count.text = data.polCount.getCodeCount()
                        tv_filter_pol_name.text = data.polName

                        tv_filter_pod_code.text = data.podCode
                        tv_filter_pod_count.text = data.podCount.getCodeCount()
                        tv_filter_pod_name.text = data.podName

                        iv_filter_period_whole.visibility = if (data.rangeKind == "P") View.INVISIBLE else View.VISIBLE
                        tv_filter_weekof.text = "${data.startWeekOf}-${data.endWeekOf}"
                    }
                }
                setSafeOnClickListener { onClickItem(position) }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    /**
     * routeSelect : FROM, ALL, TO 선택 시 ui 에 해당 route 표시
     * 선택된 route 에 해당되는 inventory list 요청
     */
    private fun onRouteSelectClick(position: Int, routeFromTo: RouteFilterPopup.RouteFromTo,
                                   routeAdapterData: RouteFilterPopup.RouteAdapterData) {
        Timber.d("f9: onRouteSelectClick - position = $position")
        when (routeFromTo) {
            RouteFilterPopup.RouteFromTo.FROM -> {
                tv_route_filter_from_code.text =
                        if (routeAdapterData.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.polOrPorPortCode
                tv_route_filter_from_name.text = routeAdapterData.polOrPorPortName
            }
            RouteFilterPopup.RouteFromTo.ALL -> {
                tv_route_filter_from_code.text =
                        if (routeAdapterData.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.polOrPorPortCode
                tv_route_filter_from_name.text = routeAdapterData.polOrPorPortName
                tv_route_filter_to_code.text =
                        if (routeAdapterData.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.podOrDelPortCode
                tv_route_filter_to_name.text = routeAdapterData.podOrDelPortName
            }
            RouteFilterPopup.RouteFromTo.TO -> {
                tv_route_filter_to_code.text =
                        if (routeAdapterData.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.podOrDelPortCode
                tv_route_filter_to_name.text = routeAdapterData.podOrDelPortName
            }
            else -> { }
        }

        val polCode = tv_route_filter_from_code.text.toString()
        val podCode = tv_route_filter_to_code.text.toString()
        val polCodeIsAll = (polCode.compareTo(getString(R.string.all), true) == 0)
                || (polCode.compareTo(getString(R.string.from), true) == 0)
        val podCodeIsAll = (podCode.compareTo(getString(R.string.all), true) == 0)
                || (podCode.compareTo(getString(R.string.to), true) == 0)

        requestInventoryDatas(if (polCodeIsAll) EmptyString else tv_route_filter_from_code.text.toString(),
                if (podCodeIsAll) EmptyString else tv_route_filter_to_code.text.toString())
    }

    /**
     * routeSelect : FROM, ALL, TO
     * All 선택 시 모든 From, To list 구성
     * From 선택 시 From 을 포함하고 있는 To list 구성
     * To 선택 시 To 를 포함하고 있는 From list 구성
     */
    private fun makeRouteAdapterDatas(routeFromTo: RouteFilterPopup.RouteFromTo) : List<RouteFilterPopup.RouteAdapterData> {
        var routeAdapterDatas = mutableListOf<RouteFilterPopup.RouteAdapterData>()

        // From, to 중 어떤겄이 채워져 있는지 체크되어야 함.
        when (routeFromTo) {
            RouteFilterPopup.RouteFromTo.FROM -> {
                // "To" check
                var podCode = tv_route_filter_to_code.text
                if ((podCode.toString().compareTo(getString(R.string.all), true) == 0)
                        || (podCode.toString().compareTo(getString(R.string.to), true) == 0)){
                    podCode = EmptyString
                }

                // true : Pod 가 없는 경우, false : Pod 가 있는 경우
                val noPodFilter = podCode.isNullOrEmpty()
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.NONE,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        if (noPodFilter) {
                            // Pol 전부
                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                    index,
                                    RouteFilterPopup.PortKind.POL,
                                    polCode ?: EmptyString,
                                    polName ?: EmptyString,
                                    -1,
                                    RouteFilterPopup.PortKind.NONE,
                                    EmptyString,
                                    EmptyString))
                        } else {
                            // Pod 가 같은 항목만
                            when (this.podCode) {
                                podCode -> {
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            index,
                                            RouteFilterPopup.PortKind.POL,
                                            polCode ?: EmptyString,
                                            polName ?: EmptyString,
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString))
                                }
                                else -> {}
                            }
                        }
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { it.polOrPorPortCode }
                        .toMutableList()
            }
            RouteFilterPopup.RouteFromTo.ALL -> {
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(-1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                index, RouteFilterPopup.PortKind.POL, polCode ?: EmptyString, polName ?: EmptyString,
                                index, RouteFilterPopup.PortKind.POD, podCode ?: EmptyString, podName ?: EmptyString))
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { Pair(it.polOrPorPortCode, it.podOrDelPortCode) }
                        .toMutableList()
            }
            RouteFilterPopup.RouteFromTo.TO -> {
                // "From" check
                var polCode = tv_route_filter_from_code.text
                if ((polCode.toString().compareTo(getString(R.string.all), true) == 0)
                        || (polCode.toString().compareTo(getString(R.string.from), true) == 0)){
                    polCode = EmptyString
                }

                // true : Pol 가 없는 경우, false : Pol 가 있는 경우
                val noPolFilter = polCode.isNullOrEmpty()
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(-1,
                        RouteFilterPopup.PortKind.NONE,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        if (noPolFilter) {
                            // Pod 전부
                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                    -1,
                                    RouteFilterPopup.PortKind.NONE,
                                    EmptyString,
                                    EmptyString,
                                    index,
                                    RouteFilterPopup.PortKind.POD,
                                    podCode ?: EmptyString,
                                    podName ?: EmptyString))
                        } else {
                            // Pol 이 같은 항목만
                            when (this.polCode) {
                                polCode -> {
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString,
                                            index,
                                            RouteFilterPopup.PortKind.POD,
                                            podCode ?: EmptyString,
                                            podName ?: EmptyString))
                                }
                                else -> {}
                            }
                        }
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { it.podOrDelPortCode }
                        .toMutableList()
            }
            else -> { }
        }

        return routeAdapterDatas
    }

    /**
     * not used
     */
    data class RouteFilterResult(
            var etdDate: String = EmptyString,
            var cryrCd: String = EmptyString,
            var polCode: String = EmptyString,
            var polCount: Int = 0,
            var polName: String = EmptyString,
            var podCode: String = EmptyString,
            var podCount: Int = 0,
            var podName: String = EmptyString,
            var rangeKind: String = EmptyString,          // partial or whole
            var minAmount: Int = 0,
            var maxAmount: Int = 0,
            var startWeekOf: String = EmptyString,
            var endWeekOf: String = EmptyString
    )

    /**
     * not used
     * MVP 에서는 사용하지 않음
     * bottom_sheet_frameout.visibility = View.VISIBLE or View.GONE
     * Bottom Split view : Live Deal Price, Deals by Voyage Week, All bid/ask on market, Your Offers on Market
     */
    private var bottomSheetState = BottomSheetBehavior.STATE_HALF_EXPANDED
    
    // STATE_COLLAPSED(0.0F), STATE_HALF_EXPANDED(0.078), STATE_EXPANDED(1.0)
    private var bottomSheetSlideOffset: Float = SPLIT_SLIDE_HALF_EXPANDED
    
    private fun initSplitView(splitDisplayCategory: SplitDisplayCategory) {
        bottom_sheet_frameout.visibility = View.GONE
        splitScreen = SplitScreen(SplitUiData(this, splitDisplayCategory,
                bottom_sheet_frameout, BottomSheetBehavior.STATE_HALF_EXPANDED, EmptyString),
                ::receiveSplitViewEvent, ::receiveSplitViewSlideOffset)
        val dm: DisplayMetrics = applicationContext.resources.displayMetrics
        splitScreen.setHalfExpandRatio(SplitConst.SPLIT_TITLE_HEIGHT_80.toPx().toFloat()/dm.heightPixels)
    }

    /**
     * not used
     * MVP 에서는 사용하지 않음
     */
    private fun initSplitView() {
        splitAdapter.add(resources.getString(R.string.split_market_live_deal_price))
        splitAdapter.add(resources.getString(R.string.split_market_deals_by_voyage_week))
        splitAdapter.add(resources.getString(R.string.split_market_all_offers_on_market))
        splitAdapter.add(resources.getString(R.string.split_market_your_offers_on_market))

        vp_market_split.apply {
            adapter = this@RouteFilterActivity.splitAdapter
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_1, MarketLiveDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_2, MarketWeekDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_3, MarketAllOfferFragment.newInstance(viewModel))
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
        initSplitView(SplitDisplayCategory.LiveDealPrice)
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
                changeSplitViewTitle(SplitDisplayCategory.AllOffersOnMarket)}
            3-> { ll_container_body_4.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourOffersOnMarket)}
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
}