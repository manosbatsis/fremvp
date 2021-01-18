package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toDp
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_LANE
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_route.*
import timber.log.Timber

class BofWizardRouteSelectFragment constructor(val viewModel: BofWizardRouteViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_route_select, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("f9: onViewCreated")
        setRxOutputs()
        initData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("f9: onAttach")
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        Timber.d("f9: onDetach")
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        Timber.d("f9: onDestroyView")
        super.onDestroyView()
    }

    override fun onStart() {
        Timber.d("f9: onStart")
        super.onStart()
    }

    override fun onStop() {
        Timber.d("f9: onStop")
        super.onStop()
    }

    override fun onResume() {
        Timber.d("f9: onResume")
        super.onResume()
    }

    override fun onPause() {
        Timber.d("f9: onPause")
        super.onPause()
    }

    //----------------------------------------------------------------------------------------------

    /**
     * fragment data init
     */
    private fun initData() {
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        // offer : Service Lane, POLs, PODs 로 구성된 offer data
        viewModel.outPuts.onGoToStepSelect()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->
                        // service lane
                        tv_service_lane_code.text = offer.serviceLaneCode
                        tv_service_lane_name.text = offer.serviceLaneName

                        offer.offerRoutes?.let { routes ->
                            // pod list up
                            routes.filter{ it.locationTypeCode == LocationTypeCode.POL }.let { carriers2 ->
                                val locationCodes = carriers2.map{ it.locationCode }.distinct()
                                val locationNames = carriers2.map{ it.locationName }.distinct()
                                val polCodesAndNames = mutableListOf<Pair<String, String>>()
                                locationCodes.zip(locationNames).forEach{ pol -> polCodesAndNames.add(Pair(pol.first, pol.second)) }
                                setPols(polCodesAndNames)
                            }

                            // pod list up
                            routes.filter{ it.locationTypeCode == LocationTypeCode.POD }.let { carriers2 ->
                                val locationCodes = carriers2.map{ it.locationCode }.distinct()
                                val locationNames = carriers2.map{ it.locationName }.distinct()
                                val podCodesAndNames = mutableListOf<Pair<String, String>>()

                                locationCodes.zip(locationNames).forEach{ pod -> podCodesAndNames.add(Pair(pod.first, pod.second)) }
                                setPods(podCodesAndNames)
                            }

                            // polcnt - pod cnt
                            //val polCnt = routes.count{ it.locationTypeCode == POL }
                            //val podCnt = routes.count{ it.locationTypeCode == POD }

                            // pol in ui card
                            val pol = routes.sortedBy { it.offerRegSeq }.filter{it.locationTypeCode == LocationTypeCode.POL }.first()
                            //tv_pol_name.text = String.format("%s+%d", pol.locationCode, polCnt )
                            tv_pol_name.text = pol.locationCode
                            tv_pol_detail.text = pol.locationName

                            // pod in ui card
                            val pod = routes.sortedBy { it.offerRegSeq }.filter{it.locationTypeCode == LocationTypeCode.POD }.first()
                            //tv_pod_name.text = String.format("%s+%d", pod.locationCode, podCnt )
                            tv_pod_name.text = pod.locationCode
                            tv_pod_detail.text = pod.locationName
                        }

                        setDataStatus(Pair(BofWizardRouteActivity.STEP_SELECT, Pair(false, offer)))
                    }
                }

    }

    private fun setListener() {
        // move to STEP_LANE
        btn_edit.setSafeOnClickListener {
            viewModel.inPuts.requestGoToOtherStep(Pair(STEP_LANE, null))
        }
    }

    private fun setDataStatus(pair: Pair<Int, Pair<Boolean, Offer?>>) {
        viewModel.inPuts.requestSetDataStatus(pair)
    }

    /**
     * POLs ui list
     */
    private fun setPols(pols: List<Pair<String, String>>) {
        ll_pols.removeAllViews()
        for (pol in pols) {
            val llRow = LinearLayout(this@BofWizardRouteSelectFragment.context)
            val height = 30.toDp().toInt()
            llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            llRow.orientation = LinearLayout.HORIZONTAL
            llRow.gravity = Gravity.CENTER_VERTICAL
            llRow.addView(makeNameTextView(pol.first))
            llRow.addView(makeDescriptionTextView(pol.second))
            ll_pols.addView(llRow)
        }
    }

    /**
     * PODs ui list
     */
    private fun setPods(pods: List<Pair<String, String>>) {
        ll_pods.removeAllViews()
        for (pod in pods) {
            val llRow = LinearLayout(this@BofWizardRouteSelectFragment.context)
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
        val textView = TextView(this@BofWizardRouteSelectFragment.context)
        textView.layoutParams = LinearLayout.LayoutParams(78.toDp().toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_eb_15_greyishbrown)
        textView.text = value
        return textView
    }

    private fun makeDescriptionTextView(value: String): TextView {
        val textView = TextView(this@BofWizardRouteSelectFragment.context)
        textView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
        textView.text = value
        return textView
    }

    //----------------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardRouteViewModel) : BofWizardRouteSelectFragment {
            return BofWizardRouteSelectFragment(viewModel)
        }
    }
}