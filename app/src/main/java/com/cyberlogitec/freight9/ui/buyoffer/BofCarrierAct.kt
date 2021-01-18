package com.cyberlogitec.freight9.ui.buyoffer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents.Companion.CARRIER_LIST
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_carrier.*
import kotlinx.android.synthetic.main.body_bof_carrier.*
import kotlinx.android.synthetic.main.bottom_bof_carrier.*
import kotlinx.android.synthetic.main.item_bof_carrier.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = BofCarrierVm::class)
class BofCarrierAct : BaseActivity<BofCarrierVm>() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onSelectChanged = { viewModel.inPuts.changeSelect(it) }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_carrier)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_carrier_edit),
                isEnableNavi = false)

        // init recyclerview
        recyclerViewInit()

        setListener()
    }

    private fun setRxOutputs() {
        // output
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offerCarriers ->

                        Handler().postDelayed({
                            sv_carrier.scrollTo(0,0)
                        }, 100)

                        adapter.datas.clear()
                        adapter.datas.addAll(offerCarriers)
                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        val intent = Intent()
                        intent.putExtra(CARRIER_LIST, ArrayList(it))
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

        viewModel.outPuts.refreshApply()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketActivity)")
                    refreshApplyButton()
                }
    }

    private fun setListener() {
        // on click toolbar right button
        toolbar.toolbar_common.toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        // on click next button
        btn_bof_carrier_next_floating.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            viewModel.inPuts.clickToNext(adapter.datas)
        }

        tv_clear_all.setSafeOnClickListener {
            changeClear(false)
            adapter.changeAll(false)
        }

        tv_select_all.setSafeOnClickListener {
            changeClear(true)
            adapter.changeAll(true)
        }
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

    private fun refreshApplyButton() {
        var count = 0
        for (offerCarrier in adapter.datas) {
            if (offerCarrier.isChecked)
                count++
        }
        if (count > 0) {
            btn_bof_carrier_next_floating.isEnabled = true
            btn_bof_carrier_next_floating.isClickable = true
            changeClear(true)
        } else {
            btn_bof_carrier_next_floating.isEnabled = false
            btn_bof_carrier_next_floating.isClickable = false
            changeClear(false)
        }
    }

    private fun recyclerViewInit() =
            recycler_view_bof_carrier.apply {
                layoutManager = LinearLayoutManager(this@BofCarrierAct)
                adapter = this@BofCarrierAct.adapter
            }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<OfferCarrier>()

        var onSelectChanged:(Parameter) -> Unit = {}
        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_carrier, parent, false))

        override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
            if(holder.adapterPosition != RecyclerView.NO_POSITION) {
                with(holder.itemView) {
                    with(datas[position] ) {
                        btn_carrier_selected.setOnCheckedChangeListener(null)
                        view_carrier_separator.visibility = if (itemCount - 1 == position) View.GONE else View.VISIBLE
                        carrier_logo.setImageResource(offerCarrierCode.getCarrierIcon())
                        tv_carrier_code.text = offerCarrierCode
                        tv_carrier_name.text = offerCarrierName
                        btn_carrier_selected.isChecked = isChecked
                        btn_carrier_selected.setOnCheckedChangeListener { buttonView, isChecked ->
                            this.isChecked = isChecked
                            onSelectChanged(Parameter.CLICK)
                        }
                    }
                }
            }
        }

        fun changeAll(status: Boolean) {
            for(offerCarrier in datas) {
                offerCarrier.isChecked = status
            }
            notifyDataSetChanged()
            onSelectChanged(Parameter.CLICK)
        }

        public class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}