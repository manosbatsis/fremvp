package com.cyberlogitec.freight9.ui.selloffer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.SellOffer.SELL_OFFER_WIZARD
import com.cyberlogitec.freight9.lib.model.ContainerSimpleInfo
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.ContractLineItem
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.startActivityWithFinish
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_PRICE
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_sof_price_check.*
import kotlinx.android.synthetic.main.body_sof_price_check.*
import kotlinx.android.synthetic.main.body_sof_volume_check.btn_edit
import kotlinx.android.synthetic.main.item_sell_offer_price_check.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = SofPriceCheckVm::class)
class SofPriceCheckAct : BaseActivity<SofPriceCheckVm>() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_price_check)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_sof_price_check, menuType = MenuType.CROSS, title = getString(R.string.selloffer_price), isEnableNavi = false)

        // init recyclerview
        recyclerViewInit()

        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onClickEdit()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofVolumeAct)")
                    if (SELL_OFFER_WIZARD) {
                        // For Sell Offer Wizard
                        val intent = Intent()
                        intent.putExtra(Intents.SELL_OFFER_STEP, STEP_PRICE)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        // For Sell Offer activity(original)
                        startActivityWithFinish( Intent(this, SofPriceAct::class.java)
                            .putExtra(Intents.MSTR_CTRK_NR, it)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { contract ->
                        Timber.d("f9: onSuccessRefresh +")
                        if (SELL_OFFER_WIZARD) {
                            contract.apply {
                                masterContractLineItems = masterContractLineItems?.filter { it.isChecked }
                            }
                        }
                        setContainerType(contract)
                        adapter.setData(contract)
                        adapter.notifyDataSetChanged()
                        Timber.d("f9: onSuccessRefresh -")
                    }

                }
    }

    private fun setListener() {
        // on click toolbar right button
        appbar_sof_price_check.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        btn_edit.setSafeOnClickListener {
            viewModel.inPuts.clickToEdit(Parameter.CLICK)
        }
    }

    private fun setContainerType(contract: Contract?) {
        contract?.let {
            val rdTermType = RdTermItemTypes.getRdTermItemType(contract.rdTermCode)
            val containerTypeCode = contract.masterContractLineItems?.first()?.masterContractPrices?.first()?.containerTypeCode
            val containerTypeInfo = ContainerSimpleInfo(rdTermType!!, containerTypeCode!!)
            val containerName = /*getString(rdTermType!!.rdNameId) + " " +*/
                    getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
            tv_sof_volume_container_type.text = containerName
        }
    }

    private fun recyclerViewInit() =
            recycler_view_sof_check.apply {
                layoutManager = LinearLayoutManager(this@SofPriceCheckAct)
                adapter = this@SofPriceCheckAct.adapter
            }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var data: Contract? = null
        var datas: List<ContractLineItem>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sell_offer_price_check, parent, false))
        }

        override fun getItemCount(): Int {
            var size = 0
            datas?.let { data ->
                size = data.size
            }
            return size
        }

        fun setData(contract: Contract?) {
            contract?.let {
                this.data = it
                it.masterContractLineItems?.let{
                    this.datas = it
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            setInitItemView(holder.itemView, position)
        }

        private fun setInitItemView(itemView: View, position: Int) {
            datas?.let { data ->
                with(data[position]) {
                    val offerPriceStr = if (offerPrice > 0) currencyFormat.format(offerPrice) else "-"
                    val costPriceStr = if (costPrice > 0) currencyFormat.format(costPrice.toInt()) else "-"
                    val yearWeek = context.getWeek(baseYearWeek)

                    itemView.tv_order_week_item.text = yearWeek
                    itemView.tv_order_cost_value.text = costPriceStr
                    itemView.tv_order_volume_input_item.text = offerPriceStr
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}