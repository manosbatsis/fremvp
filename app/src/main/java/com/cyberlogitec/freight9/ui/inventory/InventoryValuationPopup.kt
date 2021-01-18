package com.cyberlogitec.freight9.ui.inventory

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.getYMDWithHypen
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import kotlinx.android.synthetic.main.item_inventory_valuation.view.*
import kotlinx.android.synthetic.main.popup_inventory_valuation.view.*
import timber.log.Timber
import java.lang.Math.abs
import java.text.NumberFormat
import java.util.*


class InventoryValuationPopup(var view: View, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    private lateinit var inventoryValuationList: MutableList<InventoryDetailActivity.InventoryDetailItem>
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val inventoryValuationAdapter by lazy {
        InventoryValuationRecyclerAdapter()
                .apply { }
    }

    init {
        currencyFormat.minimumFractionDigits = 0

        view.iv_inventory_valuation_close.setSafeOnClickListener {
            dismiss()
        }
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0 )

    }

    fun initValue(inventoryDetailList: MutableList<InventoryDetailActivity.InventoryDetailItem>) {
        this.inventoryValuationList = inventoryDetailList
        inventoryDetailRecyclerViewInit()
        with(view) {
            // inventoryValuationList 의 f9MarketValue 의 합
            val marketValueSum = inventoryValuationList.map { it.f9MarketValue }.sum()
            tv_inventory_valuation_f9_market_value.text = currencyFormat.format(marketValueSum.toInt())
            // inventoryValuationList 의 unitCostValue 의 합
            val costValueSum = inventoryValuationList.map { it.unitCostValue }.sum()
            tv_inventory_valuation_total_cost_value.text = currencyFormat.format(costValueSum.toInt())
            // tv_inventory_valuation_f9_market_value - tv_inventory_valuation_total_cost_value
            val diffValue = (marketValueSum - costValueSum).toInt()
            var diffSymbol = if (diffValue < 0) "- " else "+ "
            val diffAbsValue = kotlin.math.abs(diffValue)
            if (diffValue > 0F) {
                diffSymbol = "+"
            }
            tv_inventory_valuation_estimate_profit_value.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"
        }
    }

    /***********************************************************************************************
     * Inventory detail Recycler view init
     */
    private fun inventoryDetailRecyclerViewInit() {
        view.recycler_inventory_valuation.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(view.context)
            adapter = inventoryValuationAdapter
        }
        setInventoryDetailRecyclerData()
    }

    private fun setInventoryDetailRecyclerData() {
        inventoryValuationAdapter.setData(inventoryValuationList)
        inventoryValuationAdapter.notifyDataSetChanged()
    }

    /***********************************************************************************************
     * Inventory Detail Recycler view adapter : Period, Volume
     */
    private class InventoryValuationRecyclerAdapter : RecyclerView.Adapter<InventoryValuationRecyclerAdapter.ViewHolder>() {

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val datas = mutableListOf<InventoryDetailActivity.InventoryDetailItem>()

        fun setData(datas: List<InventoryDetailActivity.InventoryDetailItem>) {
            this.datas.clear()
            this.datas.addAll(datas)
        }

//        fun getData(): List<InventoryDetailActivity.InventoryDetailItem> {
//            return this.datas
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_valuation, parent, false))
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]

                tv_inventory_valuation_week.text = context.getWeek(data.bseYw)
                tv_inventory_valuation_weekof.text = data.bseWeekFmDt.getYMDWithHypen()

                tv_inventory_valuation_cost_value.text = currencyFormat.format(data.unitCostValue.toInt())

                tv_inventory_valuation_market_value.text = currencyFormat.format(data.f9MarketValue.toInt())
                val diffValue = (data.f9MarketValue - data.unitCostValue).toInt()
                var diffSymbol = if (diffValue < 0) "- " else "+ "
                val diffAbsValue = abs(diffValue)
                var profitTextColor = ContextCompat.getColor(context, R.color.color_f41801)
                if (diffValue > 0F) {
                    diffSymbol = "+"
                    profitTextColor = ContextCompat.getColor(context, R.color.green_blue)
                }
                tv_inventory_valuation_market_value_profit.setTextColor(profitTextColor)
                tv_inventory_valuation_market_value_profit.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}

