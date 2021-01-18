package com.cyberlogitec.freight9.ui.buyoffer

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
import com.cyberlogitec.freight9.config.BuyOffer.BUY_OFFER_WIZARD
import com.cyberlogitec.freight9.lib.model.ContainerSimpleInfo
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.startActivityWithFinish
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_price_check.*
import kotlinx.android.synthetic.main.body_bof_price_check.*
import kotlinx.android.synthetic.main.body_bof_volume_check.btn_edit
import kotlinx.android.synthetic.main.item_buy_offer_price_check.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = BofPriceCheckVm::class)
class BofPriceCheckAct : BaseActivity<BofPriceCheckVm>() {

    // list view
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_price_check)
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
        defaultbarInit(appbar_bof_price_check,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_price),
                isEnableNavi = false)

        // init recyclerview
        recyclerViewInit()

        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onClickEdit()
                .bindToLifecycle(this)
                .subscribe {
                    if (BUY_OFFER_WIZARD) {
                        val intent = Intent()
                        intent.putExtra(Intents.BUY_OFFER_STEP, BofWizardActivity.STEP_PRICE)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        startActivityWithFinish(Intent(this, BofPriceAct::class.java)
                                .putExtra(Intents.OFFER, it as Offer)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    // select unique week.
                    it?.let { offer ->
                        if (BUY_OFFER_WIZARD) {
                            offer.apply {
                                offerLineItems = offerLineItems?.filter { it.isChecked }
                            }
                        }
                        setContainerType(offer)
                        adapter.setData( offer )
                        adapter.notifyDataSetChanged()
                    }
                }
    }

    private fun setListener() {
        // on click toolbar right button
        appbar_bof_price_check.toolbar_right_btn.setSafeOnClickListener {
            onBackPressed()
        }

        btn_edit.setSafeOnClickListener {
            viewModel.inPuts.clickToEdit(Parameter.CLICK)
        }
    }

    private fun setContainerType(offer: Offer) {
        offer.offerLineItems?.let { lineItems ->
            if (!lineItems.isNullOrEmpty()) {
                val rdTermType = RdTermItemTypes.getRdTermItemType(offer.offerRdTermCode)
                val containerTypeCode = lineItems.first().tradeContainerTypeCode
                val containerTypeInfo = ContainerSimpleInfo(rdTermType!!, containerTypeCode)
                val containerName = /*getString(rdTermType!!.rdNameId) + " " + */
                        getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
                tv_bof_price_container_type.text = containerName
            } else {
                tv_bof_price_container_type.text = getString(R.string.container_full_name)
            }
        }
    }

    private fun recyclerViewInit() =
            recycler_view_bof_check.apply {
                layoutManager = LinearLayoutManager(this@BofPriceCheckAct)
                adapter = this@BofPriceCheckAct.adapter
            }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var offer: Offer? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            Timber.d("f9: onCreateViewHolder(viewType: ${viewType})")
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_buy_offer_price_check, parent, false))
        }

        override fun getItemCount(): Int {
            var size = 0
            offer?.let { offer ->
                size = offer.offerLineItems?.size!!
            }
            return size
        }

        fun setData(offer: Offer) {
            offer.let {
                this.offer = it
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Timber.d("f9: onBindViewHolder(position(${position}))")
            setInitItemView(holder.itemView, position)
        }

        private fun setInitItemView(itemView: View, position: Int) {
            offer?.let { offer ->
                offer.offerLineItems?.let { lineItems ->
                    with(lineItems[position]) {
                        itemView.tv_week_item.text = context.getWeek(baseYearWeek)
                        itemView.tv_week_cost_value.text = if (costPrice > 0) currencyFormat.format(costPrice) else "-"
                        itemView.tv_week_unit_price.text = if (offerPrice > 0) currencyFormat.format(offerPrice) else "-"
                    }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}