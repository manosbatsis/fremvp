package com.cyberlogitec.freight9.ui.trademarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.Carrier
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_market_carrier_filter.*
import kotlinx.android.synthetic.main.item_market_filter_carrier.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber

@RequiresActivityViewModel(value = MarketFilterViewModel::class)
class MarketCarrierFilterActivity : BaseActivity<MarketFilterViewModel>() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onSelectchanged = { viewModel.inPuts.changeSelect(it) }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_market_carrier_filter)

        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.color_0d0d0d)

        toolbarInit()

        btn_filter_carrier_apply.setOnClickListener {
            Timber.d("f9: market_btn_filter_carrier_apply_click")
            viewModel.inPuts.clickToCarrierApply(adapter.datas)
        }
        tv_clear_all.setSafeOnClickListener {
            Timber.d("f9: market_btn_filter_carrier_apply_click")
            adapter.changeAll(false)
            changeClear(false)
        }
        tv_select_all.setSafeOnClickListener {
            changeClear(true)
            adapter.changeAll(true)
        }

        viewModel.outPuts.gotoMarket()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketActivity)")
                    finish()
                }

        viewModel.outPuts.refreshCarrierFilter()
                .bindToLifecycle(this)
                .subscribe {
                    adapter.datas.clear()
                    adapter.datas.addAll(it)
                    adapter.notifyDataSetChanged()
                }
        // init recyclerview
        recyclerViewInit()
    }

    private fun changeClear(isClear: Boolean) {
        if(isClear) {
            tv_clear_all.visibility = View.VISIBLE
            tv_select_all.visibility = View.GONE
        }
        else {
            tv_clear_all.visibility = View.GONE
            tv_select_all.visibility = View.VISIBLE
        }

    }

    private fun toolbarInit() {

        defaultbarInit(toolbar_common,menuType = MenuType.CROSS)
        toolbar_common.setBackgroundColor(getColor(R.color.color_0d0d0d))
        toolbar_left_btn.visibility = View.INVISIBLE
        toolbar_right_btn.setOnClickListener {
            Timber.d("f9: market_btn_filter_carrier_x_click")
            finish()
        }
    }

    private fun recyclerViewInit() {

        val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager(this).orientation)
        recycler_view_market_carrier_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_carrier_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketCarrierFilterActivity)
            adapter = this@MarketCarrierFilterActivity.adapter
        }

        viewModel.outPuts.refreshApply()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketActivity)")
                    refreshApplyButton()
                }
    }

    private fun refreshApplyButton() {
        var count = 0
        for (data in adapter.datas) {
            if(data.select!!)
                count++
        }
        if(count>0) {
            btn_filter_carrier_apply.isEnabled = true
            btn_filter_carrier_apply.isClickable = true
            changeClear(true)
        }
        else {
            btn_filter_carrier_apply.isEnabled = false
            btn_filter_carrier_apply.isClickable = false
            changeClear(false)
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        var datas = mutableListOf<Carrier>()

        var onSelectchanged:(Parameter) -> Unit = {}
        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_market_filter_carrier, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with(datas[position]){
                    when {
                        position == 0 -> {
                            space_top.visibility = View.VISIBLE
                            space_bottom.visibility = View.GONE
                        }
                        datas.size == position + 1 -> {
                            space_top.visibility = View.GONE
                            space_bottom.visibility = View.VISIBLE
                        }
                        else -> {
                            space_top.visibility = View.GONE
                            space_bottom.visibility = View.GONE
                        }
                    }
                    btn_carrier_selected.setOnCheckedChangeListener(null)
                    item_filter_carrier_logo.setImageResource(carriercode.getCarrierIcon())
                    item_filter_carrier_name.text=carriercode
                    item_filter_carrier_name_detail.text=carriername
                    btn_carrier_selected.isChecked = select!!
                    btn_carrier_selected.setOnCheckedChangeListener { buttonView, isChecked ->
                        select=isChecked
                        onSelectchanged(Parameter.CLICK)
                    }
                }
            }
        }
        fun changeAll(status: Boolean) {
            for(x in datas) {
                x.select=status
            }
            notifyDataSetChanged()
            onSelectchanged(Parameter.CLICK)
        }


        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
