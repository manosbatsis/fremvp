package com.cyberlogitec.freight9.ui.youroffers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE_FROM
import com.cyberlogitec.freight9.config.FilterType.FILTER_DATE_TO
import com.cyberlogitec.freight9.config.FilterType.FILTER_EVENT_TYPE
import com.cyberlogitec.freight9.config.FilterType.FILTER_PERIOD
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_your_offers_transaction_filter.*
import kotlinx.android.synthetic.main.body_your_offers_transaction_filter.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber


@RequiresActivityViewModel(value = YourOffersTransactionFilterViewModel::class)
class YourOffersTransactionFilterActivity : BaseActivity<YourOffersTransactionFilterViewModel>() {

    private var filterPeriod: YourOffersTransactionActivity.FilterPeriod = YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_1_WEEK
    private var filterEventType: YourOffersTransactionActivity.FilterEventType = YourOffersTransactionActivity.FilterEventType.FILTER_EVENTTYPE_ALL
    private var filterDate: YourOffersTransactionActivity.FilterDate = YourOffersTransactionActivity.FilterDate.FILTER_DATE_NEWEST
    private var filterDateFrom: String = ""
    private var filterDateTo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_transaction_filter)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                title = getString(R.string.your_offers_transaction_filter),
                isEnableNavi = true)

        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        filterPeriod = intent.getSerializableExtra(FILTER_PERIOD)
                                as YourOffersTransactionActivity.FilterPeriod
                        filterEventType = intent.getSerializableExtra(FILTER_EVENT_TYPE)
                                as YourOffersTransactionActivity.FilterEventType
                        filterDate = intent.getSerializableExtra(FILTER_DATE)
                                as YourOffersTransactionActivity.FilterDate
                        filterDateFrom = intent.getStringExtra(FILTER_DATE_FROM)
                        filterDateTo = intent.getStringExtra(FILTER_DATE_TO)
                        setUi()
                    }
                }

        viewModel.outPuts.onClickToApply()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        val intent = Intent()
                        intent.putExtra(FILTER_PERIOD, filterPeriod)
                                .putExtra(FILTER_EVENT_TYPE, filterEventType)
                                .putExtra(FILTER_DATE, filterDate)
                                .putExtra(FILTER_DATE_FROM, filterDateFrom)
                                .putExtra(FILTER_DATE_TO, filterDateTo)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
    }

    private fun setListener() {
        toolbar_left_btn.setSafeOnClickListener {
            onBackPressed()
        }

        btn_apply.setSafeOnClickListener {
            viewModel.inPuts.clickToApply(Parameter.CLICK)
        }

        rl_period_1_week.setSafeOnClickListener {
            filterPeriod = YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_1_WEEK
            setPeriod(filterPeriod)
        }

        rl_period_15_days.setSafeOnClickListener {
            filterPeriod = YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_15_DAYS
            setPeriod(filterPeriod)
        }

        rl_period_1_month.setSafeOnClickListener {
            filterPeriod = YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_1_MONTH
            setPeriod(filterPeriod)
        }

        rl_period_select.setSafeOnClickListener {
            filterPeriod = YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_SELECT
            setPeriod(filterPeriod)

            // TODO : Go Calendar

        }

        rl_event_type_all.setSafeOnClickListener {
            filterEventType = YourOffersTransactionActivity.FilterEventType.FILTER_EVENTTYPE_ALL
            setEventType(filterEventType)
        }

        rl_event_type_dealt.setSafeOnClickListener {
            filterEventType = YourOffersTransactionActivity.FilterEventType.FILTER_EVENTTYPE_DEALT
            setEventType(filterEventType)
        }

        rl_date_newest.setSafeOnClickListener {
            filterDate = YourOffersTransactionActivity.FilterDate.FILTER_DATE_NEWEST
            setDate(filterDate)
        }

        rl_date_oldest.setSafeOnClickListener {
            filterDate = YourOffersTransactionActivity.FilterDate.FILTER_DATE_OLDEST
            setDate(filterDate)
        }
    }

    private fun setUi() {
        setPeriod(filterPeriod)
        setEventType(filterEventType)
        setDate(filterDate)
    }

    private fun setPeriod(filterPeriod: YourOffersTransactionActivity.FilterPeriod) {
        tv_period_1_week.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_period_15_days.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_period_1_month.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_period_select.setTextAppearance(R.style.txt_opensans_r_15_595959)
        iv_period_1_week.visibility = View.INVISIBLE
        iv_period_15_days.visibility = View.INVISIBLE
        iv_period_1_month.visibility = View.INVISIBLE
        iv_period_select.visibility = View.INVISIBLE
        when(filterPeriod) {
            YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_1_WEEK -> {
                tv_period_1_week.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_period_1_week.visibility = View.VISIBLE
            }
            YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_15_DAYS -> {
                tv_period_15_days.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_period_15_days.visibility = View.VISIBLE
            }
            YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_1_MONTH -> {
                tv_period_1_month.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_period_1_month.visibility = View.VISIBLE
            }
            YourOffersTransactionActivity.FilterPeriod.FILTER_PERIOD_SELECT -> {
                tv_period_select.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_period_select.visibility = View.VISIBLE
            }
        }
    }

    private fun setEventType(filterEventType: YourOffersTransactionActivity.FilterEventType) {
        tv_event_type_all.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_event_type_dealt.setTextAppearance(R.style.txt_opensans_r_15_595959)
        iv_event_type_all.visibility = View.INVISIBLE
        iv_event_type_dealt.visibility = View.INVISIBLE
        when(filterEventType) {
            YourOffersTransactionActivity.FilterEventType.FILTER_EVENTTYPE_ALL -> {
                tv_event_type_all.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_event_type_all.visibility = View.VISIBLE
            }
            YourOffersTransactionActivity.FilterEventType.FILTER_EVENTTYPE_DEALT -> {
                tv_event_type_dealt.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_event_type_dealt.visibility = View.VISIBLE
            }
        }
    }

    private fun setDate(filterDate: YourOffersTransactionActivity.FilterDate) {
        tv_date_newest.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_date_oldest.setTextAppearance(R.style.txt_opensans_r_15_595959)
        iv_date_newest.visibility = View.INVISIBLE
        iv_date_oldest.visibility = View.INVISIBLE
        when(filterDate) {
            YourOffersTransactionActivity.FilterDate.FILTER_DATE_NEWEST -> {
                tv_date_newest.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_date_newest.visibility = View.VISIBLE
            }
            YourOffersTransactionActivity.FilterDate.FILTER_DATE_OLDEST -> {
                tv_date_oldest.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_date_oldest.visibility = View.VISIBLE
            }
        }
    }
}
