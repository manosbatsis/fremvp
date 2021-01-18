package com.cyberlogitec.freight9.ui.selloffer

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
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.config.SellOffer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_newoffer.*
import kotlinx.android.synthetic.main.body_sof_newoffer.*
import kotlinx.android.synthetic.main.item_offer_draft.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.io.Serializable


@RequiresActivityViewModel(value = SofNewOfferVm::class)
class SofNewOfferAct : BaseActivity<SofNewOfferVm>() {

    private var draftOfferList: MutableList<DraftOffer> = mutableListOf()

    private val adapter: RecyclerAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    onClickItem = {
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, it))
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_newoffer)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        makeDraftOfferDummy()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common, menuType = MenuType.DEFAULT, title = "Make New Sell Offer", isEnableNavi=false)

        setDraftLayout()
        setListener()
        recyclerViewInit()
    }

    private fun setRxOutputs() {
        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivity(MenuItem.MENUITEM_SELL_OFFER, MenuActivity::class.java)
                }

        viewModel.outPuts.gotoCreate()
                .bindToLifecycle(this)
                .subscribe{ masterContractNumber ->
                    Timber.d("f9: startActivity(SofContractAct)")
                    startActivity(Intent(this, SofContractAct::class.java)
                            .putExtra(Intents.MSTR_CTRK_NR, masterContractNumber))
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                ParameterAny.ANY_ITEM_INDEX -> {

                                    // TODO : Draft item 선택 시  "Set Sell Volume" 으로 이동

                                    val index = second as Int
                                    showToast("go to SofWizardActivity : $index")
//                                  startActivity(Intent(this, SofWizardActivity::class.java).putExtra(Intents.MSTR_CTRK_NR, it))
                                }
                                else -> {  }
                            }
                        }
                    }
                }
    }

    private fun setDraftLayout() {
        fl_new_offer_draft.visibility = if (SellOffer.SELL_OFFER_NO_DRAFT) View.GONE else View.VISIBLE
        ll_newoffer_empty.visibility = if (SellOffer.SELL_OFFER_NO_DRAFT) View.VISIBLE else View.GONE
        ll_newoffer_draft.visibility = if (SellOffer.SELL_OFFER_NO_DRAFT) View.GONE else View.VISIBLE
    }

    private fun setListener() {
        // wait click event (toolbar left button)
        toolbar_common.toolbar_left_btn.setSafeOnClickListener{
            it.run {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_common.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }

        btn_new_offer.setSafeOnClickListener {
            Timber.d("f9: btn_new_offer click")
            viewModel.inPuts.clickToCreate("")
        }

        btn_new_offer_draft.setSafeOnClickListener {
            Timber.d("f9: btn_new_offer_draft click")
            viewModel.inPuts.clickToCreate("")
        }
    }

    private fun recyclerViewInit() {
        recycler_draft_offer_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@SofNewOfferAct.adapter
        }

        // TODO : add all dummy list & notify
        adapter.datas.addAll(draftOfferList)
        adapter.notifyDataSetChanged()
    }

    private fun makeDraftOfferDummy() {
        for (x in 1..10) {
            draftOfferList.add(DraftOffer(
                    "ONE", 3,
                    800.0F, 1000.0F,
                    "202034", "202050",
                    4, 5,
                    "Pusan", "Southampton, Hampshire",
                    "KRPUS", "GBSOU",
                    if (x % 2 == 0) "1" else "0"
            ))
        }
    }

    /***********************************************************************************************
     * RecyclerAdapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        lateinit var context: Context
        val datas = mutableListOf<DraftOffer>()
        var onClickItem: (Int) -> Unit = { _: Int -> }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_offer_draft, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {
                with (datas[position]) {
                    iv_filter_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                    tv_filter_carrier_name.text = context.getCarrierCode(carrierCode)

                    tv_filter_container_price.text = "$${minPrice?.toInt()}-${maxPrice?.toInt()}"

                    tv_filter_pol_code.text = polCode
                    tv_filter_pol_count.text = polCount.getCodeCount()
                    tv_filter_pol_name.text = polName

                    tv_filter_pod_code.text = podCode
                    tv_filter_pod_count.text = podCount.getCodeCount()
                    tv_filter_pod_name.text = podName

                    tv_filter_weekof.text = "${context.getWeek(minYearWeek)}-${context.getWeek(maxYearWeek)}"

                    iv_filter_period_whole.visibility = if (wholeYn == ConstantTradeOffer.ALL_YN_WHOLE) View.VISIBLE else View.GONE
                }
                setSafeOnClickListener { onClickItem(position) }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    data class DraftOffer(
            val carrierCode: String?,
            val carrierCount: Int?,
            val minPrice: Float?,
            val maxPrice: Float?,
            val minYearWeek: String?,
            val maxYearWeek: String?,
            val polCount: Int?,
            val podCount: Int?,
            val polName: String?,
            val podName: String?,
            val polCode: String?,
            val podCode: String?,
            val wholeYn: String?
    ) : Serializable
}