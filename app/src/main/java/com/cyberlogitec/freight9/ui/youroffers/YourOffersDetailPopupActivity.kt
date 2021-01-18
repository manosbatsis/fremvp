package com.cyberlogitec.freight9.ui.youroffers

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.lib.model.DashboardOfferWeekDetail
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.youroffers.YourOffersActivity.Companion.yourOfferType
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.popup_your_offers_detail.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = YourOffersDetailPopupViewModel::class)
class YourOffersDetailPopupActivity : BaseActivity<YourOffersDetailPopupViewModel>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var isHistoryOpened: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.popup_your_offers_detail)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

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

        // Buy Offers : Est. Cost Value / Sell Offers : Est. Sales Value
        tv_left_cost_title.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
            getString(R.string.your_offers_popup_left_est_cost_value)
        } else {
            getString(R.string.your_offers_popup_left_est_sales_value)
        }

        // Buy Offers : Cost Value / Sell Offers : Sales Value
        tv_dealt_cost_title.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
            getString(R.string.your_offers_popup_dealt_cost_value)
        } else {
            getString(R.string.your_offers_popup_dealt_sales_value)
        }

        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { weekDetail ->
                        setData(weekDetail)
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
                                ParameterAny.ANY_DEALT_HISTORY_OPEN_CLOSED -> {
                                    isHistoryOpened = !(second as Boolean)
                                    setExpandUi(isHistoryOpened)
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
        fl_detail_popup.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CLOSE)
        }

        rl_dealt_history.setOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_DEALT_HISTORY_OPEN_CLOSED, isHistoryOpened))
        }
    }

    /**
     * offer detail 정보 ui 설정
     */
    private fun setData(weekDetail: DashboardOfferWeekDetail) {
        with(weekDetail) {
            tv_detail_popup_title.text = getWeek(baseYearWeek)

            pv_detail_hgraph.progress = if (aggDealQty + aggLeftQty > 0) {
                aggDealQty / (aggDealQty + aggLeftQty) * 100.0F
            } else 0.0F

            tv_left_amount.text = "$aggLeftQty T"
            tv_left_price.text = currencyFormat.format(aggLeftPrice)
            tv_left_cost_value.text = currencyFormat.format(aggLeftAmt.toInt())
            tv_left_trade_closing.text = tradeClosing?.getYyMmDdHhMmDateTime() ?: EmptyString

            tv_dealt_amount.text = "$aggDealQty T"
            tv_dealt_cost_value.text = currencyFormat.format(aggDealAmt.toInt())

            // Dealt History
            dealLog?.let { dealLog ->
                // dealLog의 값에 referenceEventNumber가 없는 array는 무시
                val filteredDealLog = dealLog.filter { lineItemDealHistoryLog ->
                    !lineItemDealHistoryLog.referenceEventNumber.isNullOrEmpty()
                }
                if (filteredDealLog.isNotEmpty()) {
                    setExpandData(filteredDealLog.sortedByDescending { lineItemDealHistoryLog ->
                        lineItemDealHistoryLog.eventTimestamp
                    })
                }
            }
        }
    }

    private fun setExpandUi(isHistoryOpened: Boolean) {
        iv_arrow.setImageResource(if (isHistoryOpened) R.drawable.btn_collapse_default_l else R.drawable.btn_expand_default_l)
        ll_detail_popup_content.visibility = if (isHistoryOpened) View.VISIBLE else View.GONE
        if (isHistoryOpened) {
            Handler().postDelayed({
                sv_content_root.smoothScrollTo(0, rl_dealt_history.top)
            }, 250)
        }
    }

    private fun setExpandData(dealtHistories: List<DashboardOfferWeekDetail.LineItemDealHistoryLog>) {
        iv_arrow.visibility = View.VISIBLE
        ll_detail_popup_content.removeAllViews()
        for (dealtHistory in dealtHistories) {
            val llRow = LinearLayout(this)
            val height = 40.toDp().toInt()
            llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            llRow.orientation = LinearLayout.HORIZONTAL
            llRow.gravity = Gravity.CENTER_VERTICAL
            llRow.addView(makeDateTimeTextViewInHistory(dealtHistory.eventTimestamp?.getYyMmDdHhMmDateTime()))
            llRow.addView(makeExtraTextViewInHistory(true, dealtHistory.dealPrice))
            llRow.addView(makeExtraTextViewInHistory(false, dealtHistory.dealQty))
            ll_detail_popup_content.addView(llRow)
        }
    }

    private fun makeDateTimeTextViewInHistory(value: String?): TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textView.layoutParams = params
        textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_r_13_bfbfbf)
        textView.text = value ?: EmptyString
        return textView
    }

    private fun makeExtraTextViewInHistory(isPrice: Boolean, value: Int): TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_r_13_737373)
        textView.text = if (isPrice) currencyFormat.format(value) else " | $value T"
        return textView
    }
}