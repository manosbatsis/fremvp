package com.cyberlogitec.freight9.ui.buyorder

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.ConstantTradeOffer.LOCATION_TYPE_CODE_POD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.LOCATION_TYPE_CODE_POL
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.ui.enums.PayPlanEntry
import com.cyberlogitec.freight9.lib.util.getBeginOfWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.inventory.InventoryViewModel
import com.cyberlogitec.freight9.ui.sellorder.SellOrderViewModel
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.item_order_pay_plan.view.*
import kotlinx.android.synthetic.main.popup_order_pay_plan.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


/**
 * TODO :
 * POL or POD
 * POL 변경 가능 : collect plan option 이 prepaid 일때는 POL, collect 일때는 POD
 * ll_order_pay_plan_change_pol : onClick > POL 선택
 * tv_order_pay_plan_pol_desc : POL name (CNSHA)
 * tv_order_pay_plan_pol_desc : POL desc (Shanghai, China)
 */
class PayPlanPopup(var view: View, var viewModel: Any?, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    lateinit var payPlan: PayPlan
    val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    //var selectedPolOrPodIndex = 0
    var datas: Any? = null
    var payPlanEntry = PayPlanEntry.PP_BuyOrder

    private val adapterPayCollectPlan by lazy { RecyclerPayCollectPlanAdapter().apply { } }

    init {
        currencyFormat.minimumFractionDigits = 0
        when(viewModel) {
            is SellOrderViewModel -> {
                (viewModel as SellOrderViewModel).outPuts.onSuccessRequestPortNm()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_pay_plan_pol_name.text = it
                            }
                        }
            }
            is BuyOrderViewModel -> {
                (viewModel as BuyOrderViewModel).outPuts.onSuccessRequestPortNm()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_pay_plan_pol_name.text = it
                            }
                        }
            }
            is InventoryViewModel -> {
                (viewModel as InventoryViewModel).outPuts.onSuccessRequestPortNm()
                        .bindToLifecycle(view)
                        .subscribe {
                            it?.let {
                                view.tv_order_pay_plan_pol_name.text = it
                            }
                        }
            }
        }

        view.iv_order_pay_plan_close.setSafeOnClickListener {
            dismiss()
        }
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0 )

    }

    fun initValue(title: String, datas: Any? = null, payPlanEntry: PayPlanEntry = PayPlanEntry.PP_BuyOrder) {
        makeDummyData()
        this.payPlanEntry = payPlanEntry
        setData(title, datas)
    }

    private fun setData(title: String, datas: Any? = null) {

        view.tv_pay_plan_title.text = title
        view.recycler_popup_order_pay_plan.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = adapterPayCollectPlan
        }

        view.ll_order_pay_plan_change_pol.setOnClickListener {
            // MVP 에서는 action 없다
            //view.context.showToast("POL 변경 가능\ncollect plan option 이 prepaid 일때는 POL, collect 일때는 POD")
            //showPolOrPodListDialog()
        }

        if (datas == null) { datasIsNull() }
        else {
            // Buy Order type
            when(datas) {
                is TradeOfferWrapper -> {
                    this.datas = datas
                    datasIsNotNull(datas)
                }
                else -> {
                    datasIsNull()
                }
            }
        }
    }

    private fun datasIsNull() {

        with(view) {
            // MVP 에서는 action 없다
            iv_order_pay_plan_pol_arrow.visibility = View.GONE

            // For MVP
            tv_order_pay_plan_kind.text = context.getString(R.string.buy_order_payplan_kind_prepaid_no_num)
            //tv_order_pay_plan_kind.text = context.getString(R.string.buy_order_payplan_kind_prepaid, "1")

            tv_payplan_rate_initial.text = context.getString(R.string.payplan_rate_display, payPlan.percentageInitial)
            tv_payplan_rate_midterm.text = context.getString(R.string.payplan_rate_display, payPlan.percentage3rd)
            tv_payplan_rate_remainder.text = context.getString(R.string.payplan_rate_display, payPlan.percentage3rd)
            tv_payplan_rate_initial_value.text = currencyFormat.format(450)
            tv_payplan_rate_midterm_value.text = currencyFormat.format(900)
            tv_payplan_rate_remainder_value.text = currencyFormat.format(150)

            tv_order_pay_plan_pol.text =
                    if (payPlan.polOrPods[0].isPol) view.context.getString(R.string.buy_order_payplan_pol)
                    else view.context.getString(R.string.buy_order_payplan_pod)
            tv_order_pay_plan_pol_code.text = payPlan.polOrPods[0].polOrPodCode
            tv_order_pay_plan_pol_name.text = payPlan.polOrPods[0].polOrPodName

            val titleFirst = when(payPlanEntry) {
                PayPlanEntry.PP_BuyOrder, PayPlanEntry.PP_BuyOrderDetailCondition -> {
                    context.getString(R.string.buy_order_payplan_initial_payment_table_title_paydate)
                }
                else -> {
                    context.getString(R.string.buy_order_payplan_initial_payment_table_title_colllectdate)
                }
            }
            tv_order_pay_plan_table_title_paydate.text = titleFirst
        }
        setRecyclerData()
    }

    private fun datasIsNotNull(datas: Any? = null) {
        if (datas != null) {
            when (datas) {
                is TradeOfferWrapper -> {
                    val isPPD = datas.orderTradeOfferDetail.offerPaymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
                    with(view) {

                        // MVP 에서는 action 없다
                        iv_order_pay_plan_pol_arrow.visibility = View.GONE

                        //var depSeq = datas.deposit.depSeq
                        // Prepaid or Collect
                        tv_order_pay_plan_kind.text =
                                // For MVP
                                if (isPPD) context.getString(R.string.buy_order_payplan_kind_prepaid_no_num)
                                else context.getString(R.string.buy_order_payplan_kind_collect_no_num)
                                //if (isPPD) context.getString(R.string.buy_order_payplan_kind_prepaid, depSeq)
                                //else context.getString(R.string.buy_order_payplan_kind_collect, depSeq)

                        val initPymtRto = (datas.orderTradeOfferDetail.offerLineItems[0].firstPaymentRatio * 100).toInt().toString()
                        val midTrmPymtRto = (datas.orderTradeOfferDetail.offerLineItems[0].middlePaymentRatio * 100).toInt().toString()
                        val balRto = (datas.orderTradeOfferDetail.offerLineItems[0].balancedPaymentRatio * 100).toInt().toString()
                        tv_payplan_rate_initial.text = context.getString(R.string.payplan_rate_display, initPymtRto)
                        tv_payplan_rate_midterm.text = context.getString(R.string.payplan_rate_display, midTrmPymtRto)
                        tv_payplan_rate_remainder.text = context.getString(R.string.payplan_rate_display, balRto)
                        tv_payplan_rate_initial_value.text = currencyFormat.format(getCalcAmount(datas, initPymtRto.toInt()))
                        tv_payplan_rate_midterm_value.text = currencyFormat.format(getCalcAmount(datas, midTrmPymtRto.toInt()))
                        tv_payplan_rate_remainder_value.text = currencyFormat.format(getCalcAmount(datas, balRto.toInt()))

                        // POL or POD
                        tv_order_pay_plan_pol.text = if (isPPD) context.getString(R.string.buy_order_payplan_pol) else context.getString(R.string.buy_order_payplan_pod)

                        // Pay Date or Collect Date
                        val titleFirst = when(payPlanEntry) {
                            PayPlanEntry.PP_BuyOrder, PayPlanEntry.PP_BuyOrderDetailCondition -> {
                                context.getString(R.string.buy_order_payplan_initial_payment_table_title_paydate)
                            }
                            else -> {
                                context.getString(R.string.buy_order_payplan_initial_payment_table_title_colllectdate)
                            }
                        }
                        tv_order_pay_plan_table_title_paydate.text = titleFirst

                        val defaultOfferRoute = datas.orderTradeOfferDetail.offerRoutes
                                .filter { it.offerRegSeq == 1 }
                                .sortedBy { it.locationTypeCode }
                        val locationCode = if (isPPD) {
                            defaultOfferRoute.filter { it.locationTypeCode == LOCATION_TYPE_CODE_POL}[0].locationCode
                        }
                        else {
                            defaultOfferRoute.filter { it.locationTypeCode == LOCATION_TYPE_CODE_POD}[0].locationCode
                        }
                        tv_order_pay_plan_pol_code.text = locationCode
                        when(viewModel) {
                            is SellOrderViewModel -> { (viewModel as SellOrderViewModel).inPuts.requestPortNm(locationCode)}
                            is BuyOrderViewModel -> { (viewModel as BuyOrderViewModel).inPuts.requestPortNm(locationCode)}
                            is InventoryViewModel -> { (viewModel as InventoryViewModel).inPuts.requestPortNm(locationCode)}
                        }
                    }

                    // Set Recycler data
                    //var collectListDatas = getAssignedPayCollectData(datas)
                    adapterPayCollectPlan.datas.clear()
                    // TODO : For MVP Test
                    adapterPayCollectPlan.datas.addAll(customizingAdapterPayCollectDatas(payPlan.datas))
                    //adapterPayCollectPlan.datas.addAll(getAdapterPayCollectDatas(collectListDatas))
                    adapterPayCollectPlan.notifyDataSetChanged()
                }
                else -> datasIsNull()
            }
        }
        else datasIsNull()
    }

