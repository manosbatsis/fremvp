package com.cyberlogitec.freight9.ui.buyorder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType.TYPE_NULL
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.service.Actions
import com.cyberlogitec.freight9.lib.service.EventService
import com.cyberlogitec.freight9.lib.service.ServiceState
import com.cyberlogitec.freight9.lib.service.getServiceState
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.ui.enums.*
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_buy_order.*
import kotlinx.android.synthetic.main.appbar_order.*
import kotlinx.android.synthetic.main.body_batch.*
import kotlinx.android.synthetic.main.body_buy_order.*
import kotlinx.android.synthetic.main.body_buy_order_buy.*
import kotlinx.android.synthetic.main.body_buy_order_confirm.*
import kotlinx.android.synthetic.main.bottom_batch_input.*
import kotlinx.android.synthetic.main.bottom_btn_floating.*
import kotlinx.android.synthetic.main.item_buy_order.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_order.*
import retrofit2.Response
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


@RequiresActivityViewModel(value = BuyOrderViewModel::class)
class BuyOrderActivity : BaseActivity<BuyOrderViewModel>() {

    private lateinit var layoutView: View
    private lateinit var im: InputMethodManager
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    private var containerSpinList = mutableListOf<TextItem>()

    private lateinit var borList: BorList
    private lateinit var routeDataList: RouteDataList
    private lateinit var orderTradeOfferDetail: OrderTradeOfferDetail
    private lateinit var tradeOfferWrapper: TradeOfferWrapper

    private var is40Ft = false

    private var fullContainerNameSimple = EmptyString
    private var periodAndVolumeContainerName = EmptyString
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    private var wholePartialMode: WholePartialMode = WholePartialMode.ModePartial
    private var batchInputMode: BatchInputMode = BatchInputMode.ModeBatchInputNo

    private var adapterCurrentPosition = 0
    private var initConfirmMode: Boolean = true
    private var budgetValue = 99_999_999
    private var selectedContainerIndex = 0
    private var buyDataOwnerPointerId = EmptyString
    private var inventoryClose = false
    private var actionEnable = false

    private val confirmAdapter by lazy {
        ConfirmRecyclerAdapter()
                .apply {
                    onUiListener = { listenerKind, position, isFocus, value ->
                        when (listenerKind) {
                            ListenerKind.LISTENER_SUM -> {
                                processSum()
                            }
                            ListenerKind.LISTENER_VOLUME -> {
                                processVolume(position ?: 0, value)
                            }
                            ListenerKind.LISTENER_INPUTNO_MODE -> {
                                processInputNoMode()
                            }
                            ListenerKind.LISTENER_KEYPAD -> {
                                processKeypad(position ?: -1, isFocus ?: false, value ?: EmptyString)
                            }
                        }
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        layoutView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.act_buy_order, null)
        setContentView(layoutView)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onDestroy() {
        keyboardVisibilityUtils.detachKeyboardListeners()
        super.onDestroy()
    }

    override fun onBackPressed() {
        showEditTextKeypad(false)
        if (!initConfirmMode) {
            // buy screen 인 경우 back 했을 때 처리
            initConfirmOrBuyView(true, true)
        } else {
            // confirm screen 인 경우 back 했을 때 처리
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }

    private fun finishWithResult(isOk: Boolean) {
        setResult(if (isOk) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * activity data init
     */
    private fun initData() {
        setActionEnable(false)
        currencyFormat.minimumFractionDigits = 0
        im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
                onShowKeyboard = { _, visibleDisplayFrameHeight ->
                    setScrollHeight(
                            visibleDisplayFrameHeight,
                            adapterCurrentPosition,
                            ReferencedCalcHeightLayout(
                                    // Batch Input layouts
                                    ll_batch, ll_batch_input, chk_batch_input,
                                    // Recycler layouts
                                    ll_volume_set, recycler_volume,
                                    // Floating layouts
                                    ll_inventory_floating, ll_quantity_input_floating,
                                    // NestedScrollView
                                    sv_body_root
                            )
                    )
                },
                onHideKeyboard = {
                    processKeypad(adapterCurrentPosition)
                })
    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_order, menuType = MenuType.DEFAULT, title = getString(R.string.act_buy_order))
        toolbar_right_btn.visibility = View.GONE
        toolbar_right_tv.visibility = View.GONE

        // Batch UI 초기화
        chk_batch.isChecked = false
        btn_order_make.text = getString(R.string.make_buy_order)

        requestOfferInfo()
        recyclerViewInit()
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {

        viewModel.outPuts.onSuccessRequestBuyDataOwnrPtrId()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onSuccessRequestBuyDataOwnrPtrId : it = $it")
                        buyDataOwnerPointerId = it
                    }
                }

        viewModel.outPuts.onSuccessRouteDataList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { routeDataList ->
                        this.routeDataList = routeDataList
                    }
                }

        // Offer info
        viewModel.outPuts.onSuccessRequestOfferInfo()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onSuccessRequestOfferInfo : it = $it")
                        borList = it
                        // Pol, Pos UI 업데이트
                        setPolPodValue()
                    }
                }

