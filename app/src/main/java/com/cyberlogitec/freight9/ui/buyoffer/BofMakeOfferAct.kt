package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_make_offer.*
import kotlinx.android.synthetic.main.appbar_bof_make_offer.*
import kotlinx.android.synthetic.main.body_bof_make_offer.*
import kotlinx.android.synthetic.main.body_pol_pod_card_simple.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = BofMakeOfferVm::class)
class BofMakeOfferAct : BaseActivity<BofMakeOfferVm>() {

    lateinit var offer: Offer

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private var isEnoughBudget: Boolean = false
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val budgetValue = 99_999_999
    private val feeRate = 0.01f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_make_offer)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        currencyFormat.minimumFractionDigits = 0

        // init type face (font)
        ResourcesCompat.getFont(this, R.font.opensans_regular)?.let{
            tfRegular = it
        }

        ResourcesCompat.getFont(this, R.font.opensans_bold)?.let {
            tfBold = it
        }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_bof_make_offer,
                menuType = MenuType.DONE,
                title = getString(R.string.offer_make_new_buy_offer))

        setListener()
    }

    private fun setRxOutputs() {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // output
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe { it ->
                    it?.let { offer ->
                        Timber.d("f9: onSuccessRefresh +")
                        // save offer
                        this.offer = offer
                        setPolPodValue(offer)
                        Timber.d("f9: onSuccessRefresh -")
                    }
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            val paymentPlan = it.body() as PaymentPlan
                            setHoldingDeposit(this.offer, paymentPlan)
                        } else {
                            showToast("Fail Request paymentPlan(Http)\n" + it.errorBody())
                        }
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClickNext
        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityWithFinish(Intent(this, MarketActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }


        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClick X Check
        viewModel.outPuts.onClickVolumeCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(BofVolumeCheckAct)")
                    startActivity(Intent(this, BofVolumeCheckAct::class.java).putExtra(Intents.OFFER, it as Offer))
                }

        viewModel.outPuts.onClickPriceCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(BofPriceCheckAct)")
                    startActivity(Intent(this, BofPriceCheckAct::class.java).putExtra(Intents.OFFER, it as Offer))
                }

        viewModel.outPuts.onClickPlanCheck()
                .bindToLifecycle(this)
                .subscribe {
                    startActivity(Intent(this, BofConditionCheckAct::class.java).putExtra(Intents.OFFER, it as Offer))
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClickIagree
        viewModel.outPuts.onClickIagree()
                .bindToLifecycle(this)
                .subscribe {
                    btn_make_buy_offer_next.isEnabled = chk_terms_agree.isChecked && isEnoughBudget
                }

        // outPuts - [Done]
        viewModel.outPuts.onClickDone()
                .bindToLifecycle(this)
                .subscribe {
                    showSaveDialog()
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: progress & error

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog : ${it}")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    private fun setListener() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // wait click event (toolbar left button)
        appbar_bof_make_offer.toolbar_left_btn.setSafeOnClickListener {
            onBackPressed()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        appbar_bof_make_offer.toolbar_done_btn.setSafeOnClickListener {
            viewModel.inPuts.clickToDone(Parameter.CLICK)
        }

        btn_make_buy_offer_next.setSafeOnClickListener {
            Timber.d("f9: btn_make_buy_offer_next +")
            viewModel.inPuts.clickToNext( offer )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        iv_period_and_volume.setSafeOnClickListener {
            viewModel.inPuts.clickToVolumeCheck(Parameter.CLICK)
        }

        // price check
        iv_price.setSafeOnClickListener {
            viewModel.inPuts.clickToPriceCheck(Parameter.CLICK)
        }

        // Condition check
        iv_pay_plan.setSafeOnClickListener {
            viewModel.inPuts.clickToPlanCheck(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click igree
        chk_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            viewModel.inPuts.clickToIagree(isChecked)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // whole deal option
        chk_whole_deal_apply.setOnClickListener {
            val isChecked = chk_whole_deal_apply.isChecked
            Timber.d("f9: isChecked: $isChecked")

            if (isChecked) {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_applied)
                tv_whole_deal_apply.typeface = tfBold
                this.offer.allYn = "1"  // whole
            } else  {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_not_applied)
                tv_whole_deal_apply.typeface = tfRegular
                this.offer.allYn = "0"  // partial
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click more
        tv_terms_more.setSafeOnClickListener {
            showTermsMorePopup()
        }
    }

    private fun setPolPodValue(offer: Offer) {
        if (!offer.offerCarriers.isNullOrEmpty()) {
            offer.offerCarriers?.let { carriers ->
                val carrierCode = carriers.first().offerCarrierCode
                val carriersCount = carriers.size
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
                tv_carrier_count.text = if (carriersCount > 1) String.format("+%d", carriersCount - 1) else ""
            }
        } else {
            // 지금 Step 에서는 carrier를 알 수 없음
            offer.tradeCompanyCode.let { carrierCode ->
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
                tv_carrier_count.text = Constant.EmptyString
            }
        }

        offer.offerRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POL }
                    .distinctBy { it.locationName }
                    .size
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = if (polCnt > 1) String.format("+%d", polCnt - 1) else ""
            }

            // find first pod
            val firstPod = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
            val podCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POD }
                    .distinctBy { it.locationName }
                    .size
            firstPod?.let {
                tv_pod_name.text = it.locationCode
                tv_pod_desc.text = it.locationName
                tv_pod_count.text = if (podCnt > 1) String.format("+%d", podCnt - 1) else ""
            }

            tv_period.text = Constant.EmptyString       // W01-W10
            // icon 표시하지 않음
            iv_period_whole.visibility = View.INVISIBLE
        }
    }

    private fun setHoldingDeposit(offer: Offer?, paymentPlan: PaymentPlan) {
        // Holding deposit : 사용자가 설정한 payplan의 Initial payment비율과 동일
        var holdingDeposit: Int
        offer?.let { offer ->
            offer.offerLineItems?.let { lineItems ->
                val totalPrice = lineItems.filter { it.isChecked }.map { it.offerPrice * it.offerQty }.sum()
                paymentPlan.let {
                    tv_holding_deposit_rate.text = getString(R.string.buy_offer_holding_deposit_rate, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    holdingDeposit = calcValue(calcRatio(paymentPlan.initialPaymentRatio), totalPrice)
                    tv_holding_deposit_value.text = currencyFormat.format(holdingDeposit)
                    setBalanceAndAvailableBudget(holdingDeposit)
                }
            }
        }
    }

    private fun setBalanceAndAvailableBudget(holdingDeposit: Int) {
        // required balance = holding deposit + transaction fee(holding deposit 금액의 1%)
        var balance = holdingDeposit + calcValue(calcRatio(feeRate), holdingDeposit)
        tv_required_balance_value.text = currencyFormat.format(balance)
        tv_available_account_budget_value.text = currencyFormat.format(budgetValue)

        isEnoughBudget = budgetValue >= balance
        val textColorEnoughBudget = if (isEnoughBudget) R.color.greyish_brown else R.color.orangey_red
        tv_available_account_budget.setTextColor(ContextCompat.getColor(this, textColorEnoughBudget))
        tv_available_account_budget_value.setTextColor(ContextCompat.getColor(this, textColorEnoughBudget))
    }

    private fun showSaveDialog() {
        val dialog = NormalTwoBtnDialog(title = getString(R.string.offer_save_dialog_title),
                desc = getString(R.string.offer_save_dialog_desc),
                leftBtnText = getString(R.string.offer_save_dialog_discard),
                rightBtnText = getString(R.string.offer_save_dialog_save))
        dialog.isCancelable = false
        dialog.setOnClickListener(View.OnClickListener {
            it?.let {
                dialog.dismiss()
                if (it.id == R.id.btn_right) {

                    // TODO : Save
                    showToast("SAVE offer")

                } else {
                    finish()
                }
            }
        })
        dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()
}