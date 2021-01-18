package com.cyberlogitec.freight9.ui.selloffer

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.PushEventType
import com.cyberlogitec.freight9.lib.model.Message
import com.cyberlogitec.freight9.lib.model.RxBusEvent
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.RxBus
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.FragmentStepper
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.StepsManager
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_selloffer_wizard.*
import kotlinx.android.synthetic.main.appbar_progress_sof.*
import kotlinx.android.synthetic.main.appbar_sof_volume.toolbar
import kotlinx.android.synthetic.main.appbar_sof_wizard.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofWizardViewModel::class)
class SofWizardActivity : BaseActivity<SofWizardViewModel>(),
        OnFragmentInteractionListener, StepsManager {

    private var currentStep: Int = STEP_VOLUME
    private var isOfferSummaryRefreshByBackKey: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("f9: onCreate")

        setContentView(R.layout.act_selloffer_wizard)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")
        if (isOfferSummaryRefreshByBackKey()) {
            Timber.d("f9: RxBusEvent.EVENT_OFFER_REFRESH")
            RxBus.publish(RxBusEvent(RxBusEvent.EVENT_OFFER_REFRESH, Message(0)))
        }
    }

    override fun onBackPressed() {
        Timber.v("f9: onBackPressed")
        moveBackFragment()
    }

    /**
     * 전체 step 수 리턴
     */
    override fun getCount(): Int {
        return TOTAL_STEP
    }

    /**
     * step 별 이동될 fragment 리턴
     */
    override fun getStep(position: Int): RxFragment {
        return when (position) {
            STEP_VOLUME -> SofWizardVolumeFragment(viewModel)
            STEP_PRICE -> SofWizardPriceFragment(viewModel)
            STEP_PLAN -> SofWizardPlanFragment(viewModel)
            STEP_MAKE -> SofWizardMakeFragment(viewModel)
            else -> SofWizardVolumeFragment(viewModel)
        }
    }

    override fun onFragmentInteraction() {
        if (wizard_stepper.isLastStep()) {
            showToast("Go Next activity")
        } else {
            wizard_stepper.goToNexStep()
        }
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts: onClickMake
        viewModel.outPuts.onClickMake()
                .bindToLifecycle(this)
                .subscribe { offerDiscard ->
                    if (offerDiscard) {
                        // rxEventOfferRefresh(OfferCreated Push 수신시) 로 YourOffersActivity refresh
                        setIsOfferSummaryRefreshByBackKey(false)
                        finish()
                    } else {
                        startActivityWithFinish(Intent(this, MarketActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }

        // condition detail
        viewModel.outPuts.onClickConditionDetail()
                .bindToLifecycle(this)
                .subscribe { contract ->
                    Timber.d("f9: startActivity(SofConditionTableAct)")
                    startActivity(Intent(this, SofConditionTableAct::class.java)
                            .putExtra(Intents.MSTR_CTRK, contract))
                }

        // whole route
        viewModel.outPuts.onClickWholeRoute()
                .bindToLifecycle(this)
                .subscribe { contract ->
                    Timber.d("f9: startActivity(SofRouteTableAct)")
                    startActivity(Intent(this, SofRouteGridAct::class.java)
                            .putExtra(Intents.MSTR_CTRK, contract))
                }

        // price table
        viewModel.outPuts.onClickPriceTable()
                .bindToLifecycle(this)
                .subscribe { contract ->
                    Timber.d("f9: startActivity(SofPriceTableAct) -> $contract")
                    startActivity(Intent(this, SofPriceTableAct::class.java)
                            .putExtra(Intents.MSTR_CTRK, contract))
                }

        // DONE btn enable
        viewModel.outPuts.onSetDoneBtn()
                .bindToLifecycle(this)
                .subscribe { isEnable ->
                    appbar_sof_wizard.toolbar_done_btn.isEnabled = isEnable
                }

        // Fragment 에서 NEXT btn 눌렀을 때
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

        // Title 에서 "<" 버튼, 하단의 back 버튼 눌렀을 때
        viewModel.outPuts.onClickBack()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        with(wizard_stepper) {
                            if (isFirstStep()) {
                                finish()
                            } else {
                                viewModel.inPuts.requestGoToOtherStep(Pair(getCurrentStep() - 1, null))
                            }
                        }
                    }
                }

        viewModel.outPuts.onClickCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofXXXXXCheckAct)")
                    it.let { pair ->
                        val intent = Intent()
                        when(pair.first) {
                            CHECK_VOLUME -> {
                                intent.setClass(this, SofVolumeCheckAct::class.java)
                            }
                            CHECK_PRICE -> {
                                intent.setClass(this, SofPriceCheckAct::class.java)
                            }
                            else -> {
                                intent.setClass(this, SofConditionCheckAct::class.java)
                            }
                        }
                        pair.second?.let { contract ->
                            intent.putExtra(Intents.MSTR_CTRK, contract)
                            RxActivityResult(this)
                                    .start(intent)
                                    .subscribe (
                                            { result ->
                                                if (result.isOk) {
                                                    if (result.data.hasExtra(Intents.SELL_OFFER_STEP)) {
                                                        val otherStep = result.data.getIntExtra(Intents.SELL_OFFER_STEP)
                                                        viewModel.inPuts.requestGoToOtherStep(Pair(otherStep, null))
                                                    }
                                                } else {
                                                    Timber.d("f9: NotOK --> resultCode: ${result.resultCode}")
                                                }
                                            },
                                            { throwable ->
                                                viewModel.error.onNext(throwable)
                                            }
                                    )
                        }
                    }
                }

        // Offer Discard api 호출 결과
        viewModel.baseOfferOutputs.onSuccessDiscardOffer()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: Discard Offer result : $it")
                        if (it.isSuccessful) {
                            setIsOfferSummaryRefreshByBackKey(true)
                            viewModel.baseOfferInputs.requestLongProgressBar(Parameter.EVENT)
                            Timber.d("f9: showLoadingDialog() 10 sec")
                        } else {
                            finish()
                        }
                    }
                }

        // 10초 pregress bar emitted 인 경우(Cancel 처리에 대한 결과의 Push 수신이 10초 동안 없는 경우)
        viewModel.baseOfferOutputs.onRequestLongProgressBar()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() 10 sec")
                    if (loadingDialog.isShowing()) {
                        loadingDialog.dismiss()
                        finish()
                    }
                }

        // Event Service 에서 Offer Discard 에 대한 결과를 Push 로 수신하는 경우 실행됨 (refresh is true!)
        viewModel.rxEventOfferDiscard
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { message ->
                        Timber.d("f9: rxEventOfferDiscard = ${message.toJson()}")
                        // 10초 pregress bar 강제 hide !!!
                        loadingDialog.dismiss()
                        // Discard : Cancel 처리 성공, 실패 모두인 경우
                        // YourOffersActivity 화면으로 이동 후 refresh
                        if (message.eventType.equals(PushEventType.EVENT_TYPE_OFFER_CANCELED_PASS, true)) {
                            // Discard 처리 성공인 경우 intent 의 OFFER_DISCARD 를 false 로 설정한 후 refresh 를 다시 호출
                            viewModel.inPuts.requestCallRefreshAfterDiscard(Parameter.EVENT)
                        } else {
                            // Discard : Cancel 처리 실패 및 기타 인 경우
                            finish()
                        }
                    }
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

    private fun initData() {

    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        defaultbarInit(appbar_sof_wizard, menuType = MenuType.DONE, title = getString(R.string.selloffer_wizard_volume))
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
        wizard_stepper.stepsChangeListener = object : FragmentStepper.StepsChangeListener {
            override fun onStepChanged(stepNumber: Int) {
                currentStep = stepNumber
                rl_sof_wizard_progress.visibility = View.VISIBLE
                selloffer_dot_volume.setImageResource(R.drawable.blue_circle_8_8)
                selloffer_dot_price.setImageResource(R.drawable.blue_circle_8_8)
                selloffer_dot_plan.setImageResource(R.drawable.blue_circle_8_8)
                selloffer_text_volume.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                selloffer_text_price.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                selloffer_text_plan.setTextAppearance(R.style.txt_opensans_r_10_greyishbrown)
                when(stepNumber) {
                    STEP_VOLUME -> {
                        toolbar.toolbar_title_text.text = getString(R.string.selloffer_wizard_volume)
                        selloffer_dot_volume.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_text_volume.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_PRICE -> {
                        toolbar.toolbar_title_text.text = getString(R.string.selloffer_wizard_price)
                        selloffer_dot_volume.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_dot_price.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_text_volume.setTextAppearance(R.style.txt_opensans_b_10_white)
                        selloffer_text_price.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_PLAN -> {
                        toolbar.toolbar_title_text.text = getString(R.string.selloffer_wizard_plan)
                        selloffer_dot_volume.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_dot_price.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_dot_plan.setImageResource(R.drawable.white_circle_16_16)
                        selloffer_text_volume.setTextAppearance(R.style.txt_opensans_b_10_white)
                        selloffer_text_price.setTextAppearance(R.style.txt_opensans_b_10_white)
                        selloffer_text_plan.setTextAppearance(R.style.txt_opensans_b_10_white)
                    }
                    STEP_MAKE -> {
                        toolbar.toolbar_title_text.text = getString(R.string.offer_make_new_sell_offer)
                        rl_sof_wizard_progress.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Widget Click
     */
    private fun setListener() {
        appbar_sof_wizard.toolbar_left_btn.setOnClickListener{
            Timber.d("f9: toolbar_left_btn clcick")
            moveBackFragment()
        }

        appbar_sof_wizard.toolbar_done_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_done_btn")
            showSaveDialog()
        }
    }

    /**
     * back key 선택 시  offers 에서 진입한 경우 refresh 유무 체크
     */
    private fun isOfferSummaryRefreshByBackKey() = isOfferSummaryRefreshByBackKey

    private fun setIsOfferSummaryRefreshByBackKey(isOfferSummaryRefreshByBackKey: Boolean) {
        this.isOfferSummaryRefreshByBackKey = isOfferSummaryRefreshByBackKey
    }

    private fun moveBackFragment() {
        viewModel.inPuts.clickToBack(Parameter.CLICK)
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

    companion object {
        // Step
        const val STEP_VOLUME = 0
        const val STEP_PRICE = 1
        const val STEP_PLAN = 2
        const val STEP_MAKE = 3
        const val TOTAL_STEP = 4

        // Check
        const val CHECK_VOLUME = 0
        const val CHECK_PRICE = 1
        const val CHECK_PLAN = 2
    }
}