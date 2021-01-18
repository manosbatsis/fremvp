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
import com.cyberlogitec.freight9.lib.model.OrderData
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import kotlinx.android.synthetic.main.item_buy_order_in_period_and_volume.view.*
import kotlinx.android.synthetic.main.popup_order_period_and_volume.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class PeriodAndVolumePopup(var view: View,
                           width: Int,
                           height: Int,
                           focusable: Boolean,
                           onPeriodAndVolumeEdit: (() -> Unit),
                           var is40Ft: Boolean = false) :
        PopupWindow(view, width, height, focusable) {

    val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val adapterPeriodVolumeFull by lazy {
        RecyclerPeroidVolumeAdapter(is40Ft).apply { }
    }

    init {
        currencyFormat.minimumFractionDigits = 0
        view.tv_popup_order_period_and_volume_edit.setSafeOnClickListener {
            onPeriodAndVolumeEdit()
        }
        view.iv_order_detail_period_and_volume_close.setSafeOnClickListener {
            dismiss()
        }
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0 )

    }

    fun initValue(containerName: String, datas: List<OrderData>) {
        val teuUnit = if (!is40Ft) view.context.getString(R.string.teu_unit_20ft) else view.context.getString(R.string.teu_unit_40ft)
        view.tv_title_volume_price.text = view.context.getString(R.string.volume_price_unit, teuUnit)
        view.tv_title_volume_volume.text = view.context.getString(R.string.volume_volume_unit, teuUnit)

        view.tv_popup_order_period_and_volume_container_type.text = containerName
        view.recycler_popup_order_period_and_volume.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = adapterPeriodVolumeFull
        }

        adapterPeriodVolumeFull.datas.clear()
        for (x in datas) {
            val subTotal = x.subTotal
            if (subTotal > 0) {
                adapterPeriodVolumeFull.datas.add(x)
            }
        }
        adapterPeriodVolumeFull.notifyDataSetChanged()
        view.tv_popup_order_period_and_volume_sum_value.text = currencyFormat.format(adapterPeriodVolumeFull.getCalcTotal())
    }

    private class RecyclerPeroidVolumeAdapter(val is40Ft: Boolean) : RecyclerView.Adapter<RecyclerPeroidVolumeAdapter.ViewHolder>() {

        lateinit var context: Context
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val datas = mutableListOf<OrderData>()
        var onClickItem: (Long) -> Unit = {
            Timber.d("f9: onClickItem = $it")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            return  ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_buy_order_in_period_and_volume, parent, false))
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
            with(datas[position]) {
                itemView.tv_order_week_item.text = "W${bseYw.substring(4)}"
                itemView.tv_order_price_item.text = currencyFormat.format(price)
                itemView.tv_order_volume_base_item.text = volume.toString()
                val subTotal = price * volume
                itemView.tv_order_subtotal_item.text = currencyFormat.format(subTotal)
            }
        }

        fun getCalcTotal(): Int {
            var total = 0
            for (item in datas) {
                val subTotal = item.price * item.volume
                total += subTotal
            }
            return total
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }
}

