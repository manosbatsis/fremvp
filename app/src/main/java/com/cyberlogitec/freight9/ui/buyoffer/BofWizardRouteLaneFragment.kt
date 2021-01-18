package com.cyberlogitec.freight9.ui.buyoffer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toJson
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_lane.*
import kotlinx.android.synthetic.main.item_bof_content_lane.view.*
import timber.log.Timber

class BofWizardRouteLaneFragment constructor(val viewModel: BofWizardRouteViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var allSchedules: List<Schedule>

    /**
     * Service Lane list 에 대한 Recycler view의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemClick = { clickItem(it) }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_route_lane, container, false)

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
        recyclerViewInit()
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        viewModel.outPuts.onGoToStepLane()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { schedules ->

                        bof_lane_root.scrollTo(0, 0)
                        allSchedules = schedules
                        adapter.itemList.clear()

                        // set as item (for list view)
                        val items = getItems( schedules )
                        adapter.itemList = items.toMutableList()

                        adapter.notifyDataSetChanged()

                        // init
                        setSearchSelectData("", "")
                        setDataStatus(Pair(BofWizardRouteActivity.STEP_LANE, Pair(false, null)))
                    }
                }

        viewModel.outPuts.onSearchRemove()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { serviceLaneCode ->
                        adapter.itemList.filter{ it.schedule?.serviceLaneCode == serviceLaneCode }.let { item2 ->
                            item2.map{ it.schedule!!.isLaneChecked = false }
                        }

                        adapter.notifyDataSetChanged()

                        // init
                        setSearchSelectData("", "")
                        setDataStatus(Pair(BofWizardRouteActivity.STEP_LANE, Pair(false, null)))
                    }
                }

        viewModel.outPuts.onSearchLanePopup()
                .bindToLifecycle(this)
                .subscribe {
                    showSearchPopup()
                }
    }

    private fun setListener() {

    }

    /**
     * "Service Lane" : Recyclerview <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRouteLaneFragment.context)
            adapter = this@BofWizardRouteLaneFragment.adapter
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Service Lane 의 SubRegion 별 Code 들로 구성된 items 추출
     */
    private fun getItems(schedules: List<Schedule>): List<BofRouteSearchPopup.Item> {
        val items = mutableListOf<BofRouteSearchPopup.Item>()

        val polSubRegionName = schedules.distinctBy{ it.polSubRegionName }.map{ it.polSubRegionName }
        for (x in polSubRegionName) {
            val header = BofRouteSearchPopup.Item.Builder()
                    .type(BofRouteSearchPopup.HEADER)
                    .title(x)
                    .isOpened(x == polSubRegionName.first())
                    .build()
            items.add(header)

            val rows = schedules.filter { it.polSubRegionName == x }.distinctBy { it.serviceLaneCode }

            for (y in rows) {
                val content = BofRouteSearchPopup.Item.Builder()
                        .type(BofRouteSearchPopup.CONTENT)
                        .schedule(y)
                        .isOpened(x == polSubRegionName.first())
                        .build()
                items.add(content)
            }
        }
        return items
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Service Lane item의 select 처리
     */
    private fun clickItem(position: Int) {
        adapter.itemList.map{ it.schedule?.isLaneChecked = false }

        adapter.itemList[position].schedule?.let { schedule ->
            var serviceLaneCode = ""
            var serviceLaneName = ""
            schedule.isLaneChecked = !schedule.isLaneChecked
            if (schedule.isLaneChecked) {
                serviceLaneCode = schedule.serviceLaneCode
                serviceLaneName = schedule.serviceLaneName
                adapter.itemList.filter{ it.schedule?.serviceLaneCode == schedule.serviceLaneCode }.let { item2 ->
                    item2.map{ it.schedule!!.isLaneChecked = true }
                }
            } else {
                adapter.itemList.filter{ it.schedule?.serviceLaneCode == schedule.serviceLaneCode }.let { item2 ->
                    item2.map{ it.schedule!!.isLaneChecked = false }
                }
            }
            setSearchSelectData(serviceLaneCode, serviceLaneName)
        }
        adapter.notifyDataSetChanged()

        setDataStatus()
    }

    /**
     * Service Lane item 을 selected 한 경우 ui refresh
     */
    private fun setDataStatus() {
        var selSchedules = mutableListOf<Schedule>()
        adapter.itemList.filter{ it.schedule != null }.count{ it.schedule!!.isLaneChecked }.let {
            if (it > 0) {
                selSchedules = adapter.itemList.filter{ it.schedule != null }.filter{ it.schedule!!.isLaneChecked }.map{ it.schedule!! }.toMutableList()
            } else {
                selSchedules = adapter.itemList.filter{ it.schedule != null }.map{ it.schedule!! }.toMutableList()
            }
        }

        val serviceLaneCode = selSchedules.first().serviceLaneCode
        val selAllSchedules = allSchedules
                .filter { it.serviceLaneCode == serviceLaneCode }
                .distinctBy { it.polCode }.toMutableList()

        setDataStatus(Pair(BofWizardRouteActivity.STEP_LANE,
                Pair(selAllSchedules.size > 0, selAllSchedules)))
    }

    private fun setDataStatus(pair: Pair<Int, Pair<Boolean, List<Schedule>?>>) {
        viewModel.inPuts.requestSetDataStatus(pair)
    }

    /**
     * Service Lane 을 선택한 경우 search area 의 ui refresh
     */
    private fun setSearchSelectData(serviceLaneCode: String, serviceLaneName: String) {
        viewModel.inPuts.requestSearchSelectSet(Pair(serviceLaneCode, serviceLaneName))
    }

    //----------------------------------------------------------------------------------------------

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showSearchPopup() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_bof_route_search, null)
        popupWindow = BofRouteSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onSearchItemClick)
        (popupWindow as BofRouteSearchPopup).initValue(BofRouteSearchPopup.DataKind.KIND_LANE, adapter.itemList)
        (popupWindow as BofRouteSearchPopup).setOnDismissListener { removePopup() }
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
                        setSearchSelectData(it.schedule.serviceLaneCode, it.schedule.serviceLaneName)
                    }
                }
        adapter.notifyDataSetChanged()
        setDataStatus()
    }

    //----------------------------------------------------------------------------------------------

    /**
     * recyclerview adapter
     */
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
                BofRouteSearchPopup.HEADER -> holder =
                        HeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_header_lane, parent, false)
                        )
                BofRouteSearchPopup.CONTENT -> holder =
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
                    BofRouteSearchPopup.HEADER -> {
                        bindHeader(holder as HeaderViewHolder, item)
                    }
                    BofRouteSearchPopup.CONTENT -> {
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
            while (itemList.size > endPosition && itemList[endPosition].type == BofRouteSearchPopup.CONTENT) {
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
            while (itemList.size > endPosition && itemList[endPosition].type == BofRouteSearchPopup.CONTENT) {
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

    //----------------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardRouteViewModel) : BofWizardRouteLaneFragment {
            return BofWizardRouteLaneFragment(viewModel)
        }
    }
}