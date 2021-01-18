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
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.MasterContractWithInventory
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.ui.enums.ContainerCard
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toDp
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.popup_order_price_table.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import java.text.NumberFormat
import java.util.*

class PriceTablePopup(var view: View, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    private var currentOpenContainer = ContainerCard.AllContainerClose
    val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    var datas: Any? = null

    private var isOfferNoSelect = false
    private var isOffersEntry = false
    //var offerNoValueList = mutableListOf<String>()

    init {
        currencyFormat.minimumFractionDigits = 0
        view.ll_order_price_table_offer_no.setSafeOnClickListener {
            if (this.isOfferNoSelect) showOfferNoListDialog()
        }

        view.iv_order_price_table_close.setSafeOnClickListener {
            dismiss()
        }
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0)

    }

    fun initValue(datas: Any? = null, isOfferNoSelect: Boolean = false, isOffersEntry: Boolean = false) {

        this.datas = datas
        this.isOfferNoSelect = isOfferNoSelect
        this.isOffersEntry = isOffersEntry
        view.iv_offer_no_change.visibility = if (isOfferNoSelect) View.VISIBLE else View.GONE

        if (datas == null) {
            return
        } else {
            val masterContractNumber = when (datas) {
                is TradeOfferWrapper -> {
                    datas.orderTradeOfferDetail.masterContractNumber ?: EmptyString
                }
                is MasterContractWithInventory -> {
                    datas.masterContractNumber ?: EmptyString
                }
                else -> return
            }

            // master contract number
            if (masterContractNumber.isNotEmpty()) {
                view.ll_order_price_table_master_no.visibility = View.VISIBLE
                view.tv_order_price_table_master_contract_no.text = masterContractNumber
                view.view_order_price_table_offer_no_dummy.visibility = View.GONE
            } else {
                view.ll_order_price_table_master_no.visibility = View.GONE
            }

            processPriceTableData(datas, selectedOfferNoIndex)
        }
    }

    //----------------------------------------------------------------------------------------------
    // 각 카드들 생성 후 값 설정
    //----------------------------------------------------------------------------------------------
    private fun makeSelectedTabList(datas: Any) {
        view.ll_order_price_table_fullcontainer.visibility = View.GONE
        view.ll_order_price_table_fullcontainer_folding.visibility = View.GONE
        view.ll_order_price_table_fullcontainer_list.removeAllViews()
        view.ll_order_price_table_rfcontainer.visibility = View.GONE
        view.ll_order_price_table_rfcontainer_folding.visibility = View.GONE
        view.ll_order_price_table_rfcontainer_list.removeAllViews()
        view.ll_order_price_table_emptycontainer.visibility = View.GONE
        view.ll_order_price_table_emptycontainer_folding.visibility = View.GONE
        view.ll_order_price_table_emptycontainer_list.removeAllViews()
        view.ll_order_price_table_soccontainer.visibility = View.GONE
        view.ll_order_price_table_soccontainer_folding.visibility = View.GONE
        view.ll_order_price_table_soccontainer_list.removeAllViews()

        var groupByDatas = mutableMapOf<String, List<PriceValue>>()
        when (datas) {
            is TradeOfferWrapper -> {
                view.tv_order_price_table_rdterm.text =
                        view.context.getString(RdTermItemTypes.getRdTermItemType(datas.borList.rdTermCode!!)!!.rdNameId)

                val priceValueList = mutableListOf<PriceValue>()

                val offerLineItems = if (isOffersEntry) {
                    datas.cellLineItems
                } else {
                    datas.orderTradeOfferDetail.offerLineItems
                }

                offerLineItems?.let { items ->
                    for (offerLineItem in items) {
                        val baseYearWeek = if (isOffersEntry) {
                            (offerLineItem as Dashboard.Cell.LineItem).baseYearWeek
                        } else {
                            (offerLineItem as OrderTradeOfferDetail.OfferLineItem).baseYearWeek
                        }

                        val offerPrices = if (isOffersEntry) {
                            (offerLineItem as Dashboard.Cell.LineItem).offerPrices
                        } else {
                            (offerLineItem as OrderTradeOfferDetail.OfferLineItem).offerPrices
                        }

                        for (offerPrice in offerPrices) {
                            priceValueList.add(PriceValue(baseYearWeek,
                                    offerPrice.containerTypeCode,
                                    offerPrice.containerSizeCode,
                                    offerPrice.offerPrice.toInt())) // For MVP : Float to Int
                        }
                    }
                    groupByDatas = priceValueList.groupBy { it.containerTypeCode }.toMutableMap()
                }
            }
            is MasterContractWithInventory -> {
                view.tv_order_price_table_rdterm.text =
                        view.context.getString(RdTermItemTypes.getRdTermItemType(datas.rdTermCode!!)!!.rdNameId)

                val priceValueList = mutableListOf<PriceValue>()
                for (lineItem in datas.masterContractLineItems) {
                    for (price in lineItem.masterContractPrices) {
                        priceValueList.add(PriceValue(price.baseYearWeek!!,
                                price.containerTypeCode!!,
                                price.containerSizeCode!!,
                                price.price!!.toInt())) // For MVP : Float to Int
                    }
                }
                groupByDatas = priceValueList.groupBy { it.containerTypeCode }.toMutableMap()
            }
        }

        // key : containerTypeCode
        for ((key, priceValues) in groupByDatas) {
            val llContainer = makeCardPerContainer()
            llContainer.addView(makeTitlePerCardInContainer())

            val priceValuePerWeeks = mutableListOf<PriceValuePerWeek>()
            val groupByPriceValue = priceValues.sortedBy { it.containerSizeCode }.groupBy { it.week }
            groupByPriceValue.forEach { (weekKey, priceValues) ->
                val priceValuePerWeek = PriceValuePerWeek(key, weekKey)
                for (priceValue in priceValues) {
                    when (priceValue.containerSizeCode) {
                        ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT -> {
                            priceValuePerWeek.value20 = priceValue.price
                        }
                        ConstantTradeOffer.CONTAINER_SIZE_CODE_40FT -> {
                            priceValuePerWeek.value40 = priceValue.price
                        }
                        ConstantTradeOffer.CONTAINER_SIZE_CODE_40FTHC -> {
                            priceValuePerWeek.value40HC = priceValue.price
                        }
                        ConstantTradeOffer.CONTAINER_SIZE_CODE_45FTHC -> {
                            priceValuePerWeek.value45HC = priceValue.price
                        }
                    }
                }
                priceValuePerWeeks.add(priceValuePerWeek)
            }

            for (priceValuePerWeek in priceValuePerWeeks) {
                val llRow = LinearLayout(view.context)
                val height = 40.toDp().toInt()
                llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
                llRow.orientation = LinearLayout.HORIZONTAL
                llRow.gravity = Gravity.CENTER_VERTICAL
                llRow.addView(makeTextViewInCard(false, false, view.context.getWeek(priceValuePerWeek.week), Gravity.START))

                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value20.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value40.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value40HC.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value45HC.toString(), Gravity.END))
                llContainer.addView(llRow)
            }

            when (key) {
                ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY -> {
                    view.ll_order_price_table_fullcontainer.visibility = View.VISIBLE
                    view.ll_order_price_table_fullcontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_REEFER -> {
                    view.ll_order_price_table_rfcontainer.visibility = View.VISIBLE
                    view.ll_order_price_table_rfcontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_EMPTY -> {
                    view.ll_order_price_table_emptycontainer.visibility = View.VISIBLE
                    view.ll_order_price_table_emptycontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_SOC -> {
                    view.ll_order_price_table_soccontainer.visibility = View.VISIBLE
                    view.ll_order_price_table_soccontainer_list.addView(llContainer)
                }
            }
        }

        // 카드를 클릭했을때 처리를 위한 클릭 리스너 설정
        clickContainerCard()
        // 모든 카드를 collapsed 처리
        clickContainerCardProcess(currentOpenContainer, spareVisible = true)
    }

    private fun makeCardPerContainer(): LinearLayout {
        val llList = LinearLayout(view.context)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL
        return llList
    }

    // Container type 별 Title
    private fun makeTitlePerCardInContainer(): LinearLayout {
        val llTitle = LinearLayout(view.context)

        val height = 28.toDp().toInt()
        llTitle.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        llTitle.orientation = LinearLayout.HORIZONTAL
        llTitle.gravity = Gravity.CENTER_VERTICAL

        for ((index, title) in tableTitleList.withIndex()) {
            val align: Int = when (index) {
                0 -> Gravity.START
                else -> Gravity.END
            }
            llTitle.addView(makeTextViewInCard(true, false, title, align))
        }

        return llTitle
    }

    // TODO : price 정렬 안맞는 경우 아래 함수에서 margin을 수정 후 S9 단말에서 확인한다.
    private fun makeTextViewInCard(isBold: Boolean, isCurrency: Boolean, value: String, align: Int): TextView {
        val textView = TextView(view.context)
        textView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textView.layoutParams = params
        textView.gravity = align or Gravity.CENTER_VERTICAL
        textView.setTextAppearance(if (isBold) R.style.txt_opensans_b_11_greyishbrown else R.style.txt_opensans_r_13_greyishbrown)
        textView.text = if (isCurrency) {
            if (value.toInt() <= 0) "-"
            else currencyFormat.format(value.toInt())
        } else value
        return textView
    }

    //----------------------------------------------------------------------------------------------
    // Container card 를 클릭 : Expand, Collapsed 처리
    //----------------------------------------------------------------------------------------------
    private fun clickContainerCard() {
        view.ll_order_price_table_fullcontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.FullContainer)
        }
        view.ll_order_price_table_rfcontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.RFContainer)
        }
        view.ll_order_price_table_emptycontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.EmptyContainer)
        }
        view.ll_order_price_table_soccontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.SOCContainer)
        }
    }

    private fun clickContainerCardProcess(containerCard: ContainerCard, spareVisible: Boolean = false) {
        currentOpenContainer = containerCard

        // ContainerCard.AllContainerClose 인 경우 첫번째 Containercard 로 설정하고 Open될 수 있도록 한다
        if (containerCard == ContainerCard.AllContainerClose) {
            when {
                view.ll_order_price_table_fullcontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.FullContainer
                view.ll_order_price_table_rfcontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.RFContainer
                view.ll_order_price_table_emptycontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.EmptyContainer
                view.ll_order_price_table_soccontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.SOCContainer
            }
        }

        when (currentOpenContainer) {
            ContainerCard.AllContainerClose -> {
                view.ll_order_price_table_fullcontainer_folding.visibility = View.GONE
                view.iv_order_price_table_fullcontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                view.ll_order_price_table_emptycontainer_folding.visibility = View.GONE
                view.iv_order_price_table_emptycontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                view.ll_order_price_table_soccontainer_folding.visibility = View.GONE
                view.iv_order_price_table_soccontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                view.ll_order_price_table_rfcontainer_folding.visibility = View.GONE
                view.iv_order_price_table_rfcontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
            }
            ContainerCard.FullContainer -> {
                val visible = view.ll_order_price_table_fullcontainer_folding.visibility == View.VISIBLE
                view.ll_order_price_table_fullcontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                view.iv_order_price_table_fullcontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.EmptyContainer -> {
                val visible = view.ll_order_price_table_emptycontainer_folding.visibility == View.VISIBLE
                view.ll_order_price_table_emptycontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                view.iv_order_price_table_emptycontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.SOCContainer -> {
                val visible = view.ll_order_price_table_soccontainer_folding.visibility == View.VISIBLE
                view.ll_order_price_table_soccontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                view.iv_order_price_table_soccontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.RFContainer -> {
                val visible = view.ll_order_price_table_rfcontainer_folding.visibility == View.VISIBLE
                view.ll_order_price_table_rfcontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                view.iv_order_price_table_rfcontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
        }

        view.view_pricetable_bottom_spare.visibility = if (spareVisible) View.VISIBLE else View.GONE
    }

    //----------------------------------------------------------------------------------------------

    private fun processPriceTableData(datas: Any? = null, offerNoIndex: Int = -1) {

        /**
         * TODO : offerNoIndex 에 해당되는 offer No. 의 값으로 Refresh 하도록 한다
         */

        if (datas != null) {
            var offerNumber = EmptyString
            when (datas) {
                is TradeOfferWrapper -> {
                    offerNoList.add(TextItem(datas.borList.offerNumber!!, true, 0))
                    selectedOfferNoIndex = 0
                    offerNumber = offerNoList[selectedOfferNoIndex]._value
                    if (isOfferNoSelect) {
                        offerNumber = offerNoList[offerNoIndex]._value
                    }
                }
                is MasterContractWithInventory -> {
                    // Offer number 없음
                    offerNumber = EmptyString
                }
            }

            // offer group number
            if (offerNumber.isNotEmpty()) {
                view.ll_order_price_table_offer_no.visibility = View.VISIBLE
                view.tv_order_price_table_offer_no.text = offerNumber
            } else {
                view.ll_order_price_table_offer_no.visibility = View.GONE
            }

            makeSelectedTabList(datas)
        }
    }

    //----------------------------------------------------------------------------------------------

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
            processPriceTableData(selectedOfferNoIndex)
        }

        inflatedView.picker.setItems(offerNoList)
        inflatedView.picker.index = selectedOfferNoIndex

        dialog.show()
    }

    //----------------------------------------------------------------------------------------------

//    private val layout_tag_seq = 1000000
//
//    data class ContainerOpenStatus(
//            var rdTerm: RdTermItemTypes,
//            var containerOpenCard: ContainerCard = ContainerCard.AllContainerClose
//    )

    data class PriceValue(
            var week: String = EmptyString,
            var containerTypeCode: String = EmptyString,
            var containerSizeCode: String = EmptyString,
            var price: Int = 0  // For MVP : Float to Int
    ) {
//        fun isEmptyPrices(): Boolean {
//            return price == 0
//        }
    }

    data class PriceValuePerWeek(
            var containerTypeCode: String = EmptyString,
            var week: String = EmptyString,
            var value20: Int = 0,
            var value40: Int = 0,
            var value40HC: Int = 0,
            var value45HC: Int = 0
    )

    private val tableTitleList = listOf(view.context.getString(R.string.table_title_period)
            , view.context.getString(R.string.container_size_20_abbrev)
            , view.context.getString(R.string.container_size_40_abbrev)
            , view.context.getString(R.string.container_size_40hc_abbrev)
            , view.context.getString(R.string.container_size_45hc_abbrev))

//    enum class TextAlign {
//        ALIGN_LEFT,
//        ALIGN_CENTER,
//        ALIGN_RIGHT
//    }
}

