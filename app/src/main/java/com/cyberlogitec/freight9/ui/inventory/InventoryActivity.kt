package com.cyberlogitec.freight9.ui.inventory

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.inventory.progressview.ProgressView
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_inventory.*
import kotlinx.android.synthetic.main.appbar_inventory.*
import kotlinx.android.synthetic.main.body_inventory_init.*
import kotlinx.android.synthetic.main.item_inventory_change.view.*
import kotlinx.android.synthetic.main.toolbar_inventory.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs


/**
 * TODO : Booking here! 툴팁이 탭 하면 이동하게 되어 있는데, 단순히 툴팁만 표시하는 것으로 변경되었습니다.
 *  이런 유형의 툴팁은 다른 화면에도 추가될 수 있는데,
 *  Preference 메뉴에서 툴팁을 일괄적으로 켜거나 끄거나 할 수 있도록 설정할 수 있게 하고자 하오니 개발시 참고 부탁드립니다.
 */

@RequiresActivityViewModel(value = InventoryViewModel::class)
class InventoryActivity : BaseActivity<InventoryViewModel>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var selectedLastMinuteInventoryGraphIndex = LMIGraphUIIndex.LMI_FIRST
    private var selectedPeriodType = PeriodType.PERIOD_TYPE_DEFALUT
    private val inventoryChangeAdapter by lazy {
        InventoryChangeRecyclerAdapter()
                .apply {
                    onClickItem = { clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, it)) }
                    onAnimationEnd = {
                        Handler().postDelayed({
                            v_inventory_change_index_today_index.visibility = View.VISIBLE
                            tv_inventory_change_index_today.visibility = View.VISIBLE
                        }, 1000)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_inventory)
        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        // set custom toolbar
        defaultbarInit(toolbar_inventory, isEnableNavi = false, menuType = MenuType.DEFAULT, title = getString(R.string.your_inventory_title))

        registerViewModelOutputs()
        makeDummyDatas()
        initView()
    }

    private fun registerViewModelOutputs() {

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when (parameterClick) {
                            ParameterClick.CLICK_TITLE_LEFT -> {
                                Timber.d("f9: toolbar_left_btn clcick")
                                onBackPressed()
                            }
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                Timber.d("f9: startActivity(MenuActivity)")
                                startMenuActivityWithFinish(MenuItem.MENUITEM_YOUR_INVENTORY, MenuActivity::class.java)
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
                                // go Route Filter
                                // VIEW ALL YOUR INVENTORY button onClickListener
                                ParameterAny.ANY_ITEM_OBJECT -> {
                                    // TODO : 선택한 주차 / ALL 유무 정보도 넘겨줘야 한다.
                                    val data = second as String
                                    showToast("go Route Filter!\n$data")
                                    startActivity(RouteFilterActivity::class.java)
                                }
                                ParameterAny.ANY_JUMP_TO_OTHERS -> {
                                    when(second) {
                                        is LMIGraphUIIndex -> {
                                            setLastMinuteInventory(second as LMIGraphUIIndex)
                                        }
                                        is PeriodType -> {
                                            setInventoryChagneCheckBoxUI(second as PeriodType)
                                        }
                                        // go Your Trade Dashboard
                                        is InventoryChange -> {
                                            val inventoryChange = second as InventoryChange
                                            showToast("go YourTradeDashboard!\n"
                                                    + inventoryChange.polCd + ", "
                                                    + inventoryChange.podCd)
                                        }
                                    }
                                }
                                else -> {  }
                            }
                        }
                    }
                }
    }

    private fun initView() {
        // data setting
        initData()
        setData()
    }

    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        // Inventory change recycler view
        setInventoryChangeRecyclerData(selectedPeriodType)
    }


    private fun setData() {
        setMarketCostValue()

        // Last Minute Inventory init
        setLastMinuteInventory()

        // recyclerview init
        inventoryChagneRecyclerViewInit()

        setListener()
    }

    private fun setMarketCostValue() {
        tv_inventory_market_value.text = currencyFormat.format(inventoryInfo.marketValue.toInt())
        tv_inventory_total_cost_value.text = currencyFormat.format(inventoryInfo.totalCostValue.toInt())
        val diffValue = (inventoryInfo.estimatedProfit - inventoryInfo.estimatedProfitBase).toInt()
        val diffSymbol = if (diffValue < 0) "- " else "+ "
        val diffAbsValue = abs(diffValue)
        tv_inventory_estimated_profit_value.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"       // + $XXX,XXX
    }

    private fun setLastMinuteInventory(lMIGGraphUIIndex: LMIGraphUIIndex = LMIGraphUIIndex.LMI_FIRST) {
        // graph / text / underbar 초기화
        selectedLastMinuteInventoryGraphIndex = lMIGGraphUIIndex
        with(inventoryInfo.lastMinuteInventories) {
            for (weekValue in this) {
                with(weekValue) {
                    selected = (this.index == lMIGGraphUIIndex.index && (inStockAmt > 0F || onMarketAmt > 0F))
                    val graphUI = LMIGraphUI.getLMIGraphUI(selected)
                    graphInStock!!.highlightView.color = getColor(graphUI!!.inStockColor)
                    graphInStock!!.progress = (inStockAmt / (soldAmt+bookedAmt+inStockAmt) * 100.0F)
                    graphOnMarket!!.highlightView.color = getColor(graphUI.onMarketColor)
                    graphOnMarket!!.progress = (onMarketAmt / (soldAmt+bookedAmt+inStockAmt) * 100.0F)
                    weekOfLabel!!.setTextColor(getColor(graphUI.weekNoTextColor))
                    weekOfLabel!!.text = getWeek(bseYw)
                    underBarView!!.setBackgroundColor(if (selected) getColor(graphUI.underBarColor) else graphUI.underBarColor)
                }
            }
            with(this[lMIGGraphUIIndex.index]) {
                graphInStock!!.progressAnimate()
                graphOnMarket!!.progressAnimate()
                tv_inventory_vgraph_week_of_value.text = bseDate.getYYYYMMDD(true)
                tv_inventory_vgraph_total_owned_value.text = getString(R.string.your_inventory_total_owned_value,
                        totalOwned.toInt().toString(), totalCases.toInt().toString())
            }
        }
    }

    private fun setInventoryChagneCheckBoxUI(selectedPeriodType: PeriodType) {
        when(selectedPeriodType) {
            PeriodType.PERIOD_TYPE_7DAYS -> {
                cb_inventory_change_15.isChecked = false
                cb_inventory_change_30.isChecked = false
                setInventoryChangeRecyclerData(if (cb_inventory_change_7.isChecked)
                    PeriodType.PERIOD_TYPE_7DAYS else PeriodType.PERIOD_TYPE_DEFALUT)
            }
            PeriodType.PERIOD_TYPE_15DAYS -> {
                cb_inventory_change_7.isChecked = false
                cb_inventory_change_30.isChecked = false
                setInventoryChangeRecyclerData(if (cb_inventory_change_15.isChecked)
                    PeriodType.PERIOD_TYPE_15DAYS else PeriodType.PERIOD_TYPE_DEFALUT)
            }
            PeriodType.PERIOD_TYPE_30DAYS -> {
                cb_inventory_change_7.isChecked = false
                cb_inventory_change_15.isChecked = false
                setInventoryChangeRecyclerData(if (cb_inventory_change_30.isChecked)
                    PeriodType.PERIOD_TYPE_30DAYS else PeriodType.PERIOD_TYPE_DEFALUT)
            }
            else -> { }
        }
    }

    private fun setListener() {
        // wait click event (toolbar left button)
        toolbar_inventory.toolbar_left_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_inventory.toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        /*******************************************************************************************
         * Last Minute Inventory : Vertical graph 의 onClickListener
         */
        ll_inventory_last_minute_inventory_vgraph_1.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, LMIGraphUIIndex.LMI_FIRST))
        }
        ll_inventory_last_minute_inventory_vgraph_2.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, LMIGraphUIIndex.LMI_SECOND))
        }
        ll_inventory_last_minute_inventory_vgraph_3.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, LMIGraphUIIndex.LMI_THIRD))
        }
        ll_inventory_last_minute_inventory_vgraph_4.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, LMIGraphUIIndex.LMI_FOURTH))
        }
        ll_inventory_last_minute_inventory_vgraph_5.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, LMIGraphUIIndex.LMI_FIFTH))
        }

        cv_inventory_vgraph_select.setSafeOnClickListener {
            val data = "Week selected"
            clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_OBJECT, data))
        }

        /*******************************************************************************************
         * Inventory Change: CheckBox의 onClickListener
         */
        cb_inventory_change_7.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, PeriodType.PERIOD_TYPE_7DAYS))
        }
        cb_inventory_change_15.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, PeriodType.PERIOD_TYPE_15DAYS))
        }
        cb_inventory_change_30.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_JUMP_TO_OTHERS, PeriodType.PERIOD_TYPE_30DAYS))
        }

        /*******************************************************************************************
         * VIEW ALL YOUR INVENTORY button onClickListener
         */
        btn_view_all_your_inventory.setSafeOnClickListener {
            val data = "Button selected"
            clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_OBJECT, data))
        }
    }

    private fun inventoryChagneRecyclerViewInit() {
        recycler_inventory_change.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(this@InventoryActivity)
            adapter = this@InventoryActivity.inventoryChangeAdapter
        }
    }

    private fun setInventoryChangeRecyclerData(selectedPeriodType: PeriodType) {
        when(selectedPeriodType) {
            PeriodType.PERIOD_TYPE_DEFALUT -> {
                v_inventory_change_index_other_days_index.visibility = View.INVISIBLE
                tv_inventory_change_index_other_days.visibility = View.INVISIBLE
            }
            else -> {
                v_inventory_change_index_today_index.visibility = View.INVISIBLE
                tv_inventory_change_index_today.visibility = View.INVISIBLE
                v_inventory_change_index_other_days_index.visibility = View.VISIBLE
                tv_inventory_change_index_other_days.visibility = View.VISIBLE
                tv_inventory_change_index_other_days.text = getString(R.string.your_inventory_change_index_otherdays,
                        getString(selectedPeriodType.stringId))
            }
        }

        this.selectedPeriodType = selectedPeriodType
        inventoryChangeAdapter.setData(inventoryChangeList.filter { it.periodType == selectedPeriodType })
        inventoryChangeAdapter.notifyDataSetChanged()
    }

    private class InventoryChangeRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val datas = mutableListOf<InventoryChange>()
        var onClickItem: (InventoryChange) -> Unit = {}
        var onAnimationEnd: () -> Unit = {}

        fun setData(datas: List<InventoryChange>) {
            this.datas.clear()
            this.datas.addAll(datas)
        }

