package com.cyberlogitec.freight9.ui.youroffers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.ALL_YN_WHOLE
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.FilterOnMarket.FILTER_ONMARKET
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_DASHBOARD_ITEM_DETAIL
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_NUMBER
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity
import com.cyberlogitec.freight9.ui.youroffers.YourOffersActivity.Companion.yourOfferType
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_your_offers_detail.*
import kotlinx.android.synthetic.main.appbar_your_offers_detail.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.body_your_offers_detail_bottom.*
import kotlinx.android.synthetic.main.body_your_offers_detail_recycler_card.*
import kotlinx.android.synthetic.main.body_your_offers_detail_recycler_header_partial_graph.*
import kotlinx.android.synthetic.main.body_your_offers_detail_recycler_header_partial_table.*
import kotlinx.android.synthetic.main.body_your_offers_detail_recycler_header_whole.*
import kotlinx.android.synthetic.main.item_your_offers_detail.view.*
import kotlinx.android.synthetic.main.popup_your_offers_detail_jump.view.*
import kotlinx.android.synthetic.main.toolbar_your_offers_detail.*
import retrofit2.Response
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

@RequiresActivityViewModel(value = YourOffersDetailViewModel::class)
class YourOffersDetailActivity : BaseActivity<YourOffersDetailViewModel>(){

    private lateinit var cell: Dashboard.Cell
    private lateinit var tradeOfferWrapper: TradeOfferWrapper
    private lateinit var routeDataList: RouteDataList

    private var popupWindow: PopupWindow? = null
    private var borList: BorList = BorList()
    private var isPartial: Boolean = true
    private var isViewTable: Boolean = false
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    /**
     * offer 주차별 리스트의 adapter
     */
    private val yourOfferDetailAdapter by lazy {
        YourOffersDetailAdapter()
                .apply {
                    onClickItem = { lineItem ->
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_OBJECT, lineItem))
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_detail)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    /**
     * activity data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        // set custom toolbar
        defaultbarInit(toolbar_your_offers_detail,
                menuType = MenuType.DEFAULT,
                title = when(yourOfferType) {
                    OFFER_TYPE_CODE_BUY -> {
                        getString(R.string.menu_your_buy_offers)
                    }
                    OFFER_TYPE_CODE_SELL -> {
                        getString(R.string.menu_your_sell_offers)
                    }
                    else -> { "" }
                },
                isEnableNavi = true)
        toolbar_done_btn.visibility = View.GONE
        toolbar_right_tv.visibility = View.GONE
        toolbar_right_btn.visibility = View.VISIBLE
        toolbar_right_btn.setImageResource(R.drawable.btn_more_d)

        yourOffersDetailRecyclerViewInit()
        setLayoutInit()
        setListener()

        requestOfferDetail()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { cell ->
                        Timber.d("f9: onSuccessRefresh")
                        processSuccessRefresh(cell)
                    }
                }

        viewModel.outPuts.onSuccessRequestOfferInfoDetails()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onSuccessRequestOfferInfoDetails")
                        processOfferInfoDetail(it)
                    }
                }

        viewModel.outPuts.onSuccessRouteDataList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { routeDataList ->
                        this.routeDataList = routeDataList
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_CONDITION_DETAIL -> {
                                if(::tradeOfferWrapper.isInitialized) {
                                    showDetailConditionPopup(root_your_offers_detail, viewModel, tradeOfferWrapper)
                                }
                            }
                            ParameterClick.CLICK_WHOLE_ROUTE -> {
                                if (::routeDataList.isInitialized) {
                                    showWholeRoutePopup(root_your_offers_detail, routeDataList, tradeOfferWrapper.borList)
                                }
                            }
                            ParameterClick.CLICK_PRICE_TABLE -> {
                                if(::tradeOfferWrapper.isInitialized) {
                                    showPriceTablePopup(root_your_offers_detail, tradeOfferWrapper, false, true)
                                }
                            }
                            ParameterClick.CLICK_JUMP_TO_OTHERS -> {
                                processMakeNewOffer()
                            }
                            ParameterClick.CLICK_TITLE_LEFT -> {
                                onBackPressed()
                            }
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                showEditPopup(::onCancelOrRevise)
                            }
                            ParameterClick.CLICK_TRANSACTION_STATEMENT -> {
                                processTransactionStatement()
                            }
                            else -> { }
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
                                    processViewTable(second as Boolean)
                                }
                                ParameterAny.ANY_ITEM_OBJECT -> {
                                    gotoDetailPopup(second as Dashboard.Cell.LineItem)
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        /**
         * go to offer history activity
         */
        viewModel.outPuts.onClickViewOfferHistory()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        intent.setClass(this, YourOffersHistoryActivity::class.java)
                        startActivity(intent)
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
                    Timber.d("--> f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    Timber.e("--> f9: error : $it")
                    showToast("Fail (Throwable)\n" + it.message)
                    finish()
                }

