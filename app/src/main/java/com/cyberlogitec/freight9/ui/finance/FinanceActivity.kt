package com.cyberlogitec.freight9.ui.finance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_DEAL
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_DUE
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_POD_ETA
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_POL_ETD
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_CASES
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_DATE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_INVOICE_TRANSACTION_TYPE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_ROUTE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_STATUS
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_TRANSACTION_TYPE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_INVOICE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.lib.model.finance.FinanceFilter
import com.cyberlogitec.freight9.lib.model.finance.FinanceSelectValue
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.enums.FinanceSearchTypeFilter
import com.cyberlogitec.freight9.lib.ui.enums.FinanceTypeEnum
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_finance.*
import kotlinx.android.synthetic.main.body_finance.*
import kotlinx.android.synthetic.main.body_finance.view_left_gradation
import kotlinx.android.synthetic.main.item_filter_horizontal.view.*
import kotlinx.android.synthetic.main.popup_search_common.view.*
import kotlinx.android.synthetic.main.toolbar_finance.*
import kotlinx.android.synthetic.main.toolbar_finance.toolbar_right_btn
import kotlinx.android.synthetic.main.toolbar_finance.toolbar_title_text
import timber.log.Timber

@RequiresActivityViewModel(value = FinanceViewModel::class)
class FinanceActivity : BaseActivity<FinanceViewModel>() {

    private var financeType: FinanceTypeEnum = FinanceTypeEnum.TYPE_NONE