//        fun getData(): List<InventoryChange> {
//            return this.datas
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_change, parent, false))

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]
                tv_inventory_change_item_amount.text = "${data.todayAmount.toInt()}${context.getString(R.string.teu_unit_20ft)}"
                tv_inventory_change_item_name.text = "${data.polCd} - ${data.podCd}"
                doProcessPeriodTypeProgress(holder.itemView, position)
                setSafeOnClickListener { onClickItem(data) }
            }
        }

        private fun doProcessPeriodTypeProgress(itemView: View, position: Int) {
            val data = datas[position]
            with(itemView) {
                when (data.periodType) {
                    PeriodType.PERIOD_TYPE_DEFALUT -> {
                        /**
                         * allBeforeMaxAmount : 100%
                         * pv_inventory_change_item_hgraph_2.appbar_progress_sof = todayAmount / allBeforeMaxAmount * 100.F
                         */
                        pv_inventory_change_item_hgraph_1.progress = 0.0F
                        pv_inventory_change_item_hgraph_2.progress = data.todayAmount / data.allBeforeMaxAmount * 100.0F
                        tv_inventory_change_item_diff_amount.visibility = View.INVISIBLE
                    }
                    else -> {
                        /**
                         * allBeforeMaxAmount : 100%
                         * pv_inventory_change_item_hgraph_1.appbar_progress_sof = daysBeforeAmount / allBeforeMaxAmount * 100.0F
                         * pv_inventory_change_item_hgraph_2.appbar_progress_sof = todayAmount / allBeforeMaxAmount * 100.0F
                         *
                         * 1. daysBeforeAmount 값으로 hgraph_1, hgraph_2 를 설정
                         * 2.1 todayAmount < daysBeforeAmount 인 경우
                         *     hgraph_2 를 todayAmount <- daysBeforeAmount 방향으로 이동시키고 (역방향 이동)
                         *     todayAmount 위치가 되면 animation stop
                         * 2.2 todayAmount > daysBeforeAmount 인 경우
                         *     hgraph_2 를 daysBeforeAmount -> todayAmount 방향으로 이동시키고 (순방향 이동)
                         *     todayAmount 위치가 되면 animation stop
                         * 2.3 daysBeforeAmount = todayAmount 인 경우
                         *     1. 상태 유지 (animation 없음)
                         */
                        val diffValue = data.todayAmount - data.daysBeforeAmount
                        if (data.todayAmount == data.daysBeforeAmount) {
                            pv_inventory_change_item_hgraph_1.progress = data.daysBeforeAmount / data.allBeforeMaxAmount * 100.0F
                            pv_inventory_change_item_hgraph_2.progress = data.todayAmount / data.allBeforeMaxAmount * 100.0F
                        } else {
                            if (data.todayAmount < data.daysBeforeAmount) {
                                val destValue = data.todayAmount / data.allBeforeMaxAmount * 100.0F
                                pv_inventory_change_item_hgraph_1.progress = data.daysBeforeAmount / data.allBeforeMaxAmount * 100.0F
                                pv_inventory_change_item_hgraph_2.progress = data.daysBeforeAmount / data.allBeforeMaxAmount * 100.0F
                                Handler().postDelayed({
                                    // TODO : daysBeforeAmount -> todayAmount 까지 역방향으로 이동 (시작점이 daysBeforeAmount, 역방향)
                                    pv_inventory_change_item_hgraph_2.progressAnimate(destValue)
                                    onAnimationEnd()
                                }, 1000)
                            } else {
                                pv_inventory_change_item_hgraph_1.progress = data.daysBeforeAmount / data.allBeforeMaxAmount * 100.0F
                                pv_inventory_change_item_hgraph_2.progress = data.daysBeforeAmount / data.allBeforeMaxAmount * 100.0F
                                Handler().postDelayed({
                                    // TODO : daysBeforeAmount -> todayAmount 까지 순방향으로 이동 (시작점이 daysBeforeAmount, 순방향)
                                    pv_inventory_change_item_hgraph_2.progressAnimate(data.todayAmount / data.allBeforeMaxAmount * 100.0F)
                                    onAnimationEnd()
                                }, 1000)
                            }
                        }
                        tv_inventory_change_item_diff_amount.visibility = View.VISIBLE
                        var diffSymbol = ""
                        if (diffValue > 0F) { diffSymbol = "+" }
                        tv_inventory_change_item_diff_amount.text = "$diffSymbol${diffValue.toInt()}T"
                    }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    private fun makeDummyDatas() {
        val lastMinuteInventories = mutableListOf<WeekValue>()
        lastMinuteInventories.add(WeekValue(0, "201905", "2019-03-01 00:00:00.0",
                342f, 15f, 100f, 100f, 600f, 800f,
                "MST-F9_HLC-201908-01", "HLC_MST_20190827_1",
                pv_vertical_inventory_graph_1_instock, pv_vertical_inventory_graph_1_onmarket,
                pv_vertical_inventory_graph_1_label, pv_vertical_inventory_graph_1_bar, false))
        lastMinuteInventories.add(WeekValue(1, "201906", "2019-03-08 00:00:00.0",
                332f, 20f, 100f, 300f, 500f, 600f,
                "MST-F9_HLC-201908-01", "HLC_MST_20190827_1",
                pv_vertical_inventory_graph_2_instock, pv_vertical_inventory_graph_2_onmarket,
                pv_vertical_inventory_graph_2_label, pv_vertical_inventory_graph_2_bar, false))
        lastMinuteInventories.add(WeekValue(2, "201907", "2019-03-15 00:00:00.0",
                300f, 10f, 100f, 500f, 350f, 400f,
                "MST-F9_HLC-201908-01", "HLC_MST_20190827_1",
                pv_vertical_inventory_graph_3_instock, pv_vertical_inventory_graph_3_onmarket,
                pv_vertical_inventory_graph_3_label, pv_vertical_inventory_graph_3_bar, false))
        lastMinuteInventories.add(WeekValue(3, "201908", "2019-03-22 00:00:00.0",
                400f, 30f, 300f, 100f, 300f, 600f,
                "MST-F9_HLC-201908-01", "HLC_MST_20190827_1",
                pv_vertical_inventory_graph_4_instock, pv_vertical_inventory_graph_4_onmarket,
                pv_vertical_inventory_graph_4_label, pv_vertical_inventory_graph_4_bar, false))
        lastMinuteInventories.add(WeekValue(4, "201909", "2019-03-29 00:00:00.0",
                452f, 35f, 500f, 200f, 250f, 400f,
                "MST-F9_HLC-201908-01", "HLC_MST_20190827_1",
                pv_vertical_inventory_graph_5_instock, pv_vertical_inventory_graph_5_onmarket,
                pv_vertical_inventory_graph_5_label, pv_vertical_inventory_graph_5_bar, false))

        inventoryInfo = InventoryInfo("2019-12-20 00:00:00.0",
                22000f,
                20000f,
                4500f,
                2000f,
                lastMinuteInventories
        )

        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_DEFALUT, 1000f, 0f, 300f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_DEFALUT, 1000f, 0f, 900f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_DEFALUT, 1000f, 0f, 500f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_DEFALUT, 1000f, 0f, 400f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_DEFALUT, 1000f, 0f, 800f, "ASIA", "", "USWC", ""))

        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_7DAYS, 1000f, 500f, 500f, "USWC", "", "ASIA", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_7DAYS, 1000f, 700f, 800f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_7DAYS, 1000f, 500f, 400f, "USWC", "", "ASIA", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_7DAYS, 1000f, 450f, 500f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_7DAYS, 1000f, 300f, 500f, "USWC", "", "ASIA", ""))

        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_15DAYS, 1000f, 450f, 500f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_15DAYS, 1000f, 300f, 500f, "USWC", "", "ASIA", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_15DAYS, 1000f, 500f, 500f, "ASIA", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_15DAYS, 1000f, 700f, 800f, "USWC", "", "ASIA", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_15DAYS, 1000f, 500f, 400f, "USWC", "", "USWC", ""))

        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_30DAYS, 1000f, 500f, 400f, "USWC", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_30DAYS, 1000f, 450f, 500f, "USWC", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_30DAYS, 1000f, 300f, 500f, "USWC", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_30DAYS, 1000f, 500f, 500f, "USWC", "", "USWC", ""))
        inventoryChangeList.add(InventoryChange(PeriodType.PERIOD_TYPE_30DAYS, 1000f, 700f, 800f, "USWC", "", "USWC", ""))
    }

    /***********************************************************************************************
     * dummy data class
     */
    private lateinit var inventoryInfo: InventoryInfo
    data class InventoryInfo (
            var loadDateTime: String = "",                  // load 및 refresh date, time
            var marketValue: Float = 0f,                    // F9 Market value
            var totalCostValue: Float = 0f,                 // Total cost value
            var estimatedProfit: Float = 0f,
            var estimatedProfitBase: Float = 0f,
            var lastMinuteInventories: MutableList<WeekValue>
    )

    data class WeekValue (
            var index: Int = 0,
            var bseYw: String = "",                 // 201903 > W03
            var bseDate: String = "",               // 2019-03-01
            var totalOwned: Float = 0f,
            var totalCases: Float = 0f,
            var soldAmt: Float = 0f,
            var bookedAmt: Float = 0f,
            var inStockAmt: Float = 0f,
            var onMarketAmt: Float = 0f,
            var masterContractNumber: String = "",
            var inventoryNumber: String = "",
            var graphInStock: ProgressView? = null,
            var graphOnMarket: ProgressView? = null,
            var weekOfLabel: TextView? = null,
            var underBarView: View? = null,
            var selected: Boolean = false
    )

    enum class LMIGraphUI constructor(
            val index: Int,
            val selected: Boolean,
            val onMarketColor: Int,
            val inStockColor: Int,
            val weekNoTextColor: Int,
            val underBarColor: Int
    ){
        LMI_TRUE(0, true, R.color.purpley_blue, R.color.color_302289, R.color.colorWhite, R.color.purpley_blue),
        LMI_FALSE(1, false, R.color.color_322394, R.color.color_231963, R.color.greyish_brown, Color.TRANSPARENT);
        companion object {
            fun getLMIGraphUI(selected: Boolean) : LMIGraphUI? {
                for (lMIGraphUI in values()) {
                    if (lMIGraphUI.selected == selected) {
                        return lMIGraphUI
                    }
                }
                return LMI_FALSE
            }
        }
    }

    enum class LMIGraphUIIndex constructor(
            val index: Int
    ){
        LMI_FIRST(0),
        LMI_SECOND(1),
        LMI_THIRD(2),
        LMI_FOURTH(3),
        LMI_FIFTH(4);
        fun getLMIGraphUIIndex(index: Int) : LMIGraphUIIndex? {
            for (lMIGraphUIIndex in values()) {
                if (lMIGraphUIIndex.index == index) {
                    return lMIGraphUIIndex
                }
            }
            return LMI_FIRST
        }
    }

    /***********************************************************************************************
     * Default 는 기간 선택되어지지 않고, 현재 주차의 물량만 나옴.
     */
    private var inventoryChangeList = mutableListOf<InventoryChange>()
    data class InventoryChange(
            var periodType: PeriodType? = PeriodType.PERIOD_TYPE_DEFALUT,
            // 7, 15, 30 전체 기간 중 가장 큰값
            var allBeforeMaxAmount: Float = 0f,
            // 7 days 선택 시 7일전 물량
            var daysBeforeAmount: Float = 0f,
            // 현재 Region 의 물량
            var todayAmount: Float = 0f,
            var polCd: String = "",
            var polName: String = "",
            var podCd: String = "",
            var podName: String = ""
    )

    enum class PeriodType constructor(
            val index: Int,
            val period: Int,   // 0, 7, 15, 30
            val stringId: Int
    ){
        PERIOD_TYPE_DEFALUT(0, 0, R.string.your_inventory_change_index_today),
        PERIOD_TYPE_7DAYS(1, 7, R.string.your_inventory_change_7days),
        PERIOD_TYPE_15DAYS(2, 15, R.string.your_inventory_change_15days),
        PERIOD_TYPE_30DAYS(3, 30, R.string.your_inventory_change_30days);
        companion object {
            fun getPeriodType(index: Int) : PeriodType? {
                for (periodType in values()) {
                    if (periodType.index == index) {
                        return periodType
                    }
                }
                return PERIOD_TYPE_DEFALUT
            }
        }
    }
}