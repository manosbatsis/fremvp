package com.cyberlogitec.freight9.ui.selloffer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchUIUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.SellOffer.SELL_OFFER_WIZARD
import com.cyberlogitec.freight9.lib.apitrade.PostPortRouteRequest
import com.cyberlogitec.freight9.lib.model.InventoryList
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeItemTouchHelper
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeItemTouchListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.inventory.RouteFilterPopup
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_contract.*
import kotlinx.android.synthetic.main.appbar_route_filter.*
import kotlinx.android.synthetic.main.item_route_filter_result.view.*
import kotlinx.android.synthetic.main.toolbar_route_filter.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofContractVm::class)
class SofContractAct : BaseActivity<SofContractVm>(), SwipeItemTouchListener {

    private var onSwipeItemLeft: (String) -> Unit = {}
    private var onSwipeItemRight: (String) -> Unit = {}
    private var filterLists: MutableList<InventoryList> = mutableListOf()

    /**
     * inventory 리스트의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onClickItem = { viewModel.inPuts.clickItem(it) }
                    onSwipeItemLeft = { viewModel.inPuts.swipeToLeft(it) }
                    onSwipeItemRight = { viewModel.inPuts.swipeToRight(it) }
                }
    }

    private val listFragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_contract)

        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_route_filter, menuType = MenuType.DEFAULT, title = getString(R.string.select_a_contract))
        toolbar_right_btn.visibility = View.GONE
        toolbar_right_tv.visibility = View.GONE

        // wait click event (toolbar left button)
        toolbar_left_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_left_btn clcick")
            onBackPressed()
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            //viewModel.inPuts.clickToMenu(Parameter.CLICK)
            onBackPressed()
        }

        // POL
        ll_route_filter_from.setSafeOnClickListener {
            Timber.d("f9: tv_pol_cd click")
            showRouteSelectPopup(RouteFilterPopup.RouteFromTo.FROM)
        }

        // POD
        ll_route_filter_to.setSafeOnClickListener {
            Timber.d("f9: tv_pod_cd click")
            showRouteSelectPopup(RouteFilterPopup.RouteFromTo.TO)
        }

        // BOTH
        iv_route_filter_all.setSafeOnClickListener {
            Timber.d("f9: vw_route_seprator click")
            showRouteSelectPopup(RouteFilterPopup.RouteFromTo.ALL)
        }

        // init recyclerview
        recyclerViewInit()


        viewModel.outPuts.goToNewVolume()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: SofNewVolumeAct(itemId: ${it})")
                    if (SELL_OFFER_WIZARD) {
                        // Has no Intents.OFFER extra
                        startActivity(Intent(this, SofWizardActivity::class.java)
                                .putExtra(Intents.MSTR_CTRK_NR, it)
                                .putExtra(Intents.OFFER_BY_MADE_CONDITION, false))
                    } else {
                        startActivity(Intent(this, SofVolumeAct::class.java).putExtra(Intents.MSTR_CTRK_NR, it))
                    }
                }

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            Timber.d("f9: onSuccessRefresh size: ${(it.body() as List<InventoryList>).toMutableList().size} +")
                            adapter.datas.clear()
                            adapter.datas.addAll((it.body() as List<InventoryList>).toMutableList())
                            adapter.notifyDataSetChanged()
                            Timber.d("f9: onSuccessRefresh -")
                        } else {
                            showToast("Fail Inventory list(Http)\n" + it.errorBody())
                            finish()
                        }
                    }
                }

        viewModel.outPuts.onSuccessRequestPolPodList()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            filterLists.clear()
                            filterLists = (it.body() as List<InventoryList>).toMutableList()
                        } else {
                            showToast("Fail Pol, Pod list(Http)\n" + it.errorBody())
                            finish()
                        }
                    }
                }

        viewModel.outPuts.gotoConractVolume()
                .bindToLifecycle(this)
                .subscribe {
                    //startActivity(Intent(this, SofContractVolumeAct::class.java).putExtra(Intents.INVN_NR, it))
                    //startActivity(Intent(this, SofContractRouteAct::class.java).putExtra(Intents.MSTR_CTRK_NR, it))
                }

        viewModel.outPuts.gotoContractRoute()
                .bindToLifecycle(this)
                .subscribe {
                    //startActivity(Intent(this, SofContractRouteAct::class.java).putExtra(Intents.INVN_NR, it))
                }

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog : $it")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    /**
     * inventory list screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() =
            recycler_view_contract.apply {
                layoutManager = LinearLayoutManager(this@SofContractAct)

                // init swipe
                val swipeHelper = SwipeItemTouchHelper(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, this@SofContractAct)
                ItemTouchHelper(swipeHelper).attachToRecyclerView(recycler_view_contract)

                adapter = this@SofContractAct.adapter
            }

    /**
     * inventory list screen 의 recyclerview adapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        val datas = mutableListOf<InventoryList>()

        var onClickItem: (String) -> Unit = {}

        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_route_filter_result, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]

                Timber.d("f9: data: $data")

                tv_filter_item_category.visibility = View.GONE

                iv_filter_carrier_logo.setImageResource(data.carrierCode!!.getCarrierIcon(false))
                tv_filter_carrier_name.text =
                        if (data.carrierCode.trim().isNotEmpty()) data.carrierCode
                        else context.getString(R.string.all_carriers)

                tv_filter_container_amount.text = "${data.minQty?.toFloat()?.toInt()}-${data.maxQty?.toFloat()?.toInt()}"

                tv_filter_pol_code.text = data.polCode
                tv_filter_pol_count.text = data.polCount.getCodeCount()
                tv_filter_pol_name.text = data.polName

                tv_filter_pod_code.text = data.podCode
                tv_filter_pod_count.text = data.podCount.getCodeCount()
                tv_filter_pod_name.text = data.podName

                tv_filter_weekof.text = "${context.getWeek(data.minYearWeek)}-${context.getWeek(data.maxYearWeek)}"

                iv_filter_period_whole.visibility = View.INVISIBLE

                this.setSafeOnClickListener {
                    Timber.d("f9: data.masterContractNumber: ${data.masterContractNumber}")
                    onClickItem(data.masterContractNumber!!)
                }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        if(holder is RecyclerAdapter.ViewHolder) {
            val swipedIndex = holder.bindingAdapterPosition
            val inventoryNumber = adapter.datas[swipedIndex].inventoryNumber
            Timber.d("f9: onSwiped() -> swipedIndex: $swipedIndex, direction: $direction, inventoryNumber = $inventoryNumber")

            when(direction) {
                ItemTouchHelper.LEFT -> {
                    Timber.d("f9: LEFT -> Temporary -> SellOrder")
                    onSwipeItemLeft(inventoryNumber!!)

                }
                ItemTouchHelper.RIGHT -> {
                    Timber.d("f9: RIGHT -> Temporary -> BuyOrder")
                    onSwipeItemRight(inventoryNumber!!)

                }
                else -> { Timber.d("f9: ELSE") }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onSelectedChanged(holder: RecyclerView.ViewHolder?, actionState: Int, uiUtil: ItemTouchUIUtil) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    Timber.d("f9: onSelectedChanged> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onSelected(holder.container)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
            uiUtil: ItemTouchUIUtil
    ) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    //Timber.d("f9: onChildDraw> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onDraw(c, recyclerView, holder.container, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onChildDrawOver(
            c: Canvas,
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder?,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
            uiUtil: ItemTouchUIUtil
    ) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    //Timber.d("f9: onChildDrawOver> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onDrawOver(c, recyclerView, holder.container, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, uiUtil: ItemTouchUIUtil) {
        if(holder is RecyclerAdapter.ViewHolder) {
            Timber.d("f9: clearView> ItemTouchHelper.ACTION_STATE_SWIPE")
            //uiUtil.clearView(holder.container)
        }
    }

    /**
     * From, All, To Filter Popup
     */
    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showRouteSelectPopup(routeFromTo: RouteFilterPopup.RouteFromTo) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_route_filter, null)
        popupWindow = RouteFilterPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onRouteSelectClick)
        (popupWindow as RouteFilterPopup).initValue(routeFromTo, makeRouteAdapterDatas(routeFromTo))
        popupWindow!!.showAtLocation(view, Gravity.TOP, 0, 0 )
    }

    /**
     * RouteFilterPopup(From / All / To) 의 Recycler item click 시 호출되는 함수
     */
    private fun onRouteSelectClick(position: Int, routeFromTo: RouteFilterPopup.RouteFromTo,
                                   routeAdapterData: RouteFilterPopup.RouteAdapterData) {
        Timber.d("f9: onRouteSelectClick - position = $position")
        when (routeFromTo) {
            RouteFilterPopup.RouteFromTo.FROM -> {
                tv_route_filter_from_code.text =
                        if (routeAdapterData.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.polOrPorPortCode
                tv_route_filter_from_name.text = routeAdapterData.polOrPorPortName
            }
            RouteFilterPopup.RouteFromTo.ALL -> {
                tv_route_filter_from_code.text =
                        if (routeAdapterData.polOrPorPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.polOrPorPortCode
                tv_route_filter_from_name.text = routeAdapterData.polOrPorPortName
                tv_route_filter_to_code.text =
                        if (routeAdapterData.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.podOrDelPortCode
                tv_route_filter_to_name.text = routeAdapterData.podOrDelPortName
            }
            RouteFilterPopup.RouteFromTo.TO -> {
                tv_route_filter_to_code.text =
                        if (routeAdapterData.podOrDelPortKind == RouteFilterPopup.PortKind.ALL) getString(R.string.route_filter_all)
                        else routeAdapterData.podOrDelPortCode
                tv_route_filter_to_name.text = routeAdapterData.podOrDelPortName
            }
            else -> { }
        }

        val polCode = tv_route_filter_from_code.text.toString()
        val podCode = tv_route_filter_to_code.text.toString()
        val polCodeIsAll = (polCode.compareTo(getString(R.string.all), true) == 0)
                || (polCode.compareTo(getString(R.string.from), true) == 0)
        val podCodeIsAll = (podCode.compareTo(getString(R.string.all), true) == 0)
                || (podCode.compareTo(getString(R.string.to), true) == 0)

        viewModel.inPuts.afterFeaturedRoute(PostPortRouteRequest(if (polCodeIsAll) EmptyString else tv_route_filter_from_code.text.toString(),
                if (podCodeIsAll) EmptyString else tv_route_filter_to_code.text.toString()))
    }

    /**
     * routeSelect : FROM, ALL, TO
     * All 선택 시 모든 From, To list 구성
     * From 선택 시 From 을 포함하고 있는 To list 구성
     * To 선택 시 To 를 포함하고 있는 From list 구성
     */
    private fun makeRouteAdapterDatas(routeFromTo: RouteFilterPopup.RouteFromTo) : List<RouteFilterPopup.RouteAdapterData> {
        var routeAdapterDatas = mutableListOf<RouteFilterPopup.RouteAdapterData>()
        // From, to 중 어떤겄이 채워져 있는지 체크되어야 함.
        when (routeFromTo) {
            RouteFilterPopup.RouteFromTo.FROM -> {
                // "To" check
                var podCode = tv_route_filter_to_code.text
                if ((podCode.toString().compareTo(getString(R.string.all), true) == 0)
                        || (podCode.toString().compareTo(getString(R.string.to), true) == 0)){
                    podCode = EmptyString
                }

                // true : Pod 가 없는 경우, false : Pod 가 있는 경우
                val noPodFilter = podCode.isNullOrEmpty()
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.NONE,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        if (noPodFilter) {
                            // Pol 전부
                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                    index,
                                    RouteFilterPopup.PortKind.POL,
                                    polCode ?: EmptyString,
                                    polName ?: EmptyString,
                                    -1,
                                    RouteFilterPopup.PortKind.NONE,
                                    EmptyString,
                                    EmptyString))
                        } else {
                            // Pod 가 같은 항목만
                            when (this.podCode) {
                                podCode -> {
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            index,
                                            RouteFilterPopup.PortKind.POL,
                                            polCode ?: EmptyString,
                                            polName ?: EmptyString,
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString))
                                }
                                else -> {}
                            }
                        }
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { it.polOrPorPortCode }
                        .toMutableList()
            }
            RouteFilterPopup.RouteFromTo.ALL -> {
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(-1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                index, RouteFilterPopup.PortKind.POL,
                                polCode ?: EmptyString, polName ?: EmptyString,
                                index, RouteFilterPopup.PortKind.POD,
                                podCode ?: EmptyString, podName ?: EmptyString))
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { Pair(it.polOrPorPortCode, it.podOrDelPortCode) }
                        .toMutableList()
            }
            RouteFilterPopup.RouteFromTo.TO -> {
                // "From" check
                var polCode = tv_route_filter_from_code.text
                if ((polCode.toString().compareTo(getString(R.string.all), true) == 0)
                        || (polCode.toString().compareTo(getString(R.string.from), true) == 0)){
                    polCode = EmptyString
                }

                // true : Pol 가 없는 경우, false : Pol 가 있는 경우
                val noPolFilter = polCode.isNullOrEmpty()
                routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(-1,
                        RouteFilterPopup.PortKind.NONE,
                        EmptyString,
                        EmptyString,
                        -1,
                        RouteFilterPopup.PortKind.ALL,
                        EmptyString,
                        EmptyString))
                for ((index, data) in filterLists.withIndex()) {
                    with(data) {
                        if (noPolFilter) {
                            // Pod 전부
                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                    -1,
                                    RouteFilterPopup.PortKind.NONE,
                                    EmptyString,
                                    EmptyString,
                                    index,
                                    RouteFilterPopup.PortKind.POD,
                                    podCode ?: EmptyString,
                                    podName ?: EmptyString))
                        } else {
                            // Pol 이 같은 항목만
                            when (this.polCode) {
                                polCode -> {
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString,
                                            index,
                                            RouteFilterPopup.PortKind.POD,
                                            podCode ?: EmptyString,
                                            podName ?: EmptyString))
                                }
                                else -> {}
                            }
                        }
                    }
                }
                routeAdapterDatas = routeAdapterDatas
                        .distinctBy { it.podOrDelPortCode }
                        .toMutableList()
            }
            else -> { }
        }
        return routeAdapterDatas
    }
}