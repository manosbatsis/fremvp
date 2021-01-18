package com.cyberlogitec.freight9.ui.buyorder

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.MasterContractWithInventory
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.PayPlanEntry
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.toDp
import com.cyberlogitec.freight9.ui.inventory.InventoryViewModel
import com.cyberlogitec.freight9.ui.sellorder.SellOrderViewModel
import com.cyberlogitec.freight9.ui.youroffers.YourOffersDetailViewModel
import com.cyberlogitec.freight9.ui.youroffers.YourOffersHistoryDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.popup_order_detail_condition.view.*
import kotlinx.android.synthetic.main.popup_order_pay_plan.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*

class DetailConditionPopup(var view: View, var viewModel: Any?, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    private var isOfferNoSelect = false
    private var isEntryInventory = false
    var parentView: View? = null
    var datas: Any? = null
    //var offerNoValueList = mutableListOf<String>()

    init {
        // 03.1 New Sell offer_06.1 로 이동 ?
        view.tv_order_detail_condition_go_calendar.setOnClickListener {
//            view.context.startActivity(Intent(view.context, SofConditionDetailAct::class.java))
            showPayPlan()
        }

        view.ll_order_detail_condition_offer_no.setSafeOnClickListener {
            if (isOfferNoSelect) showOfferNoListDialog()
        }

        view.tv_order_detail_condition_transaction_statement.setSafeOnClickListener {
            view.context.showToast("GO_TRANSACTION_STATEMENT")
        }

        view.iv_order_detail_condition_close.setSafeOnClickListener {
            dismiss()
        }

        when (viewModel) {
            is BuyOrderViewModel -> {
                (viewModel as BuyOrderViewModel).outPuts.onSuccessRequestCarrierName()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_detail_condition_carrier_desc.text = it
                            }
                        }
            }
            is SellOrderViewModel -> {
                (viewModel as SellOrderViewModel).outPuts.onSuccessRequestCarrierName()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_detail_condition_carrier_desc.text = it
                            }
                        }
            }
            is YourOffersDetailViewModel -> {
                (viewModel as YourOffersDetailViewModel).outPuts.onSuccessRequestCarrierName()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_detail_condition_carrier_desc.text = it
                            }
                        }
            }
            is YourOffersHistoryDetailViewModel -> {
                (viewModel as YourOffersHistoryDetailViewModel).outPuts.onSuccessRequestCarrierName()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_detail_condition_carrier_desc.text = it
                            }
                        }
            }
            is InventoryViewModel -> {
                (viewModel as InventoryViewModel).outPuts.onSuccessRequestCarrierName()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_detail_condition_carrier_desc.text = it
                            }
                        }
                (viewModel as InventoryViewModel).outPuts.onSuccessRequestPaymentPlan()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                if (it.isSuccessful) {
                                    val paymentPlan = it.body() as PaymentPlan
                                    with(datas as MasterContractWithInventory) {
                                        val isPPD = paymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
                                        val pymtType =
                                                if (isPPD) view.context.getString(R.string.payplan_prepaid)
                                                else view.context.getString(R.string.payplan_collect)
                                        val payPlan = view.context.getString(R.string.payplan_rate_full_display, pymtType,
                                                (paymentPlan.initialPaymentRatio * 100).toInt().toString(),
                                                (paymentPlan.middlePaymentRatio * 100).toInt().toString(),
                                                (paymentPlan.balancePaymentRatio * 100).toInt().toString())
                                        view.tv_order_detail_condition_payplan.text = payPlan
                                    }
                                } else {
                                    view.context.showToast("Fail Save Offer(Http)\n" + it.errorBody())
                                }
                            }
                        }
            }
        }
    }

    fun show(parent: View) {
        parentView = parent
        showAtLocation(parent, Gravity.CENTER, 0, 0)
    }

    fun initValue(datas: Any? = null, isOfferNoSelect: Boolean = false, isEntryInventory: Boolean = false) {

        /**
         * TODO : offerNoIndex 에 해당되는 offer No. 의 값으로 Refresh 하도록 한다
         *  this.isOfferNoSelect : true - Spin control (offer no 선택 유무)
         */
        this.isOfferNoSelect = isOfferNoSelect
        this.isEntryInventory = isEntryInventory
        this.selectedOfferNoIndex = -1
        view.iv_offer_no_change.visibility = if (isOfferNoSelect) View.VISIBLE else View.GONE
        view.tv_order_detail_condition_transaction_statement.visibility = if (isEntryInventory) View.VISIBLE else View.GONE

        if (isOfferNoSelect) {
            makeOfferNoList()
            selectedOfferNoIndex = 0
            view.tv_order_detail_condition_offer_no.text = offerNoList[selectedOfferNoIndex]._value
        }

        if (datas != null) {
            this.datas = datas
            setData(selectedOfferNoIndex, datas)
        }
    }

    private fun setData(offerNoIndex: Int = -1, datas: Any? = null) {

        /**
         * TODO : offerNoIndex 에 해당되는 offer No. 의 값으로 Refresh 하도록 한다
         */
        if (isOfferNoSelect) {
            view.tv_order_detail_condition_offer_no.text = offerNoList[offerNoIndex]._value
        }

        if (datas != null) {
            val masterContractNumber: String
            val offerNumber: String
            when (datas) {
                is TradeOfferWrapper -> {
                    // master contract no.
                    masterContractNumber =  datas.orderTradeOfferDetail.masterContractNumber ?: EmptyString
                    offerNumber = datas.borList.offerNumber ?: EmptyString
                }
                is MasterContractWithInventory -> {
                    // master contract no.
                    masterContractNumber = datas.masterContractNumber ?: EmptyString
                    offerNumber = EmptyString // TODO : 표시할 값 없음
                }
                else -> {
                    return
                }
            }
            // master contract number
            if (masterContractNumber.isNotEmpty()) {
                view.ll_order_detail_condition_master_no.visibility = View.VISIBLE
                view.tv_order_detail_condition_master_contract_no.text = masterContractNumber
                view.view_order_detail_condition_offer_no_dummy.visibility = View.GONE
            } else {
                view.ll_order_detail_condition_master_no.visibility = View.GONE
            }
            // offer group number
            if (offerNumber.isNotEmpty()) {
                view.ll_order_detail_condition_offer_no.visibility = View.VISIBLE
                view.tv_order_detail_condition_offer_no.text = offerNumber
            } else {
                view.ll_order_detail_condition_offer_no.visibility = View.GONE
            }
            // rd terms
            view.tv_order_detail_condition_rdterm.text = getRdTermsNames(datas)
            // container type
            view.ll_order_detail_condition_containertype.removeAllViews()
            view.ll_order_detail_condition_containertype.addView(makeContainerTypeLayout(datas))
            // pay plan
            view.tv_order_detail_condition_payplan.text = getPayPlan(datas)
            // carrier logo
            view.iv_order_detail_condition_carrier_logo.setImageResource(getCarrierCode(datas).getCarrierIcon(false))
            // carrier code
            val isValidCarrierCode = getCarrierCode(datas).isNotEmpty()
            view.tv_order_detail_condition_carrier.text =
                    if (isValidCarrierCode) getCarrierCode(datas)
                    else view.context.getString(R.string.all_carriers)
            // operator
            view.tv_order_detail_condition_operator.text = getOperatorName(datas)
            // Carrier name(Full name)
            requestCarrierName(datas)
        }
    }

    private fun getRdTermsNames(datas: Any? = null): String {
        var rdTermNames = EmptyString
        when (datas) {
            is TradeOfferWrapper -> {
                rdTermNames = view.context.getString(RdTermItemTypes.getRdTermItemType(datas.borList.rdTermCode!!)!!.rdNameId)
            }
            is MasterContractWithInventory -> {
                rdTermNames = view.context.getString(RdTermItemTypes.getRdTermItemType(datas.rdTermCode!!)!!.rdNameId)
            }
        }
        return rdTermNames
    }

    private fun getPayPlan(datas: Any? = null): String {
        var payPlan = EmptyString
        when (datas) {
            is TradeOfferWrapper -> {
                val isPPD = datas.orderTradeOfferDetail.offerPaymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
                val pymtType = if (isPPD) view.context.getString(R.string.payplan_prepaid)
                else view.context.getString(R.string.payplan_collect)

                val initPymtRto = (datas.orderTradeOfferDetail.offerLineItems[0].firstPaymentRatio * 100).toInt().toString()
                val midTrmPymtRto = (datas.orderTradeOfferDetail.offerLineItems[0].middlePaymentRatio * 100).toInt().toString()
                val balRto = (datas.orderTradeOfferDetail.offerLineItems[0].balancedPaymentRatio * 100).toInt().toString()
                payPlan = view.context.getString(R.string.payplan_rate_full_display,
                        pymtType, initPymtRto, midTrmPymtRto, balRto)
            }
            is MasterContractWithInventory -> {
                // 매번요청?
                (viewModel as InventoryViewModel).inPuts.requestPaymentPlan(datas.paymentPlanCode!!)
            }
        }
        return payPlan
    }

    private fun getCarrierCode(datas: Any? = null): String {
        var carrierCode = EmptyString
        when (datas) {
            is TradeOfferWrapper -> {
                carrierCode = datas.borList.cryrCd ?: EmptyString
            }
            is MasterContractWithInventory -> {
                carrierCode = datas.masterContractCarriers[0].carrierCode ?: EmptyString
            }
        }
        return carrierCode.trim()
    }

    private fun requestCarrierName(datas: Any? = null) {
        var carrierCode = EmptyString
        when (datas) {
            is TradeOfferWrapper -> {
                carrierCode = datas.borList.cryrCd ?: EmptyString
            }
            is MasterContractWithInventory -> {
                carrierCode = datas.masterContractCarriers[0].carrierCode ?: EmptyString
            }
        }

        if (carrierCode.trim().isNotEmpty()) {
            when (viewModel) {
                is BuyOrderViewModel -> {
                    (viewModel as BuyOrderViewModel).inPuts.requestCarrierName(carrierCode)
                }
                is SellOrderViewModel -> {
                    (viewModel as SellOrderViewModel).inPuts.requestCarrierName(carrierCode)
                }
                is InventoryViewModel -> {
                    (viewModel as InventoryViewModel).inPuts.requestCarrierName(carrierCode)
                }
                is YourOffersDetailViewModel -> {
                    (viewModel as YourOffersDetailViewModel).inPuts.requestCarrierName(carrierCode)
                }
                is YourOffersHistoryDetailViewModel -> {
                    (viewModel as YourOffersHistoryDetailViewModel).inPuts.requestCarrierName(carrierCode)
                }
            }
        }
    }

    private fun makeContainerNameView(containerType: String): TextView {
        // 90dp
        val textviewLeft = TextView(view.context)
        textviewLeft.layoutParams = LinearLayout.LayoutParams(84.toDp().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        textviewLeft.gravity = Gravity.START
        textviewLeft.includeFontPadding = false
        textviewLeft.setTextAppearance(R.style.txt_opensans_r_15_595959)
        textviewLeft.text = view.context.getString(ContainerName.getContainerName(containerType)!!.nameMiddleId)
        return textviewLeft
    }

    private fun makeContainerSizeView(): TextView {
        // 0dp (weight 1)
        val textviewRight = TextView(view.context)
        textviewRight.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        val params = textviewRight.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textviewRight.layoutParams = params
        textviewRight.gravity = Gravity.START
        textviewRight.includeFontPadding = false
        textviewRight.setTextAppearance(R.style.txt_opensans_r_15_595959)
        return textviewRight
    }

    private fun makeContainerTypeLayout(datas: Any? = null): LinearLayout {
        val llList = LinearLayout(view.context)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL

        var groupByContainerTypeCode = mutableMapOf<String?, List<Any>>()
        when (datas) {
            is TradeOfferWrapper -> {
                groupByContainerTypeCode = datas.orderTradeOfferDetail.offerLineItems[0].offerPrices
                        .sortedBy { it.containerSizeCode }
                        .groupBy { it.containerTypeCode }
                        .toMutableMap()
            }
            is MasterContractWithInventory -> {
                groupByContainerTypeCode = datas.masterContractLineItems[0].masterContractPrices
                        .sortedBy { it.containerSizeCode }
                        .groupBy { it.containerTypeCode }
                        .toMutableMap()
            }
        }

        for ((key, values) in groupByContainerTypeCode) {
            val llSub = LinearLayout(view.context)
            llSub.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            llSub.orientation = LinearLayout.HORIZONTAL
            llSub.gravity = Gravity.CENTER_VERTICAL
            val margin = 4.toDp().toInt()
            val params = llSub.layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, margin, 0, margin)
            llSub.layoutParams = params

            // Left TextView
            llSub.addView(makeContainerNameView(key!!))
            // Right TextView
            val containerSizeView = makeContainerSizeView()

            // Size 구성
            var rightContent = EmptyString
            for ((index, value) in values.withIndex()) {
                var id = R.string.container_size_20_abbrev

                var containerSizeCode = EmptyString
                when (value) {
                    is OrderTradeOfferDetail.OfferLineItem.OfferPrice -> {
                        containerSizeCode = value.containerSizeCode
                    }
                    is MasterContractWithInventory.MasterContractLineItem.MasterContractPrice -> {
                        containerSizeCode = value.containerSizeCode!!
                    }
                }

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
                val nameShort = view.context.getString(id)
                rightContent += (if (index > -1 && index < values.size - 1) ("$nameShort, ") else nameShort)
            }
            containerSizeView.text = rightContent

            llSub.addView(containerSizeView)
            llList.addView(llSub)
        }

        return llList
    }

    // 운영자, 시스템, 서비스의 운영을 담당하는 주체
    // dataOwnrPtrId : 데이터 소유자를 의미하는 PARTNER ID
    private fun getOperatorName(datas: Any? = null): String {
        var operatorName = EmptyString
        when (datas) {
            is TradeOfferWrapper -> {
                operatorName = "TODO"
            }
        }
        return operatorName
    }

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showPayPlan() {
        with(view) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_order_pay_plan, null)
            popupWindow = PayPlanPopup(popupView, viewModel, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, false)
            when (datas) {
                is TradeOfferWrapper -> {
                    /**
                     * TODO : Sell Offer 의 모든 주차에 대한 plan 인가?
                     *  40ft 또는 20ft / F type 의 모든 주차에 대한 sellable Qty 에 대해서...?
                     */
                    (popupWindow as PayPlanPopup).initValue(context.getString(R.string.buy_order_condition_popup_pay_plan), datas, PayPlanEntry.PP_BuyOrderDetailCondition)
                }
                else -> {
                    (popupWindow as PayPlanPopup).initValue(context.getString(R.string.buy_order_condition_popup_pay_plan))
                }
            }
            popupView.iv_order_pay_plan_close.setOnClickListener {
                removePopup()
            }
        }
        (popupWindow as PayPlanPopup).isOutsideTouchable = true
        (popupWindow as PayPlanPopup).isFocusable = true
        (popupWindow as PayPlanPopup).show(parentView!!)
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    private var selectedOfferNoIndex = -1
    private var offerNoList = mutableListOf<TextItem>()
    @SuppressLint("InflateParams")
    private fun showOfferNoListDialog() {
        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflatedView = inflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(view.context)

        dialog.setCancelable(false)
        dialog.setContentView(inflatedView)

        inflatedView.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        inflatedView.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                inflatedView.picker.setSelected(index)
                selectedOfferNoIndex = index
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            setData(selectedOfferNoIndex)
        }

        inflatedView.picker.setItems(offerNoList)
        inflatedView.picker.index = selectedOfferNoIndex

        dialog.show()
    }

    private fun makeOfferNoList() {
        offerNoList.add(TextItem("OFR-HLC-20191113-S01", true, 0))
        offerNoList.add(TextItem("OFR-HLC-20191113-S02", false, 1))
        offerNoList.add(TextItem("OFR-HLC-20191113-S03", false, 2))
        offerNoList.add(TextItem("OFR-HLC-20191113-S04", false, 3))
        offerNoList.add(TextItem("OFR-HLC-20191113-S05", false, 4))
        selectedOfferNoIndex = 0
    }
}