        // Offer detail info : OrderTradeOfferDetail dto
        viewModel.outPuts.onSuccessRequestOfferInfoDetails()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onSuccessRequestOfferInfoDetails : it = $it")
                        processOfferInfoDetails(it)
                    }
                    initConfirmOrBuyView(true)
                }

        viewModel.outPuts.onSuccessRequestBuyOrderSave()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: Save result : $it")
                        if (it.isSuccessful) {
                            doEventService(Actions.PUSH_NOTIFICATION, it.body() as Message)
                            finishWithResult(true)
                        } else {
                            showToast("Fail Save Offer(Http)\n" + it.errorBody())
                            finishWithResult(false)
                        }
                    }
                }

        //------------------------------------------------------------------------------------------

        // etc : java.net.SocketTimeoutException: failed to connect
        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    Timber.e("f9: error : $it")
                    showToast("Fail (Throwable)\n" + it.message)
                    finishWithResult(false)
                }

        //------------------------------------------------------------------------------------------

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        if (::tradeOfferWrapper.isInitialized) {

                            if (!isActionEnable()) {
                                return@subscribe
                            }

                            if (chk_batch_input.isChecked) {
                                // EditText 에 Focus 표시 안함.
                                setBatchInputEditTextIsNormal(
                                        et_batch_input,
                                        isNormal = true,
                                        isValueSet = true,
                                        value = et_batch_input.text.toString()
                                )
                            }

                            // Keypad 표시 안함
                            showEditTextKeypad(false)

                            when (parameterClick) {
                                ParameterClick.CLICK_CONDITION_DETAIL -> {
                                    showDetailConditionPopup(root_order, viewModel, tradeOfferWrapper)
                                }
                                ParameterClick.CLICK_WHOLE_ROUTE -> {
                                    if (::routeDataList.isInitialized and ::borList.isInitialized) {
                                        showWholeRoutePopup(root_order, routeDataList, borList)
                                    }
                                }
                                ParameterClick.CLICK_PRICE_TABLE -> {
                                    showPriceTablePopup(root_order, tradeOfferWrapper, false)
                                }
                                ParameterClick.CLICK_TERMS_MORE -> {
                                    showTermsMorePopup(root_order)
                                }
                                ParameterClick.CLICK_PAY_PLAN -> {
                                    // Pay Plan
                                    showPayPlanPopup(root_order, viewModel, tradeOfferWrapper)
                                }
                                ParameterClick.CLICK_PERIOD_AND_VOLUME -> {
                                    showPeriodAndVolumePopup(root_order,
                                            periodAndVolumeContainerName,
                                            confirmAdapter.datas,
                                            ::onPeriodAndVolumeEdit)
                                }
                                ParameterClick.CLICK_SPIN_CONTAINER_TYPE -> {
                                    // MVP : Container spin 동작 없음
                                    //showContainerTypeDialog()
                                    return@subscribe
                                }
                                ParameterClick.CLICK_TITLE_RIGHT_TV -> {
                                    showCancelDialog()
                                }
                                ParameterClick.CLICK_MAKE_BTN -> {
                                    processOrderSaveOrGoSellOffer()
                                }
                                ParameterClick.CLICK_CONFIRM_BTN -> {
                                    initConfirmOrBuyView(false)
                                }
                                ParameterClick.CLICK_TITLE_LEFT -> {
                                    onBackPressed()
                                }
                                ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                    startActivity(MenuActivity::class.java)
                                }
                                ParameterClick.CLICK_QUANTITY_ENTER_BTN -> {
                                    processQuantityEnter()
                                }
                                ParameterClick.CLICK_CLOSE -> {
                                    inventoryClose = true
                                    ll_inventory_floating.visibility = View.GONE
                                    // floating height 다시 계산
                                    showLayoutByKeypad(true, getReferencedShowKeypadLayout())
                                }
                                ParameterClick.CLICK_CARRIER_MORE -> {
                                    showToast("More Function : Pending")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        sharePermissionCheck()
                                    }
                                }
                                ParameterClick.CLICK_JUMP_TO_OTHERS -> {
                                    if (btn_order_confirm.isEnabled) {
                                        processOrderSaveOrGoSellOffer(false)
                                    }
                                }
                                else -> { }
                            }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->

                        if (!isActionEnable()) {
                            return@subscribe
                        }

                        with(parameterPair) {
                            when (first) {
                                ParameterAny.ANY_BATCH_INPUT_ET -> {
                                    val secondValue = second as Boolean
                                    if (secondValue and chk_batch_input.isChecked) {
                                        setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = false)
                                        processKeypad(-1, secondValue, getVolumeBatchInputData())
                                    }
                                }
                                ParameterAny.ANY_TERM_AGREE -> {
                                    btn_order_make.isEnabled = second as Boolean
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        // Go Sell Offer 인 경우
        viewModel.onSuccessRequestOfferUsingSameCondition
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { triple ->

                        val offerType = triple.first
                        val offerDiscard = triple.second
                        val offer = triple.third

                        Timber.d("f9: Request Same Condition Offer : offerType = $offerType, discard = $offerDiscard, offer = $offer")

                        // Has Intents.OFFER extra
                        RxActivityResult(this@BuyOrderActivity)
                                .start(Intent(this, SofWizardActivity::class.java)
                                        .putExtra(Intents.MSTR_CTRK_NR, tradeOfferWrapper.orderTradeOfferDetail.masterContractNumber)
                                        .putExtra(Intents.OFFER, offer)
                                        .putExtra(Intents.OFFER_DISCARD, offerDiscard)
                                        .putExtra(Intents.OFFER_BY_MADE_CONDITION, true)
                                        .putExtra(Intents.OFFER_MAKE_STEP, true))
                                .subscribe(
                                        { result ->
                                            finishWithResult(true)
                                        },
                                        { throwable ->
                                            viewModel.error.onNext(throwable)
                                            finishWithResult(false)
                                        }
                                )
                    }
                }
    }

    /**
     * total sum 과 balance 체크 후 confirm button status 설정
     */
    private fun setButtonEnable(isTotalSumOk: Boolean, isBalanceOk: Boolean) {
        val isEnable = isTotalSumOk && isBalanceOk
        btn_order_confirm.isEnabled = isEnable

        var btnOrderConfirmText = getString(R.string.confirm)
        if (!isEnable) btnOrderConfirmText = getString(if (isBalanceOk) R.string.confirm else R.string.shortage_of_balance)
        btn_order_confirm.text = btnOrderConfirmText

        val textColorInShortage = if (isBalanceOk) R.color.greyish_brown else R.color.orangey_red
        tv_buy_order_deal_amount_balance_title.setTextColor(ContextCompat.getColor(this, textColorInShortage))
        tv_buy_order_deal_amount_balance_value.setTextColor(ContextCompat.getColor(this, textColorInShortage))

        tv_buy_order_make_a_new_sell_offer_using_this_condition.isEnabled = isEnable
        val textColor = if (isEnable) R.color.blue_violet else R.color.greyish_brown
        tv_buy_order_make_a_new_sell_offer_using_this_condition.setTextColor(getColor(textColor))
        val background = if (isEnable) R.drawable.bg_round_corner_blue_violet else R.drawable.bg_round_corner_greyish_brown_fill_pale
        tv_buy_order_make_a_new_sell_offer_using_this_condition.setBackgroundResource(background)
    }

    private fun setActionEnable(isEnable: Boolean) {
        actionEnable = isEnable
    }

    private fun isActionEnable(): Boolean = actionEnable

    /**
     * Confirm, MakeBuyOrder mode 변경 시 look change
     */
    private fun initConfirmOrBuyView(initConfirmMode: Boolean, onBackPressed: Boolean = false) {
        this.initConfirmMode = initConfirmMode
        ll_buy_order_confirm.visibility = if (this.initConfirmMode) View.VISIBLE else View.GONE
        ll_buy_order_buy.visibility = if (this.initConfirmMode) View.GONE else View.VISIBLE
        btn_order_confirm.visibility = if (this.initConfirmMode) View.VISIBLE else View.GONE
        btn_order_make.visibility = if (this.initConfirmMode) View.GONE else View.VISIBLE

        toolbar_right_tv.visibility = if (this.initConfirmMode) View.GONE else View.VISIBLE
        // For MVP : gone 처리
        ll_buy_order_make_a_new_sell_offer_using_this_condition.visibility = View.VISIBLE
        //ll_buy_order_make_a_new_sell_offer_using_this_condition.visibility = if (this.initConfirmMode) View.VISIBLE else View.GONE

        // edit disable 처리. focus 는 enable
        et_batch_input.isFocusable = true
        et_batch_input.isFocusableInTouchMode = true
        et_batch_input.inputType = TYPE_NULL

        if (initConfirmMode) {
            if (!onBackPressed) {
                setConfirmViewInit()
            }
        } else {
            setBuyViewInit()
        }
        sv_body_root.smoothScrollTo(0, 0)
    }

    /**
     * Market 에서 전달받은 offer 정보로 offer detail 요청
     */
    private fun requestOfferInfo() = viewModel.inPuts.requestOfferInfo(intent)

    /**
     * Make Buy Order 요청
     */
    private fun requestBuyOrderSave(orderTradeOfferDetail: OrderTradeOfferDetail) {
        viewModel.inPuts.requestBuyOrderSave(orderTradeOfferDetail)
    }

    /**
     * 40ft 유무 return
     */
    private fun getInitIsForTUnit() = is40Ft

    /**
     * Market으로 부터 전달 받은 offer item 정보로 UI card 에 pol, pod 정보 표시
     */
    private fun setPolPodValue() {
        with(borList) {
            // wholeYn (Partial, Whole)
            wholePartialMode = if (wholeYn == ConstantTradeOffer.ALL_YN_PARTIAL) WholePartialMode.ModePartial else WholePartialMode.ModeWhole
            setWholePartialMode(wholePartialMode)

            iv_carrier_logo.setImageResource(cryrCd.getCarrierIcon(false))
            tv_carrier_name.text = getCarrierCode(cryrCd)
            tv_carrier_count.text = carrierCount.getCodeCount()

            tv_pol_name.text = locPolCd
            tv_pol_count.text = locPolCnt.getCodeCount()
            tv_pol_desc.text = locPolNm

            tv_pod_name.text = locPodCd
            tv_pod_count.text = locPodCnt.getCodeCount()
            tv_pod_desc.text = locPodNm

            // TODO : Period set
            tv_period.text = EmptyString       // W01-W10
            // icon 표시하지 않음
            iv_period_whole.visibility = View.INVISIBLE
        }
        checkIs40Ft(borList)
    }

    /**
     * TODO : 40ft 유무 체크 (현재 20ft 를 defalut 처리)
     */
    private fun checkIs40Ft(borList: BorList): Boolean {
        // US, CA 지역은 40'
        if (borList.locPolCd!!.startsWith("us", true) || borList.locPolCd!!.startsWith("ca", true)) is40Ft = true
        else is40Ft = borList.locPodCd!!.startsWith("us", true) || borList.locPodCd!!.startsWith("ca", true)

        // TODO : 일단 20Ft 를 Default 로 한다.
        // 추후 관련 컬럼 추가될 예정임.
        is40Ft = false
        return is40Ft
    }

    /**
     * Confirm screen 초기화
     */
    private fun setConfirmViewInit() {
        tv_buy_order_container_type.text = containerSpinList[selectedContainerIndex]._value
        /**
         * TODO : fullDryContainerNameSimple
         *   노선별로 정해져 있는 Full dry 20' 를 기준으로 한다.(미주인 경우 40')
         *   PRICE TABLE 의 Full container의 주차별 가격???
         *   CY-CY 20ft Dry
         */
        tv_buy_order_total_deal_amount_desc.text = "($fullContainerNameSimple)"
        updateDepositUI()
        recyclerViewInitData()
    }

    /**
     * initial payment payable ui에 ration 표시
     */
    private fun updateDepositUI() {
        val initPaymentRatio = (orderTradeOfferDetail.offerLineItems[0].firstPaymentRatio * 100).toInt()
        tv_buy_order_deal_payment_payable_desc.text =
                getString(R.string.volume_deal_payment_payable_desc, initPaymentRatio)
    }

    /**
     * Buy screen 초기화
     */
    private fun setBuyViewInit() {
        chk_buy_order_terms_agree.isChecked = false
        btn_order_make.isEnabled = false
        tv_buy_order_initial_payment_payable_value.text = tv_buy_order_deal_payment_payable_value.text

        /**
         * TODO : init value
         *
         */

    }

    /**
     * keypad show 처리
     */
    private fun showEditTextKeypad(show: Boolean, value: String = EmptyString) {
        showQuantityEditTextKeypad(im, show, value, getReferencedShowKeypadLayout())
    }

    private fun getReferencedShowKeypadLayout() =
            ReferencedShowKeypadLayout(
                    et_quantity_input_floating, chk_batch_max,
                    ll_bottom_floating, ll_bottom_btn_floating, ll_inventory_floating,
                    sv_body_root
            )

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {

        iv_inventory_close_floating.setOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CLOSE)
        }

        // Batch : check / uncheck
        chk_batch.setOnCheckedChangeListener { _, isChecked ->
            processBatchCheck(isChecked)
        }

        // Batch Input : check / uncheck
        chk_batch_input.setOnCheckedChangeListener { _, isChecked ->
            processBatchInputCheck(isChecked)
        }

        // Batch Max : check / uncheck
        chk_batch_max.setOnCheckedChangeListener { _, isChecked ->
            processBatchMaxCheck(isChecked)
        }

        // Batch Input EditText : hasFocus, hasNotFocus
        et_batch_input.setOnFocusChangeListener { _, hasFocus ->
            clickViewParameterAny(Pair(ParameterAny.ANY_BATCH_INPUT_ET, hasFocus))
        }

        et_quantity_input_floating.setOnEditorActionListener OnEditorActionListener@{ _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.keyCode == KeyEvent.KEYCODE_BACK) {
                processKeypad(-1)
                return@OnEditorActionListener true
            }
            false
        }

        et_quantity_input_floating.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                when (chk_batch_input.isChecked) {
                    true -> {
                        setVolumeBatchInputData(p0.toString())
                    }
                    else -> {
                        val valueInt = if (p0.toString().isEmpty()) 0 else p0.toString().toInt()
                        confirmAdapter.datas[adapterCurrentPosition].volume = valueInt
                        confirmAdapter.notifyItemChanged(adapterCurrentPosition)
                        processSum()
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // batch input enter 처리
        btn_quantity_enter_floating.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_QUANTITY_ENTER_BTN)
        }

        chk_buy_order_period_all.setOnCheckedChangeListener { _, _ ->
            // TODO : do nothing now
        }

        tv_buy_order_make_a_new_sell_offer_using_this_condition.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_JUMP_TO_OTHERS)
        }

        iv_carrier_more.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CARRIER_MORE)
        }

        //------------------------------------------------------------------------------------------

        toolbar_left_btn.setOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
        }

        toolbar_right_btn.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        tv_link_condition_detail.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CONDITION_DETAIL)
        }

        tv_link_whole_route.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_WHOLE_ROUTE)
        }

        tv_price_table.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_PRICE_TABLE)
        }

        ll_buy_order_buy_period_and_volume.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_PERIOD_AND_VOLUME)
        }

        ll_buy_order_buy_pay_plan.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_PAY_PLAN)
        }

        ll_buy_order_container_type.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_SPIN_CONTAINER_TYPE)
        }

        tv_buy_order_terms_more.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TERMS_MORE)
        }

        btn_order_confirm.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_CONFIRM_BTN)
        }

        //------------------------------------------------------------------------------------------

        btn_order_make.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_MAKE_BTN)
        }

        chk_buy_order_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            clickViewParameterAny(Pair(ParameterAny.ANY_TERM_AGREE, isChecked))
        }

        toolbar_right_tv.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_TV)
        }
    }

    /**
     * return batch input value
     */
    private fun getVolumeBatchInputData() = et_batch_input.text.toString()

    /**
     * batch input value 를 UI 에 설정 (over check)
     */
    private fun setVolumeBatchInputData(inputData: Any?) {
        var setValue = EmptyString
        when (inputData) {
            is String -> {
                setValue = if (inputData.isEmpty()) EmptyString else inputData
            }
            is Int -> {
                setValue = if (inputData > 0) inputData.toString() else EmptyString
            }
        }
        overEditTextUi(if (setValue.isEmpty()) 0 else setValue.toInt())
    }

    /**
     * whole, partial ui 표시
     */
    private fun setWholePartialMode(wholePartialMode: WholePartialMode) {
        ll_batch_root.visibility =
                if (wholePartialMode == WholePartialMode.ModeWhole) View.GONE else View.VISIBLE
        chk_buy_order_period_all.visibility =
                if (wholePartialMode == WholePartialMode.ModeWhole) View.GONE else View.INVISIBLE
        view_buy_order_title_dummy.visibility =
                if (wholePartialMode == WholePartialMode.ModeWhole) View.VISIBLE else View.GONE
        tv_buy_order_title_period.gravity = Gravity.CENTER_VERTICAL or
                if (wholePartialMode == WholePartialMode.ModeWhole) Gravity.START else Gravity.END
        confirmAdapter.setWholePartialMode(wholePartialMode)
        callAdapterNotifyChanged()
    }

    /**
     * confirm 화면 adapter refresh : 특정 position의 item 또는 모든 items
     */
    private fun callAdapterNotifyChanged(isItemChanged: Boolean = false, position: Int = -1) {
        if (isItemChanged) {
            Timber.d("f9: callAdapterNotifyDataSetChanged : notifyItemChanged() - $position")
            confirmAdapter.notifyItemChanged(position)
        } else {
            Timber.d("f9: callAdapterNotifyDataSetChanged : notifyDataSetChanged()")
            confirmAdapter.notifyDataSetChanged()
        }
    }

    /**
     * batch input 시 각 주차별로 offerWeekQty를 체크하여 over 유무 판단
     */
    private fun overCheckVolumeBase(value: Int): Boolean {
        // 일부 주차의 volume 만 0 인 항목이 있음. 모든 주차에 대해서 over 인지 check
        val overCount = confirmAdapter.datas.filter { value > 0 && value > it.offerWeekQty }.size
        Timber.d("f9: overCheckVolumeBase = $overCount")
        return overCount == confirmAdapter.datas.size
    }

    private fun overEditTextUi(valueInt: Int) {
        val isOver = overCheckVolumeBase(valueInt)
        setBatchInputEditTextIsOver(et_batch_input, if (valueInt > 0) valueInt.toString() else EmptyString, isOver)
    }

    private fun onPeriodAndVolumeEdit() {
        initConfirmOrBuyView(true)
    }

    /**
     * not used
     */
    @SuppressLint("InflateParams")
    private fun showContainerTypeDialog() {
        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(false)
        dialog.setContentView(view)

        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                selectedContainerIndex = index
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            tv_buy_order_container_type.text = containerSpinList[selectedContainerIndex]._value
            recyclerViewInitData()
        }

        view.picker.setItems(containerSpinList)
        view.picker.index = selectedContainerIndex

        dialog.show()
    }

    /**
     * MVP
     * Cancel popup 노출
     * Cancel : Popup 닫히고 현재 페이지 유지
     * OK : 02_trade market_002 (Ask)로 이동
     */
    private fun showCancelDialog() {
        val dialog = NormalTwoBtnDialog(title = getString(R.string.buy_order_cancel_title),
                desc = getString(R.string.buy_order_cancel_desc_only_order),
                leftBtnText = getString(R.string.cancel),
                rightBtnText = getString(R.string.ok))
        dialog.isCancelable = false
        dialog.setOnClickListener(View.OnClickListener {
            it?.let {
                dialog.dismiss()
                if (it.id == R.id.btn_right) {
                    finishWithResult(false)
                }
            }
        })
        dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
    }

    /**
     * deal total amount 계산 후 return
     */
    private fun getCalcDealAmount(): Int {
        var dealAmount = 0
        with(tradeOfferWrapper) {
            for (data in datas) {
                with(data) {
                    // check가 true 인 항목들
                    dealAmount += (price * if (check) volume else 0)
                }
            }
        }
        return dealAmount
    }

    /**
     * Confirm screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_volume.apply {
            layoutManager = LinearLayoutManager(this@BuyOrderActivity)
            adapter = this@BuyOrderActivity.confirmAdapter
        }
    }

    /**
     * recyclerview 에 표시할 item list 구성
     * Container spin list 변경 시 BuyOrder 의 price, isT 설정
     * 20ft, 40ft 기준으로 단위, 수량 변환 (T <> F)
     */
    private fun makeBuyOrderDatas(): List<OrderData> {
        // RecyclerView title set : T or F
        val teuUnit = if (getInitIsForTUnit()) getString(R.string.teu_unit_40ft) else getString(R.string.teu_unit_20ft)
        tv_title_volume_price.text = getString(R.string.volume_price_unit, teuUnit)
        tv_title_volume_volume.text = getString(R.string.volume_volume_unit, teuUnit)

        if (::tradeOfferWrapper.isInitialized and ::borList.isInitialized) {
            with(tradeOfferWrapper) {
                // 기 입력되었던 volume 값을 map 으로 추출
                val previousDatasMap = datas.groupBy { it.bseYw }

                // selectedContainerIndex 에 맞는 datas 구성
                datas.clear()
                val sortedOfferLineItems = orderTradeOfferDetail.offerLineItems
                        .sortedBy { offerLineItem -> offerLineItem.baseYearWeek }
                for (offerLineItem in sortedOfferLineItems) {
                    if (offerLineItem.offerQty > 0) {
                        datas.add(OrderData(
                                borList.baseYearWeek == offerLineItem.baseYearWeek,               // blink : 임시. Market에서 선택한 주인 경우 true
                                false,
                                wholePartialMode == WholePartialMode.ModeWhole,
                                offerLineItem.baseYearWeek,
                                offerLineItem.offerPrice.toInt(),   // For MVP : Float to Int
                                0,
                                offerLineItem.offerRemainderQty,
                                0,
                                is40Ft))
                    }
                }

                for (data in datas) {
                    for ((key, value) in previousDatasMap) {
                        if (key == data.bseYw) {
                            data.volume = value.first().volume
                        }
                    }
                    data.check = if (wholePartialMode == WholePartialMode.ModeWhole) true else data.volume > 0
                    if (chk_batch_max.isChecked) {
                        data.volume = data.offerWeekQty
                    } else {
                        if (data.volume > data.offerWeekQty) {
                            data.volume = data.offerWeekQty
                        }
                        when (chk_batch_input.isChecked) {
                            true -> {
                                setVolumeBatchInputData(data.volume)
                            }
                        }
                    }
                }
            }
        }

        return tradeOfferWrapper.datas
    }

    /**
     * confrim screen 의 recyclerview, adapter 초기화
     */
    private fun recyclerViewInitData() {
        confirmAdapter.datas.clear()
        confirmAdapter.datas.addAll(makeBuyOrderDatas())
        callAdapterNotifyChanged()

        processSum()
        tv_buy_order_deal_available_account_budget_value.text = currencyFormat.format(budgetValue)
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Confirm screen 의 recyclerview adapter
     */
    private class ConfirmRecyclerAdapter : RecyclerView.Adapter<ConfirmRecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        lateinit var im: InputMethodManager
        private var batchInputMode = BatchInputMode.ModeBatchInputNo
        private var wholePartialMode = WholePartialMode.ModePartial
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val datas = mutableListOf<OrderData>()
        var onUiListener: (ListenerKind, Int?, Boolean?, String?) -> Unit = { _, _, _, _ -> }
        private var previousVolume: Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_buy_order, parent, false))
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                currencyFormat.minimumFractionDigits = 0
                setInitItemView(itemView, position)
                processVolume(itemView, position)
            }
        }

        private fun setInitItemView(itemView: View, position: Int) {
            if (wholePartialMode == WholePartialMode.ModeWhole) {
                itemView.chk_order_period_item.visibility = View.GONE
                itemView.view_buy_order_title_dummy.visibility = View.VISIBLE
                itemView.et_order_volume_input_item.visibility = View.GONE
                itemView.tv_order_volume_input_item.visibility = View.VISIBLE
                itemView.tv_order_week_item.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                // 모든 Text color 는 greyish_brown 으로, background 는 transparent로 표시
                itemView.tv_order_volume_input_item.setTextColor(ContextCompat.getColor(context, R.color.greyish_brown))
            } else {
                val margin = 4.toDp().toInt()
                val params = itemView.ll_order_volume_item_root.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, margin, 0, margin)
                itemView.ll_order_volume_item_root.layoutParams = params

                itemView.chk_order_period_item.visibility = View.VISIBLE
                itemView.view_buy_order_title_dummy.visibility = View.GONE
                itemView.et_order_volume_input_item.visibility = View.VISIBLE
                itemView.tv_order_volume_input_item.visibility = View.GONE
                itemView.tv_order_week_item.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }

            itemView.et_order_volume_input_item.inputType = TYPE_NULL

            with(datas[position]) {
                // 키패드가 사라진경우 max 값 over check 후 over 시 0 으로 설정
                if (!focused) {
                    if (volume > getMaxVolumeBase(position)) {
                        check = false; volume = 0; subTotal = 0
                    }
                }

                setItemViewBackgroundColor(itemView, blink)
                itemView.chk_order_period_item.isChecked = check
                itemView.tv_order_week_item.text = context.getWeek(bseYw)
                itemView.tv_order_price_item.text = currencyFormat.format(price)
                itemView.tv_order_volume_base_item.text = "/" +
                        context.getConvertedTeuValue(offerWeekQty, isT, false)

                var editFocus = false
                if (wholePartialMode == WholePartialMode.ModeWhole) {
                    volume = offerWeekQty
                    itemView.tv_order_volume_input_item.text = volume.toString()
                    calcSubTotal(itemView, position)
                } else {
                    when (batchInputMode) {
                        BatchInputMode.ModeBatchInputNo -> {
                            setVolumeValue(itemView, position)
                            itemView.tv_order_subtotal_item.text = currencyFormat.format(subTotal)
                            editFocus = focused
                        }
                        BatchInputMode.ModeBatchInputYes,
                        BatchInputMode.ModeBatchInputMax -> {
                            calcSubTotal(itemView, position)
                        }
                    }
                }

                var isOver = false
                val etOrderVolumeInputItemBackground =
                        if (editFocus) {
                            if (volume > getMaxVolumeBase(position)) {
                                isOver = true
                                R.drawable.bg_order_edittext_orangey_red_border
                            } else {
                                R.drawable.bg_order_edittext_blue_border
                            }
                        } else {
                            R.drawable.bg_order_edittext_gray_border
                        }
                val etOrderVolumeInputItemTextColor =
                        if (editFocus) {
                            if (volume > getMaxVolumeBase(position)) R.color.orangey_red
                            else R.color.blue_violet
                        } else R.color.greyish_brown
                itemView.et_order_volume_input_item.setBackgroundResource(etOrderVolumeInputItemBackground)
                itemView.et_order_volume_input_item.setTextColor(ContextCompat.getColor(context, etOrderVolumeInputItemTextColor))
                if (isOver) {
                    val shake = AnimationUtils.loadAnimation(context, R.anim.edittext_shake)
                    itemView.et_order_volume_input_item.startAnimation(shake)
                }
            }
        }

        private fun setItemViewBackgroundColor(itemView: View, blink: Boolean) {
            val backgroundColor = ContextCompat.getColor(context, if (blink) R.color.color_f2f2f2 else R.color.white)
            itemView.ll_order_volume_item_root.setBackgroundColor(backgroundColor)
        }

        private fun getMaxVolumeBase(position: Int): Int {
            var maxVolumeBase = 0
            if (position > -1) {
                maxVolumeBase = datas[position].offerWeekQty
            } else {
                for (x in datas) {
                    if (x.offerWeekQty > maxVolumeBase) {
                        maxVolumeBase = x.offerWeekQty
                    }
                }
            }
            return maxVolumeBase
        }

        private fun processVolume(itemView: View, position: Int) {
            itemView.chk_order_period_item.setOnTouchListener { v, event ->
                with(datas[position]) {
                    // 현재 check 상태
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (!check) {
                                var isFocus = false
                                val value: String
                                // uncheck > check
                                when (this@ConfirmRecyclerAdapter.batchInputMode) {
                                    BatchInputMode.ModeBatchInputYes -> {
                                        // et_buy_order_volume_batch_input 의 값으로 채움.(check > Uncheck 시 previousVolume
                                        value = previousVolume.toString()
                                    }
                                    BatchInputMode.ModeBatchInputMax -> {
                                        value = offerWeekQty.toString()
                                    }
                                    else -> {
                                        value = itemView.et_order_volume_input_item.text.toString()
                                        isFocus = true
                                    }
                                }
                                onUiListener(ListenerKind.LISTENER_KEYPAD, position, isFocus, value)
                            } else {
                                // check > uncheck
                                previousVolume = volume
                                volume = 0
                                calcSubTotal(itemView, position)
                                onUiListener(ListenerKind.LISTENER_KEYPAD, position, false, EmptyString)

                                if (this@ConfirmRecyclerAdapter.batchInputMode != BatchInputMode.ModeBatchInputNo) {
                                    onUiListener(ListenerKind.LISTENER_INPUTNO_MODE, position, false, EmptyString)
                                }
                            }
                            v.performClick()
                        }
                        else -> { }
                    }
                }
                true
            }

            // ModeBatchInputNo 인 경우 처리
            itemView.et_order_volume_input_item.setOnTouchListener { v, event ->
                if (this.batchInputMode == BatchInputMode.ModeBatchInputNo) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val value = itemView.et_order_volume_input_item.text.toString()
                            onUiListener(ListenerKind.LISTENER_KEYPAD, position, true, value)
                            v.performClick()
                        }
                    }
                }
                true
            }
        }

        private fun calcSubTotal(itemView: View, position: Int) {
            with(datas[position]) {
                subTotal = (price * if (check) volume else 0)
                setVolumeValue(itemView, position)
                itemView.tv_order_subtotal_item.text = currencyFormat.format(subTotal)
                onUiListener(ListenerKind.LISTENER_SUM, null, null, null)
            }
        }

        private fun setVolumeValue(itemView: View, position: Int) {
            itemView.et_order_volume_input_item.setText(if (datas[position].volume > 0) "${datas[position].volume}" else EmptyString)
            onUiListener(
                    ListenerKind.LISTENER_VOLUME,
                    position,
                    false,
                    itemView.et_order_volume_input_item.text.toString()
            )
        }

        fun setWholePartialMode(wholePartialMode: WholePartialMode) {
            this.wholePartialMode = wholePartialMode
        }

        // InputMode 변경 시 초기화
        fun setBatchData(batchInputMode: BatchInputMode, value: Int = 0) {
            this.previousVolume = 0
            this.batchInputMode = batchInputMode
            for (item in datas) {
                item.focused = false
                if (batchInputMode == BatchInputMode.ModeBatchInputMax) {
                    // volume 이 0 보다 큰 주차들에 대해서만
                    if (item.offerWeekQty > 0) {
                        item.check = true
                        item.volume = item.offerWeekQty
                    }
                } else if (batchInputMode == BatchInputMode.ModeBatchInputYes) {
                    item.check = value > 0
                    item.volume = value
                }
            }
        }

        fun getCalcTotal(): Int {
            var total = 0
            for (item in datas) {
                with(item) {
                    // check가 true 인 항목들
                    subTotal = (price * if (check) volume else 0)
                    total += subTotal
                }
            }
            return total
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Whole Route 에서 사용
     */
    //private fun getWholeRoutesWrappers() = wholeRoutesWrappers
    //private var wholeRoutesWrappers = mutableListOf<WholeRoutesWrapper>()
    //private fun getWholeRoutes() = wholeRoutes
    //private var wholeRoutes = mutableListOf<WholeRoute>()

    /**
     * Pending function
     */
    private fun sharePermissionCheck() {
        Dexter.withActivity(this@BuyOrderActivity)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()) {
                                shareScreenShot()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                    ) {
                        // Remember to invoke this method when the custom rationale is closed
                        // or just by default if you don't want to use any custom rationale.
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener {
                    Timber.d(it.name)
                }
                .check()
    }

    /**
     * not used
     */
    private fun shareScreenShot() {
        val width = sv_body_root.getChildAt(0).width
        val height = sv_body_root.getChildAt(0).height
        val captureView = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        sv_body_root.getLocationInWindow(locationOfViewInWindow)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + width, locationOfViewInWindow[1] + height),
                        captureView, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver,
                                captureView, "Buy Order", null)
                        shareImages(Uri.parse(bitmapPath))
                    }
                }, Handler())
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    /**
     * not used
     */
    private fun shareImages(uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }

    /**
     * 아래는 Rest 응답 message. Messagebox에 저장하지 않음(Push 노출도 하지 않음. "errorCode": "0" 인겄만)
        {
        "eventType": "ProductOfferCreated",
        "timestamp": "20200331154205326650",
        "errorCode": "0",
        "errorMessage": "Order Registration is registered successfully and try deal.",
        "memo": "Route[ CNSHACNSHA:CNSHA:CNYTN:CNYTN:CNYTN: - DEHAMGBLGP:GBSOU:DEHAM:GBLGP:GBSOU:],  Qty[ Min Qty:300.0,Max Qty:300.0,Sum Qty:3600.0 ]",
        "userId": "USR92",
        "offerNumber": "P202003311542031511920004824",
        "offerChangeSeq": 0,
        "referenceOfferNumber": null,
        "referenceChangeSeq": null,
        "dealNumber": null,
        "dealChangeSeq": 0,
        "messageTitle": "Offering System Message"
        }
     */
    private fun doEventService(action: Actions, message: Message? = null) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP_SERVICE) return
        Intent(this, EventService::class.java).also {
            it.action = action.name
            if (action == Actions.PUSH_NOTIFICATION) {
                if (message == null) return@also
                Timber.d("f9: Starting the service : message.errorCode = %s", message.errorCode!!.toInt())
                if (message.errorCode!!.toInt() == 0) return@also
                it.putExtra(Actions.PUSH_NOTIFICATION.name, message)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.d("f9: Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            Timber.d("f9: Starting the service in < 26 Mode")
            startService(it)
        }
    }

    /**
     * offer details info 처리
     */
    private fun processOfferInfoDetails(response: Response<OrderTradeOfferDetail>) {
        if (response.isSuccessful) {
            //--------------------------------------------------------------------------
            // tradeOfferDetail api 응답 데이터 원본
            setActionEnable(true)
            if (!isActionEnable()) {
                loadingDialog.dismiss()
                return
            }

            orderTradeOfferDetail = response.body() as OrderTradeOfferDetail

            /**
             * TODO
             *  T or F 유무값은 추후 Detail 요청 시 Flag 값을 내려주기로 함
             *  > offerQty, offerPrice 등 T or F 와 관련있는 값들은 다 변환이 되어서 내려주기로 함.
             *   그냥, 사용하기만 하면됨.
             *   "T", "F" 표기를 위한 구분값으로만 위 Flag 값으로 사용
             *
             *   일단, T 로 사용하기로 함
             */
            is40Ft = false

            //--------------------------------------------------------------------------
            // tradeOfferWrapper 설정
            //--------------------------------------------------------------------------
            tradeOfferWrapper = TradeOfferWrapper(borList, orderTradeOfferDetail,
                    mutableListOf(), mutableListOf())

            //--------------------------------------------------------------------------------------
            // RdTerm : rdTermCode
            // borList 에서 추출한다.
            //--------------------------------------------------------------------------------------
            val rdTermType = RdTermItemTypes.getRdTermItemType(borList.rdTermCode!!)
            Timber.d("f9: Reference\n[RdTerm] : $rdTermType")

            //--------------------------------------------------------------------------------------
            // Container spin : RdTerm + containerTypeCode
            // borList 에서 추출한다.
            //--------------------------------------------------------------------------------------
            val containerTypeInfo = ContainerSimpleInfo(rdTermType!!, borList.containerTypeCode!!)
            val containerSpinName = getString(rdTermType.rdNameId) + " " +
                    getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
            containerSpinList.add(TextItem(containerSpinName, true, 0))
            selectedContainerIndex = 0

            //--------------------------------------------------------------------------
            // Deal amount 에 표시할 description(Container type)
            // Spin 에 표시된 이름 그대로 사용한다
            //--------------------------------------------------------------------------
            fullContainerNameSimple = containerSpinList[selectedContainerIndex]._value
            periodAndVolumeContainerName = getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
        } else {
            showToast("Fail Save Offer(Http)\n" + response.errorBody())
            finishWithResult(false)
            return
        }
    }

    /**
     * Make New Order or Go Sell Offer
     */
    private fun processOrderSaveOrGoSellOffer(makeOrder: Boolean = true) {
        //  check 된 주차(whole 인 경우 모두)에 대한 offerLineItem 들만 서버에 전달하도록 한다
        with(tradeOfferWrapper) {
            val checkedBaseYearWeek = datas.filter { it.check }
            for (buyOrder in checkedBaseYearWeek) {
                for (offerLineItem in orderTradeOfferDetail.offerLineItems) {
                    if (buyOrder.bseYw == offerLineItem.baseYearWeek) {
                        offerLineItem.checked = true
                        offerLineItem.offerQty = buyOrder.volume
                    }
                }
            }

            orderTradeOfferDetail.offerLineItems = orderTradeOfferDetail.offerLineItems.filter { it.checked }

            val offerSelections = ArrayList<OrderTradeOfferDetail.OfferSelection>()
            offerSelections.add(OrderTradeOfferDetail.OfferSelection(borList.referenceOfferNumber!!, borList.referenceOfferChangeSeq!!))
            orderTradeOfferDetail.offerSelections = offerSelections

            // TODO : Hard coding
            val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val companyCode = Random().ints(4, 0, source.length)
                    .toArray()
                    .map(source::get)
                    .joinToString(EmptyString)
            orderTradeOfferDetail.tradeCompanyCode = companyCode.toUpperCase(Locale.getDefault())
            orderTradeOfferDetail.tradeRoleCode = "002"     // 002=Forwarder
            orderTradeOfferDetail.referenceOfferNumber = borList.offerNumber!!
            orderTradeOfferDetail.offerTypeCode = OFFER_TYPE_CODE_BUY

            if (makeOrder) {
                Timber.d("f9: MAKE > $orderTradeOfferDetail")
                requestBuyOrderSave(orderTradeOfferDetail)
            } else {
                // Make a New Buy Offer using This Condition
                Timber.d("f9: Go Sell Offer > $orderTradeOfferDetail")
                viewModel.requestOfferUsingSameCondition(Triple(
                        OFFER_TYPE_CODE_SELL,
                        false,
                        tradeOfferWrapper.orderTradeOfferDetail)
                )
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Batch, BatchInput, BatchMax checkbox
     * - Mode 변환
     * RecyclerView 의 Checkbox, EditText Touch
     * - Mode change, KeyPad Show/Hide, SUM process
     */
    // chk_batch : check, unCheck
    private fun processBatchCheck(isChecked: Boolean) {
        this.adapterCurrentPosition = 0
        setBatchCheck(isChecked, layoutView)
        if (isChecked.not()) {
            this.batchInputMode = BatchInputMode.ModeBatchInputNo
            confirmAdapter.setBatchData(this.batchInputMode)
            // Keypad 표시 안함
            showEditTextKeypad(false)
        }
    }

    /**
     * chk_batch_input : check, unCheck
     */
    private fun processBatchInputCheck(isChecked: Boolean, value: String = EmptyString) {
        this.adapterCurrentPosition = 0
        setBatchInputCheck(isChecked, value, layoutView)
        if (isChecked) {
            this.batchInputMode = BatchInputMode.ModeBatchInputYes
            // List item 의 volume 을 0 으로 초기화 (Total, initial 포함)
            confirmAdapter.setBatchData(this.batchInputMode)
            processKeypad(-1, true)
        } else {
            // Keypad 표시 안함
            showEditTextKeypad(false)
        }
    }

    /**
     * chk_batch_max : check, unCheck
     */
    private fun processBatchMaxCheck(isChecked: Boolean) {
        this.adapterCurrentPosition = 0
        setBatchMaxCheck(isChecked, layoutView)
        if (isChecked) {
            this.batchInputMode = BatchInputMode.ModeBatchInputMax
            // List item 의 volume 을 MAX 로 초기화 (Total, initial 포함)
            confirmAdapter.setBatchData(this.batchInputMode)
            processSum()
            processKeypad(-1)
        }
    }

    /**
     * sum 계산 후 ui 에 표시, button status 설정
     */
    private fun processSum() {
        // item 별 sub Total 및 전체 항목의 SUM 을 계산 (check == true)
        val totalSum = confirmAdapter.getCalcTotal()
        // 전체 항목의 Total Deal Amount 를 계산 (check == true)
        val totalDealAmount = getCalcDealAmount()

        val iniPymtRto = (orderTradeOfferDetail.offerLineItems[0].firstPaymentRatio * 100).toInt()
        val payableValue = (iniPymtRto.toFloat() / 100 * totalDealAmount).toInt()
        val expectedBalance = budgetValue.minus(payableValue)
        tv_buy_order_sum_total.text = currencyFormat.format(totalSum)
        tv_buy_order_total_deal_amount_value.text = currencyFormat.format(totalDealAmount)
        tv_buy_order_deal_payment_payable_value.text = currencyFormat.format(payableValue)
        tv_buy_order_deal_amount_balance_value.text = currencyFormat.format(expectedBalance)
        tv_buy_order_total_deal_amount_desc.text = "($fullContainerNameSimple)"
        setButtonEnable(totalSum > 0, expectedBalance > 0)
    }

    /**
     * not used
     */
    private fun processVolume(position: Int, value: Any?) {
        if (this.batchInputMode != BatchInputMode.ModeBatchInputNo) return
        // TODO : Do nothing now
    }

    /**
     * process input mode ui
     */
    private fun processInputNoMode() {
        chk_batch.isChecked = false
        chk_batch_input.isChecked = false
        chk_batch_max.isChecked = false
    }

    /**
     * keypad 로 입력한 value 값 처리
     */
    private fun processKeypad(position: Int, isFocus: Boolean = false, value: String = EmptyString) {

        adapterCurrentPosition = position

        // TODO : Consider inventory information...
        if (ll_inventory_floating.visibility == View.VISIBLE) {
            ll_inventory_floating.visibility = View.GONE
            tv_inventory_info.text = EmptyString
        }

        showEditTextKeypad(isFocus, value)

        // KeyBoard 가 내려갈때 checkbox, edittext 확인한다.
        if (position > -1) {
            val checkCondition = batchInputMode == BatchInputMode.ModeBatchInputYes
                    || batchInputMode == BatchInputMode.ModeBatchInputMax
            confirmAdapter.datas.map { it.focused = false; it.check = it.volume > 0 }
            with(confirmAdapter.datas[position]) {
                // ModeBatchInputYes, ModeBatchInputMax 인 경우 unCheck > Check 시 volume 처리
                if (checkCondition) {
                    volume = if (value.isEmpty()) 0 else value.toInt()
                }
                val isCheck = volume > 0
                focused = isFocus
                check = if (focused) true else isCheck
            }
            callAdapterNotifyChanged(true, position)
        } else {
            callAdapterNotifyChanged()
        }
    }

    /**
     * keypad floating button 에 대한 처리
     */
    private fun processQuantityEnter() {
        val value = et_quantity_input_floating.text.toString()
        val valueInt = if (value.isEmpty()) 0 else value.toInt()
        when (batchInputMode == BatchInputMode.ModeBatchInputYes) {
            true -> {
                val isOver = overCheckVolumeBase(valueInt)
                setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = true,
                        value = if (isOver) EmptyString else value)
                confirmAdapter.setBatchData(BatchInputMode.ModeBatchInputYes, if (isOver) 0 else valueInt)
            }
            else -> {
                confirmAdapter.datas[adapterCurrentPosition].volume = valueInt
            }
        }
        processSum()
        processKeypad(adapterCurrentPosition)
    }
}