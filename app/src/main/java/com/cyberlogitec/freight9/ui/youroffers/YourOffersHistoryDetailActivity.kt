package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.AllYn.ALL_YN_WHOLE
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_DEALT
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_CANCELED
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_PLACED
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_WEEK_EXPIRED
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.DashboardOfferHistory
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_your_offers_history_detail.*
import kotlinx.android.synthetic.main.appbar_your_offers_history_detail.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.body_your_offers_history_detail.*
import kotlinx.android.synthetic.main.item_your_offers_history_detail.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = YourOffersHistoryDetailViewModel::class)
class YourOffersHistoryDetailActivity : BaseActivity<YourOffersHistoryDetailViewModel>() {

    private lateinit var cell: Dashboard.Cell
    private lateinit var tradeOfferWrapper: TradeOfferWrapper

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var isYourOfferTypeBuy: Boolean = true
    private var popupWindow: PopupWindow? = null
    private var routeDataList = RouteDataList()
    // TODO : Alpha-R2 에서는 01PS 만 있음. 추후 변경 될 수 있으므로 동적으로 전달 받도록 한다
    private var initPaymentRatio: Float = 0.3F

    /**
     * offer history event 의 주차별 리스트의 adapter
     */
    private val adapter by lazy {
        HistoryDetailAdapter()
                .apply {

                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_history_detail)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
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
        defaultbarInit(toolbar_common,
                title = EmptyString,
                isEnableNavi = true)

        recyclerViewInit()
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        val eventCell = intent.getSerializableExtra(Intents.YOUR_OFFER_HISTORY_ITEM) as DashboardOfferHistory.EventCell
                        this.cell = intent.getSerializableExtra(Intents.YOUR_OFFER_DASHBOARD_ITEM) as Dashboard.Cell

                        val tradeOfferWrapper = intent.getSerializableExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO)
                        tradeOfferWrapper?.let { receivedTradeOfferWrapper ->
                            this.tradeOfferWrapper = receivedTradeOfferWrapper as TradeOfferWrapper
                            this.tradeOfferWrapper.orderTradeOfferDetail.offerLineItems[0].firstPaymentRatio.let { ratio ->
                                if (ratio > 0.0F) {
                                    this.initPaymentRatio = ratio
                                }
                            }
                            // Use for whole route
                            this.routeDataList = makeRouteDataList(this.tradeOfferWrapper.orderTradeOfferDetail.offerRoutes)
                            setPolPodData(this.cell)
                            setHistoryDataToUi(eventCell)
                        }
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_CONDITION_DETAIL -> {
                                if(::tradeOfferWrapper.isInitialized) {
                                    showDetailConditionPopup(root_your_offers_history_detail, viewModel, tradeOfferWrapper)
                                }
                            }
                            ParameterClick.CLICK_WHOLE_ROUTE -> {
                                if(::tradeOfferWrapper.isInitialized) {
                                    showWholeRoutePopup(root_your_offers_history_detail, routeDataList, tradeOfferWrapper.borList)
                                }
                            }
                            ParameterClick.CLICK_PRICE_TABLE -> {
                                if(::tradeOfferWrapper.isInitialized) {
                                    showPriceTablePopup(root_your_offers_history_detail, tradeOfferWrapper, false, true)
                                }
                            }
                            ParameterClick.CLICK_TITLE_LEFT -> {
                                onBackPressed()
                            }
                            else -> { }
                        }
                    }
                }
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        toolbar_left_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
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
    }

    /**
     * Offer history event list screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_offer_history_detail.apply {
            layoutManager = LinearLayoutManager(this@YourOffersHistoryDetailActivity)
            adapter = this@YourOffersHistoryDetailActivity.adapter
        }
    }

    /**
     * history event data 를 ui 에 set
     */
    private fun setHistoryDataToUi(eventCell: DashboardOfferHistory.EventCell) {

        isYourOfferTypeBuy = YourOffersActivity.yourOfferType == OFFER_TYPE_CODE_BUY

        val eventCodeResource
                = YourOffersHistoryActivity.EventKind.getEventType(eventCell.eventCode)

        if (eventCell.eventCode == EVENT_CODE_WEEK_EXPIRED) {
            toolbar_title_text.text = getString(
                    if (isYourOfferTypeBuy) {
                        eventCodeResource.buyId
                    } else {
                        eventCodeResource.sellId
                    }, "W${eventCell.baseYearWeek.substring(startIndex = 4)}")
        } else {
            toolbar_title_text.text = getString(
                    if (isYourOfferTypeBuy) { eventCodeResource.buyId }
                    else { eventCodeResource.sellId })
        }

        setRecyclerLayout(eventCell)
        setOfferNo(this.cell)
        setEventInfo(eventCell)
        setRecyclerData(eventCell)
    }

    /**
     * * Placed
     * Period & Volume
     * Period / Price(/T) / Left / Cost Value,Sales Value
     * SUM
     * * Dealt
     * Period & Volume
     * Period / Price(/T) / Dealt / Cost Value, Sales Value
     * SUM / TOTAL DEPOSIT VALUE (30%)
     * * Expired
     * Expiration Note
     * Period / Volume / Timestamp
     * NONE
     * * Canceled
     * Period & Volume
     * Period / Price(/T) / Left / Est. Cost Value, Sales Value
     * TOTLA ESTIMATED COST, SUM
     */
    private fun setRecyclerLayout(eventCell: DashboardOfferHistory.EventCell) {
        when(eventCell.eventCode) {
            EVENT_CODE_OFFER_PLACED -> {
                tv_recycler_top.text = getString(R.string.your_offers_history_detail_period_volume)
                tv_recycler_title1.text = getString(R.string.your_offers_history_detail_title_period)
                tv_recycler_title2.text = getString(R.string.your_offers_history_detail_title_price)
                tv_recycler_title3.text = getString(R.string.left)
                tv_recycler_title4.text = getString(
                        if (isYourOfferTypeBuy) {
                            R.string.cost_value
                        } else {
                            R.string.sales_value
                        })

                ll_recycler_bottom1.visibility = View.VISIBLE
                vw_bottom1_divider.visibility = View.VISIBLE
                ll_recycler_bottom2.visibility = View.GONE
                vw_bottom2_divider.visibility = View.GONE
                tv_bottom1_title.text = getString(R.string.volume_sum)
            }
            EVENT_CODE_DEALT -> {
                tv_recycler_top.text = getString(R.string.your_offers_history_detail_period_volume)
                tv_recycler_title1.text = getString(R.string.your_offers_history_detail_title_period)
                tv_recycler_title2.text = getString(R.string.your_offers_history_detail_title_price)
                tv_recycler_title3.text = getString(R.string.dealt)
                tv_recycler_title4.text = getString(
                        if (isYourOfferTypeBuy) {
                            R.string.your_offers_history_detail_title_cost_value
                        } else {
                            R.string.your_offers_history_detail_title_sales_value
                        })

                ll_recycler_bottom1.visibility = View.VISIBLE
                vw_bottom1_divider.visibility = View.VISIBLE
                ll_recycler_bottom2.visibility = View.VISIBLE
                vw_bottom2_divider.visibility = View.VISIBLE
                tv_bottom1_title.text = getString(R.string.volume_sum)
                tv_bottom2_title.text = getString(R.string.your_offers_history_detail_total_deposit_value,
                        (initPaymentRatio * 100).toInt())
            }
            EVENT_CODE_WEEK_EXPIRED -> {
                tv_recycler_top.text = getString(R.string.your_offers_history_detail_expiration_note)
                tv_recycler_title1.text = getString(R.string.your_offers_history_detail_title_period)
                tv_recycler_title2.visibility = View.GONE
                tv_recycler_title3.text = getString(R.string.your_offers_history_detail_title_volume)
                tv_recycler_title4.text = getString(R.string.your_offers_history_detail_title_timestamp)

                ll_recycler_bottom1.visibility = View.GONE
                vw_bottom1_divider.visibility = View.GONE
                ll_recycler_bottom2.visibility = View.GONE
                vw_bottom2_divider.visibility = View.GONE
            }
            EVENT_CODE_OFFER_CANCELED -> {
                tv_recycler_top.text = getString(R.string.your_offers_history_detail_period_volume)
                tv_recycler_title1.text = getString(R.string.your_offers_history_detail_title_period)
                tv_recycler_title2.text = getString(R.string.your_offers_history_detail_title_price)
                tv_recycler_title3.text = getString(R.string.left)
                tv_recycler_title4.text = getString(
                        if (isYourOfferTypeBuy) {
                            R.string.your_offers_popup_left_est_cost_value
                        } else {
                            R.string.your_offers_history_detail_title_sales_value
                        })

                ll_recycler_bottom1.visibility = View.VISIBLE
                vw_bottom1_divider.visibility = View.VISIBLE
                ll_recycler_bottom2.visibility = View.GONE
                vw_bottom2_divider.visibility = View.GONE
                tv_bottom1_title.text = getString(
                        if (isYourOfferTypeBuy) {
                            R.string.your_offers_history_detail_total_estimated_cost
                        } else {
                            R.string.volume_sum
                        })
            }
            else -> {
                cv_recycler.visibility = View.GONE
            }
        }

        ll_bottom_deal_options.visibility = if (eventCell.allYn == ALL_YN_WHOLE) {
            View.VISIBLE
        }
        else {
            View.GONE
        }
    }

    /**
     * offer number set
     */
    private fun setOfferNo(cell: Dashboard.Cell) {
        tv_offer_no.text = cell.offerNumber
    }

    /**
     * pol, pod data set
     */
    private fun setPolPodData(cell: Dashboard.Cell) {
        with(cell) {
            carrierItem?.let { items ->
                if (items.isNotEmpty()) {
                    iv_carrier_logo.setImageResource(items.first().carrierCode.getCarrierIcon(false))
                    tv_carrier_name.text = getCarrierCode(items.first().carrierCode)
                    tv_carrier_count.text = items.size.getCodeCount()
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
     * history event info set
     */
    private fun setEventInfo(eventCell: DashboardOfferHistory.EventCell) {
        with(eventCell) {
            val eventCodeResource
                    = YourOffersHistoryActivity.EventKind.getEventType(eventCode)
            if (eventCode == EVENT_CODE_WEEK_EXPIRED) {
                tv_event_value.text = getString(
                        if (isYourOfferTypeBuy) {
                            eventCodeResource.buyId
                        } else {
                            eventCodeResource.sellId
                        }, "W${baseYearWeek.substring(startIndex = 4)}"
                )
            } else {
                tv_event_value.text = getString(
                        if (isYourOfferTypeBuy) { eventCodeResource.buyId }
                        else { eventCodeResource.sellId }
                )
            }

            YourOffersHistoryActivity.StatusKind.getStatusType(eventCodeResource.statusCode)?.let {
                tv_status_value.text = getString(it.id)
            }

            // weekExpired 인 경우 해당 주차의 eventTimestamp 를 표시한다.
            val eventTimestamp = if (eventCode == EVENT_CODE_WEEK_EXPIRED) {
                eventCell.eventLog.find { it.baseYearWeek == baseYearWeek }
                        ?.eventTimestamp
                        ?.getYyMmDdHhMmDateTime(true)
                        ?: EmptyString
            } else {
                lastEventTimestamp.getYyMmDdHhMmDateTime(true)
            }
            tv_date_value.text = getString(R.string.your_offers_date_time_host, eventTimestamp, lastEventHost)
        }
    }

    /**
     * Set Recycler data...
     */
    private fun setRecyclerData(eventCell: DashboardOfferHistory.EventCell) {

        // Offer Place, Canceled : SUM (Price * Left)
        // Dealt : SUM (dealAmt)
        tv_bottom1_value.text = currencyFormat.format(eventCell
                .eventLog
                .sumByLong {
                    if (eventCell.eventCode == EVENT_CODE_DEALT) {
                        it.dealAmt.toLong()
                    } else {
                        it.leftAmt.toLong()
                    }
                })

        // Dealt : TOTAL DEPOSIT VALUE(30%) $XXXX XXXX : sumBy(dealAmt) * firstPaymentRatio
        // Canceled(Buy) : TOTAL ESTIMATED COST : sumBy(leftAmt)
        val totalValue = eventCell.eventLog.sumByLong {
            if (eventCell.eventCode == EVENT_CODE_DEALT) {
                it.dealAmt.toLong()
            } else {
                it.leftAmt.toLong()
            }}

        tv_bottom2_value.text = currencyFormat.format(
                if (eventCell.eventCode == EVENT_CODE_DEALT) {
                    (totalValue * initPaymentRatio).toInt()
                } else {
                    totalValue
                })

        adapter.setData(YourOffersActivity.yourOfferType,
                eventCell.eventCode,
                eventCell.eventLog.sortedBy { it.baseYearWeek })
        adapter.notifyDataSetChanged()
    }

    /***********************************************************************************************
     * Your Offers History Detail Recycler view adapter
     */
    class HistoryDetailAdapter : RecyclerView.Adapter<HistoryDetailAdapter.ViewHolder>() {

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        var datas = mutableListOf<DashboardOfferHistory.EventCell.EventLog>()
        lateinit var context: Context
        private var offerType: String = OFFER_TYPE_CODE_BUY
        private var eventCode: String = EVENT_CODE_OFFER_PLACED

        fun setData(offerType: String,
                    eventCode: String,
                    eventLogs: List<DashboardOfferHistory.EventCell.EventLog>) {
            this.offerType = offerType
            this.eventCode = eventCode
            this.datas.clear()
            this.datas.addAll(eventLogs.toMutableList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_your_offers_history_detail, parent, false))
        }

        override fun getItemCount() = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(datas[position]) {
                setUiData(holder, this)
            }
        }

        /**
         * * Buy
         * Offer Placed : Period, Price, Left, Cost Value(Sales Value)
         * Dealt : Period, Price, Dealt, Cost Value(Sales Value)
         * Expired : Period, Volume(offerQty?) Timestamp
         * Canceled : Period, Price, Left, Est. Cost Value(Sales Value)
         */
        private fun setUiData(holder: ViewHolder, eventLog: DashboardOfferHistory.EventCell.EventLog) {

            with(holder.itemView) {

                tv_item_value2.visibility = if (eventCode == EVENT_CODE_WEEK_EXPIRED) View.GONE else View.VISIBLE

                var price = eventLog.leftPrice
                // EVENT_CODE_WEEK_EXPIRED
                var qty = eventLog.leftQty
                when(eventCode) {
                    EVENT_CODE_DEALT -> {
                        price = eventLog.dealPrice
                        qty = eventLog.dealQty
                    }
                }

                // Period
                tv_item_value1.text = "W${eventLog.baseYearWeek.substring(startIndex = 4)}"

                // Price
                tv_item_value2.text = currencyFormat.format(price)

                // volume, Dealt/Left
                tv_item_value3.text = "${qty.toInt()}T"

                // Timestamp, Value
                tv_item_value4.text = if (eventCode == EVENT_CODE_WEEK_EXPIRED) {
                    // YYYYMMDDHHMMSSsss
                    eventLog.eventTimestamp?.getYyMmDdHhMmDateTime() ?: EmptyString
                } else {
                    // EVENT_CODE_DEALT : dealAmt
                    // EVENT_CODE_OFFER_PLACED, EVENT_CODE_OFFER_CANCELED : leftAmt
                    currencyFormat.format(when(eventCode) {
                        EVENT_CODE_DEALT -> {
                            eventLog.dealAmt
                        }
                        EVENT_CODE_OFFER_PLACED, EVENT_CODE_OFFER_CANCELED -> {
                            eventLog.leftAmt
                        }
                        else -> { }})
                }

                if (eventCode == EVENT_CODE_WEEK_EXPIRED) {
                    setItemViewBackgroundColor(holder.itemView, eventLog.isSelected)
                }
            }
        }

        private fun setItemViewBackgroundColor(itemView: View, isSelected: Boolean) {
            val backgroundColor = ContextCompat.getColor(context, if (isSelected) R.color.color_f2f2f2 else R.color.white)
            itemView.ll_your_offers_history_detail_item_root.setBackgroundColor(backgroundColor)
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
