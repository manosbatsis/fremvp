package com.cyberlogitec.freight9.ui.selloffer

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_make_offer.*
import kotlinx.android.synthetic.main.appbar_sof_make_offer.*
import kotlinx.android.synthetic.main.body_offer_make_offer.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofMakeOfferVm::class)
class SofMakeOfferAct : BaseActivity<SofMakeOfferVm>() {

    lateinit var contract: Contract
    lateinit var offer: Offer

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_make_offer)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        // init type face (font)
        ResourcesCompat.getFont(this, R.font.opensans_regular)?.let{
            tfRegular = it
        }

        ResourcesCompat.getFont(this, R.font.opensans_bold)?.let {
            tfBold = it
        }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_sof_make_offer, menuType = MenuType.DONE, title = getString(R.string.offer_make_new_sell_offer))

        setListener()
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // output

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh +")

                    // save contract
                    this.contract = it
                    // pod pol card set
                    setPolPodValue(it)
                    // whole not applied
                    this.offer.allYn = "0"  // partial

                    Timber.d("f9: onSuccessRefresh -")
                }

        viewModel.outPuts.onSuccessRefresh2()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh 1 -")

                    this.offer = it

                    Timber.d("f9: onSuccessRefresh 2 -")
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClickNext

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityWithFinish(Intent(this, MarketActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: tables

        viewModel.outPuts.onClickConditionDetail()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(ConditionTableActivity)")
                    startActivity(Intent(this, SofConditionTableAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        viewModel.outPuts.onClickWholeRoute()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(RouteTableActivity)")
                    startActivity(Intent(this, SofRouteGridAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        viewModel.outPuts.onClickPriceTable()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofPriceTableAct)")
                    startActivity(Intent(this, SofPriceTableAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClick X Check

        viewModel.outPuts.onClickVolumeCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofVolumeCheckAct)")
                    startActivity(Intent(this, SofVolumeCheckAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        viewModel.outPuts.onClickPriceCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofPriceCheckAct)")
                    startActivity(Intent(this, SofPriceCheckAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        viewModel.outPuts.onClickPlanCheck()
                .bindToLifecycle(this)
                .subscribe {
                    startActivity(Intent(this, SofConditionCheckAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                }

        viewModel.outPuts.onClickDealOptionsInfo()
                .bindToLifecycle(this)
                .subscribe {
                    // TODO : Deal options info
                }

        // outPuts - [Done]
        viewModel.outPuts.onClickDone()
                .bindToLifecycle(this)
                .subscribe {
                    showSaveDialog()
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: progress & error

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog : ${it}")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    private fun setListener() {
        // wait click event (toolbar left button)
        appbar_sof_make_offer.toolbar_left_btn.setSafeOnClickListener {
            onBackPressed()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        appbar_sof_make_offer.toolbar_done_btn.setSafeOnClickListener {
            viewModel.inPuts.clickToDone(Parameter.CLICK)
        }

        btn_make_sell_offer_next.setSafeOnClickListener {
            Timber.d("f9: btn_make_sell_offer_next +")

            viewModel.inPuts.clickToNext( offer )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        // conidtion detail
        tv_link_condition_detail.setSafeOnClickListener {
            viewModel.inPuts.clickToConditionDetail(Parameter.CLICK)
        }

        // whole route
        tv_link_whole_route.setSafeOnClickListener {
            viewModel.inPuts.clickToWholeRoute(Parameter.CLICK)
        }

        tv_price_table.setSafeOnClickListener {
            viewModel.inPuts.clickToPriceTable(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        iv_period_and_volume.setSafeOnClickListener {
            viewModel.inPuts.clickToVolumeCheck(Parameter.CLICK)
        }

        // price check
        iv_price.setSafeOnClickListener {
            viewModel.inPuts.clickToPriceCheck(Parameter.CLICK)
        }

        // plan check
        iv_pay_plan.setSafeOnClickListener {
            viewModel.inPuts.clickToPlanCheck(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click igree
        chk_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            btn_make_sell_offer_next.isEnabled = isChecked
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // whole deal option
        chk_whole_deal_apply.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("f9: isChecked: $isChecked")
            if (isChecked) {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_applied)
                tv_whole_deal_apply.typeface = tfBold
                this.offer.allYn = "1"  // whole
            } else {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_not_applied)
                tv_whole_deal_apply.typeface = tfRegular
                this.offer.allYn = "0"  // partial
            }
        }

        iv_check_mark.setSafeOnClickListener {
            viewModel.inPuts.clickToDealOptionsInfo(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click more
        tv_terms_more.setSafeOnClickListener {
            showTermsMorePopup()
        }
    }

    private fun setPolPodValue(contract: Contract?) {
        contract?.masterContractCarriers?.let { carriers ->
            val carrierCode = carriers.first().carrierCode
            val carriersCount = carriers.size
            iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
            tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
            tv_carrier_count.text = if (carriersCount > 1) String.format("+%d", carriersCount - 1) else ""
        }

        contract?.masterContractRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes.count{ it.locationTypeCode == LocationTypeCode.POL && it.locationCode == firstPol?.locationCode }
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = if (polCnt > 1) String.format("+%d", polCnt -1) else ""
            }

            // find first pod
            val firstPod = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
            val podCnt = routes.count{ it.locationTypeCode == LocationTypeCode.POD && it.locationCode == firstPod?.locationCode }
            firstPod?.let {
                tv_pod_name.text = it.locationCode
                tv_pod_desc.text = it.locationName
                tv_pod_count.text = if (podCnt > 1) String.format("+%d", podCnt -1) else ""
            }

            tv_period.text = ""       // W01-W10
            // icon 표시하지 않음
            iv_period_whole.visibility = View.INVISIBLE
        }
    }

    private fun showSaveDialog() {
        val dialog = NormalTwoBtnDialog(title = getString(R.string.offer_save_dialog_title),
                desc = getString(R.string.offer_save_dialog_desc),
                leftBtnText = getString(R.string.offer_save_dialog_discard),
                rightBtnText = getString(R.string.offer_save_dialog_save))
        dialog.isCancelable = false
        dialog.setOnClickListener(View.OnClickListener {
            it?.let {
                dialog.dismiss()
                if (it.id == R.id.btn_right) {

                    // TODO : Save
                    showToast("SAVE offer")

                } else {
                    finish()
                }
            }
        })
        dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
    }
}