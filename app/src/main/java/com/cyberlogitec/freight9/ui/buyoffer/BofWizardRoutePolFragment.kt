package com.cyberlogitec.freight9.ui.buyoffer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toDp
import com.cyberlogitec.freight9.lib.util.toJson
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_pol_frag.*
import kotlinx.android.synthetic.main.body_bof_pol_frag.recycler_view
import kotlinx.android.synthetic.main.body_bof_pol_frag.recycler_view_selected_ports
import kotlinx.android.synthetic.main.item_bof_content_pol.view.*
import kotlinx.android.synthetic.main.item_bof_select_pol_horizontal.view.*
import timber.log.Timber

class BofWizardRoutePolFragment constructor(val viewModel: BofWizardRouteViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var allSchedules: List<Schedule>

    /**
     * POLs list 에 대한 Recycler view의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemCheck = { clickItem(it) }
                }
    }

    /**
     * 선택된 POLs list 에 대한 Horizontal Recycler view의 adapter
     */
    private val adapter2 by lazy {
        RecyclerAdapter2()
                .apply {
                    onItemRemove = { clickRemoveItem(it) }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_route_pol, container, false)

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
        viewModel.outPuts.onGoToStepPol()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { schedules ->

                        bof_pol_frag_root.run {
                            scrollTo(0, 0)
                            header = ll_bof_pol_horizontal_list
                            stickListener = { _ ->
                                // Do nothing
                            }
                            freeListener = { _ ->
                                // Do nothing
                            }
                        }

                        allSchedules = schedules
                        Timber.d("f9: it.polSubRegionName -> ${ schedules.map { it.polSubRegionName  }.distinct() }")
                        val items = getItems(schedules)
                        adapter.itemList.clear()
                        adapter.itemList = items.toMutableList()
                        adapter.notifyDataSetChanged()

                        adapter2.itemList.clear()
                        adapter2.notifyDataSetChanged()

                        setDataStatus(Pair(BofWizardRouteActivity.STEP_POL, Pair(false, null)))
                    }
                }

        viewModel.outPuts.onStepPolInitList()
                .bindToLifecycle(this)
                .subscribe {
                    clickRemoveAllItem()
                }

        viewModel.outPuts.onSearchPolPopup()
                .bindToLifecycle(this)
                .subscribe {
                    showSearchPopup()
                }
    }

    private fun setListener() {

    }

    /**
     * "POLs" : Recyclerview <-> Adapter
     * Selected "POLs" : Recyclerview <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRoutePolFragment.context)
            adapter = this@BofWizardRoutePolFragment.adapter
        }

        recycler_view_selected_ports.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRoutePolFragment.context,
                    RecyclerView.HORIZONTAL, false)
            adapter = this@BofWizardRoutePolFragment.adapter2
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * POL 의 Region name, SubRegion name 별 Code 들로 구성된 items 추출
     */
    private fun getItems(schedules: List<Schedule>): List<BofRouteSearchPopup.Item> {
        val items = mutableListOf<BofRouteSearchPopup.Item>()

        val polRegionNames = schedules
                .distinctBy { it.polCode }
                .distinctBy{ it.polRegionName }
                .map{ it.polRegionName }

        for ((headerIndex, x) in polRegionNames.withIndex()) {
            val header = BofRouteSearchPopup.Item.Builder()
                    .type(BofRouteSearchPopup.HEADER)
                    .title(x)
                    .schedule( null )
                    .isOpened(x == polRegionNames.first())
                    .build()
            items.add(header)

            val subRegionNames = schedules.filter{ it.polRegionName == x }.map { it.polSubRegionName }.distinct()
            for (y in subRegionNames) {
                val subHeader = BofRouteSearchPopup.Item.Builder()
                        .type(BofRouteSearchPopup.SUB_HEADER)
                        .title(y)
                        .schedule( null )
                        .isOpened(x == polRegionNames.first())
                        .isSubOpened(x == polRegionNames.first())
                        .build()
                items.add(subHeader)

                val polSchedules = schedules.filter{ it.polRegionName == x }.filter{ it.polSubRegionName == y }.distinctBy { it.polCode }
                for (z in polSchedules) {
                    val content = BofRouteSearchPopup.Item.Builder()
                            .type(BofRouteSearchPopup.CONTENT)
                            .title(null)
                            .schedule( z )
                            .isOpened(headerIndex == 0) // open cond is same as subheader
                            .isSubOpened(headerIndex == 0) // open cond is same as subheader
                            .build()
                    items.add(content)
                }
            }
        }
        return items
    }

    //----------------------------------------------------------------------------------------------

    /**
     * POLs item의 select 처리
     */
    private fun clickItem(position: Int) {
        adapter.itemList[position].schedule?.let { schedule ->
            val isExist = adapter2.itemList.filter { it.polCode == schedule.polCode }.isNotEmpty()
            if (!isExist) {
                adapter2.itemList.add(schedule)
                adapter2.notifyDataSetChanged()
            }

            val polCd = schedule.polCode
            adapter.itemList
                    .filter{ it.schedule != null }
                    .filter{ it.schedule?.polCode == polCd }
                    .map{ it.schedule?.isPolChecked = true }
        }
        setDataStatus()
    }

    private fun clickRemoveItem(schedule: Schedule) {
        adapter2.itemList.remove(schedule)
        adapter2.notifyDataSetChanged()

        val polCd = schedule.polCode
        adapter.itemList
                .filter{ it.schedule != null }
                .filter{ it.schedule?.polCode == polCd }
                .map{ it.schedule?.isPolChecked = false }
        adapter.notifyDataSetChanged()
        setDataStatus()
    }

    private fun clickRemoveAllItem(){
        adapter2.itemList.clear()
        adapter2.notifyDataSetChanged()

        adapter.itemList
                .filter{ it.schedule != null }
                .map{ it.schedule?.isPolChecked = false }
        adapter.notifyDataSetChanged()
        setHorizontalVisible(false)
    }

    /**
     * POLs item 을 selected 한 경우 ui refresh
     */
    private fun setDataStatus() {
        val schedules = adapter.itemList
                .filter { it.schedule != null }
                .map { it.schedule!! }
        val polCheckedSchedules = schedules.filter { it.isPolChecked }
        for (schedule in allSchedules) {
            for (polCheckedSchedule in polCheckedSchedules) {
                if (schedule.polCode == polCheckedSchedule.polCode && polCheckedSchedule.isPolChecked) {
                    schedule.isPolChecked = true
                }
            }
        }

        val selAllSchedules = allSchedules
                .filter { it.isPolChecked }.toMutableList()

        val isPolCheckedCount = adapter.itemList.count{ it.schedule?.isPolChecked == true }
        setHorizontalVisible(isPolCheckedCount > 0)

        setDataStatus(Pair(BofWizardRouteActivity.STEP_POL,
                Pair(isPolCheckedCount > 0, selAllSchedules)))
    }

    private fun setDataStatus(pair: Pair<Int, Pair<Boolean, List<Schedule>?>>) {
        viewModel.inPuts.requestSetDataStatus(pair)
    }

    private fun setHorizontalVisible(isVisible: Boolean) {
        ll_bof_pol_horizontal_list.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    //----------------------------------------------------------------------------------------------

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showSearchPopup() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_bof_route_search, null)
        popupWindow = BofRouteSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onSearchItemClick)
        (popupWindow as BofRouteSearchPopup).initValue(BofRouteSearchPopup.DataKind.KIND_POL, adapter.itemList)
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
        selectedItem.schedule?.let { schedule ->
            val isExist = adapter2.itemList.filter { it.polCode == schedule.polCode }.isNotEmpty()
            if (!isExist) {
                adapter2.itemList.add(schedule)
                adapter2.notifyDataSetChanged()
            }

            val polCd = schedule.polCode
            adapter.itemList
                    .filter { it.schedule != null }
                    .filter { it.schedule?.polCode == polCd }
                    .map { it.schedule?.isPolChecked = true }
            adapter.notifyDataSetChanged()
        }
        setDataStatus()
    }

    //----------------------------------------------------------------------------------------------

    /**
     * recyclerview adapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var onItemCheck: (Int) -> Unit = {}
        var itemList = mutableListOf<BofRouteSearchPopup.Item>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            var holder: RecyclerView.ViewHolder? = null

            when(viewType) {
                BofRouteSearchPopup.HEADER -> holder =
                        HeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_header_pol, parent, false)
                        )
                BofRouteSearchPopup.SUB_HEADER -> holder =
                        SubHeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_sub_header_pol, parent, false)
                        )
                BofRouteSearchPopup.CONTENT -> holder =
                        ContentViewHolder(
                                inflater.inflate(R.layout.item_bof_content_pol, parent, false)
                        )
            }
            return holder ?: throw IllegalStateException("Item type unspecified.")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                val item = itemList[holder.bindingAdapterPosition]
                when(item.type) {
                    BofRouteSearchPopup.HEADER -> {
                        bindHeader(holder as HeaderViewHolder, item)
                    }
                    BofRouteSearchPopup.SUB_HEADER -> {
                        bindSubHeader(holder as SubHeaderViewHolder, item)
                    }
                    BofRouteSearchPopup.CONTENT -> {
                        bindContent(holder as ContentViewHolder, item)
                    }
                }
            }
        }

        override fun getItemCount() = itemList.count()

        override fun getItemViewType(position: Int) = itemList[position].type

        private fun bindHeader(holder: HeaderViewHolder, item: BofRouteSearchPopup.Item) {
            if(item.isOpened) {
                holder.arrow.setImageResource(R.drawable.default_white_up)
            } else {
                holder.arrow.setImageResource(R.drawable.selected_white_down)
            }

            item.title?.let {
                holder.title.text = it
            }

            holder.backgroundContainer.setSafeOnClickListener {
                if(item.isOpened) {
                    item.isOpened = false
                    shrinkContents(holder.bindingAdapterPosition + 1, BofRouteSearchPopup.HEADER)
                } else {
                    item.isOpened = true
                    expandContents(holder.bindingAdapterPosition + 1, BofRouteSearchPopup.HEADER)
                }

                notifyItemRangeChanged(holder.bindingAdapterPosition, 1)
            }
        }

        private fun bindSubHeader(holder: SubHeaderViewHolder, item: BofRouteSearchPopup.Item) {
            resizeSubHeader(holder, item.isOpened, item.isSubOpened)

            if(item.isSubOpened) {
                holder.arrow.setImageResource(R.drawable.default_white_up)
            } else {
                holder.arrow.setImageResource(R.drawable.selected_white_down)
            }

            item.title?.let {
                holder.title.text = it
            }

            holder.backgroundContainer.setSafeOnClickListener {
                if(item.isSubOpened) {
                    item.isSubOpened = false
                    shrinkContents(holder.bindingAdapterPosition + 1, BofRouteSearchPopup.SUB_HEADER)
                } else {
                    item.isSubOpened = true
                    expandContents(holder.bindingAdapterPosition + 1, BofRouteSearchPopup.SUB_HEADER)
                }
                notifyItemRangeChanged(holder.bindingAdapterPosition, 1)
            }
        }

        private fun expandContents(startPosition: Int, type: Int) {
            var endPosition = startPosition
            when(type) {
                BofRouteSearchPopup.HEADER -> {
                    while ((endPosition < itemList.size) && (itemList[endPosition].type == BofRouteSearchPopup.CONTENT || itemList[endPosition].type == BofRouteSearchPopup.SUB_HEADER)) {
                        itemList[endPosition].isOpened = true
                        itemList[endPosition].isSubOpened = true
                        endPosition++
                    }
                }
                BofRouteSearchPopup.SUB_HEADER -> {
                    while ((endPosition < itemList.size) && (itemList[endPosition].type == BofRouteSearchPopup.CONTENT)) {
                        itemList[endPosition].isOpened = true
                        endPosition++
                    }
                }
            }

            // contents redraw
            notifyItemRangeChanged(startPosition, endPosition - 1)

            // header redraw
            notifyItemRangeChanged(startPosition-1, 1)
        }

        private fun shrinkContents(startPosition: Int, type: Int) {
            var endPosition = startPosition
            when(type) {
                BofRouteSearchPopup.HEADER -> {
                    while ( (endPosition < itemList.size )  && (itemList[endPosition].type == BofRouteSearchPopup.CONTENT || itemList[endPosition].type == BofRouteSearchPopup.SUB_HEADER) ) {
                        itemList[endPosition].isOpened = false
                        itemList[endPosition].isSubOpened = false
                        endPosition++
                    }
                }
                BofRouteSearchPopup.SUB_HEADER -> {
                    while ( (endPosition < itemList.size )  && (itemList[endPosition].type == BofRouteSearchPopup.CONTENT) ) {
                        itemList[endPosition].isOpened = false
                        endPosition++
                    }
                }
            }

            // contents redraw
            notifyItemRangeChanged(startPosition, endPosition - 1)

            // header redraw
            notifyItemRangeChanged(startPosition-1, 1)
        }

        private fun bindContent(holder: ContentViewHolder, item: BofRouteSearchPopup.Item) {
            resizeContent(holder, item.isOpened)

            with(holder.container) {
                item.schedule?.let {
                    tv_content_polCd.text = it.polCode
                    tv_content_polNm.text = it.polName

                    if (it.isPolChecked) {
                        holder.container.setBackgroundResource(R.color.blue_violet)
                        tv_content_polCd.setTextColor(context.getColor(R.color.white))
                        tv_content_polNm.setTextColor(context.getColor(R.color.white))
                    } else {
                        holder.container.setBackgroundResource(R.color.pale_gray)
                        tv_content_polCd.setTextColor(context.getColor(R.color.greyish_brown))
                        tv_content_polNm.setTextColor(context.getColor(R.color.greyish_brown))
                    }
                }
            }

            holder.container.setSafeOnClickListener {
                item.schedule?.let {
                    it.isPolChecked = true
                    onItemCheck(holder.bindingAdapterPosition)
                    notifyItemChanged(holder.bindingAdapterPosition)
                }
            }
        }

        private fun resizeContent(holder: ContentViewHolder, isOpened: Boolean) {
            val container = holder.backgroundContainer
            if(isOpened) {
                container.visibility = View.VISIBLE
                container.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 40.toDp().toInt())
            } else {
                container.visibility = View.GONE
                container.layoutParams = FrameLayout.LayoutParams(0, 0)
            }
        }

        private fun resizeSubHeader(holder: SubHeaderViewHolder, isOpened: Boolean, isSubOpened: Boolean) {
            val container = holder.backgroundContainer
            if (!isOpened && !isSubOpened) {
                container.visibility = View.GONE
                container.layoutParams = FrameLayout.LayoutParams(0, 0)
            } else {
                container.visibility = View.VISIBLE
                container.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 40.toDp().toInt())
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: RelativeLayout = itemView.findViewById(R.id.item_bof_header_pol)
            val title: TextView = itemView.findViewById(R.id.header_title)
            val arrow: ImageView = itemView.findViewById(R.id.header_arrow)
        }

        class SubHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: LinearLayout = itemView.findViewById(R.id.item_bof_sub_header_pol)
            val title: TextView = itemView.findViewById(R.id.sub_header_title)
            val arrow: ImageView = itemView.findViewById(R.id.sub_header_arrow)
        }

        class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: FrameLayout = itemView.findViewById(R.id.item_bof_content_pol)
            val container: LinearLayout = itemView.findViewById(R.id.lo_bof_content_pol)
        }
    }

    /**
     * Horizontal Selected POLs recyclerview adapter
     */
    private class RecyclerAdapter2 : RecyclerView.Adapter<RecyclerAdapter2.ViewHolder>() {
        var onItemRemove: (Schedule) -> Unit = {}
        var itemList = mutableListOf<Schedule>()

        override fun getItemCount() = itemList.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_select_pol_horizontal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = itemList[position]

                // set pol
                this.tv_polCd.text = data.polCode
                this.tv_polNm.text = data.polName

                this.iv_pol_close.setSafeOnClickListener {
                    onItemRemove(data)
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardRouteViewModel) : BofWizardRoutePolFragment {
            return BofWizardRoutePolFragment(viewModel)
        }
    }
}