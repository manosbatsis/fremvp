package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType.TYPE_NULL
import android.text.TextWatcher
import android.view.*
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
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.ContainerSimpleInfo
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.ui.enums.BatchInputMode
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.ListenerKind
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_price.*
import kotlinx.android.synthetic.main.body_batch.*
import kotlinx.android.synthetic.main.body_bof_price.*
import kotlinx.android.synthetic.main.body_pol_pod_card_simple.*
import kotlinx.android.synthetic.main.bottom_batch_input.*
import kotlinx.android.synthetic.main.bottom_bof_price.*
import kotlinx.android.synthetic.main.item_buy_offer_price.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = BofPriceVm::class)
class BofPriceAct : BaseActivity<BofPriceVm>() {

    private lateinit var im: InputMethodManager
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var batchInputMode: BatchInputMode = BatchInputMode.ModeBatchInputNo
    private var adapterCurrentPosition = 0
    private var actionEnable = false

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onUiListener = { listenerKind, position, value, isFocus ->
                        uiListener(listenerKind, position, value, isFocus)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_price)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
   }

    override fun onDestroy() {
        super.onDestroy()

        keyboardVisibilityUtils.detachKeyboardListeners()
        Timber.v("f9: onDestroy")
    }

    override fun onBackPressed() {
        showEditTextKeypad(false, "")
        super.onBackPressed()
        Timber.v("f9: onBackPressed")
    }

    private fun initData() {
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
                    uiListener(ListenerKind.LISTENER_KEYPAD, adapterCurrentPosition, "", false)
                })
    }

    private fun initView() {

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_bof_price, menuType = MenuType.DONE, title = getString(R.string.buy_offer_wizard_price))

        et_batch_input.isFocusable = true
        et_batch_input.isFocusableInTouchMode = true
        et_batch_input.inputType = TYPE_NULL

        recyclerViewInit()

        setListener()
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh2 --> it: ${it}")
                    it?.let { offer ->
                        setActionEnable(isEnable = true)
                        setPolPodValue(offer)
                        setContainerType(offer)
                        adapter.setData(offer)
                        adapter.notifyDataSetChanged()
                        offer.offerLineItems?.let { lineItems ->
                            btn_next.isEnabled = lineItems.any { it.offerPrice > 0 }
                        }
                    }
                    Timber.d("f9: onSuccessRefresh2 -")
                }

        viewModel.outPuts.onClickDone()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickDone")
                    //startActivityWithFinish(Intent(this, BofOfferAct::class.java).putExtra(Intents.OFER_GRP_NR, it).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                    onBackPressed()
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickNext (BofPriceConfirmAct)")
                    startActivity(Intent(this, BofConditionAct::class.java).putExtra(Intents.OFFER, it as Offer))
                }

        ////////////////////////////////////////////////////////////////////////////////////////////

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

    private fun recyclerViewInit() {
        recycler_volume.apply {
            layoutManager = LinearLayoutManager(this@BofPriceAct)
            adapter = this@BofPriceAct.adapter
        }
    }

    private fun setListener() {
        appbar_bof_price.toolbar_left_btn.setSafeOnClickListener{
            Timber.d("f9: toolbar_left_btn clcick")
            onBackPressed()
        }

        appbar_bof_price.toolbar_done_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_done_btn")
            showSaveDialog()
        }

        btn_next.setSafeOnClickListener {
            Timber.d("f9: btn_price_next_floating")
            adapter.getData().let {
                viewModel.inPuts.clickToNext( it )
            }
        }

        chk_batch.setOnCheckedChangeListener { _, isChecked ->
            if (!isActionEnable()) return@setOnCheckedChangeListener
            setBatchInputMode(BatchInputMode.ModeBatchInputNo, isChecked)
        }

        chk_batch_input.setOnCheckedChangeListener { _, isChecked ->
            setBatchInputMode(BatchInputMode.ModeBatchInputYes, isChecked)
        }

        // Max input
        chk_batch_max.setOnCheckedChangeListener { _, isChecked ->
            setBatchInputMode(BatchInputMode.ModeBatchInputMax, isChecked)
        }

        et_batch_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus and chk_batch_input.isChecked) {
                setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = false)
                uiListener(ListenerKind.LISTENER_KEYPAD, -1, et_batch_input.text.toString(), hasFocus)
            }
        }

        et_quantity_input_floating.setOnEditorActionListener OnEditorActionListener@{ _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.keyCode == KeyEvent.KEYCODE_BACK) {
                uiListener(ListenerKind.LISTENER_KEYPAD, -1, "", false)
                return@OnEditorActionListener true
            }
            false
        }

        et_quantity_input_floating.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                when (chk_batch_input.isChecked) {
                    true -> {
                        val value = p0.toString()
                        val valueInt = if (value.isEmpty()) 0 else value.toInt()
                        overEditTextUi(valueInt)
                    }
                    else -> {
                        val valueInt = if (p0.toString().isEmpty()) 0 else p0.toString().toInt()
                        adapter.getData().offerLineItems?.let { offerLineItems ->
                            offerLineItems[adapterCurrentPosition].offerPrice = valueInt
                            adapter.notifyItemChanged(adapterCurrentPosition)
                        }
                        uiListener(ListenerKind.LISTENER_SUM, 0, null)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        btn_quantity_enter_floating.setSafeOnClickListener {
            val value = et_quantity_input_floating.text.toString()
            val valueInt = if (value.isEmpty()) 0 else value.toInt()
            when (batchInputMode == BatchInputMode.ModeBatchInputYes) {
                true -> {
                    setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = true, value = value)
                    adapter.setBatchInputMode(BatchInputMode.ModeBatchInputYes, false, valueInt)
                }
                else -> {
                    adapter.getData().offerLineItems?.let { offerLineItems ->
                        offerLineItems[adapterCurrentPosition].offerPrice = valueInt
                    }
                }
            }
            adapter.notifyDataSetChanged()
            uiListener(ListenerKind.LISTENER_SUM, 0, null)
            uiListener(ListenerKind.LISTENER_KEYPAD, adapterCurrentPosition, "", false)
        }
    }

    private fun setActionEnable(isEnable: Boolean) {
        actionEnable = isEnable
    }

    private fun isActionEnable(): Boolean = actionEnable

    private fun setContainerType(offer: Offer) {
        offer.offerLineItems?.let { lineItems ->
            if (!lineItems.isNullOrEmpty()) {
                val rdTermType = RdTermItemTypes.getRdTermItemType(offer.offerRdTermCode)
                val containerTypeCode = lineItems.first().tradeContainerTypeCode
                val containerTypeInfo = ContainerSimpleInfo(rdTermType!!, containerTypeCode)
                val containerName = /*getString(rdTermType!!.rdNameId) + " " + */
                        getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
                tv_container_type.text = containerName
            } else {
                // 지금 Step 에서는 container type, size 정보를 알 수 없음
                tv_container_type.text = getString(R.string.container_full_name)
            }
        }
    }

    private fun setPolPodValue(offer: Offer) {
        setBatchInputMode(BatchInputMode.ModeBatchInputNo, false)
        if (!offer.offerCarriers.isNullOrEmpty()) {
            offer.offerCarriers?.let { carriers ->
                val carrierCode = carriers.first().offerCarrierCode
                val carriersCount = carriers.size
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
                tv_carrier_count.text = if (carriersCount > 1) String.format("+%d", carriersCount - 1) else ""
            }
        } else {
            // 지금 Step 에서는 all carriers
            iv_carrier_logo.setImageResource(getString(R.string.all_carriers).getCarrierIcon(false))
            tv_carrier_name.text = getString(R.string.all_carriers)
            tv_carrier_count.text = ""
        }

        offer.offerRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes.count{ it.locationTypeCode == LocationTypeCode.POL && it.locationCode == firstPol?.locationCode }
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = if (polCnt > 1) String.format("+%d", polCnt -1) else ""
            }

            // find first pod
            val firstPod = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
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

    private fun showEditTextKeypad(show: Boolean, value: String = "") {
        showQuantityEditTextKeypad(im, show, value, getReferencedShowKeypadLayout())
    }

    private fun getReferencedShowKeypadLayout() =
            ReferencedShowKeypadLayout(
                    et_quantity_input_floating, chk_batch_max,
                    ll_bottom_floating, ll_bottom_btn_floating, ll_inventory_floating,
                    sv_body_root
            )

    private fun overEditTextUi(valueInt: Int) {
        // jgkim hmmm shake editText
        setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = true,
                value = if (valueInt > 0) valueInt.toString() else "")
    }

    private fun setBatchInputMode(batchInputMode: BatchInputMode, isChecked: Boolean) {
        this.adapterCurrentPosition = 0
        if (isChecked) this.batchInputMode = batchInputMode
        when (batchInputMode) {
            BatchInputMode.ModeBatchInputNo -> {
                // isChecked == true 에 대한 처리만 한다
                // visible/invisible batch, max layout
                if (isChecked) {
                    ll_batch.visibility = View.GONE
                    ll_batch_input.visibility = View.VISIBLE
                    // batch input checked 상태
                    chk_batch_input.isChecked = true
                    // batch input editText 의 enable frame 보여준다.
                    setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = false)
                    chk_batch_max.isChecked = false
                }
            }
            BatchInputMode.ModeBatchInputYes -> {
                if (isChecked) {
                    changeMode(batchInputMode)
                    uiListener(ListenerKind.LISTENER_KEYPAD, -1, "", true)
                }
            }
            BatchInputMode.ModeBatchInputMax -> {
                if (isChecked) {
                    changeMode(batchInputMode)
                    uiListener(ListenerKind.LISTENER_SUM, 0, null)
                    uiListener(ListenerKind.LISTENER_KEYPAD, -1, "", false)
                }
                tv_batch_max.setTextColor(ContextCompat
                        .getColor(this@BofPriceAct, if (isChecked) R.color.greyish_brown else R.color.very_light_pink))
            }
        }

        // ll_buy_order_volume_batch is View.GONE
        if (ll_batch.visibility != View.VISIBLE) {
            if (!chk_batch_input.isChecked and !chk_batch_max.isChecked) {
                changeMode(BatchInputMode.ModeBatchInputNo)
                // datas 초기화 : check, volume, sub Total 때문에
                //confirmRecyclerViewInitData(selectedContainerIndex)
                uiListener(ListenerKind.LISTENER_SUM, 0, null)
                uiListener(ListenerKind.LISTENER_KEYPAD, -1, "", false)
            }
        }
    }

    private fun uiListener(listenerKind: ListenerKind, position: Int, value: Any?, isFocus: Boolean = false) {
        when (listenerKind) {
            ListenerKind.LISTENER_VOLUME -> {
                if (this.batchInputMode != BatchInputMode.ModeBatchInputNo) return
                // TODO : Do nothing now
            }

            // ModeBatchInputNo mode 로 변경
            ListenerKind.LISTENER_INPUTNO_MODE -> {
                changeMode(BatchInputMode.ModeBatchInputNo)
                this.batchInputMode = BatchInputMode.ModeBatchInputNo
            }

            // Adapter 의 EditText focus event 발생 시 호출됨
            ListenerKind.LISTENER_KEYPAD -> {
                adapterCurrentPosition = position
                // MAX, Batch Input
                ll_inventory_floating.visibility = View.GONE
                tv_inventory_info.text = ""//getInventoryInfo(position)
                showEditTextKeypad(isFocus, value as String)
                // KeyBoard 가 내려갈때 checkbox, edittext 확인한다.
                if (position > -1) {
                    adapter.getData().offerLineItems?.let { offerLineItems ->
                        for (element in offerLineItems) {
                            with(element) {
                                focused = false
                                isChecked = offerQty > 0
                            }
                        }
                    }

                    val checkCondition = batchInputMode == BatchInputMode.ModeBatchInputYes
                            || batchInputMode == BatchInputMode.ModeBatchInputMax

                    adapter.getData().offerLineItems?.let { offerLineItems ->
                        with(offerLineItems[position]) {
                            // ModeBatchInputYes, ModeBatchInputMax 인 경우 unCheck > Check 시 volume 처리
                            if (checkCondition) {
                                offerQty = if (value.isEmpty()) 0 else value.toInt()
                            }
                            val isCheck = offerQty > 0
                            focused = isFocus
                            isChecked = if (focused) true else isCheck
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                btn_next.isEnabled = adapter.isEnableNextStatus()
            }
            else -> { }
        }
    }

    private fun changeMode(batchInputMode: BatchInputMode) {
        when (batchInputMode) {
            BatchInputMode.ModeBatchInputNo -> {
                ll_batch.visibility = View.VISIBLE
                chk_batch.isChecked = false
                ll_batch_input.visibility = View.GONE
                chk_batch_input.isChecked = false
                chk_batch_max.isChecked = false
                setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = false)
                this.batchInputMode = batchInputMode
                adapter.setBatchInputMode(this.batchInputMode, false, 0)
            }
            BatchInputMode.ModeBatchInputYes -> {
                chk_batch_max.isChecked = false
                setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = true)
                adapter.setBatchInputMode(this.batchInputMode, false, 0)
            }
            BatchInputMode.ModeBatchInputMax -> {
                chk_batch_input.isChecked = false
                setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = true)
                adapter.setBatchInputMode(this.batchInputMode, true, 0)
            }
        }
        adapter.notifyDataSetChanged()
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

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        lateinit var im: InputMethodManager
        private var batchInputMode = BatchInputMode.ModeBatchInputNo
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var offer: Offer = Offer()
        var onUiListener: (ListenerKind, Int, Any?, Boolean) -> Unit = { _, _, _, _ -> }
        private var previousPrice: Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_buy_offer_price, parent, false))
        }

        override fun getItemCount(): Int {
            var size = 0
            offer.offerLineItems?.let { lineItems ->
                size = lineItems.size
            }
            return size
        }

        fun setData(offer: Offer) {
            offer.let {
                this.offer = it
            }
        }

        fun getData(): Offer {
            return this.offer
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                currencyFormat.minimumFractionDigits = 0
                setInitItemView(itemView, position)
                processVolume(itemView, position)
            }
        }

        private fun setInitItemView(itemView: View, position: Int) {

            offer.offerLineItems?.let { lineItems ->
                with(lineItems[position]) {

                    val offerPriceStr = offerPrice.toString()
                    val yearWeek = context.getWeek(baseYearWeek)
                    val margin = 4.toDp().toInt()
                    val params = itemView.ll_item_root.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(0, margin, 0, margin)
                    itemView.ll_item_root.layoutParams = params

                    itemView.tv_input_item.visibility = View.GONE
                    itemView.chk_period_item.visibility = View.GONE
                    itemView.et_input_item.visibility = View.VISIBLE

                    val enableDisableTextColor = R.color.greyish_brown
                    itemView.tv_week_item.setTextColor(ContextCompat.getColor(context, enableDisableTextColor))
                    itemView.tv_base_item.setTextColor(ContextCompat.getColor(context, enableDisableTextColor))
                    itemView.et_input_item.inputType = TYPE_NULL

                    setItemViewBackgroundColor(itemView, blink)
                    itemView.chk_period_item.isChecked = isChecked
                    itemView.tv_week_item.text = yearWeek
                    itemView.tv_cost_value.text = if (costPrice > 0) currencyFormat.format(costPrice) else "-"

                    var editFocus = false

                    when (batchInputMode) {
                        BatchInputMode.ModeBatchInputNo -> {
                            editFocus = focused
                        }
                        BatchInputMode.ModeBatchInputYes,
                        BatchInputMode.ModeBatchInputMax -> {
                            itemView.tv_input_item.text = if (offerPrice > 0) offerPriceStr else ""
                        }
                    }
                    setVolumeValue(itemView, position)

                    val etOrderVolumeInputItemBackground =
                            if (editFocus) {
                                R.drawable.bg_order_edittext_blue_border
                            } else {
                                R.drawable.bg_order_edittext_gray_border
                            }
                    val etOrderVolumeInputItemTextColor =
                            if (editFocus) {
                                R.color.blue_violet
                            } else {
                                R.color.greyish_brown
                            }
                    itemView.et_input_item.setBackgroundResource(etOrderVolumeInputItemBackground)
                    itemView.et_input_item.setTextColor(ContextCompat.getColor(context, etOrderVolumeInputItemTextColor))
                }
            }
        }

        private fun setItemViewBackgroundColor(itemView: View, blink: Boolean) {
            val backgroundColor = ContextCompat.getColor(context, if (blink) R.color.color_f2f2f2 else R.color.white)
            itemView.ll_item_root.setBackgroundColor(backgroundColor)
        }

        private fun processVolume(itemView: View, position: Int) {
            itemView.chk_period_item.setOnTouchListener { v, event ->
                offer.offerLineItems?.let { lineItems ->
                    with(lineItems[position]) {
                        // 현재 check 상태
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (!isChecked) {
                                    var isFocus = false
                                    var value: String
                                    // uncheck > check
                                    when (batchInputMode) {
                                        BatchInputMode.ModeBatchInputYes -> {
                                            // et_sell_order_volume_batch_input 의 값으로 채움.(check > Uncheck 시 previousPrice
                                            value = previousPrice.toString()
                                        }
                                        BatchInputMode.ModeBatchInputMax -> {
                                            value = if (offerPrice > 0) offerPrice.toString() else ""
                                        }
                                        else -> {
                                            value = if (offerPrice > 0) offerPrice.toString() else ""
                                            isFocus = true
                                        }
                                    }
                                    onUiListener(ListenerKind.LISTENER_KEYPAD, position, value, isFocus)
                                } else {
                                    // check > uncheck
                                    previousPrice = offerPrice
                                    offerPrice = 0
                                    onUiListener(ListenerKind.LISTENER_KEYPAD, position, "", false)

                                    if (batchInputMode != BatchInputMode.ModeBatchInputNo) {
                                        onUiListener(ListenerKind.LISTENER_INPUTNO_MODE, position, "", false)
                                    }
                                }
                                v.performClick()
                            }
                            else -> { }
                        }
                    }
                }
                true
            }

            itemView.et_input_item.setOnTouchListener { v, event ->
                if (batchInputMode == BatchInputMode.ModeBatchInputNo) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            offer.offerLineItems?.let { lineItems ->
                                with(lineItems[position]) {
                                    val value = if (offerPrice > 0) offerPrice.toString() else ""
                                    onUiListener(ListenerKind.LISTENER_KEYPAD, position, value, true)
                                }
                            }
                            v.performClick()
                        }
                    }
                }
                true
            }
        }

        private fun setVolumeValue(itemView: View, position: Int) {
            offer.offerLineItems?.let { lineItems ->
                with(lineItems[position]) {
                    val offerPriceStr = if (offerPrice > 0) currencyFormat.format(offerPrice) else ""
                    itemView.et_input_item.setText(offerPriceStr)
                    onUiListener(ListenerKind.LISTENER_VOLUME, position,
                            if (offerPrice > 0) offerPrice.toString() else "", false)
                }
            }
        }

        fun setBatchInputMode(batchInputMode: BatchInputMode, isMax: Boolean, value: Int) {
            this.previousPrice = 0
            this.batchInputMode = batchInputMode
            offer.offerLineItems?.let { lineItems ->
                for (item in lineItems) {
                    with(item) {
                        if (isMax) {
                            offerPrice = costPrice.toInt()
                            isChecked = offerPrice > 0
                        } else {
                            if (batchInputMode == BatchInputMode.ModeBatchInputYes) {
                                offerPrice = value
                                isChecked = value > 0
                            }
                        }
                    }
                }
            }
        }

        fun isEnableNextStatus() : Boolean {
            var isEnable = false
            offer.offerLineItems?.let { lineItems ->
                for (item in lineItems) {
                    if (item.offerPrice > 0) {
                        isEnable = true
                    }
                }
            }
            return isEnable
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}