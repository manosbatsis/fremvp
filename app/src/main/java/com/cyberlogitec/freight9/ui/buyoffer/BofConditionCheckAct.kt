package com.cyberlogitec.freight9.ui.buyoffer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.BuyOffer
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_CCT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ContainerSizeType
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.model.OfferLineItem
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_condition_check.*
import kotlinx.android.synthetic.main.body_bof_condition_check.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = BofConditionCheckVm::class)
class BofConditionCheckAct : BaseActivity<BofConditionCheckVm>() {

    private var tabList = mutableListOf<TabEnum>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_condition_check)
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
        tabList.add(TabEnum.TAB_CONTAINER_DETAIL)
        tabList.add(TabEnum.TAB_PAY_PLAN)

        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_bof_condition_check,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_conditions),
                isEnableNavi = false)

        makeTabList()
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // onClickPlanDetail
        viewModel.outPuts.onClickPlanDetail()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        // TODO : Do nothing currently
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // onClickEdit
        viewModel.outPuts.onClickEdit()
                .bindToLifecycle(this)
                .subscribe {
                    if (BuyOffer.BUY_OFFER_WIZARD) {
                        val intent = Intent()
                        intent.putExtra(Intents.BUY_OFFER_STEP, BofWizardActivity.STEP_CONDITIONS)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        startActivityWithFinish(Intent(this, BofConditionAct::class.java)
                                .putExtra(Intents.OFFER, it as Offer)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // onSuccessRefresh
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->
                        Timber.d("f9: offer - $offer")
                        mOffer = offer
                        setRdTermName(offer)
                        setContainerTypeAndSize(offer)
                        setCarrier(offer)
                        requestPaymentPlan(offer.offerPaymentPlanCode)
                    }
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            val paymentPlan = it.body() as PaymentPlan
                            setPayPlan(mOffer, paymentPlan)
                        } else {
                            showToast("Fail Request paymentPlan(Http)\n" + it.errorBody())
                        }
                    }
                }
    }

    private fun setListener() {
        appbar_bof_condition_check.toolbar_right_btn.setSafeOnClickListener {
            onBackPressed()
        }

        btn_edit.setSafeOnClickListener {
            btn_edit.isEnabled = false
            viewModel.inPuts.clickToEdit(Parameter.CLICK)
        }

        tv_collect_condition_on_calendar.setSafeOnClickListener {
            viewModel.inPuts.clickToPlanDetail(Parameter.CLICK)
        }
    }

    private fun requestPaymentPlan(paymentPlan: String?) {
        paymentPlan?.let {
            viewModel.inPuts.requestPaymentPlan(paymentPlan)
        }
    }

    private fun makeTabList() {
        for (tab in tabList) {
            val llItem = LinearLayout(this)
            llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            llItem.orientation = LinearLayout.VERTICAL
            llItem.gravity = Gravity.BOTTOM
            val margin = 12.toDp().toInt()
            val params = llItem.layoutParams as LinearLayout.LayoutParams
            params.setMargins(margin, 0, margin, 0)
            llItem.layoutParams = params

            llItem.addView(makeTabTextView(getString(tab.nameId)))
            llItem.addView(makeTabImageView())
            llItem.tag = tab.tagSeq

            llItem.setOnClickListener { clickTabProcess(llItem, tab) }
            ll_tab_horizontal.addView(llItem)
        }
        // 첫번째 Tab 초기화
        clickTabProcess(null, tabList[0])
    }

    private fun clickTabProcess(clicklayout: LinearLayout?, tab: TabEnum) {

        val clickTagid = if (null == clicklayout) layout_tab_tag_seq else clicklayout.tag as Int

        // 선택한 tab highlight, 그 외 tab dark
        makeSelectedTabList(tab)

        for (index in 0 until ll_tab_horizontal.childCount) {
            val childview = ll_tab_horizontal.getChildAt(index) as LinearLayout

            var textcolorvalue = R.color.greyish_brown
            var viewcolorvalue = R.color.black
            if (childview.tag as Int == clickTagid) {
                textcolorvalue = R.color.colorWhite
                viewcolorvalue = R.color.purpley_blue
            }

            val subChildCount = childview.childCount
            for (subIndex in 0 until subChildCount) {
                when (val subChildView = childview.getChildAt(subIndex)) {
                    is TextView -> {
                        subChildView.setTextColor(getColor(textcolorvalue))
                    }
                    is View -> {
                        subChildView.setBackgroundColor(getColor(viewcolorvalue))
                    }
                }
            }
        }
    }

    private fun makeTabTextView(title: String): TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        params.setMargins(0, 0, 0, 4.toDp().toInt())
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textView.setTextAppearance(R.style.txt_opensans_b_16_greyishbrown)
        textView.text = title
        return textView
    }

    private fun makeTabImageView(): View {
        val imageview = View(this)
        imageview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4.toDp().toInt())
        imageview.setBackgroundColor(getColor(R.color.black))
        return imageview
    }

    // Container detail, Pay plan
    private fun makeSelectedTabList(tab: TabEnum) {
        if (tab.index == 0) {
            // Container detail
            bof_condition_check_container_detail.visibility = View.VISIBLE
            bof_condition_check_payplan.visibility = View.GONE
        } else if (tab.index == 1) {
            // Pay plan
            bof_condition_check_container_detail.visibility = View.GONE
            bof_condition_check_payplan.visibility = View.VISIBLE
        }
    }

    private fun makeCarriers(offer: Offer) {
        offer.offerCarriers?.filter { it.isChecked }?.let { offerCarriers ->
            ll_offer_condition_carriers.removeAllViews()
            for (carrier in offerCarriers) {
                val llItem = LinearLayout(this)
                llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 32.toDp().toInt())
                llItem.orientation = LinearLayout.HORIZONTAL
                llItem.gravity = Gravity.CENTER_VERTICAL

                // add carrier icon
                val imageView = ImageView(this)
                imageView.layoutParams = LinearLayout.LayoutParams(22.toDp().toInt(), 22.toDp().toInt())
                imageView.setImageResource(carrier.offerCarrierCode.getCarrierIcon(false))
                llItem.addView(imageView)

                // add carrier code
                val carrierCodeTextView = TextView(this)
                carrierCodeTextView.layoutParams = LinearLayout.LayoutParams(53.toDp().toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
                val codeParams = carrierCodeTextView.layoutParams as LinearLayout.LayoutParams
                codeParams.setMargins(2.toDp().toInt(), 0, 0, 0)
                carrierCodeTextView.layoutParams = codeParams
                carrierCodeTextView.gravity = Gravity.CENTER_VERTICAL
                carrierCodeTextView.setTextAppearance(R.style.txt_opensans_b_15_greyishbrown)
                carrierCodeTextView.text = carrier.offerCarrierCode
                llItem.addView(carrierCodeTextView)

                // add carrier name
                val carrierNameTextView = TextView(this)
                carrierNameTextView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
                val nameParams = carrierNameTextView.layoutParams as LinearLayout.LayoutParams
                nameParams.weight = 1.0f
                carrierNameTextView.layoutParams = nameParams
                carrierNameTextView.gravity = Gravity.CENTER_VERTICAL
                carrierNameTextView.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                carrierNameTextView.text = carrier.offerCarrierName
                llItem.addView(carrierNameTextView)

                ll_offer_condition_carriers.addView(llItem)
            }
        }
    }

    private fun setRdTermName(offer: Offer) {
        tv_offer_condition_rdterm.text = getString(RdTermItemTypes.getRdTermItemType(offer.offerRdTermCode)!!.rdNameId)
    }

    private fun setContainerTypeAndSize(offer: Offer) {
        ll_offer_condition_containertype.removeAllViews()
        ll_offer_condition_containertype.addView(makeContainerTypeLayout(offer.offerLineItems))
    }

    private fun setCarrier(offer: Offer) {
        makeCarriers(offer)
    }

    private fun setPayPlan(offer: Offer, paymentPlan: PaymentPlan) {
        offer.offerLineItems?.let { lineItems ->
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            currencyFormat.minimumFractionDigits = 0

            // total amount = sum of prices * quantities
            val totalPrice = lineItems.filter { it.isChecked }.map { it.offerPrice * it.offerQty }.sum()
            tv_deal_amount.text = currencyFormat.format(totalPrice).toString()

            // tv_payment_term (01: PPD, 02:CCT)
            offer.offerPaymentTermCode.let {paymentTermCode ->
                when (paymentTermCode) {
                    OFFER_PAYMENT_TERM_CODE_PPD -> {
                        tv_payment_term.text = getString(R.string.payplan_prepaid)
                    }
                    OFFER_PAYMENT_TERM_CODE_CCT -> {
                        tv_payment_term.text = getString(R.string.payplan_collect)
                    }
                }
            }

            // paymentPlan
            tv_initial_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
            tv_midterm_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
            tv_ramainder_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())

            tv_initial_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.initialPaymentRatio), totalPrice))
            tv_midterm_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.middlePaymentRatio), totalPrice))
            tv_ramainder_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.balancePaymentRatio), totalPrice))
        }
    }

    private fun makeContainerTypeLayout(offerLineItems: List<OfferLineItem>?): LinearLayout {
        val llList = LinearLayout(this)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL

        offerLineItems?.let {
            val containerTypeCode = offerLineItems[0].tradeContainerTypeCode
            val llSub = LinearLayout(this)
            llSub.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            llSub.orientation = LinearLayout.HORIZONTAL
            llSub.gravity = Gravity.CENTER_VERTICAL
            val margin = 4.toDp().toInt()
            val params = llSub.layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, margin, 0, margin)
            llSub.layoutParams = params

            // Left TextView
            llSub.addView(makeContainerNameView(containerTypeCode))

            // Right TextView
            val containerSizeView = makeContainerSizeView()

            // Size 구성
            var id = R.string.container_size_20_abbrev
            when (offerLineItems[0].tradeContainerSizeCode) {
                ContainerSizeType.N20FT_TYPE -> {
                    id = R.string.container_size_20_abbrev
                }
                ContainerSizeType.N40FT_TYPE -> {
                    id = R.string.container_size_40_abbrev
                }
                ContainerSizeType.N45FT_TYPE -> {
                    id = R.string.container_size_40hc_abbrev
                }
                ContainerSizeType.N45HC_TYPE -> {
                    id = R.string.container_size_45hc_abbrev
                }
            }
            val nameShort = getString(id)
            containerSizeView.text = nameShort

            llSub.addView(containerSizeView)
            llList.addView(llSub)
        }
        return llList
    }

    private fun makeContainerNameView(containerType: String): TextView {
        // 90dp
        val textViewLeft = TextView(this)
        textViewLeft.layoutParams = LinearLayout.LayoutParams(84.toDp().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        textViewLeft.gravity = Gravity.START
        textViewLeft.includeFontPadding = false
        textViewLeft.setTextAppearance(R.style.txt_opensans_r_15_595959)
        textViewLeft.text = getString(ContainerName.getContainerName(containerType)!!.nameMiddleId)
        return textViewLeft
    }

    private fun makeContainerSizeView(): TextView {
        // 0dp (weight 1)
        val textViewRight = TextView(this)
        textViewRight.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        val params = textViewRight.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textViewRight.layoutParams = params
        textViewRight.gravity = Gravity.START
        textViewRight.includeFontPadding = false
        textViewRight.setTextAppearance(R.style.txt_opensans_r_15_595959)
        return textViewRight
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()

    enum class TabEnum constructor(
            val index: Int,
            val nameId: Int,
            val tagSeq: Int
    ) {
        TAB_CONTAINER_DETAIL(0, R.string.buy_offer_container_detail, layout_tab_tag_seq),
        TAB_PAY_PLAN(1, R.string.buy_offer_pay_plan, layout_tab_tag_seq + 1),
    }

    companion object {
        const val layout_tab_tag_seq = 1000000
        var mOffer = Offer()
        var mCarriers = mutableListOf<OfferCarrier>()
    }
}