//    @SuppressLint("InflateParams")
//    private fun showPolOrPodListDialog() {
//        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val inflatedView = inflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
//        val dialog = BottomSheetDialog(view.context)
//
//        dialog.setCancelable(false)
//        dialog.setContentView(inflatedView)
//
//        inflatedView.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
//        inflatedView.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
//            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
//                inflatedView.picker.setSelected(index)
//                selectedPolOrPodIndex = index
//            }
//        })
//
//        dialog.btn_done.setOnClickListener {
//            dialog.hide()
//            /**
//             * TODO : Spin control 에 POL, POD 목록 표시
//             *  POL, POD 목록과 각각에 대한 price 를 알고 있어야...
//             *  tv_order_pay_plan_pol_code, tv_order_pay_plan_pol_name
//             *  Initial Payment / Value, RecyclerView, SubTotal, Initial Payment, Deal Amount
//             */
//            datasIsNotNull(this.datas)
//        }
//
//        inflatedView.picker.setItems(containerList)
//        inflatedView.picker.index = selectedPolOrPodIndex
//
//        dialog.show()
//    }

    private fun getCalcAmount(datas: Any?, rate: Int): Int {
        var dealAmount = 0
        when(datas) {
            is TradeOfferWrapper -> {
                val offerLineItems = datas.orderTradeOfferDetail.offerLineItems
                // deep copy
                val clonedDatas = datas.datas.map { it.copy() }
                if (payPlanEntry == PayPlanEntry.PP_BuyOrderDetailCondition) {
                    /**
                     * data.initPrice(20Ft 또는 40Ft)
                     * condition detail 에서 진입한 경우 offer 수량에 대한 pay plan 을 보여준다.
                     * buy order 에서 진입한 경우 buy volume 수량에 대한 pay plan 을 보여준다.
                     * TODO : 일단 20Ft 기준으로 계산(price, volume)하고 추수, 20Ft, 40Ft 구분 가능한 컬럼이 생기면
                     *  그 컬럼값을 참조하도록 한다.
                     */
                    for (offerLineItem in offerLineItems) {
                        for (clonedData in clonedDatas) {
                            if (offerLineItem.baseYearWeek == clonedData.bseYw) {
                                clonedData.price = offerLineItem.offerPrice.toInt() // For MVP : Float to Int
                                clonedData.volume = offerLineItem.offerQty
                            }
                        }
                    }
                }
                for(clonedData in clonedDatas) {
                    if (clonedData.volume <= 0) continue
                    dealAmount += (clonedData.price * clonedData.volume)
                }
            }
        }

        return (rate.toFloat() / 100 * dealAmount).toInt()
    }

