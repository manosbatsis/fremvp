package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.BuyOffer.BUY_OFFER_WIZARD
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.FragmentStepper
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.StepsManager
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_wizard_route.*
import kotlinx.android.synthetic.main.act_bof_wizard_route.wizard_stepper
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.*
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.et_search
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.iv_servicelane_cancel
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.ll_search
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.ll_select
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.servicelane_code
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.servicelane_name
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.toolbar
import kotlinx.android.synthetic.main.appbar_bof_wizard_route_progress.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber

@RequiresActivityViewModel(value = BofWizardRouteViewModel::class)
class BofWizardRouteActivity : BaseActivity<BofWizardRouteViewModel>(),
        OnFragmentInteractionListener, StepsManager {

    private lateinit var im: InputMethodManager

    private var currentStep: Int = STEP_RECENTLY
    private var recentlyRouteDataPair: Pair<Boolean, Offer?> = Pair(false, null)
    private var laneScheduleDataPair: Pair<Boolean, List<Schedule>?> = Pair(false, null)
    private var polScheduleDataPair: Pair<Boolean, List<Schedule>?> = Pair(false, null)
    private var podScheduleDataPair: Pair<Boolean, List<Schedule>?> = Pair(false, null)
    private var selectOfferDataPair: Pair<Boolean, Offer?> = Pair(false, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_wizard_route)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")
        showHideKeypad(false)
    }

    override fun onBackPressed() {
        Timber.v("f9: onBackPressed")
        moveBackFragment()
    }

    /**
     * 전체 step 수 리턴
     */
    override fun getCount() = TOTAL_STEP

    /**
     * step 별 이동될 fragment 리턴
     */
    override fun getStep(position: Int): RxFragment {
        return when (position) {
            STEP_RECENTLY -> BofWizardRouteRecentFragment(viewModel)
            STEP_LANE -> BofWizardRouteLaneFragment(viewModel)
            STEP_POL -> BofWizardRoutePolFragment(viewModel)
            STEP_POD -> BofWizardRoutePodFragment(viewModel)
            STEP_SELECT -> BofWizardRouteSelectFragment(viewModel)
            else -> BofWizardRouteRecentFragment(viewModel)
        }
    }

    override fun onFragmentInteraction() {
        if (wizard_stepper.isLastStep()) {
            showToast("Go Next activity")
        } else {
            wizard_stepper.goToNexStep()
        }
    }

    private fun setRxOutputs() {

        // Fragment 에서 SKIP, NEXT btn 눌렀을 때 처리
        viewModel.outPuts.onGoToOtherStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onClickNext --> currentStep: $it")
                        with(wizard_stepper) {
                            if (it.first != getCurrentStep()) {
                                goToOtherStep(it.first)
                            }
                        }
                    }
                }

        // "Select Route" last step 에서 NEXT btn 눌렀을 때 "Set Buy Volume" 으로 이동
        viewModel.outPuts.onGoToBuyVolume()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->
                        if (BUY_OFFER_WIZARD) {
                            startActivity(Intent(this, BofWizardActivity::class.java)
                                    .putExtra(Intents.OFFER, offer)
                                    .putExtra(Intents.OFFER_BY_MADE_CONDITION, false))
                        } else {
                            startActivity(Intent(this, BofVolumeAct::class.java)
                                    .putExtra(Intents.OFFER, offer))
                        }
                    }
                }

        viewModel.outPuts.onSetDataStatus()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { pair ->
                        setDataStatus(pair)
                    }
                }

        viewModel.outPuts.onSearchInit()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        ll_search.visibility = View.VISIBLE
                        ll_select.visibility = View.GONE

                        et_search.setText("")
                        et_search.clearFocus()
                        et_search.isCursorVisible = currentStep == STEP_RECENTLY
                        et_search.isFocusable = currentStep == STEP_RECENTLY
                        et_search.isFocusableInTouchMode = currentStep == STEP_RECENTLY
                    }
                }

        viewModel.outPuts.onSearchSelectSet()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { pair ->
                        servicelane_code.text = pair.first
                        servicelane_name.text = pair.second
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_TO_BOF_DONE -> {
                                showSaveDialog()
                            }
                            ParameterClick.CLICK_TO_BOF_CLOSE -> {
                                finish()
                            }
                            ParameterClick.CLICK_TO_BOF_BACK -> {
                                setBackStatus()
                            }
                            else -> {  }
                        }
                    }
                }

        // SELECT > Buy Volume 으로 이동 시 Preference 에 저장 후 success 유무
        viewModel.outPuts.onSuccessSaveToPreference()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { isFinish ->
                        if (isFinish) {
                            finish()
                        }
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    private fun initData() {
        im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        defaultbarInit(appbar_bof_route_wizard,
                isEnableNavi = false,
                menuType = MenuType.CROSS,
                title = getString(R.string.buyoffer_wizard_route))
        initStepper()
        setListener()
    }

    /**
     * step ui 초기화
     * step change 시 ui 처리
     */
    private fun initStepper() {
        wizard_stepper.setTotalStep(TOTAL_STEP)
        wizard_stepper.setParentActivity(this)

        // step change 시 step 별 Top area ui 구성
        wizard_stepper.stepsChangeListener = object : FragmentStepper.StepsChangeListener {
            override fun onStepChanged(stepNumber: Int) {

                currentStep = stepNumber

                // tool bar
                toolbar.toolbar_left_btn.visibility = View.GONE
                toolbar.toolbar_right_btn.visibility = View.GONE
                toolbar.toolbar_done_btn.visibility = View.GONE
                toolbar.toolbar_left_btn.setImageResource(R.drawable.ic_toolbar_back)
                toolbar.toolbar_right_btn.setImageResource(R.drawable.ic_toolbar_cross)

                // progress step
                ll_progress_search.visibility = if (stepNumber == STEP_SELECT) View.GONE else View.VISIBLE
                rl_bof_route_wizard_progress.visibility = View.VISIBLE
                buyoffer_route_dot_recently.setImageResource(R.drawable.grey_circle_8_8)
                buyoffer_route_dot_lane.setImageResource(R.drawable.grey_circle_8_8)
                buyoffer_route_dot_pols.setImageResource(R.drawable.grey_circle_8_8)
                buyoffer_route_dot_pods.setImageResource(R.drawable.grey_circle_8_8)
                buyoffer_route_text_recently.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                buyoffer_route_text_lane.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                buyoffer_route_text_pols.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                buyoffer_route_text_pods.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)

                setAppBarExpanded(true)

                // init search area
                searchUiInit()

                // hide keypad
                showHideKeypad(false)

                when(stepNumber) {
                    STEP_RECENTLY -> {
                        // title left, right : NONE, CROSS
                        toolbar.toolbar_right_btn.visibility = View.VISIBLE
                        buyoffer_route_dot_recently.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_text_recently.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_LANE -> {
                        // title left, right : BACK, CROSS
                        toolbar.toolbar_left_btn.visibility = View.VISIBLE
                        toolbar.toolbar_right_btn.visibility = View.VISIBLE
                        buyoffer_route_dot_recently.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_lane.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_text_recently.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_lane.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_POL -> {
                        // title left, right : BACK, CROSS
                        toolbar.toolbar_left_btn.visibility = View.VISIBLE
                        toolbar.toolbar_right_btn.visibility = View.VISIBLE
                        buyoffer_route_dot_recently.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_lane.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_pols.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_text_recently.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_lane.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_pols.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_POD -> {
                        // title left, right : BACK, CROSS
                        toolbar.toolbar_left_btn.visibility = View.VISIBLE
                        toolbar.toolbar_right_btn.visibility = View.VISIBLE
                        buyoffer_route_dot_recently.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_lane.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_pols.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_dot_pods.setImageResource(R.drawable.white_grey_circle_16_16)
                        buyoffer_route_text_recently.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_lane.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_pols.setTextAppearance(R.style.txt_opensans_b_10_white)
                        buyoffer_route_text_pods.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_SELECT -> {
                        // title left, right : NONE, DONE
                        toolbar.toolbar_done_btn.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setListener() {

        // action : move previous step
        appbar_bof_route_wizard.toolbar_left_btn.setSafeOnClickListener {
            moveBackFragment()
        }

        // action : finish activity
        appbar_bof_route_wizard.toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TO_BOF_CLOSE)
        }

        /**
         * "Want save your changes ?"
         * "You will lose your changes if you continue without saving them"
         * "DISCARD" | "SAVE"
         * action : finish activity
         */
        appbar_bof_route_wizard.toolbar_done_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TO_BOF_DONE)
        }

        /**
         * Service Lane, POLs, PODs 에서 search popup 처리
         */
        et_search.setSafeOnClickListener {
            when(currentStep) {
                STEP_LANE -> {
                    viewModel.inPuts.requestSearchLanePopup(Parameter.EVENT)
                }
                STEP_POL -> {
                    viewModel.inPuts.requestSearchPolPopup(Parameter.EVENT)
                }
                STEP_POD -> {
                    viewModel.inPuts.requestSearchPodPopup(Parameter.EVENT)
                }
                else -> { }
            }
        }

        et_search.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showHideKeypad(true)
                v.performClick()
            }
            false
        }

        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (currentStep == STEP_RECENTLY) {
                    searchFilter(false, p0.toString())
                }
            }
        })

        iv_servicelane_cancel.setSafeOnClickListener {
            // STEP_LANE : init scheduleDataPair, init BottomButton
            viewModel.inPuts.requestSearchRemove(servicelane_code.text.toString())
        }

        // select, skip button
        btn_select_skip.setOnClickListener {
            setNextStatus()
        }
    }

    private fun setAppBarExpanded(isExpanded: Boolean) {
        appbar_bof_route_wizard.setExpanded(isExpanded)
    }

    private fun requestGoToOtherStep(pair: Pair<Int, Any?>) {
        viewModel.inPuts.requestGoToOtherStep(pair)
    }

    private fun searchFilter(isCheck: Boolean, filterString: String) {
        viewModel.inPuts.requestSearchFilter(Pair(isCheck, filterString))
    }

    private fun searchUiInit() {
        viewModel.inPuts.requestSearchInit(Parameter.EVENT)
    }

    private fun moveBackFragment() {
        clickViewParameterClick(ParameterClick.CLICK_TO_BOF_BACK)
    }

    private fun requestSaveToPreference(isFinish: Boolean) {
        viewModel.inPuts.requestSaveToPreference(isFinish)
    }

    /**
     * Previous, Back button action (step 이동)
     */
    private fun setBackStatus() {
        with(wizard_stepper) {
            if (isFirstStep()) {
                finish()
            } else {

                var moveStep = getCurrentStep() - 1

                if (currentStep == STEP_SELECT) {
                    /**
                     * Recently("SELECT") > Select > Back > Recently : podScheduleDataPair.second == null
                     * Recently("SKIP")   > Lane > Pol > Pod > Select > Back > Recently
                     */
                    if (isRecentToSelect()) {
                        moveStep = STEP_RECENTLY
                    }
                    requestGoToOtherStep(Pair(moveStep, null))
                } else {
                    requestGoToOtherStep(Pair(moveStep, null))
                }

                initStepData(moveStep)
            }
        }
    }

    /**
     * Bottom button action : SELECT, SKIP, NEXT
     */
    private fun setNextStatus() {
        when(currentStep) {
            STEP_RECENTLY -> {
                /**
                 * select button : <true, offer> : Recently > Select
                 * skip button   : <false, null> : Recently > Lane
                 */
                if (recentlyRouteDataPair.first) {
                    requestGoToOtherStep(Pair(STEP_SELECT, recentlyRouteDataPair.second))
                    // isLoadOnly : true - Recently > Select > Lane 을 위해 미리 schedule data load
                    viewModel.inPuts.requestLoadScheduleData(true)
                } else {
                    // Go to Lane step
                    viewModel.inPuts.requestLoadScheduleData(false)
                }
            }
            STEP_LANE -> {
                // "SELECT" - second is List<Schedule>
                requestGoToOtherStep(Pair(STEP_POL, laneScheduleDataPair.second))
                // move to pol after remove checked item
                viewModel.inPuts.requestSearchRemove(servicelane_code.text.toString())
            }
            STEP_POL -> {
                // "SELECT" - second is List<Schedule>
                requestGoToOtherStep(Pair(STEP_POD, polScheduleDataPair.second))
                // move to pod after remove checked item
                viewModel.inPuts.requestStepPolInitList(Parameter.EVENT)
            }
            STEP_POD -> {
                // "SELECT" - second is List<Schedule>
                requestGoToOtherStep(Pair(STEP_SELECT, podScheduleDataPair.second))
                // move to select after remove checked item
                viewModel.inPuts.requestStepPodInitList(Parameter.EVENT)
            }
            STEP_SELECT -> {
                // Go to set buy volume
                viewModel.inPuts.requestGoToBuyVolume(selectOfferDataPair.second as Offer)
            }
        }
    }

    /**
     * Fragment 에서 item select 또는 초기화 한 경우 ui refresh
     */
    private fun setDataStatus(pair: Pair<Int, Any?>) {
        when(pair.first) {
            // RecentFragment 에서 Route 를 select, unSelect 한 경우
            STEP_RECENTLY -> {
                pair.second?.let { routeData ->
                    /**
                     * select   : <true, offer>
                     * unSelect : <false, null>
                     */
                    recentlyRouteDataPair = routeData as Pair<Boolean, Offer>
                    if (recentlyRouteDataPair.first) {
                        showHideKeypad(false)
                        btn_select_skip.text = getString(R.string.select)
                    } else {
                        btn_select_skip.text = getString(R.string.skip)
                    }
                    // button 은 항상 enabled
                    btn_select_skip.isEnabled = true
                }
            }
            STEP_LANE -> {
                pair.second?.let { scheduleData ->
                    laneScheduleDataPair = scheduleData as Pair<Boolean, List<Schedule>>
                    btn_select_skip.text = getString(R.string.select)
                    btn_select_skip.isEnabled = scheduleData.first
                    ll_search.visibility = if (scheduleData.first) View.GONE else View.VISIBLE
                    ll_select.visibility = if (scheduleData.first) View.VISIBLE else View.GONE
                }
            }
            STEP_POL -> {
                pair.second?.let { scheduleData ->
                    polScheduleDataPair = scheduleData as Pair<Boolean, List<Schedule>>
                    btn_select_skip.text = getString(R.string.select)
                    btn_select_skip.isEnabled = scheduleData.first
                }
            }
            STEP_POD -> {
                pair.second?.let { scheduleData ->
                    podScheduleDataPair = scheduleData as Pair<Boolean, List<Schedule>>
                    btn_select_skip.text = getString(R.string.select)
                    btn_select_skip.isEnabled = scheduleData.first
                }
            }
            STEP_SELECT -> {
                pair.second?.let { offerData ->
                    selectOfferDataPair = offerData as Pair<Boolean, Offer>
                    btn_select_skip.text = getString(R.string.next)
                    // button 은 항상 enabled
                    btn_select_skip.isEnabled = true
                }
            }
            else -> { }
        }
    }

    /**
     * lane, pol, pod step 이동 시 data 초기화
     */
    private fun initStepData(moveStep: Int) {
        when(moveStep) {
            STEP_LANE -> { laneScheduleDataPair = Pair(false, null) }
            STEP_POL -> { polScheduleDataPair = Pair(false, null) }
            STEP_POD -> { podScheduleDataPair = Pair(false, null) }
            else -> { }
        }
    }

    private fun showHideKeypad(isShow: Boolean) {
        if (isShow) {
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            im.showSoftInput(et_search, 0)
        } else {
            im.hideSoftInputFromWindow(et_search.windowToken, 0)
        }
    }

    /**
     * podScheduleDataPair.second == null : Recent > Select (Back) > Recent
     * podScheduleDataPair.second != null : Recent > Lane > Pol > Pod > Select (Back) > Pod
     */
    private fun isRecentToSelect() = podScheduleDataPair.second == null

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
                    requestSaveToPreference(true)
                } else {
                    finish()
                }
            }
        })
        dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
    }

    companion object {
        // Step
        const val STEP_RECENTLY = 0
        const val STEP_LANE = 1
        const val STEP_POL = 2
        const val STEP_POD = 3
        const val STEP_SELECT = 4
        const val TOTAL_STEP = 5
    }
}