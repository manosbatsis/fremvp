package com.cyberlogitec.freight9.ui.buyoffer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.ui.buyoffer.BofRouteSearchPopup.Companion.CONTENT
import com.cyberlogitec.freight9.ui.buyoffer.BofRouteSearchPopup.Companion.HEADER
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_lane.*
import kotlinx.android.synthetic.main.appbar_bof_lane.*
import kotlinx.android.synthetic.main.body_bof_lane.*
import kotlinx.android.synthetic.main.item_bof_content_lane.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.util.*


@RequiresActivityViewModel(value = BofLaneVm::class)
class BofLaneAct : BaseActivity<BofLaneVm>() {

    private lateinit var allSchedules: List<Schedule>

    // list view
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemClick = { viewModel.inPuts.clickToItem(it) }
                }
    }

    override fun onBackPressed() {
        if (popupWindow != null) {
            removePopup()
        } else {
            Timber.v("f9: onBackPressed --> prevent backpress")
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_lane)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        // Do nothing
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_bof_lane,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_select_route),
                isEnableNavi = true)

        // init recycler views
        recyclerViewInit()
        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { schedules ->
                        allSchedules = schedules
                        adapter.itemList.clear()

                        // init
                        servicelane_code.text = Constant.EmptyString
                        servicelane_name.text = Constant.EmptyString

                        // set as item (for list view)
                        val items = getItems( schedules )
                        adapter.itemList = items.toMutableList()

                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onClickItem()
                .bindToLifecycle(this)
                .subscribe {
                    adapter.itemList.map{ it.schedule?.isLaneChecked = false }

                    adapter.itemList.get(it).schedule?.let { schedule ->
                        schedule.isLaneChecked = !schedule.isLaneChecked

                        if (schedule.isLaneChecked) {
                            servicelane_code.text = schedule.serviceLaneCode
                            servicelane_name.text = schedule.serviceLaneName

                            adapter.itemList.filter{ it.schedule?.serviceLaneCode == schedule.serviceLaneCode }.let { item2 ->
                                item2.map{ it.schedule!!.isLaneChecked = true }
                            }
                        } else {
                            servicelane_code.text = Constant.EmptyString
                            servicelane_name.text = Constant.EmptyString

                            adapter.itemList.filter{ it.schedule?.serviceLaneCode == schedule.serviceLaneCode }.let { item2 ->
                                item2.map{ it.schedule!!.isLaneChecked = false }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

        viewModel.outPuts.onClickItemRemove()
                .bindToLifecycle(this)
                .subscribe { serviceLaneCode ->
                    servicelane_code.text = Constant.EmptyString
                    servicelane_name.text = Constant.EmptyString
                    adapter.itemList.filter{ it.schedule?.serviceLaneCode == serviceLaneCode }.let { item2 ->
                        item2.map{ it.schedule!!.isLaneChecked = false }
                    }
                    adapter.notifyDataSetChanged()
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    startActivity(Intent(this, BofPolAct::class.java).putExtra(Intents.SCHEDULE_LIST, ArrayList(it)))
                }

        viewModel.outPuts.onClickSearch()
                .bindToLifecycle(this)
                .subscribe {
                    showSearchPopup()
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

    private fun setListener() {
        // wait click event (toolbar left button)
        appbar_bof_lane.toolbar_left_btn.setSafeOnClickListener{
            it.let {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // emit event to viewModel -> show drawer menu
        appbar_bof_lane.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn clcick")
            onBackPressed()
        }

        iv_servicelane_cancel.setSafeOnClickListener {
            val servicelaneCd = servicelane_code.text.toString()
            viewModel.inPuts.clickToItemRemove( servicelaneCd )
        }

        btn_lane_next.setSafeOnClickListener {
            var sel_schedules = mutableListOf<Schedule>()
            adapter.itemList.filter{ it.schedule != null }.count{ it.schedule!!.isLaneChecked }.let {
                if (it > 0) {
                    sel_schedules = adapter.itemList.filter{ it.schedule != null }.filter{ it.schedule!!.isLaneChecked }.map{ it.schedule!! }.toMutableList()
                } else {
                    sel_schedules = adapter.itemList.filter{ it.schedule != null }.map{ it.schedule!! }.toMutableList()
                }
            }

            var serviceLaneCode = sel_schedules.first().serviceLaneCode
            var selAllSchedules = allSchedules
                    .filter { it.serviceLaneCode == serviceLaneCode }
                    .distinctBy { it.polCode }.toMutableList()
            viewModel.inPuts.clickToNext(selAllSchedules)
        }

        servicelane_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val value = p0.toString()
                setButtonStatus(value.isNotEmpty())
            }
        })

        ll_search.setSafeOnClickListener {
            viewModel.inPuts.clickToSearch(Parameter.CLICK)
        }
    }

    private fun setButtonStatus(isEnable: Boolean) {
        btn_lane_next.isEnabled = isEnable
        setSelectLayout(isEnable)
    }

    private fun setSelectLayout(isEnable: Boolean) {
        ll_search.visibility = if (isEnable) View.GONE else View.VISIBLE
        ll_select.visibility = if (isEnable) View.VISIBLE else View.GONE
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofLaneAct)
            adapter = this@BofLaneAct.adapter
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun getItems(schedules: List<Schedule>): List<BofRouteSearchPopup.Item> {
        val items = mutableListOf<BofRouteSearchPopup.Item>()

        val polSubRegionName = schedules.distinctBy{ it.polSubRegionName }.map{ it.polSubRegionName }
        for (x in polSubRegionName) {
            val header = BofRouteSearchPopup.Item.Builder()
                    .type(HEADER)
                    .title(x)
                    .isOpened(x == polSubRegionName.first())
                    .build()
            items.add(header)

            val rows = schedules.filter { it.polSubRegionName == x }.distinctBy { it.serviceLaneCode }

            for (y in rows) {
                val content = BofRouteSearchPopup.Item.Builder()
                        .type(CONTENT)
                        .schedule(y)
                        .isOpened(x == polSubRegionName.first())
                        .build()
                items.add(content)
            }
        }
        return items
    }

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showSearchPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_bof_route_search, null)
        popupWindow = BofRouteSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onSearchItemClick)
        (popupWindow as BofRouteSearchPopup).initValue(BofRouteSearchPopup.DataKind.KIND_LANE, adapter.itemList)
        (popupWindow as BofRouteSearchPopup).setOnDismissListener {
            removePopup()
        }
        popupWindow!!.showAtLocation(view, Gravity.TOP, 0, 0 )
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    /**
     * Search popup 에서 search 된 item click 처리
     */
    private fun onSearchItemClick(selectedItem: BofRouteSearchPopup.Item) {
        removePopup()
        adapter.itemList.map { it.schedule?.isLaneChecked = false }
        adapter.itemList
                .filter { it.schedule?.serviceLaneCode == selectedItem.schedule?.serviceLaneCode }
                .let { item ->
                    item.map{
                        it.schedule!!.isLaneChecked = true
                        servicelane_code.text = it.schedule.serviceLaneCode
                        servicelane_name.text = it.schedule.serviceLaneName
                    }
                }
        adapter.notifyDataSetChanged()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var onItemClick: (Int) -> Unit = {}
        var itemList = mutableListOf<BofRouteSearchPopup.Item>()

        private lateinit var tfLight: Typeface
        private lateinit var tfBold: Typeface

        override fun getItemCount(): Int {
            return itemList.count()
        }

        override fun getItemViewType(position: Int): Int {
            return itemList[position].type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            var holder: RecyclerView.ViewHolder? = null

            // font
            tfLight = ResourcesCompat.getFont(parent.context, R.font.opensans_light)!!
            tfBold = ResourcesCompat.getFont(parent.context, R.font.opensans_bold)!!

            when(viewType) {
                HEADER -> holder =
                        HeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_header_lane, parent, false)
                        )
                CONTENT -> holder =
                        ContentViewHolder(
                                inflater.inflate(R.layout.item_bof_content_lane, parent, false)
                        )
            }
            return holder ?: throw IllegalStateException("Item type unspecified.")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if(holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                Timber.d("onBindViewHolder(poistion.adapterPosition: ${holder.bindingAdapterPosition}, position: ${position} )")
                val item = itemList[holder.bindingAdapterPosition]

                when(item.type) {
                    HEADER -> {
                        bindHeader(holder as HeaderViewHolder, item)
                    }
                    CONTENT -> {
                        bindContent(holder as ContentViewHolder, item, position)
                    }
                }
            }
        }

        private fun bindHeader(holder: HeaderViewHolder, item: BofRouteSearchPopup.Item) {
            if(item.isOpened) {
                holder.arrow.setImageResource(R.drawable.default_white_up)
            } else {
                holder.arrow.setImageResource(R.drawable.selected_white_down)
            }

            holder.title.text = item.title
            holder.container.setSafeOnClickListener {
                if(item.isOpened) {
                    item.isOpened = false
                    shrinkContents(holder.bindingAdapterPosition + 1)
                } else {
                    item.isOpened = true
                    expandContents(holder.bindingAdapterPosition + 1)
                }
            }
        }

        private fun expandContents(startPosition: Int) {
            var endPosition = startPosition
            while (itemList.size > endPosition && itemList[endPosition].type == CONTENT) {
                itemList[endPosition].isOpened = true
                endPosition++
            }

            // contents redraw
            notifyItemRangeChanged(startPosition, endPosition - 1)

            // header redraw
            notifyItemRangeChanged(startPosition-1, 1)
        }

        private fun shrinkContents(startPosition: Int) {
            var endPosition = startPosition
            while (itemList.size > endPosition && itemList[endPosition].type == CONTENT) {
                itemList[endPosition].isOpened = false
                endPosition++
            }

            // contents redraw
            notifyItemRangeChanged(startPosition, endPosition - 1)

            // header redraw
            notifyItemRangeChanged(startPosition-1, 1)
        }

        private fun bindContent(holder: ContentViewHolder, item: BofRouteSearchPopup.Item, position: Int) {
            resizeContent(holder, item.isOpened)

            with (holder.container) {
                item.schedule?.let { bofLane ->
                    tv_lane_cd.text = bofLane.serviceLaneCode
                    tv_lane_desc.text = bofLane.serviceLaneName

                    if (bofLane.isLaneChecked) {
                        tv_lane_cd.setTextColor(context.getColor(R.color.blue_violet))
                        tv_lane_desc.setTextColor(context.getColor(R.color.blue_violet))
                    } else {
                        tv_lane_cd.setTextColor(context.getColor(R.color.greyish_brown))
                        tv_lane_desc.setTextColor(context.getColor(R.color.greyish_brown))
                    }

                    lo_bof_content_lane.setSafeOnClickListener {
                        bofLane.isLaneChecked = !bofLane.isLaneChecked
                        onItemClick(position)
                    }
                }
            }
        }

        private fun resizeContent(holder: ContentViewHolder, isOpened: Boolean) {
            val container = holder.backgroundContainer
            if(isOpened) {
                container.visibility = View.VISIBLE
                container.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            } else {
                container.visibility = View.GONE
                container.layoutParams = FrameLayout.LayoutParams(0, 0)
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val container: RelativeLayout = itemView.findViewById(R.id.item_bof_header_lane)
            val title: TextView = itemView.findViewById(R.id.tv_header_bof_lane_title)
            val arrow: ImageView = itemView.findViewById(R.id.iv_header_bof_lane_arrow)
        }

        class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: FrameLayout = itemView.findViewById(R.id.item_bof_content_lane)
            val container: LinearLayout = itemView.findViewById(R.id.lo_bof_content_lane)
        }
    }
}