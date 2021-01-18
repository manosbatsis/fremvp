package com.cyberlogitec.freight9.ui.selloffer

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.ContractLineItem
import com.cyberlogitec.freight9.lib.ui.enums.ContainerCard
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.toDp
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_header_offer_condition.*
import kotlinx.android.synthetic.main.appbar_offer_price_table.*
import kotlinx.android.synthetic.main.body_offer_price_table.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = SofPriceTableVm::class)
class SofPriceTableAct : BaseActivity<SofPriceTableVm>() {

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var currentOpenContainer = ContainerCard.AllContainerClose
    private var tableTitleList : List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_price_table)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        tableTitleList = listOf(getString(R.string.table_title_period)
                , getString(R.string.container_size_20_abbrev)
                , getString(R.string.container_size_40_abbrev)
                , getString(R.string.container_size_40hc_abbrev)
                , getString(R.string.container_size_45hc_abbrev))
        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_offer_price_table,
                menuType = MenuType.CROSS,
                title = getString(R.string.selloffer_price_table),
                isEnableNavi = false)
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh2 +")
                    // select unique week.
                    it?.let { contract ->

                        tv_offer_condition_master_contract_no.text = contract.masterContractNumber

                        // RD Term
                        setRdTermName(contract.rdTermCode)

                        contract.masterContractLineItems?.let { contractLineItems ->
                            processPriceTableData(contractLineItems)
                        }
                    }
                    Timber.d("f9: onSuccessRefresh2 -")
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
        // wait click event (toolbar left button)
        appbar_offer_price_table.toolbar_left_btn.setSafeOnClickListener{
            Timber.d("f9: toolbar_left_btn clcick")
            onBackPressed()
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        appbar_offer_price_table.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            //viewModel.inPuts.clickToMenu(Parameter.CLICK)
            onBackPressed()
        }
    }

    private fun setRdTermName(rdTermCode: String) {
        tv_offer_price_table_rdterm.text = getString(RdTermItemTypes.getRdTermItemType(rdTermCode)!!.rdNameId)
    }

    private fun processPriceTableData(contractLineItems: List<ContractLineItem>) {
        ll_price_table_fullcontainer.visibility = View.GONE
        ll_price_table_fullcontainer_folding.visibility = View.GONE
        ll_price_table_fullcontainer_list.removeAllViews()
        ll_price_table_rfcontainer.visibility = View.GONE
        ll_price_table_rfcontainer_folding.visibility = View.GONE
        ll_price_table_rfcontainer_list.removeAllViews()
        ll_price_table_emptycontainer.visibility = View.GONE
        ll_price_table_emptycontainer_folding.visibility = View.GONE
        ll_price_table_emptycontainer_list.removeAllViews()
        ll_price_table_soccontainer.visibility = View.GONE
        ll_price_table_soccontainer_folding.visibility = View.GONE
        ll_price_table_soccontainer_list.removeAllViews()

        val priceValueList = mutableListOf<PriceValue>()
        for (lineItem in contractLineItems) {
            for (price in lineItem.masterContractPrices!!) {
                priceValueList.add(PriceValue(price.baseYearWeek,
                        price.containerTypeCode,
                        price.containerSizeCode,
                        price.price.toInt())) // For MVP : Float to Int
            }
        }

        val groupByDatas = priceValueList.groupBy { it.containerTypeCode }.toMutableMap()
        // key : containerTypeCode
        for ((key, priceValues) in groupByDatas) {
            val llContainer = makeCardPerContainer()
            llContainer.addView(makeTitlePerCardInContainer())

            val priceValuePerWeeks = mutableListOf<PriceValuePerWeek>()
            val groupByPriceValue = priceValues
                    .sortedBy { it.containerSizeCode }
                    .groupBy { it.week }
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
                val llRow = LinearLayout(this)
                val height = 40.toDp().toInt()
                llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
                llRow.orientation = LinearLayout.HORIZONTAL
                llRow.gravity = Gravity.CENTER_VERTICAL
                llRow.addView(makeTextViewInCard(false, false, getWeek(priceValuePerWeek.week), Gravity.START))

                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value20.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value40.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value40HC.toString(), Gravity.END))
                llRow.addView(makeTextViewInCard(false, true, priceValuePerWeek.value45HC.toString(), Gravity.END))
                llContainer.addView(llRow)
            }

            when (key) {
                ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY -> {
                    ll_price_table_fullcontainer.visibility = View.VISIBLE
                    ll_price_table_fullcontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_REEFER -> {
                    ll_price_table_rfcontainer.visibility = View.VISIBLE
                    ll_price_table_rfcontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_EMPTY -> {
                    ll_price_table_emptycontainer.visibility = View.VISIBLE
                    ll_price_table_emptycontainer_list.addView(llContainer)
                }
                ConstantTradeOffer.CONTAINER_TYPE_CODE_SOC -> {
                    ll_price_table_soccontainer.visibility = View.VISIBLE
                    ll_price_table_soccontainer_list.addView(llContainer)
                }
            }
        }

        // 카드를 클릭했을때 처리를 위한 클릭 리스너 설정
        clickContainerCard()
        // 모든 카드를 collapsed 처리
        clickContainerCardProcess(currentOpenContainer, spareVisible = true)
    }

    private fun makeCardPerContainer(): LinearLayout {
        val llList = LinearLayout(this)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL
        return llList
    }

    // Container type 별 Title
    private fun makeTitlePerCardInContainer(): LinearLayout {
        val llTitle = LinearLayout(this)

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
        val textView = TextView(this)
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
        ll_price_table_fullcontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.FullContainer)
        }
        ll_price_table_rfcontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.RFContainer)
        }
        ll_price_table_emptycontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.EmptyContainer)
        }
        ll_price_table_soccontainer.setOnClickListener {
            clickContainerCardProcess(ContainerCard.SOCContainer)
        }
    }

    private fun clickContainerCardProcess(containerCard: ContainerCard, spareVisible: Boolean = false) {
        currentOpenContainer = containerCard

        // ContainerCard.AllContainerClose 인 경우 첫번째 Containercard 로 설정하고 Open될 수 있도록 한다
        if (containerCard == ContainerCard.AllContainerClose) {
            when {
                ll_price_table_fullcontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.FullContainer
                ll_price_table_rfcontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.RFContainer
                ll_price_table_emptycontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.EmptyContainer
                ll_price_table_soccontainer.visibility == View.VISIBLE -> currentOpenContainer = ContainerCard.SOCContainer
            }
        }

        when (currentOpenContainer) {
            ContainerCard.AllContainerClose -> {
                ll_price_table_fullcontainer_folding.visibility = View.GONE
                iv_price_table_fullcontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                ll_price_table_emptycontainer_folding.visibility = View.GONE
                iv_price_table_emptycontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                ll_price_table_soccontainer_folding.visibility = View.GONE
                iv_price_table_soccontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
                ll_price_table_rfcontainer_folding.visibility = View.GONE
                iv_price_table_rfcontainer_folding.setBackgroundResource(R.drawable.selected_white_down)
            }
            ContainerCard.FullContainer -> {
                val visible = ll_price_table_fullcontainer_folding.visibility == View.VISIBLE
                ll_price_table_fullcontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                iv_price_table_fullcontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.EmptyContainer -> {
                val visible = ll_price_table_emptycontainer_folding.visibility == View.VISIBLE
                ll_price_table_emptycontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                iv_price_table_emptycontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.SOCContainer -> {
                val visible = ll_price_table_soccontainer_folding.visibility == View.VISIBLE
                ll_price_table_soccontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                iv_price_table_soccontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
            ContainerCard.RFContainer -> {
                val visible = ll_price_table_rfcontainer_folding.visibility == View.VISIBLE
                ll_price_table_rfcontainer_folding.visibility = if (visible) View.GONE else View.VISIBLE
                iv_price_table_rfcontainer_folding.setBackgroundResource(if (visible) R.drawable.selected_white_down else R.drawable.default_white_up)
            }
        }

        view_pricetable_bottom_spare.visibility = if (spareVisible) View.VISIBLE else View.GONE
    }

    data class PriceValue(
            var week: String = "",
            var containerTypeCode: String = "",
            var containerSizeCode: String = "",
            var price: Int = 0  // For MVP : Float to Int
    ) {
    }

    data class PriceValuePerWeek(
            var containerTypeCode: String = "",
            var week: String = "",
            var value20: Int = 0,
            var value40: Int = 0,
            var value40HC: Int = 0,
            var value45HC: Int = 0
    )
}