package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE_FROM
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE_TO
import com.cyberlogitec.freight9.config.FilterType.FILTER_EVENT_TYPE
import com.cyberlogitec.freight9.config.FilterType.FILTER_PERIOD
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_your_offers_transaction.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber
import java.io.Serializable
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = YourOffersTransactionViewModel::class)
class YourOffersTransactionActivity : BaseActivity<YourOffersTransactionViewModel>() {

    private var isShowBalance: Boolean = false
    private var filterPeriod: FilterPeriod = FilterPeriod.FILTER_PERIOD_1_WEEK
    private var filterEventType: FilterEventType = FilterEventType.FILTER_EVENTTYPE_ALL
    private var filterDate: FilterDate = FilterDate.FILTER_DATE_NEWEST
    private var filterDateFrom: String = ""
    private var filterDateTo: String = ""

    private val adapter by lazy {
        TransactionAdapter()
                .apply {

                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_transaction)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                title = getString(R.string.your_offers_transaction_statement),
                isEnableNavi=false)

        recyclerViewInit()
        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        // TODO : Do something
                        setData()
                    }
                }
    }

    private fun setListener() {
        toolbar_right_btn.setSafeOnClickListener {
            onBackPressed()
        }

        ll_transaction_show_hide.setSafeOnClickListener {
            setShowBalanceCheck()
        }

        ll_transaction_filter.setSafeOnClickListener {
            RxActivityResult(this)
                    .start(Intent(this, YourOffersTransactionFilterActivity::class.java)
                            .putExtra(FILTER_PERIOD, filterPeriod)
                            .putExtra(FILTER_EVENT_TYPE, filterEventType)
                            .putExtra(FILTER_DATE, filterDate)
                            .putExtra(FILTER_DATE_FROM, filterDateFrom)
                            .putExtra(FILTER_DATE_TO, filterDateTo))
                    .subscribe(
                            { result ->
                                if (result.isOk) {
                                    filterPeriod = result.data.getSerializableExtra(FILTER_PERIOD) as FilterPeriod
                                    filterEventType = result.data.getSerializableExtra(FILTER_EVENT_TYPE) as FilterEventType
                                    filterDate = result.data.getSerializableExtra(FILTER_DATE) as FilterDate
                                    filterDateFrom = result.data.getStringExtra(FILTER_DATE_FROM)
                                    filterDateTo = result.data.getStringExtra(FILTER_DATE_TO)
                                    setData()
                                } else {
                                    Timber.d("f9: NotOK --> resultCode:  ${result.resultCode}")
                                }
                            },
                            {
                                viewModel.error.onNext(it)
                            }
                    )
        }
    }

    private fun setShowBalanceCheck() {
        isShowBalance = !isShowBalance
        chk_show_hide.isChecked = isShowBalance
        tv_show_hide.text = getString(
                if (isShowBalance) { R.string.your_offers_transaction_hide_balance }
                else { R.string.your_offers_transaction_show_balance })
        val textColor = getColor(if (isShowBalance) R.color.color_333333 else R.color.color_c7c7c7)
        val typeface = getFont(this,
                if (isShowBalance) { R.font.opensans_bold }
                else { R.font.opensans_regular })
        tv_show_hide.setTextColor(textColor)
        tv_show_hide.typeface = typeface


        // TODO : show balance check

    }

    private fun setData() {
        // filter
        tv_filter.text = getString(R.string.your_offers_transaction_filter_result,
                getString(filterPeriod.stringId).toString().toLowerCase(),
                getString(filterEventType.stringId).toString().toLowerCase(),
                getString(filterDate.stringId).toString().toLowerCase())

        setRecyclerData()
    }

    private fun setRecyclerData() {

        // TODO

    }

    private fun recyclerViewInit() {
        recycler_offer_transaction.apply {
            layoutManager = LinearLayoutManager(this@YourOffersTransactionActivity)
            adapter = this@YourOffersTransactionActivity.adapter
        }
    }

    class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        lateinit var context: Context
        var datas = mutableListOf<TransactionItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_your_offers_transaction, parent, false))
        }

        override fun getItemCount() = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(datas[position]) {
                setUiData(holder, this)
            }
        }

        private fun setUiData(holder: ViewHolder, transactionItem: TransactionItem) {
            with(holder.itemView) {

            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------

    data class TransactionItem(
            var date: String,
            var time: String,
            var polCode: String,
            var polName: String,
            var polCount: Int,
            var podCode: String,
            var podName: String,
            var podCount: Int,
            var inOut: Int,
            var price: Int,
            var baseYearWeek: String,
            var inventoryVolume: Int,
            var balance: Int,
            var offerNr: String,
            var transactionNr: String,
            var deposit: Int,
            var personInCharge: String,
            var account: String
    )

    enum class FilterPeriod (
            val index: Int,
            val stringId: Int
    ): Serializable
    {
        FILTER_PERIOD_1_WEEK(0, R.string.your_offers_transaction_filter_1_week),
        FILTER_PERIOD_15_DAYS(1, R.string.your_offers_transaction_filter_15_days),
        FILTER_PERIOD_1_MONTH(2, R.string.your_offers_transaction_filter_1_month),
        FILTER_PERIOD_SELECT(3, R.string.your_offers_transaction_filter_select);
        companion object {
            fun getFilterPeriod(index: Int): FilterPeriod? {
                for (filterPeriod in values()) {
                    if (filterPeriod.index == index) {
                        return filterPeriod
                    }
                }
                return FILTER_PERIOD_1_WEEK
            }
        }
    }

    enum class FilterEventType (
            val index: Int,
            val stringId: Int
    ): Serializable
    {
        FILTER_EVENTTYPE_ALL(0, R.string.your_offers_transaction_filter_all),
        FILTER_EVENTTYPE_DEALT(1, R.string.your_offers_transaction_filter_dealt);
        companion object {
            fun getFilterEventType(index: Int): FilterEventType? {
                for (filterEventType in values()) {
                    if (filterEventType.index == index) {
                        return filterEventType
                    }
                }
                return FILTER_EVENTTYPE_ALL
            }
        }
    }

    enum class FilterDate (
            val index: Int,
            val stringId: Int
    ): Serializable
    {
        FILTER_DATE_NEWEST(0, R.string.your_offers_transaction_filter_newest),
        FILTER_DATE_OLDEST(1, R.string.your_offers_transaction_filter_oldest);
        companion object {
            fun getFilterDate(index: Int): FilterDate? {
                for (filterDate in values()) {
                    if (filterDate.index == index) {
                        return filterDate
                    }
                }
                return FILTER_DATE_NEWEST
            }
        }
    }
}