//    private fun getAdapterPayCollectDatas(collectListDatas: List<CollectListData>) : List<AdapterPayCollectData> {
//        val adapterPayCollectDataList = mutableListOf<AdapterPayCollectData>()
//        // collectListDatas 의 모든 data 를 adapterCollectDataList 에 add
//        // 각 row당 2nd 또는 3rd 의 1개 값만 가지고 있음
//        for (collectListDate in collectListDatas) {
//            val adapterCollectData = AdapterPayCollectData()
//            with(collectListDate) {
//                adapterCollectData.collectDate = collectDate
//                adapterCollectData.exist2nd = is2nd
//                adapterCollectData.exist3rd = !is2nd
//                if (is2nd) {
//                    adapterCollectData.yearWeek2nd = yearWeek
//                    adapterCollectData.value2nd = value
//                } else {
//                    adapterCollectData.yearWeek3rd = yearWeek
//                    adapterCollectData.value3rd = value
//                }
//            }
//            adapterPayCollectDataList.add(adapterCollectData)
//        }
//        return customizingAdapterPayCollectDatas(adapterPayCollectDataList)
//    }

    private fun customizingAdapterPayCollectDatas(adapterDatas: List<AdapterPayCollectData>)
            : List<AdapterPayCollectData> {
        val innerAdapterDatas = adapterDatas.toList()

        for (adapterData in adapterDatas) {
            // row에 빈곳이 있으면 찾아서 넣고 현재 위치는 초기화한다.
            // 빈곳이 없으면 현재 위치 유지한다.
            with(adapterData) {
                val collectDate = collectDate
                val exist2nd = exist2nd
                val exist3rd = exist3rd

                for (innerAdapterData in innerAdapterDatas) {
                    val innerCollectDate = innerAdapterData.collectDate
                    // 같은 collect date 자리를 찾은 경우
                    if (collectDate == innerCollectDate) {
                        // 2nd 의 자리를 찾은 경우
                        if (exist2nd && !innerAdapterData.check2nd) {
                            innerAdapterData.exist2nd = exist2nd
                            innerAdapterData.yearWeek2nd = adapterData.yearWeek2nd
                            innerAdapterData.value2nd = adapterData.value2nd
                            innerAdapterData.check2nd = true
                            break
                        }
                        // 3rd 의 자리를 찾은 경우
                        else if (exist3rd && !innerAdapterData.check3rd) {
                            innerAdapterData.exist3rd = exist3rd
                            innerAdapterData.yearWeek3rd = adapterData.yearWeek3rd
                            innerAdapterData.value3rd = adapterData.value3rd
                            innerAdapterData.check3rd = true
                            break
                        }
                    }
                }
            }
        }
        return innerAdapterDatas.filter { it.check2nd || it.check3rd }.toList()
    }

    private val midDate = listOf("2019-12-20 20:59:01.0", "2019-12-27 20:59:01.0", "2020-01-03 20:59:01.0")
