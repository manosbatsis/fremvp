package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import android.os.Bundle
import android.os.Handler
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
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.ContainerSimpleInfo
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.ContractLineItem
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.enums.BatchInputMode
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.ListenerKind
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_PLAN
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_batch.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.body_sof_price.*
import kotlinx.android.synthetic.main.bottom_batch_input.*
import kotlinx.android.synthetic.main.fragment_sof_price.*
import kotlinx.android.synthetic.main.item_sell_offer_price.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


class SofWizardPriceFragment constructor(val viewModel: SofWizardViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var im: InputMethodManager
    private var keyboardVisibilityUtils: KeyboardVisibilityUtils? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var batchInputMode: BatchInputMode = BatchInputMode.ModeBatchInputNo
    private var adapterCurrentPosition = -1
    private var actionEnable = false

    /**
     * inventory 주차별 리스트의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onUiListener = { listenerKind, position, value, isFocus ->
                        uiListener(listenerKind, position, value, isFocus)
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_sof_price, container, false)

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
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Timber.d("f9: onDetach")
        releaseKeyboardResource()
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
        initKeyboardUtils()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("f9: onPause")
        showEditTextKeypad(false)
        releaseKeyboardResource()
    }

    /**
     * keypad visibility check util 리소스 해제
     */
    private fun releaseKeyboardResource() {
        if (keyboardVisibilityUtils != null) {
            keyboardVisibilityUtils!!.detachKeyboardListeners()
            keyboardVisibilityUtils = null
        }
    }

    /**
     * keypad visibility check util 초기화
     */
    private fun initKeyboardUtils() {
        if (keyboardVisibilityUtils == null) {
            keyboardVisibilityUtils = KeyboardVisibilityUtils(activity!!.window,
                    onShowKeyboard = { _, visibleDisplayFrameHeight ->
                        context?.setScrollHeight(
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
    }

    /**
     * fragment data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        im = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        et_batch_input.isFocusable = true
        et_batch_input.isFocusableInTouchMode = true
        et_batch_input.inputType = TYPE_NULL

        recyclerViewInit()
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        /**
         * contract 정보 가져와서 설정
         */
        viewModel.outPuts.onGoToPriceStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { contract ->
                        Timber.d("f9: Rx - onGoToPriceStep " + contract.toJson())
                        Handler().postDelayed({
                            sv_body_root.scrollTo(0, 0)
                        }, 100)

                        mContract = contract.toJson().fromJson<Contract>()
                        val priceContract = contract.toJson().fromJson<Contract>()

                        priceContract?.let { priceContract_ ->
                            priceContract_.masterContractLineItems?.let { contractLineItems_ ->
                                for (item in contractLineItems_) {
                                    val findInventoryDetail = contract.inventory?.inventoryDetails
                                            ?.find { item.baseYearWeek == it.baseYearWeek }
                                    item.costPrice = findInventoryDetail?.inOutPrice?.toFloat() ?: 0.0F
                                }
                            }

                            /*
                            * offerQty > 0 인 row 만 보여준다
                            * Price 변경 시 mContract 의 masterContractLineItems 에 반영되도록 한다
                            * (입력 > Back > Next 시 입력된 Price 표시되도록)
                            * */
                            priceContract_.masterContractLineItems = contract.masterContractLineItems?.let {
                                it.filter { it.offerQty > 0 }
                            }

                            setAdapterData(priceContract_)
                        }
                    }
                }
    }

    /**
     * price screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_volume.apply {
            layoutManager = LinearLayoutManager(this@SofWizardPriceFragment.context)
            adapter = this@SofWizardPriceFragment.adapter
        }
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        tv_link_condition_detail.setSafeOnClickListener {
            if (!isActionEnable()) return@setSafeOnClickListener
            viewModel.inPuts.clickToConditionDetail(Parameter.CLICK)
        }

        tv_link_whole_route.setSafeOnClickListener {
            if (!isActionEnable()) return@setSafeOnClickListener
            viewModel.inPuts.clickToWholeRoute(Parameter.CLICK)
        }

        tv_price_table.setSafeOnClickListener {
            if (!isActionEnable()) return@setSafeOnClickListener
            viewModel.inPuts.clickToPriceTable(Parameter.CLICK)
        }

        btn_next.setSafeOnClickListener {
            Timber.d("f9: btn_volume_next_floating")
            adapter.getData()?.let { contract ->
                // 모드를 BatchInputNo 로 설정 후 next 이동
                // (onViewCreate : savedInstanceState 시 check event 방지)
                changeMode(BatchInputMode.ModeBatchInputNo)

                mContract?.masterContractLineItems?.let { lineItems ->
                    for (item in lineItems) {
                        val findItem = contract.masterContractLineItems?.find {
                            it.baseYearWeek == item.baseYearWeek
                        }
                        item.offerPrice = findItem?.offerPrice ?: 0
                        // offerPrice 가 0 이면 offerQty 도 0 으로 설정 (For VolumeFragment)
                        if (item.offerPrice == 0) {
                            item.offerQty = 0
                            item.isChecked = false
                        }
                    }
                }

                // BehaviorSubject 에 담아놓는다
                viewModel.inPuts.requestGoToOtherStep(Pair(STEP_PLAN, mContract))
            }
        }

        // Batch : check / uncheck
        chk_batch.setOnCheckedChangeListener { _, isChecked ->
            if (!isActionEnable()) return@setOnCheckedChangeListener
            setBatchInputMode(BatchInputMode.ModeBatchInputNo, isChecked)
        }

        // Batch Input : check / uncheck
        chk_batch_input.setOnCheckedChangeListener { _, isChecked ->
            setBatchInputMode(BatchInputMode.ModeBatchInputYes, isChecked)
        }

        // Batch Max : check / uncheck
        chk_batch_max.setOnCheckedChangeListener { _, isChecked ->
            setBatchInputMode(BatchInputMode.ModeBatchInputMax, isChecked)
        }

        // Batch Input EditText : hasFocus, hasNotFocus
        et_batch_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus and chk_batch_input.isChecked) {
                context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = false)
                showEditTextKeypad(hasFocus, et_batch_input.text.toString())
            }
        }

        et_quantity_input_floating.setOnEditorActionListener OnEditorActionListener@{ _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.keyCode == KeyEvent.KEYCODE_BACK) {
                showEditTextKeypad(false, Constant.EmptyString)
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
                        adapter.datas?.let { data ->
                            if (adapterCurrentPosition > -1) {
                                data[adapterCurrentPosition].offerPrice = valueInt
                                callAdapterNotifyChanged(true, adapterCurrentPosition)
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // batch input enter 처리
        btn_quantity_enter_floating.setSafeOnClickListener {
            val value = et_quantity_input_floating.text.toString()
            val valueInt = if (value.isEmpty()) 0 else value.toInt()
            when (batchInputMode == BatchInputMode.ModeBatchInputYes) {
                true -> {
                    context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = true, value = value)
                    adapter.setBatchInputMode(BatchInputMode.ModeBatchInputYes, false, valueInt)
                    callAdapterNotifyChanged()
                }
                else -> {
                    adapter.datas?.let { data ->
                        if (adapterCurrentPosition > -1) {
                            data[adapterCurrentPosition].offerPrice = valueInt
                            callAdapterNotifyChanged(true, adapterCurrentPosition)
                        }
                    }
                }
            }
            showEditTextKeypad(false, Constant.EmptyString)
        }
    }

    private fun setActionEnable(isEnable: Boolean) {
        actionEnable = isEnable
    }

    private fun isActionEnable(): Boolean = actionEnable

    private fun setAdapterData(contract: Contract) {

        adapterCurrentPosition = -1

        setActionEnable(isEnable = true)
        setPolPodValue(contract)
        setContainerType(contract)
        setBatchInitMode()

        adapter.setData(contract)
        callAdapterNotifyChanged()

        btn_next.isEnabled = adapter.isEnableNextStatus()
    }

    /**
     * 전달 받은 inventory 정보로 UI card 에 container 표시
     */
    private fun setContainerType(contract: Contract) {
        contract.let { contractData ->
            val rdTermType = RdTermItemTypes.getRdTermItemType(contractData.rdTermCode)
            val containerTypeCode = contractData.masterContractLineItems?.first()?.masterContractPrices?.first()?.containerTypeCode
            val containerTypeInfo = ContainerSimpleInfo(rdTermType!!, containerTypeCode!!)
            val containerName = /*getString(rdTermType!!.rdNameId) + " " +*/
                    getString(ContainerName.getContainerName(containerTypeInfo.containerTypeCode)!!.nameFullId)
            tv_price_container_type.text = containerName
        }
    }

    /**
     * 전달 받은 inventory 정보로 UI card 에 pol, pod 표시
     */
    private fun setPolPodValue(contract: Contract) {
        contract.masterContractCarriers?.let { carriers ->
            val carrierCode = carriers.first().carrierCode
            val carriersCount = carriers.size
            iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
            tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
            tv_carrier_count.text = carriersCount.getCodeCount(false)
        }

        contract.masterContractRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POL }
                    .distinctBy { it.locationCode }.count()
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = polCnt.getCodeCount(false)
            }

            // find first pod
            val firstPod = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
            val podCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POD }
                    .distinctBy { it.locationCode }.count()
            firstPod?.let {
                tv_pod_name.text = it.locationCode
                tv_pod_desc.text = it.locationName
                tv_pod_count.text = podCnt.getCodeCount(false)
            }

            tv_period.text = ""       // W01-W10
            // icon 표시하지 않음
            iv_period_whole.visibility = View.INVISIBLE
        }
    }

    /**
     * keypad show 처리
     */
    private fun showEditTextKeypad(show: Boolean, value: String = "") {
        context?.showQuantityEditTextKeypad(im, show, value, getReferencedShowKeypadLayout())
    }

    private fun getReferencedShowKeypadLayout() =
            ReferencedShowKeypadLayout(
                    et_quantity_input_floating, chk_batch_max,
                    ll_bottom_floating, ll_bottom_btn_floating, ll_inventory_floating,
                    sv_body_root
            )

    private fun overEditTextUi(valueInt: Int) {
        // jgkim hmmm shake editText
        context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = true,
                value = if (valueInt > 0) valueInt.toString() else "")
    }

    private fun setBatchInitMode() {
        changeMode(BatchInputMode.ModeBatchInputNo)
        this.batchInputMode = BatchInputMode.ModeBatchInputNo
    }

    private fun setBatchInputMode(batchInputMode: BatchInputMode, isChecked: Boolean) {
        this.adapterCurrentPosition = -1
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
                    context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = false)
                    chk_batch_max.isChecked = false
                }
            }
            BatchInputMode.ModeBatchInputYes -> {
                if (isChecked) {
                    changeMode(batchInputMode)
                    showEditTextKeypad(true, Constant.EmptyString)
                }
            }
            BatchInputMode.ModeBatchInputMax -> {
                if (isChecked) {
                    changeMode(batchInputMode)
                    showEditTextKeypad(false, Constant.EmptyString)
                }
                tv_batch_max.setTextColor(ContextCompat
                        .getColor(activity!!, if (isChecked) R.color.greyish_brown else R.color.very_light_pink))
            }
        }

        // ll_buy_order_volume_batch is View.GONE
        if (ll_batch.visibility != View.VISIBLE) {
            if (!chk_batch_input.isChecked and !chk_batch_max.isChecked) {
                changeMode(BatchInputMode.ModeBatchInputNo)
                // datas 초기화 : check, volume, sub Total 때문에
                //confirmRecyclerViewInitData(selectedContainerIndex)
                showEditTextKeypad(false, Constant.EmptyString)
            }
        }
    }

    /**
     * price input, keypad show/hide, input mode change event 에 대한 ui 처리
     */
    private fun uiListener(listenerKind: ListenerKind, position: Int, value: Any?, isFocus: Boolean = false) {
        when (listenerKind) {
            ListenerKind.LISTENER_VOLUME -> {
                if (this.batchInputMode != BatchInputMode.ModeBatchInputNo) return
                // TODO : Do nothing now
            }

            // ModeBatchInputNo mode 로 변경
            ListenerKind.LISTENER_INPUTNO_MODE -> {
                setBatchInitMode()
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
                    adapter.datas?.let { data ->
                        for (element in data) {
                            with(element) {
                                focused = false
                            }
                        }
                    }

                    val checkCondition = batchInputMode == BatchInputMode.ModeBatchInputYes
                            || batchInputMode == BatchInputMode.ModeBatchInputMax

                    adapter.datas?.let { data ->
                        with(data[position]) {
                            // ModeBatchInputYes, ModeBatchInputMax 인 경우 unCheck > Check 시 volume 처리
                            if (checkCondition) {
                                offerPrice = if (value.isEmpty()) 0 else value.toInt()
                            }
                            focused = isFocus
                        }
                    }
                    callAdapterNotifyChanged(true, position)
                } else {
                    callAdapterNotifyChanged()
                }
                btn_next.isEnabled = adapter.isEnableNextStatus()
            }
            else -> { }
        }
    }

    /**
     * batch input, max input, normal input : mode change 에 따른 ui, adapter 처리
     */
    private fun changeMode(batchInputMode: BatchInputMode) {
        when (batchInputMode) {
            BatchInputMode.ModeBatchInputNo -> {
                ll_batch.visibility = View.VISIBLE
                chk_batch.isChecked = false
                ll_batch_input.visibility = View.GONE
                chk_batch_input.isChecked = false
                chk_batch_max.isChecked = false
                context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = false)
                this.batchInputMode = batchInputMode
                adapter.setBatchInputMode(this.batchInputMode, false, 0)
            }
            BatchInputMode.ModeBatchInputYes -> {
                chk_batch_max.isChecked = false
                context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = false, isValueSet = true)
                adapter.setBatchInputMode(this.batchInputMode, false, 0)
                callAdapterNotifyChanged()
            }
            BatchInputMode.ModeBatchInputMax -> {
                chk_batch_input.isChecked = false
                context?.setBatchInputEditTextIsNormal(et_batch_input, isNormal = true, isValueSet = true)
                adapter.setBatchInputMode(this.batchInputMode, true, 0)
                callAdapterNotifyChanged()
            }
        }
    }

    /**
     * Price 화면 adapter refresh : 특정 position의 item 또는 모든 items
     */
    private fun callAdapterNotifyChanged(isItemChanged: Boolean = false, position: Int = -1) {
        if (isItemChanged) {
            Timber.d("f9: callAdapterNotifyDataSetChanged : notifyItemChanged() - $position")
            adapter.notifyItemChanged(position)
        } else {
            Timber.d("f9: callAdapterNotifyDataSetChanged : notifyDataSetChanged()")
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Price screen 의 recyclerview adapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        lateinit var im: InputMethodManager
        private var batchInputMode = BatchInputMode.ModeBatchInputNo
        private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var data: Contract? = null
        var datas: List<ContractLineItem>? = null
        var onUiListener: (ListenerKind, Int, Any?, Boolean) -> Unit = { _, _, _, _ -> }
        private var previousPrice: Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sell_offer_price, parent, false))
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

        fun getData(): Contract? {
            this.data?.masterContractLineItems = this.datas
            return this.data
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                currencyFormat.minimumFractionDigits = 0
                setInitItemView(itemView, position)
                processVolume(itemView, position)
            }
        }

        private fun setInitItemView(itemView: View, position: Int) {

            datas?.let { data ->
                with(data[position]) {

                    val offerPriceStr = offerPrice.toString()
                    val yearWeek = context.getWeek(baseYearWeek)
                    val margin = 4.toDp().toInt()
                    val params = itemView.ll_order_volume_item_root.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(0, margin, 0, margin)
                    itemView.ll_order_volume_item_root.layoutParams = params

                    itemView.tv_order_volume_input_item.visibility = View.GONE
                    itemView.chk_order_period_item.visibility = View.GONE
                    itemView.et_order_volume_input_item.visibility = View.VISIBLE

                    val enableDisableTextColor = R.color.greyish_brown
                    itemView.tv_order_week_item.setTextColor(ContextCompat.getColor(context, enableDisableTextColor))
                    itemView.tv_order_volume_base_item.setTextColor(ContextCompat.getColor(context, enableDisableTextColor))
                    itemView.et_order_volume_input_item.inputType = TYPE_NULL

                    setItemViewBackgroundColor(itemView, blink)
                    itemView.chk_order_period_item.isChecked = isChecked
                    itemView.tv_order_week_item.text = yearWeek
                    itemView.tv_order_cost_value.text = currencyFormat.format(costPrice)

                    var editFocus = false

                    when (batchInputMode) {
                        BatchInputMode.ModeBatchInputNo -> {
                            editFocus = focused
                        }
                        BatchInputMode.ModeBatchInputYes,
                        BatchInputMode.ModeBatchInputMax -> {
                            itemView.tv_order_volume_input_item.text = if (offerPrice > 0) offerPriceStr else ""
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
                    itemView.et_order_volume_input_item.setBackgroundResource(etOrderVolumeInputItemBackground)
                    itemView.et_order_volume_input_item.setTextColor(ContextCompat.getColor(context, etOrderVolumeInputItemTextColor))
                }
            }
        }

        private fun setItemViewBackgroundColor(itemView: View, blink: Boolean) {
            val backgroundColor = ContextCompat.getColor(context, if (blink) R.color.color_f2f2f2 else R.color.white)
            itemView.ll_order_volume_item_root.setBackgroundColor(backgroundColor)
        }

        private fun processVolume(itemView: View, position: Int) {
            itemView.chk_order_period_item.setOnTouchListener { v, event ->
                datas?.let { data ->
                    with(data[position]) {
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

            itemView.et_order_volume_input_item.setOnTouchListener { v, event ->
                if (batchInputMode == BatchInputMode.ModeBatchInputNo) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            datas?.let { data ->
                                with(data[position]) {
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
            datas?.let { data ->
                with(data[position]) {
                    val offerPriceStr = if (offerPrice > 0) currencyFormat.format(offerPrice) else ""
                    itemView.et_order_volume_input_item.setText(offerPriceStr)
                    onUiListener(ListenerKind.LISTENER_VOLUME, position,
                            if (offerPrice > 0) offerPrice.toString() else "", false)
                }
            }
        }

        fun setBatchInputMode(batchInputMode: BatchInputMode, isMax: Boolean, value: Int) {
            this.previousPrice = 0
            this.batchInputMode = batchInputMode
            datas?.let { data ->
                for (item in data) {
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
            datas?.let { data ->
                for (item in data) {
                    if (item.offerPrice > 0) {
                        isEnable = true
                    }
                }
            }
            return isEnable
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        var mContract: Contract? = null

        @JvmStatic
        fun newInstance(viewModel: SofWizardViewModel) : SofWizardPriceFragment {
            return SofWizardPriceFragment(viewModel)
        }
    }
}