    private val financeFilterAdapter by lazy {
        FinanceFilterAdapter()
                .apply {
                    onClickItem = { moveType ->
                        financeType.financeSearchTypeFilter.financeFilters.scrollType = moveType
                        clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, financeType))
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_finance)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (it.hasExtra(Intents.FINANCE_TYPE)) {
                viewModel.inputs.injectIntent(it)
            }
        }
    }

    private fun setRxOutputs() {
        viewModel.outputs.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { type ->

                        // type 이 같으면 return. (activity > Filter or Search popup > onResume)
                        if (financeType.typeCode == type) {
                            return@let
                        }

                        financeType = FinanceTypeEnum.getFinanceTypeEnum(type)

                        ab_finance.setExpanded(true)
                        recycler_finance_list.scrollToPosition(0)
                        setToolbarTitle(financeType)

                        when(financeType.typeCode) {
                            FINANCE_TYPE_PAY_COLLECT_PLAN -> {
                                showToast("Pay/Collect Plan")
                            }
                            FINANCE_TYPE_TRANSACTION_STATEMENT -> {
                                showToast("Transaction Statement")
                            }
                            FINANCE_TYPE_INVOICE -> {
                                showToast("Invoice")
                            }
                        }

                        initData()
                        searchInit(financeType)
                        setFilterDataToHorizontalList(financeType)
                        requestFinance()
                        doProcessFilter(financeType)
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            // show main menu
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                startMenuActivity(
                                        financeType.menuId,
                                        MenuActivity::class.java
                                )
                            }
                            // init search area
                            ParameterClick.CLICK_SEARCH_INIT -> {
                                searchFilter(Constant.EmptyString)
                            }
                            // toggle "items", "description" in Transaction Statement
                            ParameterClick.CLICK_SEARCH_TYPE -> {
                                with(financeType.financeSearchTypeFilter.financeFilters) {
                                    isDefaultSearchType = !isDefaultSearchType
                                    if (isDefaultSearchType) {
                                        // items
                                        searchHint(R.string.finance_search_hint_search_items)
                                    } else {
                                        // description
                                        searchHint(R.string.finance_search_hint_search_description)
                                    }
                                }
                            }
                            // show search input popup
                            ParameterClick.CLICK_SEARCH_POPUP -> {
                                ab_finance.setExpanded(true)
                                recycler_finance_list.scrollToPosition(0)
                                Handler().postDelayed({
                                    showSearchPopup(::onSearchWordInput)
                                }, 50)
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
                                ParameterAny.ANY_SEARCH_FILTER -> {
                                    val type = second as FinanceTypeEnum
                                    doProcessFilter(type)
                                }
                                // go filter activity
                                ParameterAny.ANY_SEARCH_GO_FILTER -> {
                                    val type = second as FinanceTypeEnum
                                    RxActivityResult(this@FinanceActivity)
                                            .start(Intent(this@FinanceActivity, FinanceFilterActivity::class.java)
                                                    .putExtra(Intents.FINANCE_FILTER, type.financeSearchTypeFilter)
                                            )
                                            .subscribe(
                                                    { result ->
                                                        if (result.isOk) {
                                                            financeType.financeSearchTypeFilter = result.data.getSerializableExtra(Intents.FINANCE_FILTER)
                                                                    as FinanceSearchTypeFilter
                                                            viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, financeType))
                                                            Timber.d("f9: OK --> data:  ${financeType.financeSearchTypeFilter.toJson()}")
                                                        } else {
                                                            Timber.d("f9: NotOK --> resultCode:  ${result.resultCode}")
                                                        }
                                                    },
                                                    {
                                                        viewModel.error.onNext(it)
                                                    }
                                            )
                                }
                                else -> {  }
                            }
                        }
                    }
                }
    }

    private fun initData() {
        if (financeType.typeCode == FINANCE_TYPE_PAY_COLLECT_PLAN) {
            setDummyFilterData()
        }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_finance,
                menuType = MenuType.DEFAULT,
                title = getString(R.string.menu_finance_pay_collect_plan),
                isEnableNavi = false)

        recyclerViewInit()
        searchInit()
        setFilterDataToHorizontalList()
        setListener()
    }

    private fun recyclerViewInit() {
        recycler_finance_filter_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
            adapter = this@FinanceActivity.financeFilterAdapter
        }
    }

    private fun setListener() {
        //------------------------------------------------------------------------------------------
        // on click toolbar right button
        toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        // on click search word textivew
        tv_finance_search.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clickViewParameterClick(ParameterClick.CLICK_SEARCH_POPUP)
                v.performClick()
            }
            false
        }

        // on click search word clear
        iv_finance_search_clear.setSafeOnClickListener {
            searchUiInit()
        }

        // on click search type category (transaction statement : items <-> description)
        ll_finance_no.setOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_SEARCH_TYPE)
        }

        // on click filter image
        iv_finance_filter.setSafeOnClickListener {
            financeType.financeSearchTypeFilter.financeFilters.scrollType = FILTER_SCROLL_TO_TOP
            clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, financeType))
        }

        recycler_finance_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1)) {
                    /* TOP of List */
                } else if (!recyclerView.canScrollVertically(1)){
                    /* End of List */
                } else {
                    /* Idle */
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val diffYPosition = dx - dy
                if (diffYPosition > 0) {
                    // Scroll Down
                    if (!isAppBarExpanded()) {
                        ab_finance.setExpanded(true)
                    }
                } else {
                    /* Scroll Up */
                }
            }
        })

        recycler_finance_filter_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val isTopOfList = !recyclerView.canScrollHorizontally(-1)
                view_left_gradation.visibility = if (isTopOfList) View.GONE else View.VISIBLE
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { }
        })
    }

    private fun isAppBarExpanded() = (ab_finance.height - ab_finance.bottom) == 0

    private fun searchUiInit() {
        viewModel.clickViewParameterClick(ParameterClick.CLICK_SEARCH_INIT)
    }

    /*
     * Pay/Collect Plan :  view_top_margin_dummy = VISIBLE, ll_finance_no = GONE
     * Transaction Statement :  view_top_margin_dummy = GONE, ll_finance_no = VISIBLE
     * Invoice :  view_top_margin_dummy = VISIBLE, ll_finance_no = GONE
     */
    private fun searchInit(type: FinanceTypeEnum = financeType) {
        val isSearchTypeVisible = type.financeSearchTypeFilter.isSearchTypeVisible
        view_top_margin_dummy.visibility = if (isSearchTypeVisible) {
            View.GONE
        } else {
            View.VISIBLE
        }
        ll_finance_no.visibility = if (isSearchTypeVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // set hint
        var hintResId = R.string.finance_search_hint_search_buyer_seller
        when(type.typeCode) {
            FINANCE_TYPE_TRANSACTION_STATEMENT -> {
                hintResId = R.string.finance_search_hint_search_items
            }
            FINANCE_TYPE_INVOICE -> {
                hintResId = R.string.finance_search_hint_search_issuer
            }
            else -> {  }
        }
        searchHint(hintResId)
    }

    private fun onSearchWordInput(searchWord: String) {
        searchFilter(searchWord)
    }

    private fun searchHint(hintResId: Int) {
        tv_finance_search.hint = getString(hintResId)
    }

    private fun searchFilter(searchWord: String) {
        financeType.financeSearchTypeFilter.financeFilters.searchValue = searchWord
        viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, financeType))
    }

    private fun showSearchPopup(onSearchWordInput: ((String) -> Unit)) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_search_common, null)
        if (financeType.typeCode != FINANCE_TYPE_TRANSACTION_STATEMENT) {
            val llParam = view.view_search_top.layoutParams as LinearLayout.LayoutParams
            llParam.height = 72.toDp().toInt()
            view.view_search_top.layoutParams == llParam
        }
        val popupWindow = FinanceSearchPopup(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true,
                tv_finance_search.text.toString(),
                if (financeType.typeCode == FINANCE_TYPE_TRANSACTION_STATEMENT) {
                    getString(if (financeType.financeSearchTypeFilter.financeFilters.isDefaultSearchType) {
                        financeType.financeSearchTypeFilter.searchHintResIdDefault
                    } else {
                        financeType.financeSearchTypeFilter.searchHintResIdExtra
                    })
                } else {
                    getString(financeType.financeSearchTypeFilter.searchHintResIdDefault)
                },
                onSearchWordInput
        )
        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0 )
    }

    /*
    * [Filter horizontal layout]
    *
    * > FINANCE_TYPE_PAY_COLLECT_PLAN
    * Status : All Status, Collected, Uncollected, Collected-UnConfirmed,
    *          Collected +X, Uncollected +X, Collected-UnConfirmed +X
    * Route : ref. CargoTracking
    * Date : ref. CargoTracking
    * Collect Plan : All Plan, Prepaid, Collect
    *
    * > FINANCE_TYPE_TRANSACTION_STATEMENT
    * Transaction Type : All Type, Initial Payment, Mid Term Payment, Remainder Payment
    *                    Initial Payment +X, Mid Term Payment +X, Remainder Payment +X
    * Cases : All Cases, Amount In, Amount Out, Space In, Space Out
    *         Amount In +X, Amount Out +X, Space In +X, Space Out +X
    * Date : ref. CargoTracking
    *
    * > FINANCE_TYPE_INVOICE
    * Transaction Type : All Type, Unpaid, Unconfirmed
    * Date : ref. CargoTracking
    *
    * */
    private fun setFilterDataToHorizontalList(type: FinanceTypeEnum = financeType) {

        financeFilterAdapter.itemList.clear()

        with(type.financeSearchTypeFilter.financeFilters) {
            when (type.typeCode) {
                FINANCE_TYPE_PAY_COLLECT_PLAN -> {
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            0,
                            FILTER_SCROLL_TO_STATUS,
                            payCollectPlanStatus.getBubbleDisplayText(this@FinanceActivity)
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            1,
                            FILTER_SCROLL_TO_ROUTE,
                            getFilterRoute(this)
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            2,
                            FILTER_SCROLL_TO_DATE,
                            getFilterDate(this).first,
                            getFilterDate(this).second
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            3,
                            FILTER_SCROLL_TO_COLLECT_PLAN,
                            collectPlan.getBubbleDisplayText(this@FinanceActivity)
                    ))
                }
                FINANCE_TYPE_TRANSACTION_STATEMENT -> {
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            0,
                            FILTER_SCROLL_TO_TRANSACTION_TYPE,
                            transactionStatementType.getBubbleDisplayText(this@FinanceActivity)
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            1,
                            FILTER_SCROLL_TO_CASES,
                            transactionCases.getBubbleDisplayText(this@FinanceActivity)
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            2,
                            FILTER_SCROLL_TO_DATE,
                            getFilterDate(this).first,
                            getFilterDate(this).second
                    ))
                }
                else -> {
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            0,
                            FILTER_SCROLL_TO_INVOICE_TRANSACTION_TYPE,
                            transactionInvoiceType.getBubbleDisplayText(this@FinanceActivity)
                    ))
                    financeFilterAdapter.itemList.add(FinanceSelectValue(
                            1,
                            FILTER_SCROLL_TO_DATE,
                            getFilterDate(this).first,
                            getFilterDate(this).second
                    ))
                }
            }
        }
        financeFilterAdapter.notifyDataSetChanged()
    }

    private fun getFilterRoute(financeFilter: FinanceFilter) = with(financeFilter) {
            val allPols = routePolCode.isEmpty() || routePolCode == getString(R.string.finance_filters_route_pol_all)
            val allPods = routePodCode.isEmpty() || routePodCode == getString(R.string.finance_filters_route_pod_all)
            if (allPols && allPods) {
                getString(R.string.finance_filter_all_routes)
            } else if (allPols && !allPods) {
                getString(R.string.finance_filters_route_pol_all) + " - " + routePodCode
            } else if (!allPols && allPods) {
                routePolCode + " - " + getString(R.string.finance_filters_route_pod_all)
            } else {
                "$routePolCode - $routePodCode"
            }
        }

    private fun getFilterDate(financeFilter: FinanceFilter) = with(financeFilter) {
        val prefix: String
        var secondDateFilter = ""
        when (dateType) {
            FILTER_DATE_DEAL -> {
                if (dateStarts.year < 1 || dateStarts.month < 1 || dateStarts.day < 1) {
                    prefix = getString(R.string.finance_filter_all_deal_date)
                } else {
                    prefix = getString(R.string.finance_filters_date_deal)
                    secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                            " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
                }
            }
            FILTER_DATE_DUE -> {
                if (dateStarts.year < 1 || dateStarts.month < 1 || dateStarts.day < 1) {
                    prefix = getString(R.string.finance_filter_all_due_date)
                } else {
                    prefix = getString(R.string.finance_filters_date_due)
                    secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                            " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
                }
            }
            FILTER_DATE_POL_ETD, FILTER_DATE_POD_ETA -> {
                prefix = if (dateType == CargoTrackingFilterDate.FILTER_DATE_POL_ETD) {
                    getString(R.string.finance_time_symbol_etd)
                } else {
                    getString(R.string.finance_time_symbol_eta)
                }
                secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                        " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
            }
            else -> {
                prefix = Constant.EmptyString
                secondDateFilter = Constant.EmptyString
            }
        }
        Pair(prefix, secondDateFilter)
    }

    private fun doProcessFilter(type: FinanceTypeEnum = financeType) {
        // set search input number to UI
        tv_finance_search.text = type.financeSearchTypeFilter.financeFilters.searchValue
        iv_finance_search_clear.visibility = if (type.financeSearchTypeFilter.financeFilters.searchValue.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // set extra filter date to UI
        setFilterDataToHorizontalList(type)

        showToast("doProcessFilter...\n" + "search text : ${type.financeSearchTypeFilter.financeFilters.searchValue}")
    }

    private fun setToolbarTitle(type: FinanceTypeEnum = financeType) {
        toolbar_title_text.text = getString(type.titleResId)
    }

    private fun requestFinance() {

    }

    private class FinanceFilterAdapter : RecyclerView.Adapter<FinanceFilterAdapter.ViewHolder>() {

        var itemList = mutableListOf<FinanceSelectValue>()
        var onClickItem: (Int) -> Unit = { _: Int -> }

        override fun getItemCount() = itemList.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_filter_horizontal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {

                setItemMargin(this, position)

                with(itemList[position]) {
                    tv_filter_value_duration.visibility = if (detail.isEmpty()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    tv_filter_value.text = value
                    // ALL From ~ To, ETD From ~ To, ETA From ~ To
                    tv_filter_value_duration.text = "  $detail"

                    setOnClickListener {
                        onClickItem(moveType)
                    }
                }
            }
        }

        private fun setItemMargin(view: View, position: Int) {
            var marginLeft = 16.toDp().toInt()
            if (position == 0) {
                marginLeft = 0.toDp().toInt()
            }
            with(view) {
                val llParams = ll_item_filter.layoutParams as LinearLayout.LayoutParams
                llParams.leftMargin = marginLeft
                ll_item_filter.layoutParams = llParams
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    private fun setDummyFilterData() {
        with(financeType.financeSearchTypeFilter.financeFilters) {
            if (routePolList.isEmpty()) {
                // 첫번째는 빈값 (All POLs)
                routePolList.add(FinanceFilter.Route(
                        code = getString(R.string.finance_filters_route_pol_all),
                        isSelected = true)
                )
                routePolList.add(FinanceFilter.Route("KRPUS", "Pusan"))
                routePolList.add(FinanceFilter.Route("CNTAO", "Qingdao, Shandong"))
                routePolList.add(FinanceFilter.Route("CNSHA", "Shanghai, Shanghai"))
                routePolList.add(FinanceFilter.Route("CNNGB", "Ningbo, Zhejiang"))
                routePolList.add(FinanceFilter.Route("TWKHH", "Kaohsiung City"))
            }

            if (routePodList.isEmpty()) {
                // 첫번째는 빈값 (All PODs)
                routePodList.add(FinanceFilter.Route(
                        code = getString(R.string.finance_filters_route_pod_all),
                        isSelected = true)
                )
                routePodList.add(FinanceFilter.Route("CNSHA", "Shanghai, Shanghai"))
                routePodList.add(FinanceFilter.Route("CNSHK", "Shekou, Guangdong"))
                routePodList.add(FinanceFilter.Route("SGSIN", "Singapore"))
                routePodList.add(FinanceFilter.Route("SAJED", "jeddah"))
                routePodList.add(FinanceFilter.Route("CNYTN", "Yantian, Guangdong"))
            }

            val calendar = java.util.Calendar.getInstance()
            dateStarts = FinanceFilter.Date(calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH),
                    calendar.get(java.util.Calendar.YEAR))
            dateEnds = FinanceFilter.Date(2, 28, 2021)
        }
    }
}