//    private val balDate = listOf("2019-12-27 20:59:01.0", "2020-01-03 20:59:01.0", "2020-01-10 20:59:01.0")

//    private fun getAssignedPayCollectData(datas: Any? = null) : List<CollectListData> {
//
//        var midRatio: String
//        var balRatio: String
//        val collectList = mutableListOf<CollectListData>()
//
//        when(datas) {
//            is TradeOfferWrapper -> {
//                val offerLineItems = datas.orderTradeOfferDetail.offerLineItems
//                // deep copy
//                val clonedDatas = datas.datas.map { it.copy() }
//                if (payPlanEntry == PayPlanEntry.PP_BuyOrderDetailCondition) {
//                    /**
//                     * data.initPrice(20Ft 또는 40Ft)
//                     * condition detail 에서 진입한 경우 offer 수량에 대한 pay plan 을 보여준다.
//                     * buy order 에서 진입한 경우 buy volume 수량에 대한 pay plan 을 보여준다.
//                     * TODO : 일단 20Ft 기준으로 계산(price, volume)하고 추수, 20Ft, 40Ft 구분 가능한 컬럼이 생기면
//                     *  그 컬럼값을 참조하도록 한다.
//                     */
//                    for (offerLineItem in offerLineItems) {
//                        for (clonedData in clonedDatas) {
//                            if (offerLineItem.baseYearWeek == clonedData.bseYw) {
//                                clonedData.price = offerLineItem.offerPrice.toInt() // For MVP : Float to Int
//                                clonedData.volume = offerLineItem.offerQty
//                            }
//                        }
//                    }
//                }
//
////                for (clonedData in clonedDatas) {
////                    if (clonedData.volume <= 0) continue
////                    var currDetails = details.filter { it.bseYw == clonedData.bseYw }[0]
////                    var bseYw = currDetails.bseYw
////                    var midTrmPymtDt = currDetails.midTrmPymtDt
////                    var balDt = currDetails.balDt
////
////                    var collectListData = CollectListData()
////                    collectListData.yearWeek = view.context.getWeek(bseYw)
////                    collectListData.is2nd = true
////                    midRatio = datas.deposit.midTrmPymtRto!!
////                    collectListData.ratio = midRatio
////                    collectListData.value = (midRatio.toFloat() / 100 * (clonedData.initPrice * clonedData.volume)).toInt()
////                    collectListData.collectDate = midTrmPymtDt!!.getBeginOfWeek()
////                    collectList.add(collectListData)
////
////                    collectListData = CollectListData()
////                    collectListData.yearWeek = view.context.getWeek(bseYw)
////                    collectListData.is2nd = false
////                    balRatio = datas.deposit.balRto!!
////                    collectListData.ratio = balRatio
////                    collectListData.value = (balRatio.toFloat() / 100 * (clonedData.initPrice * clonedData.volume)).toInt()
////                    collectListData.collectDate = balDt!!.getBeginOfWeek()
////                    collectList.add(collectListData)
////                }
//            }
//            else -> { }
//        }
//        return collectList
//    }

    private fun setRecyclerData() {
        adapterPayCollectPlan.datas.addAll(payPlan.datas)
        adapterPayCollectPlan.notifyDataSetChanged()
    }

    private class RecyclerPayCollectPlanAdapter : RecyclerView.Adapter<RecyclerPayCollectPlanAdapter.ViewHolder>() {

        lateinit var context: Context
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val datas = mutableListOf<Any?>()
        var onClickItem: (Long) -> Unit = {
            Timber.d("f9: onClickItem = $it")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            return  ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_order_pay_plan, parent, false))
        }

        override fun getItemCount(): Int {
            val size = datas.size
            Timber.d("f9: getItemCount = $size")
            return size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with (holder) {
                currencyFormat.minimumFractionDigits = 0
                setItemData(itemView, position)
            }
        }

        private fun setItemData(itemView: View, position: Int) {

            when(datas[position]) {
                is AdapterPayCollectData -> {
                    with(datas[position] as AdapterPayCollectData) {
                        // Initial payment Deal Date or '20-02-04
                        itemView.tv_order_pay_plan_table_item_date.text =
                                if (position == 0) context.getString(R.string.payplan_initial_payment_dealdate)
                                else collectDate
                        itemView.tv_order_pay_plan_table_item_midterm.text =
                                if (value2nd == 0) "-"
                                else context.getString(R.string.buy_order_payplan_initial_payment_table_value_2nd,
                                        yearWeek2nd, currencyFormat.format(value2nd))
                        itemView.tv_order_pay_plan_table_item_remainder.text =
                                if (value3rd == 0) "-"
                                else context.getString(R.string.buy_order_payplan_initial_payment_table_value_3rd,
                                        yearWeek3rd, currencyFormat.format(value3rd))
                        itemView.tv_order_pay_plan_table_item_sum.text = currencyFormat.format(value2nd + value3rd)
                    }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

    private fun makeDummyData() {
        payPlan = PayPlan(
            planName =  "plan 1 (30% 60% 10%)",
            polOrPods = makePolOrPods(),
            percentageInitial = "30",
            percentage2nd = "60",
            percentage3rd = "10",
            initialValue = 4500,
            datas = makeCollectDatas(),
            subTotal = 9500,
            initialPayment = 4500,
            dealAmount = 1500
        )
    }

    private fun makePolOrPods(): MutableList<PolOrPod> {
        val polOrPods = mutableListOf<PolOrPod>()
        for (polorPod in 1..5) {
            polOrPods.add(PolOrPod(true, "CNSHA" + polorPod, "Shanghai, China" + polorPod))
        }
        return polOrPods
    }

    private fun makeCollectDatas(): MutableList<AdapterPayCollectData> {
        val adapterPayCollectDatas = mutableListOf<AdapterPayCollectData>()
        adapterPayCollectDatas.add(AdapterPayCollectData(midDate[0].getBeginOfWeek(),
                true, true, "W03", 1000,
                true, true, "W03", 2000))
        adapterPayCollectDatas.add(AdapterPayCollectData(midDate[1].getBeginOfWeek(),
                true, true, "W04", 1000,
                true, true, "W04", 2000))
        adapterPayCollectDatas.add(AdapterPayCollectData(midDate[2].getBeginOfWeek(),
                true, true, "W05", 1000,
                true, true, "W05", 2000))
        return adapterPayCollectDatas
    }

    data class PayPlan(
            var planName: String = "",              // plan 1 (30% 60% 10%)
            var polOrPods: MutableList<PolOrPod>,   // CNSHA / Shanghai, China
            var percentageInitial: String = "",   // 30%
            var percentage2nd: String = "",        // 60%
            var percentage3rd: String = "",        // 10%
            var initialValue: Int = 0,              // $4,500
            var datas: MutableList<AdapterPayCollectData>,
            var subTotal: Int = 0,
            var initialPayment: Int = 0,
            var dealAmount: Int = 0
    )

    data class PolOrPod(
            var isPol: Boolean = true,
            var polOrPodCode: String = "",
            var polOrPodName: String = ""
    )

    data class AdapterPayCollectData(
            var collectDate: String = "",       // 19-12-13
            var check2nd: Boolean = false,
            var exist2nd: Boolean = false,      // true or false
            var yearWeek2nd: String = "",       // 01, 02, 03...
            var value2nd: Int = 0,               // 10
            var check3rd: Boolean = false,
            var exist3rd: Boolean = false,
            var yearWeek3rd: String = "",
            var value3rd: Int = 0
    )

//    data class CollectListData(
//            var collectDate: String = "",
//            var yearWeek: String = "",
//            var is2nd: Boolean = true,
//            var ratio: String = "",
//            var value: Int = 0
//    )

//    private val containerList = listOf(
//            TextItem("KRPUS BUSAN, Republic Of Korea", true)
//            , TextItem("USOAK OAKLAND, CA")
//            , TextItem("CNSHA SHANGHAI, PR China")
//            , TextItem("CNNGB NINGBO")
//            , TextItem("USLO1 USLAX BLOCK STOWAGE")
//            , TextItem("USLO2 USLAX BLOCK STOWAGE")
//            , TextItem("USLO3 USLAX BLOCK STOWAGE")
//            , TextItem("USLO4 USLAX BLOCK STOWAGE")
//            , TextItem("USLO5 USLAX BLOCK STOWAGE")
//            , TextItem("USLO6 USLAX BLOCK STOWAGE")
//    )
}

