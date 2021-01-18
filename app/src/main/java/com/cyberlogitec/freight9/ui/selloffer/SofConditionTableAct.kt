package com.cyberlogitec.freight9.ui.selloffer

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.ContractLineItem
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.toDp
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_header_offer_condition.*
import kotlinx.android.synthetic.main.appbar_offer_condition_table.*
import kotlinx.android.synthetic.main.popup_offer_condition_table.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofConditionTableVm::class)
class SofConditionTableAct : BaseActivity<SofConditionTableVm>() {

    companion object {
        var mContract = Contract()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("f9: onCreate")

        setContentView(R.layout.act_offer_condition_table)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {


        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_offer_condition_table,
                menuType = MenuType.CROSS,
                title = getString(R.string.selloffer_condition_detail),
                isEnableNavi=false)
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh +")
                    it?.let { contract ->
                        mContract = contract
                        // master contract no
                        tv_offer_condition_master_contract_no.text = contract.masterContractNumber
                        // RD Term
                        setRdTermName(contract.rdTermCode)
                        // Container Type & Size
                        contract.masterContractLineItems?.let { masterContractLineItems ->
                            setContainerTypeAndSize(masterContractLineItems)
                        }
                        // Carriers
                        contract.masterContractCarriers?.let { carriers ->
                            setCarrier(carriers.first().carrierCode)
                        }
                    }
                    Timber.d("f9: onSuccessRefresh -")
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            val currentPaymentPlan = it.body() as PaymentPlan
                            setPaymentTermName(mContract.paymentTermCode, currentPaymentPlan)
                        } else {
                            showToast("Fail Get Payment(Http)\n" + it.errorBody())
                        }
                    }
                }

        viewModel.outPuts.onSuccessRequestCarrierDescription()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { carrierDescription ->
                        setCarrierDescription(carrierDescription)
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
        appbar_offer_condition_table.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }
    }

    private fun setRdTermName(rdTermCode: String) {
        tv_offer_condition_rdterm.text = getString(RdTermItemTypes.getRdTermItemType(rdTermCode)!!.rdNameId)
    }

    private fun setContainerTypeAndSize(masterContractLineItems: List<ContractLineItem>) {
        ll_offer_condition_containertype.removeAllViews()
        ll_offer_condition_containertype.addView(makeContainerTypeLayout(masterContractLineItems))
    }

    private fun setPaymentTermName(paymentTermCode: String, paymentPlan: PaymentPlan) {
        val paymentType =
                if (paymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD) {
                    getString(R.string.payplan_prepaid)
                } else {
                    getString(R.string.payplan_collect)
                }
        val payPlan = getString(R.string.payplan_rate_full_display, paymentType,
                (paymentPlan.initialPaymentRatio * 100).toInt().toString(),
                (paymentPlan.middlePaymentRatio * 100).toInt().toString(),
                (paymentPlan.balancePaymentRatio * 100).toInt().toString())
        tv_offer_condition_payplan.text = payPlan
    }

    private fun setCarrier(carrierCode: String) {
        var code = carrierCode.trim()
        iv_offer_condition_carrier_logo.setImageResource(code.getCarrierIcon(false))
        val isValidCarrierCode = code.isNotEmpty()
        tv_offer_condition_carrier.text =
                if (isValidCarrierCode) {
                    code
                } else {
                    getString(R.string.all_carriers)
                }
        requestCarrierDescription(carrierCode)
    }

    private fun requestCarrierDescription(carrierCode: String) {
        viewModel.inPuts.requestCarrierDescription(carrierCode)
    }

    private fun setCarrierDescription(carrierDescription: String) {
        tv_offer_condition_carrier_desc.text = carrierDescription
    }

    private fun makeContainerTypeLayout(masterContractLineItems: List<ContractLineItem>): LinearLayout {
        val llList = LinearLayout(this)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL

        var groupByContainerTypeCode = masterContractLineItems[0].masterContractPrices!!
                .sortedBy { it.containerSizeCode }
                .groupBy { it.containerTypeCode }
                .toMutableMap()

        for ((key, values) in groupByContainerTypeCode) {
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
            llSub.addView(makeContainerNameView(key))
            // Right TextView
            val containerSizeView = makeContainerSizeView()

            // Size 구성
            var rightContent = ""
            for ((index, value) in values.withIndex()) {
                var id = R.string.container_size_20_abbrev
                var containerSizeCode = value.containerSizeCode
                when (containerSizeCode) {
                    ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT -> {
                        id = R.string.container_size_20_abbrev
                    }
                    ConstantTradeOffer.CONTAINER_SIZE_CODE_40FT -> {
                        id = R.string.container_size_40_abbrev
                    }
                    ConstantTradeOffer.CONTAINER_SIZE_CODE_40FTHC -> {
                        id = R.string.container_size_40hc_abbrev
                    }
                    ConstantTradeOffer.CONTAINER_SIZE_CODE_45FTHC -> {
                        id = R.string.container_size_45hc_abbrev
                    }
                }
                val nameShort = getString(id)
                rightContent += (if (index > -1 && index < values.size - 1) ("$nameShort, ") else nameShort)
            }
            containerSizeView.text = rightContent

            llSub.addView(containerSizeView)
            llList.addView(llSub)
        }
        return llList
    }

    private fun makeContainerNameView(containerType: String): TextView {
        // 90dp
        val textviewLeft = TextView(this)
        textviewLeft.layoutParams = LinearLayout.LayoutParams(84.toDp().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        textviewLeft.gravity = Gravity.START
        textviewLeft.includeFontPadding = false
        textviewLeft.setTextAppearance(R.style.txt_opensans_r_15_595959)
        textviewLeft.text = getString(ContainerName.getContainerName(containerType)!!.nameMiddleId)
        return textviewLeft
    }

    private fun makeContainerSizeView(): TextView {
        // 0dp (weight 1)
        val textviewRight = TextView(this)
        textviewRight.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        val params = textviewRight.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textviewRight.layoutParams = params
        textviewRight.gravity = Gravity.START
        textviewRight.includeFontPadding = false
        textviewRight.setTextAppearance(R.style.txt_opensans_r_15_595959)
        return textviewRight
    }
}