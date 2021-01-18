package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.LocationTypeCode.POD
import com.cyberlogitec.freight9.config.LocationTypeCode.POL
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_route.*
import kotlinx.android.synthetic.main.body_bof_route.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = BofRouteVm::class)
class BofRouteAct : BaseActivity<BofRouteVm>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_route)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {

        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.DONE,
                title = getString(R.string.buy_offer_select_route),
                isEnableNavi = false)
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onClickEdit()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityWithFinish(Intent(this, BofLaneAct::class.java)
                            .putExtra(Intents.OFFER, it)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }

        viewModel.outPuts.onClickDone()
                .bindToLifecycle(this)
                .subscribe {
                    onBackPressed()
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    startActivity(Intent(this, BofVolumeAct::class.java).putExtra(Intents.OFFER, it))
                }

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->

                        // service lane
                        tv_service_lane_code.text = offer.serviceLaneCode
                        tv_service_lane_name.text = offer.serviceLaneName

                        offer.offerRoutes?.let { routes ->
                            // pod list up
                            routes.filter{ it.locationTypeCode == POL }.let { carriers2 ->
                                val locationCodes = carriers2.map{ it.locationCode }.distinct()
                                val locationNames = carriers2.map{ it.locationName }.distinct()
                                val polCodesAndNames = mutableListOf<Pair<String, String>>()
                                locationCodes.zip(locationNames).forEach{ pol -> polCodesAndNames.add(Pair(pol.first, pol.second)) }
                                setPols(polCodesAndNames)
                            }

                            // pod list up
                            routes.filter{ it.locationTypeCode == POD }.let { carriers2 ->
                                val locationCodes = carriers2.map{ it.locationCode }.distinct()
                                val locationNames = carriers2.map{ it.locationName }.distinct()
                                val podCodesAndNames = mutableListOf<Pair<String, String>>()

                                locationCodes.zip(locationNames).forEach{ pod -> podCodesAndNames.add(Pair(pod.first, pod.second)) }
                                setPods(podCodesAndNames)
                            }

                            // polcnt - pod cnt
                            //val polCnt = routes.count{ it.locationTypeCode == POL }
                            //val podCnt = routes.count{ it.locationTypeCode == POD }

                            // pol
                            val pol = routes.sortedBy { it.offerRegSeq }.filter{it.locationTypeCode == POL}.first()
                            //tv_pol_name.text = String.format("%s+%d", pol.locationCode, polCnt )
                            tv_pol_name.text = pol.locationCode
                            tv_pol_detail.text = pol.locationName

                            // pod
                            val pod = routes.sortedBy { it.offerRegSeq }.filter{it.locationTypeCode == POD}.first()
                            //tv_pod_name.text = String.format("%s+%d", pod.locationCode, podCnt )
                            tv_pod_name.text = pod.locationCode
                            tv_pod_detail.text = pod.locationName
                        }
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //

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
        // on click left button
        toolbar_common.toolbar_left_btn.setSafeOnClickListener {
            onBackPressed()
        }

        // on click toolbar right button
        toolbar_common.toolbar_done_btn.setSafeOnClickListener {
            viewModel.inPuts.clickToDone(Parameter.CLICK)
        }

        btn_edit.setSafeOnClickListener {
            viewModel.inPuts.clickToEdit(Parameter.CLICK)
        }

        btn_bof_route_select.setSafeOnClickListener {
            viewModel.inPuts.clickToNext( Parameter.CLICK )
        }
    }

    private fun setPols(pols: List<Pair<String, String>>) {
        ll_pols.removeAllViews()
        for (pol in pols) {
            val llRow = LinearLayout(this)
            val height = 30.toDp().toInt()
            llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            llRow.orientation = LinearLayout.HORIZONTAL
            llRow.gravity = Gravity.CENTER_VERTICAL
            llRow.addView(makeNameTextView(pol.first))
            llRow.addView(makeDescriptionTextView(pol.second))
            ll_pols.addView(llRow)
        }
    }

    private fun setPods(pods: List<Pair<String, String>>) {
        ll_pods.removeAllViews()
        for (pod in pods) {
            val llRow = LinearLayout(this)
            val height = 30.toDp().toInt()
            llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            llRow.orientation = LinearLayout.HORIZONTAL
            llRow.gravity = Gravity.CENTER_VERTICAL
            llRow.addView(makeNameTextView(pod.first))
            llRow.addView(makeDescriptionTextView(pod.second))
            ll_pods.addView(llRow)
        }
    }

    private fun makeNameTextView(value: String): TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(78.toDp().toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_eb_15_greyishbrown)
        textView.text = value
        return textView
    }

    private fun makeDescriptionTextView(value: String): TextView {
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
        textView.text = value
        return textView
    }
}