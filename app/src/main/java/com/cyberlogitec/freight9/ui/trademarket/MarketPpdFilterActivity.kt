package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.Payment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_market_ppd_filter.*
import kotlinx.android.synthetic.main.item_market_filter_container.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber

@RequiresActivityViewModel(value = MarketPpdFilterViewModel::class)
class MarketPpdFilterActivity : BaseActivity<MarketPpdFilterViewModel>() {

    private val typeAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }
    private val planAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("onCreate")

        setContentView(R.layout.act_market_ppd_filter)

        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.color_0d0d0d)

        toolbarInit()

        btn_filter_apply.setOnClickListener {
            Timber.d("btn_filter_apply_click")
            val list = mutableListOf<Payment>()
            list.addAll(planAdapter.datas)
            list.addAll(typeAdapter.datas)
            viewModel.inPuts.clickToApply(list)
        }

        viewModel.outPuts.gotoMarket()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("startActivity(MarketActivity)")
                    //startActivityWithFinish(MarketActivity::class.java)
                    finish()
                }

        viewModel.outPuts.refreshPaymentType()
                .bindToLifecycle(this)
                .subscribe { it ->
                    Timber.d("refreshPaymentType")
                    typeAdapter.datas.clear()
                    typeAdapter.datas.addAll(it.sortedByDescending { it.paymenttypecode })
                    typeAdapter.notifyDataSetChanged()
                }
        viewModel.outPuts.refreshPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("refreshPaymentPlan")
                    planAdapter.datas.clear()
                    planAdapter.datas.addAll(it)
                    planAdapter.notifyDataSetChanged()
                }


        // init recyclerview
        recyclerViewInit()
    }

    private fun toolbarInit() {
        defaultbarInit(toolbar_common,menuType = MenuType.CROSS)
        toolbar_common.setBackgroundColor(getColor(R.color.color_0d0d0d))
        toolbar_left_btn.visibility = View.INVISIBLE
        toolbar_right_btn.setOnClickListener {
            Timber.d("market_btn_filter_ppd_x_click")
            finish()
        }
    }

    private fun recyclerViewInit() {
        val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager(this).orientation)
        recycler_view_market_ppd_type_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_ppd_type_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketPpdFilterActivity)
            adapter = this@MarketPpdFilterActivity.typeAdapter
        }
        recycler_view_market_ppd_plan_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_ppd_plan_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketPpdFilterActivity)
            adapter = this@MarketPpdFilterActivity.planAdapter
        }

    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        private lateinit var tfRegular: Typeface
        private lateinit var tfBold: Typeface
        val datas = mutableListOf<Payment>()
        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            tfRegular = ResourcesCompat.getFont(parent.context, R.font.opensans_regular)!!
            tfBold = ResourcesCompat.getFont(parent.context, R.font.opensans_bold)!!
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_market_filter_container, parent, false))

        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with(datas[position]) {
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
                    when(dataType) {
                        "type" -> {
                            when(paymenttypecode) {
                                "P" -> { tv_item.text = resources.getString(R.string.filter_payment_type_ppd_prepaid) }
                                "C" -> { tv_item.text = resources.getString(R.string.filter_payment_type_cct_collect) }
                            }
                        }
                        "plan" -> {
                            tv_item.text = "$iniPymtRto% - $midTrmPymtRto% - $balRto%"
                        }
                    }
                    if(selected!!) {
                        tv_item.typeface = tfBold
                    }else {
                        tv_item.typeface = tfRegular
                    }
                    tv_item.isSelected = selected!!
                    cb_select.isChecked = selected!!
                }

                rl_market_filter_item.setOnClickListener {
                    if(!datas[position].selected!!){
                        clearSelected()
                        datas[position].selected = !datas[position].selected!!
                        notifyDataSetChanged()
                    }
                }
            }
        }
        fun clearSelected() {
            for(data in datas) {
                data.selected = false
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