        // Offer Discard api 호출 결과
        viewModel.baseOfferOutputs.onSuccessDiscardOffer()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: Discard Offer result : $it")
                        if (it.isSuccessful) {
                            // 성공하면...Max 10 초 동안 loadingDialog 보여주고 time out 되면 finish()
                            viewModel.baseOfferInputs.requestLongProgressBar(Parameter.EVENT)
                            Timber.d("f9: showLoadingDialog() 10 sec")
                        } else {
                            // 실패하면...finish
                            finish()
                        }
                    }
                }

        // Revise, Go Offer 인 경우 activity 호출 호 finish. (Cancel, New Offer push 는 YourOffersActivity 에서 수신)
        viewModel.onSuccessRequestOfferUsingSameCondition
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { triple ->

                        val offerType = triple.first
                        val offerDiscard = triple.second
                        val offer = triple.third

                        Timber.d("f9: Request Same Condition Offer : offerType = $offerType, discard = $offerDiscard, offer = $offer")

                        if (offerType == OFFER_TYPE_CODE_BUY) {
                            startActivityWithFinish(Intent(this, BofWizardActivity::class.java)
                                    .putExtra(Intents.OFFER, offer)
                                    .putExtra(Intents.OFFER_DISCARD, offerDiscard)
                                    .putExtra(Intents.OFFER_BY_MADE_CONDITION, true)
                                    .putExtra(Intents.OFFER_MAKE_STEP, true))
                        } else {
                            // Has Intents.OFFER extra
                            // TODO : masterContractNumber를 알 수 없음...
                            if (offer.masterContractNumber.isEmpty()) {
                                showToast("masterContractNumber is Empty!")
                            } else {
                                startActivityWithFinish(Intent(this, SofWizardActivity::class.java)
                                                .putExtra(Intents.MSTR_CTRK_NR, offer.masterContractNumber)
                                                .putExtra(Intents.OFFER, offer)
                                                .putExtra(Intents.OFFER_DISCARD, offerDiscard)
                                                .putExtra(Intents.OFFER_BY_MADE_CONDITION, true)
                                                .putExtra(Intents.OFFER_MAKE_STEP, true))
                            }
                        }
                    }
                }

        // 10초 pregress bar emitted 인 경우(Cancel 처리에 대한 결과의 Push 수신이 10초 동안 없는 경우)
        // YourOffersActivity 화면으로 이동 후 refresh
        viewModel.baseOfferOutputs.onRequestLongProgressBar()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() 10 sec")
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss()
                        finish()
                    }
                }

        // Event Service 에서 Offer Discard 에 대한 결과를 Push 로 수신하는 경우 실행됨 (refresh is true!)
        viewModel.rxEventOfferDiscard
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { message ->
                        Timber.d("f9: rxEventOfferDiscard = ${message.toJson()}")
                        // 10초 pregress bar 강제 hide !!!
                        loadingDialog.dismiss()
                        // Discard : Cancel 처리 성공, 실패 모두인 경우
                        // Revise : Cancel 처리 성공, 실패 모두인 경우
                        // YourOffersActivity 화면으로 이동 후 refresh (RESULT_OK)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        // wait click event (toolbar left button)
        toolbar_left_btn.setSafeOnClickListener{
            Timber.d("f9: toolbar_left_btn clcick")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
        }

        toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        tv_link_condition_detail.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CONDITION_DETAIL)
        }

        tv_link_whole_route.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_WHOLE_ROUTE)
        }

        tv_price_table.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_PRICE_TABLE)
        }

        iv_view_offer_history.setSafeOnClickListener {
            if (::tradeOfferWrapper.isInitialized) {
                viewModel.inPuts.clickViewOfferHistory(tradeOfferWrapper)
            }
        }

        iv_view_transaction_statement.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TRANSACTION_STATEMENT)
        }

        tv_make_a_new_offer_using_this_condition.setSafeOnClickListener {
            if (::tradeOfferWrapper.isInitialized) {
                clickViewParameterClick(ParameterClick.CLICK_JUMP_TO_OTHERS)
            }
        }

        //------------------------------------------------------------------------------------------
        // View Table, View Graph : Only Partial
        chk_your_offers_detail_view_table.setOnCheckedChangeListener { _, isChecked ->
            clickViewParameterAny(Pair(ParameterAny.ANY_VIEW_CHK, isChecked))
        }
    }

    /**
     * Layout 초기화
     */
    private fun setLayoutInit() {

        // Title Right btn : OnMarket - VISIBLE, Closed - INVISIBLE
        if (::cell.isInitialized) {
            toolbar_right_btn.visibility = if (cell.offerStatus == FILTER_ONMARKET) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }

        // Recycler card
        ll_your_offers_detail_recycler_view_table.visibility = if (isPartial) View.VISIBLE else View.GONE
        view_recycler_bottom_divider.visibility = if (isPartial) View.GONE else View.VISIBLE
        rl_sum.visibility = if (isPartial) View.GONE else View.VISIBLE

        cv_view_offer_dealt_estimated_value.visibility = if (!isPartial) View.GONE else View.VISIBLE
        ll_whole_deal_option.visibility = if (isPartial) View.GONE else View.VISIBLE

        var dealtCostValueTitleResId = -1
        var estimatedTotalCostValueTitleResId = -1
        var bottomButtoonResId = -1
        when(yourOfferType) {
            OFFER_TYPE_CODE_BUY -> {
                dealtCostValueTitleResId = R.string.your_offer_dealt_cost_value
                estimatedTotalCostValueTitleResId = R.string.your_offer_estimated_total_cost_value
                bottomButtoonResId = R.string.buy_order_select_new_sell_offer
            }
            OFFER_TYPE_CODE_SELL -> {
                dealtCostValueTitleResId = R.string.your_offer_dealt_sales_value
                estimatedTotalCostValueTitleResId = R.string.your_offer_estimated_total_sales_value
                bottomButtoonResId = R.string.buy_order_select_new_buy_offer
            }
        }
        tv_dealt_cost_value_title.text = getString(dealtCostValueTitleResId)
        tv_estimated_total_cost_value_title.text = getString(estimatedTotalCostValueTitleResId)
        tv_make_a_new_offer_using_this_condition.text = getString(bottomButtoonResId)

        changeRecyclerHeaderLayout(this.isViewTable, isPartial)
    }

    /**
     * offer detail 요청
     */
    private fun requestOfferDetail() {
        viewModel.inPuts.callRequestOfferInfoDetails(Parameter.EVENT)
    }

    /***********************************************************************************************
     * Inventory detail Recycler view init
     */
    private fun yourOffersDetailRecyclerViewInit() {
        recycler_detail.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(this@YourOffersDetailActivity)
            adapter = this@YourOffersDetailActivity.yourOfferDetailAdapter
        }
    }

    /**
     * Partial(View Table, Hide Table), Whole
     * Buy Offers, Whole Offers 에 따라 Header 의 title 변경되어야 함
     *
     * Whole
     * Buy Offers  : Period, Price(/T), Dealt, Cost Value
     * Sell Offers : Period, Price(/T),  Left, Sales Value
     * > tv_title_whole_dealt, tv_title_whole_value
     *
     * Partial - View Table
     * Buy Offers  : Period, Price(/T), Dealt, Left, Cost Value
     * Sell Offers : Period, Price(/T), Dealt, Left, Sales Value
     * > tv_title_partial_table_value
     *
     * Partial - View Graph
     * Buy Offers  : Period, -graph-, Dealt, Left
     * Sell Offers : Period, -graph-, Dealt, Left
     */
    private fun changeRecyclerHeaderLayout(isViewTable: Boolean, isPartial: Boolean) {

        ll_your_offers_detail_recycler.visibility = View.VISIBLE
        ll_your_offers_detail_recycler_header_partial_graph.visibility = View.GONE
        ll_your_offers_detail_recycler_header_partial_table.visibility = View.GONE
        ll_your_offers_detail_recycler_header_whole.visibility = View.GONE

        if (isPartial) {
            if (isViewTable) {
                ll_your_offers_detail_recycler_header_partial_table.visibility = View.VISIBLE
                tv_title_partial_table_value.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                    getString(R.string.cost_value)
                } else {
                    getString(R.string.sales_value)
                }
            } else {
                ll_your_offers_detail_recycler_header_partial_graph.visibility = View.VISIBLE
            }
        } else {
            ll_your_offers_detail_recycler_header_whole.visibility = View.VISIBLE
            tv_title_whole_dealt.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                getString(R.string.dealt)
            } else {
                getString(R.string.left)
            }
            tv_title_whole_value.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                getString(R.string.cost_value)
            } else {
                getString(R.string.sales_value)
            }
        }
    }

    /**
     * 전달 받은 offer cell 정보로 UI card 에 pol, pod 표시
     */
    private fun setPolPodData(cell: Dashboard.Cell) {
        with(cell) {
            carrierItem?.let { items ->
                if (items.isNotEmpty()) {
                    iv_carrier_logo.setImageResource(items.first().carrierCode.getCarrierIcon(false))
                    tv_carrier_name.text = getCarrierCode(items.first().carrierCode)
                    tv_carrier_count.text = items.size.getCodeCount(false)
                }
            }

            tv_pol_name.text = headPolCode
            tv_pol_count.text = polCount.getCodeCount()
            tv_pol_desc.text = headPolName

            tv_pod_name.text = headPodCode
            tv_pod_count.text = podCount.getCodeCount()
            tv_pod_desc.text = headPodName
        }
    }

    /**
     * Set Recycler data...
     */
    private fun setRecyclerData(cell: Dashboard.Cell) {
        cell.lineItem?.let { lineItems ->
            yourOfferDetailAdapter.setData(yourOfferType,
                    this.isPartial,
                    lineItems.sortedBy { it.baseYearWeek })
            yourOfferDetailAdapter.notifyDataSetChanged()
            setLayoutData(cell)
        }
    }

    /**
     * dealt, estimated total cost value 설정
     */
    private fun setLayoutData(cell: Dashboard.Cell) {
        with(cell) {
            /**
             * [only Whole]
             * RecyclerView > Cost, Sales Value 들의 모든 주차의 합
             * Cost Value(OFFER_BUY)   = sumBy(leftAmt)
             * Sales Value(OFFER_SELL) = sumBy(leftAmt)
             */
            lineItem?.let { lineItems ->
                tv_sum_total.text = currencyFormat.format(lineItems.sumByLong {
                    it.leftAmt.toLong()
                })

                /**
                 * [only Partial]
                 * Dealt Cost Value(OFFER_BUY)   = sumBy(dealAmt)
                 * Dealt Sales Value(OFFER_SELL) = sumBy(dealAmt)
                 */
                tv_dealt_cost_value.text = currencyFormat.format(lineItems.sumByLong {
                    it.dealAmt.toLong()
                })

                /**
                 * [only Partial]
                 * Estimated Total Cost Value(OFFER_BUY)   = sumBy((dealAmt + leftAmt))
                 * Estimated Total Sales Value(OFFER_SELL) = sumBy((dealAmt + leftAmt))
                 */
                tv_estimated_total_cost_value.text = currencyFormat.format(lineItems.sumByLong {
                    (it.dealAmt + it.leftAmt).toLong()
                })
            }
        }
    }

    /**
     * screen refresh 시 처리
     */
    private fun processSuccessRefresh(cell: Dashboard.Cell) {
        this.cell = cell
        setPolPodData(cell)

        this.isPartial = cell.allYn != ALL_YN_WHOLE
        setLayoutInit()
        setRecyclerData(cell)
    }

    /**
     * offer detail 정보 설정
     */
    private fun processOfferInfoDetail(offerDetail: Any) {
        when (offerDetail) {
            is TradeOfferWrapper -> {
                this.tradeOfferWrapper = offerDetail
            }
            is Response<*> -> {
                if (offerDetail.isSuccessful) {
                    // BorList 구성 (Card link 에서 사용)
                    if (::cell.isInitialized) {
                        with(cell) {
                            borList.offerNumber = offerNumber
                            borList.cryrCd = getCarrierCode(carrierItem?.first()?.carrierCode)
                            borList.carrierCount = carrierItem?.size
                            borList.locPolCd = headPolCode
                            borList.locPodCd = headPodCode
                            borList.locPolCnt = polCount
                            borList.locPodCnt = podCount
                            borList.locPolNm = headPolName
                            borList.locPodNm = headPodName
                        }
                        val orderTradeOfferDetail = offerDetail.body() as OrderTradeOfferDetail
                        borList.rdTermCode = orderTradeOfferDetail.offerRdTermCode
                        cell.lineItem?.let { lineItems ->
                            lineItems.map { lineItem ->
                                lineItem.offerPrices = orderTradeOfferDetail.offerLineItems.first().offerPrices
                            }
                            tradeOfferWrapper = TradeOfferWrapper(
                                    borList,
                                    orderTradeOfferDetail,
                                    mutableListOf(),
                                    lineItems.toMutableList())
                        }
                    }
                } else {
                    Timber.e("f9: Fail Request Offer Detail(Http)\n${offerDetail.errorBody()}")
                    return
                }
            }
        }
    }

    /*
    * Your Sell Offers > Make a New Buy Offer using This Condition
    * Your Buy Offers  > Make a New Sell Offer using This Condition
    * */
    private fun processMakeNewOffer() {
        if (::tradeOfferWrapper.isInitialized) {
            viewModel.requestOfferUsingSameCondition(Triple(
                    if (yourOfferType == OFFER_TYPE_CODE_BUY) {
                        OFFER_TYPE_CODE_SELL
                    } else {
                        OFFER_TYPE_CODE_BUY
                    },
                    false,
                    tradeOfferWrapper.orderTradeOfferDetail)
            )
        }
    }

    /**
     * View table type, Progress type 의 ui 처리
     */
    private fun processViewTable(isChecked: Boolean) {
        isViewTable = isChecked

        val textColor = ContextCompat.getColor(this@YourOffersDetailActivity,
                if (isViewTable) R.color.color_333333 else R.color.color_c7c7c7)
        tv_your_offers_detail_view_table.setTextColor(textColor)

        val typeface = ResourcesCompat.getFont(this@YourOffersDetailActivity,
                if (isViewTable) R.font.opensans_bold else R.font.opensans_regular)
        tv_your_offers_detail_view_table.typeface = typeface

        changeRecyclerHeaderLayout(isViewTable, this.isPartial)
        yourOfferDetailAdapter.setViewTable(isViewTable)
    }

    /**
     * TODO : View Transaction Statement 화면 이동
     */
    private fun processTransactionStatement() {
        startActivity( Intent(this, YourOffersTransactionActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(Intents.YOUR_OFFER_TYPE, yourOfferType))
    }

    @SuppressLint("InflateParams")
    private fun showEditPopup(onCancelOrRevise: ((Boolean) -> Unit)) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPopup = inflater.inflate(R.layout.popup_your_offers_detail_jump, null)
        popupWindow = YourOffersDetailJumpPopup(viewPopup, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true, this.supportFragmentManager, onCancelOrRevise)
        (popupWindow as YourOffersDetailJumpPopup).initValue()
        (popupWindow as YourOffersDetailJumpPopup).show(viewPopup.rl_popup_your_offers_detail_jump)
        (popupWindow as YourOffersDetailJumpPopup).isOutsideTouchable = true
        viewPopup.btn_your_offers_detail_jump_cancel.setOnClickListener { removePopup() }
    }

    /**
     * offer cancel, revise 처리
     */
    private fun onCancelOrRevise(isCancel: Boolean) {
        if (::tradeOfferWrapper.isInitialized) {
            if (isCancel) {
                viewModel.baseOfferInputs.requestDiscardOffer(tradeOfferWrapper.orderTradeOfferDetail)
            } else {
                viewModel.requestOfferUsingSameCondition(Triple(
                        yourOfferType,
                        true,
                        tradeOfferWrapper.orderTradeOfferDetail)
                )
            }
        }
    }

    /**
     * list item 의 ">" 선택 시 처리
     */
    private fun gotoDetailPopup(lineItem: Dashboard.Cell.LineItem) {
        startActivity(Intent(this, YourOffersDetailPopupActivity::class.java)
                .putExtra(YOUR_OFFER_NUMBER, this.cell.offerNumber)
                .putExtra(YOUR_OFFER_DASHBOARD_ITEM_DETAIL, lineItem))
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    /***********************************************************************************************
     * Your Offers Detail Recycler view adapter
     * [Buy Offers / Sell Offers]
     * Cost Value = Price X Dealt
     */
    private class YourOffersDetailAdapter : RecyclerView.Adapter<YourOffersDetailAdapter.ViewHolder>() {

        lateinit var context: Context
        var onClickItem: (Dashboard.Cell.LineItem) -> Unit = {}
        val data = mutableListOf<Dashboard.Cell.LineItem>()
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

        private var yourOfferType: String = OFFER_TYPE_CODE_BUY
        private var isPartial: Boolean = true
        private var isViewTable: Boolean = false

        fun setViewTable(isViewTable: Boolean) {
            this.isViewTable = isViewTable
            notifyDataSetChanged()
        }

        fun setData(yourOfferType: String, isPartial: Boolean, lineItem: List<Dashboard.Cell.LineItem>) {
            this.yourOfferType = yourOfferType
            this.isPartial = isPartial
            this.data.clear()
            this.data.addAll(lineItem.toMutableList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_your_offers_detail, parent, false))
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                ll_your_offers_detail_recycler_partial_graph.visibility = View.GONE
                ll_your_offers_detail_recycler_partial_table.visibility = View.GONE
                ll_your_offers_detail_recycler_whole.visibility = View.GONE
                if (isPartial) {
                    if (isViewTable) {
                        ll_your_offers_detail_recycler_partial_table.visibility = View.VISIBLE
                    } else {
                        ll_your_offers_detail_recycler_partial_graph.visibility = View.VISIBLE
                    }
                } else {
                    ll_your_offers_detail_recycler_whole.visibility = View.VISIBLE
                }

                if (isPartial) {
                    if (isViewTable) {
                        setPartialTableItems(this, data[position])
                    } else {
                        setPartialGraphItems(this, data[position])
                    }
                } else {
                    setWholeItems(this, data[position])
                }
            }
        }

        private fun setPartialGraphItems(itemView: View, lineItem: Dashboard.Cell.LineItem) {
            with(lineItem) {
                itemView.tv_partial_graph_period.text = context.getWeek(baseYearWeek)

                val maxItem = data.maxBy { it.dealQty + it.leftQty }
                maxItem?.let {
                    if (dealQty + leftQty == dealQty) {
                        itemView.pv_partial_graph.highlightView.radius = 15.toDp()
                    } else {
                        itemView.pv_partial_graph.highlightView.radius = 0.toDp()
                    }
                    val maxValue = (maxItem.dealQty + maxItem.leftQty).toFloat()
                    itemView.pv_partial_graph_max.progress = ((dealQty.toFloat() + leftQty.toFloat()) / maxValue) * 100.0F
                    itemView.pv_partial_graph.progress = (dealQty.toFloat() / maxValue) * 100.0F
                }

                // dealQty
                itemView.tv_partial_graph_dealt.text = "${dealQty}T"
                // offerRemainderQty
                itemView.tv_partial_graph_left.text = "${leftQty}T"
            }
            itemView.iv_partial_graph.setSafeOnClickListener { onClickItem(lineItem) }
        }

        private fun setPartialTableItems(itemView: View, lineItem: Dashboard.Cell.LineItem) {
            with(lineItem) {
                itemView.tv_partial_table_period.text = context.getWeek(baseYearWeek)
                // OFFER_BUY, OFFER_SELL : leftPrice
                itemView.tv_partial_table_price.text = currencyFormat.format(leftPrice.toInt())
                // dealQty
                itemView.tv_partial_table_dealt.text = "${dealQty}T"
                // leftQty
                itemView.tv_partial_table_left.text = "${leftQty}T"
                // OFFER_BUY, OFFER_SELL : leftAmt
                itemView.tv_partial_table_value.text = currencyFormat.format(leftAmt.toInt())
            }
            itemView.iv_partial_table.setSafeOnClickListener { onClickItem(lineItem) }
        }

        private fun setWholeItems(itemView: View, lineItem: Dashboard.Cell.LineItem) {
            with(lineItem) {
                itemView.tv_whole_period.text = context.getWeek(baseYearWeek)
                itemView.tv_whole_price.text = currencyFormat.format(leftPrice.toInt())
                itemView.tv_whole_dealt.text =
                        if (yourOfferType == OFFER_TYPE_CODE_BUY) { "${dealQty}T" }
                        else { "${leftQty}T" }
                itemView.tv_whole_value.text = currencyFormat.format(leftAmt.toInt())
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}