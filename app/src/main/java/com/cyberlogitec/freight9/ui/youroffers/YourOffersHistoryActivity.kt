package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_ALL_DEALT
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_DEALT
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_CANCELED
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_CLOSED
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_PLACED
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_WEEK_EXPIRED
import com.cyberlogitec.freight9.config.StatusType.STATUS_DRAFT_CLOSED_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_DRAFT_CLOSING_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_DRAFT_MARKET_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_DRAFT_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_MARKET_CLOSED_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_MARKET_DRAFT_TYPE
import com.cyberlogitec.freight9.config.StatusType.STATUS_MARKET_TYPE
import com.cyberlogitec.freight9.lib.model.DashboardOfferHistory
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_your_offers_history.*
import kotlinx.android.synthetic.main.item_your_offers_history.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = YourOffersHistoryViewModel::class)
class YourOffersHistoryActivity : BaseActivity<YourOffersHistoryViewModel>() {

    private val adapter by lazy {
        HistoryAdapter()
                .apply {
                    onClickItem = { _, eventCell ->
                        viewModel.inPuts.clickItem(eventCell)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_history)
        (application as App).component.inject(this)

        setRxOutputs()
        initView()
    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                title = when(YourOffersActivity.yourOfferType) {
                    OFFER_TYPE_CODE_BUY -> {
                        getString(R.string.your_offers_buy_offer_history)
                    }
                    OFFER_TYPE_CODE_SELL -> {
                        getString(R.string.your_offers_sell_offer_history)
                    }
                    else -> { "" }
                },
                isEnableNavi=false)

        recyclerViewInit()
        setListener()

        requestOfferEvents()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { eventCells ->
                        setData(eventCells)
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when (parameterClick) {
                            ParameterClick.CLICK_CLOSE -> {
                                onBackPressed()
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
                                    val intent = second as Intent
                                    intent.setClass(this@YourOffersHistoryActivity,
                                            YourOffersHistoryDetailActivity::class.java)
                                    startActivity(intent)
                                }
                                else -> {  }
                            }
                        }
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
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CLOSE)
        }
    }

    /**
     * Offer history list screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_offer_history.apply {
            layoutManager = LinearLayoutManager(this@YourOffersHistoryActivity)
            adapter = this@YourOffersHistoryActivity.adapter
        }
    }

    /**
     * offer history event 요청
     */
    private fun requestOfferEvents() {
        viewModel.inPuts.requestOfferEvents(Parameter.EVENT)
    }

    /**
     * offer history event list 를 adapter 에 set
     */
    private fun setData(eventCells: List<DashboardOfferHistory.EventCell>) {
        adapter.setData(YourOffersActivity.yourOfferType, eventCells)
        adapter.notifyDataSetChanged()
    }

    /**
     * offer history event list 의 RecyclerAdapter
     */
    class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        lateinit var context: Context
        var datas = mutableListOf<DashboardOfferHistory.EventCell>()
        private var offerType: String = OFFER_TYPE_CODE_BUY

        var onClickItem: (Int, DashboardOfferHistory.EventCell) -> Unit =
                { position, dashboardOfferHistory ->
                    Timber.d("f9 : $position, $dashboardOfferHistory")
                }

        fun setData(offerType: String, eventCells: List<DashboardOfferHistory.EventCell>) {
            this.offerType = offerType
            this.datas.clear()
            this.datas.addAll(eventCells.toMutableList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_your_offers_history, parent, false))
        }

        override fun getItemCount() = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            with(holder.itemView) {

                if (position > 0
                        && datas[position].lastEventTimestamp.getYYYYMMDD(true)
                        == datas[position-1].lastEventTimestamp.getYYYYMMDD(true)) {
                    tv_top_balloon.visibility = View.GONE
                    vw_top_margin.visibility = View.GONE
                } else {
                    tv_top_balloon.visibility = View.VISIBLE
                    if (position == 0) {
                        vw_top_margin.visibility = View.GONE
                    } else {
                        vw_top_margin.visibility = View.VISIBLE
                    }
                }

                with(datas[position]) {
                    setUiData(holder, this)
                }

                iv_history_detail.setOnClickListener {
                    onClickItem(position, datas[position])
                }
            }
        }

        private fun setUiData(holder: ViewHolder, eventCell: DashboardOfferHistory.EventCell) {
            with(holder.itemView) {
                with(eventCell) {

                    tv_top_balloon.text = lastEventTimestamp.getYMDNoSymbol()

                    val eventKind = EventKind.getEventType(eventCode)

                    var eventString = context.getString(
                            if (offerType == OFFER_TYPE_CODE_BUY) { eventKind.buyId }
                            else { eventKind.sellId })

                    if (eventCode == EVENT_CODE_WEEK_EXPIRED) {
                        eventString = context.getString(
                                if (offerType == OFFER_TYPE_CODE_BUY) { eventKind.buyId }
                                else { eventKind.sellId },
                                "W${baseYearWeek.substring(startIndex = 4)}")
                    }

                    tv_history_event.text = eventString

                    tv_history_desc.text = context.getString(
                            if (offerType == OFFER_TYPE_CODE_BUY) { eventKind.buyDescId }
                            else { eventKind.sellDescId })

                    tv_history_desc_detail.text = getDescription(eventCell)

                    // HH:MM:SS | HOST
                    tv_date_time_behaviour.text = "${lastEventTimestamp.getHhMmSsDateTime()} | $lastEventHost"
                }
            }
        }

        private fun getDescription(eventCell: DashboardOfferHistory.EventCell): String {
            var description = ""
            with(eventCell) {
                when (eventCode) {
                    // W01-W10 or W01..W10
                    EVENT_CODE_DEALT -> {
                        description = continuousWeeksDescription(eventLog)
                    }
                    // $2,000 | 100 T (left Price, LeftQty)
                    EVENT_CODE_WEEK_EXPIRED -> {
                        val findBaseYearWeek = eventLog.find { it.baseYearWeek == baseYearWeek }
                        description = currencyFormat.format(findBaseYearWeek?.leftPrice ?: 0) +
                                " | " + (findBaseYearWeek?.leftQty?.toInt() ?: 0) + " T"
                    }
                    else -> {
                        description = ""
                    }
                }
            }
            return description
        }

        // Check W01-W10 or W01..W10 : EVENT_CODE_DEALT
        private fun continuousWeeksDescription(eventLogs: List<DashboardOfferHistory.EventCell.EventLog>): String {

            val sortedEventDetail = eventLogs.sortedBy { it.baseYearWeek }
            val totalSize = sortedEventDetail.size

            var minYear = 0; var minWeek = 0
            val minBaseYearWeek = sortedEventDetail.minBy { it.baseYearWeek }?.baseYearWeek
            minBaseYearWeek?.let { minYearWeek ->
                minYear = minYearWeek.substring(0, 4).toInt()
                minWeek = minYearWeek.substring(4, 6).toInt()
            }

            var maxYear = 0; var maxWeek = 0
            val maxBaseYearWeek = sortedEventDetail.maxBy { it.baseYearWeek }?.baseYearWeek
            maxBaseYearWeek?.let { maxYearWeek ->
                maxYear = maxYearWeek.substring(0, 4).toInt()
                maxWeek = maxYearWeek.substring(4, 6).toInt()
            }

            val weekRange: Int
            if (maxYear > 0 && minYear > 0) {
                if (minYear != maxYear) {
                    val thisMaxWeekNumber = Calendar.getInstance().getYearWeeks(minYear)
                    weekRange = thisMaxWeekNumber + maxWeek - minWeek + 1
                } else {
                    weekRange = maxWeek - minWeek + 1
                }
            } else {
                weekRange = 0
            }

            return if (weekRange == 0) {
                ""
            } else {
                if (weekRange == totalSize) {
                    context.getWeek(minBaseYearWeek) + "-" + context.getWeek(maxBaseYearWeek)
                } else {
                    context.getWeek(minBaseYearWeek) + ".." + context.getWeek(maxBaseYearWeek)
                }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------

    enum class StatusKind (
            val statusCode: String,
            val id: Int
    )
    {
        // Dealt (Sell/Buy), Expired
        STATUS_MARKET(STATUS_MARKET_TYPE, R.string.your_offers_status_on_market),
        // Offer Canceled, Offer Closed, All Sold out/All Bought out
        STATUS_MARKET_CLOSED(STATUS_MARKET_CLOSED_TYPE, R.string.your_offers_status_on_market_closed),
        STATUS_MARKET_DRAFT(STATUS_MARKET_DRAFT_TYPE, R.string.your_offers_status_on_market_draft),
        STATUS_DRAFT(STATUS_DRAFT_TYPE, R.string.your_offers_status_draft),
        STATUS_DRAFT_CLOSED(STATUS_DRAFT_CLOSED_TYPE, R.string.your_offers_status_draft_closed),
        // Sell/Buy Offer Placed
        STATUS_DRAFT_MARKET(STATUS_DRAFT_MARKET_TYPE, R.string.your_offers_status_draft_on_market),
        STATUS_DRAFT_CLOSING(STATUS_DRAFT_CLOSING_TYPE, R.string.your_offers_status_draft_closing);

        companion object {
            fun getStatusType(statusCode: String): StatusKind? {
                for (statusKind in values()) {
                    if (statusKind.statusCode == statusCode) {
                        return statusKind
                    }
                }
                return STATUS_MARKET
            }
        }
    }

    enum class EventKind (
            val eventCode: String,
            val statusCode: String,
            val buyId: Int,
            val sellId: Int,
            val buyDescId: Int,
            val sellDescId: Int
    )
    {
        // Draft > On Market
        EVENT_PLACED(EVENT_CODE_OFFER_PLACED,
                STATUS_DRAFT_MARKET_TYPE,
                R.string.your_offers_event_buy_offer_placed,
                R.string.your_offers_event_sell_offer_placed,
                R.string.your_offers_event_buy_offer_placed_desc,
                R.string.your_offers_event_sell_offer_placed_desc),
        // On Market
        EVENT_DEALT(EVENT_CODE_DEALT,
                STATUS_MARKET_TYPE,
                R.string.your_offers_event_buy_dealt_buy,
                R.string.your_offers_event_sell_dealt_buy,
                R.string.your_offers_event_buy_dealt_buy_desc,
                R.string.your_offers_event_sell_dealt_buy_desc),
        // On Market
        EVENT_EXPIRED(EVENT_CODE_WEEK_EXPIRED,
                STATUS_MARKET_TYPE,
                R.string.your_offers_event_buy_week_expired,
                R.string.your_offers_event_sell_week_expired,
                R.string.your_offers_event_buy_week_expired_desc,
                R.string.your_offers_event_sell_week_expired_desc),
        // On Market > Closed
        EVENT_CANCELED(EVENT_CODE_OFFER_CANCELED,
                STATUS_MARKET_CLOSED_TYPE,
                R.string.your_offers_event_buy_offer_canceled,
                R.string.your_offers_event_sell_offer_canceled,
                R.string.your_offers_event_buy_offer_canceled_desc,
                R.string.your_offers_event_sell_offer_canceled_desc),
        // On Market > Closed
        EVENT_CLOSED(EVENT_CODE_OFFER_CLOSED,
                STATUS_MARKET_CLOSED_TYPE,
                R.string.your_offers_event_buy_offer_closed,
                R.string.your_offers_event_sell_offer_closed,
                R.string.your_offers_event_buy_offer_closed_desc,
                R.string.your_offers_event_sell_offer_closed_desc),
        // On Market > Closed
        EVENT_BOUGHT_SOLD_OUT(EVENT_CODE_ALL_DEALT,
                STATUS_MARKET_CLOSED_TYPE,
                R.string.your_offers_event_buy_all_out,
                R.string.your_offers_event_sell_all_out,
                R.string.your_offers_event_buy_all_out_desc,
                R.string.your_offers_event_sell_all_out_desc);

        companion object {
            fun getEventType(eventCode: String): EventKind {
                for (eventKind in values()) {
                    if (eventKind.eventCode == eventCode) {
                        return eventKind
                    }
                }
                return EVENT_PLACED
            }
        }
    }
}
