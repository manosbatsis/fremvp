package com.cyberlogitec.freight9.ui.buyoffer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
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
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_wizard_route.*
import kotlinx.android.synthetic.main.appbar_your_offers.*
import kotlinx.android.synthetic.main.body_bof_pod_frag.*
import kotlinx.android.synthetic.main.body_bof_pod_frag.recycler_view
import kotlinx.android.synthetic.main.body_bof_pod_frag.recycler_view_selected_ports
import kotlinx.android.synthetic.main.body_bof_pol_frag.*
import kotlinx.android.synthetic.main.body_your_offers.*
import kotlinx.android.synthetic.main.item_bof_content_pod.view.*
import kotlinx.android.synthetic.main.item_bof_select_pod_horizontal.view.*
import timber.log.Timber

class BofWizardRoutePodFragment constructor(val viewModel: BofWizardRouteViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var intentSchedules: List<Schedule>

    /**
     * PODs list 에 대한 Recycler view의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemCheck = { clickItem(it) }
                }
    }

    /**
     * 선택된 PODs list 에 대한 Horizontal Recycler view의 adapter
     */
    private val adapter2 by lazy {
        RecyclerAdapter2()
                .apply {
                    onItemRemove = { clickRemoveItem(it) }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_route_pod, container, false)

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
        viewModel.outPuts.onGoToStepPod()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { schedules ->

                        bof_pod_frag_root.run {
                            scrollTo(0, 0)
                            header = ll_bof_pod_horizontal_list
                            stickListener = { _ ->
                                // Do nothing
                            }
                            freeListener = { _ ->
                                // Do nothing
                            }
                        }

                        intentSchedules = schedules
                        adapter.itemList.clear()
                        adapter2.itemList.clear()

                        /**
                         * Service Lane, POLs 에 해당되는 schedule list 요청
                         */
                        for (schedule in intentSchedules.filter{it.isPolChecked}) {
                            viewModel.inPuts.requestLoadSchedulePartialData(schedule)
                        }
                    }
                }

        /**
         * Service Lane, POLs 에 해당되는 schedule list 구성
         */
        viewModel.outPuts.onLoadSchedulePartialData()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { schedules ->
                        val items = getItems(schedules)

                        for (item in items) {
                            if (adapter.itemList.filter {adapterItem ->
                                        (adapterItem.title == item.title && adapterItem.type == item.type
                                                && (item.type == BofRouteSearchPopup.HEADER || item.type == BofRouteSearchPopup.SUB_HEADER)) ||
                                                (item.type == BofRouteSearchPopup.CONTENT && adapterItem.schedule != null && item.schedule != null
                                                        && adapterItem.schedule.podCode == item.schedule.podCode)
                                    }.isNotEmpty()) {
                                continue
                            } else {
//                                if (item.type == CONTENT) {
//                                    item.schedule?.isPolChecked = true
//                                }
                                adapter.itemList.add(item)
                            }
                        }

                        adapter.itemList
                                .filter { it.schedule != null }
                        //.map { it.schedule?.isPolChecked = true}

                        adapter.notifyDataSetChanged()
                        adapter2.notifyDataSetChanged()

                        setDataStatus(Pair(BofWizardRouteActivity.STEP_POD, Pair(false, null)))
                    }
                }

        viewModel.outPuts.onStepPodInitList()
                .bindToLifecycle(this)
                .subscribe {
                    clickRemoveAllItem()
                }

        viewModel.outPuts.onSearchPodPopup()
                .bindToLifecycle(this)
                .subscribe {
                    showSearchPopup()
                }
    }

    private fun setListener() {

    }

    /**
     * "PODs" : Recyclerview <-> Adapter
     * Selected "PODs" : Recyclerview <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRoutePodFragment.context)
            adapter = this@BofWizardRoutePodFragment.adapter
        }

        recycler_view_selected_ports.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRoutePodFragment.context,
                    RecyclerView.HORIZONTAL, false)
            adapter = this@BofWizardRoutePodFragment.adapter2
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * POD 의 Region name, SubRegion name 별 Code 들로 구성된 items 추출
     */
    private fun getItems(schedules: List<Schedule>): List<BofRouteSearchPopup.Item> {
        val items = mutableListOf<BofRouteSearchPopup.Item>()

        val podRegionNames = schedules.distinctBy{ it.podRegionName }.map{ it.podRegionName }

        for ((headerIndex, x) in podRegionNames.withIndex()) {
            val header = BofRouteSearchPopup.Item.Builder()
                    .type(BofRouteSearchPopup.HEADER)
                    .title(x)
                    .schedule( null )
                    .isOpened(x == podRegionNames.first())
                    .build()
            items.add(header)

            val subRegionNames = schedules.filter{ it.podRegionName == x }.map { it.podSubRegionName }.distinct()

            for (y in subRegionNames) {
                val subHeader = BofRouteSearchPopup.Item.Builder()
                        .type(BofRouteSearchPopup.SUB_HEADER)
                        .title(y)
                        .schedule( null )
                        .isOpened(x == podRegionNames.first())
                        .isSubOpened(x == podRegionNames.first())
                        .build()
                items.add(subHeader)

                val podSchedules = schedules.filter{ it.podRegionName == x }.filter{ it.podSubRegionName == y }.distinctBy { it.podCode }
                for (z in podSchedules) {
                    Timber.d("f9: \n31-> x:${ x }, y:${ y }, z.podCode --> ${ z.podCode }")

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
     * PODs item의 select 처리
     */
    private fun clickItem(position: Int) {
        adapter.itemList[position].schedule?.let { schedule ->
            val isExist = adapter2.itemList.filter { it.podCode == schedule.podCode }.isNotEmpty()
            if (!isExist) {
                adapter2.itemList.add(schedule)
                adapter2.notifyDataSetChanged()
            }

            val podCd = schedule.podCode
            adapter.itemList
                    .filter{ it.schedule != null }
                    .filter{ it.schedule?.podCode == podCd }
                    .map{ it.schedule?.isPodChecked = true; it }
                    .map{ it.schedule?.isPolChecked = true }
        }
        setDataStatus()
    }

    private fun clickRemoveItem(schedule: Schedule) {
        adapter2.itemList.remove( schedule )
        adapter2.notifyDataSetChanged()

        val podCd = schedule.podCode
        adapter.itemList
                .find{ it.schedule?.podCode == podCd }?.schedule?.let {
                    it.isPodChecked = false
                    it.isPolChecked = false
                }
        adapter.notifyDataSetChanged()
        setDataStatus()
    }

    private fun clickRemoveAllItem(){
        adapter2.itemList.clear()
        adapter2.notifyDataSetChanged()

        adapter.itemList.map {
            it.schedule?.let {
                it.isPodChecked = false
                it.isPolChecked = false
            }
        }
        adapter.notifyDataSetChanged()
        setHorizontalVisible(false)
    }

    /**
     * PODs item 을 selected 한 경우 ui refresh
     */
    private fun setDataStatus() {
        val schedules = adapter.itemList
                .filter { it.schedule != null }
                .map { it.schedule!! }.toMutableList()

        schedules.addAll(intentSchedules)

        val isPodCheckedCount = adapter.itemList.count{ it.schedule?.isPodChecked == true }
        setHorizontalVisible(isPodCheckedCount > 0)
        setDataStatus(Pair(BofWizardRouteActivity.STEP_POD,
                Pair(isPodCheckedCount > 0, schedules)))
    }

    private fun setDataStatus(pair: Pair<Int, Pair<Boolean, List<Schedule>?>>) {
        viewModel.inPuts.requestSetDataStatus(pair)
    }

    private fun setHorizontalVisible(isVisible: Boolean) {
        ll_bof_pod_horizontal_list.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    //----------------------------------------------------------------------------------------------

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showSearchPopup() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_bof_route_search, null)
        popupWindow = BofRouteSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onSearchItemClick)
        (popupWindow as BofRouteSearchPopup).initValue(BofRouteSearchPopup.DataKind.KIND_POD, adapter.itemList)
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
            val isExist = adapter2.itemList.filter { it.podCode == schedule.podCode }.isNotEmpty()
            if (!isExist) {
                adapter2.itemList.add(schedule)
                adapter2.notifyDataSetChanged()
            }

            val podCd = schedule.podCode
            adapter.itemList
                    .filter { it.schedule != null }
                    .filter { it.schedule?.podCode == podCd }
                    .map { it.schedule?.isPodChecked = true }
            adapter.notifyDataSetChanged()

            setDataStatus()
        }
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
                                inflater.inflate(R.layout.item_bof_header_pod, parent, false)
                        )
                BofRouteSearchPopup.SUB_HEADER -> holder =
                        SubHeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_sub_header_pod, parent, false)
                        )
                BofRouteSearchPopup.CONTENT -> holder =
                        ContentViewHolder(
                                inflater.inflate(R.layout.item_bof_content_pod, parent, false)
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
                    tv_content_podCd.text = it.podCode
                    tv_content_podNm.text = it.podName

                    if (it.isPodChecked) {
                        holder.container.setBackgroundResource(R.color.blue_violet)
                        tv_content_podCd.setTextColor(context.getColor(R.color.white))
                        tv_content_podNm.setTextColor(context.getColor(R.color.white))
                    } else {
                        holder.container.setBackgroundResource(R.color.pale_gray)
                        tv_content_podCd.setTextColor(context.getColor(R.color.greyish_brown))
                        tv_content_podNm.setTextColor(context.getColor(R.color.greyish_brown))
                    }
                }
            }

            holder.container.setSafeOnClickListener {
                item.schedule?.let {
                    it.isPodChecked = true
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
            val backgroundContainer: RelativeLayout = itemView.findViewById(R.id.item_bof_header_pod)
            val title: TextView = itemView.findViewById(R.id.header_title)
            val arrow: ImageView = itemView.findViewById(R.id.header_arrow)
        }

        class SubHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: LinearLayout = itemView.findViewById(R.id.item_bof_sub_header_pod)
            val title: TextView = itemView.findViewById(R.id.sub_header_title)
            val arrow: ImageView = itemView.findViewById(R.id.sub_header_arrow)
        }

        class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val backgroundContainer: FrameLayout = itemView.findViewById(R.id.item_bof_content_pod)
            val container: LinearLayout = itemView.findViewById(R.id.lo_bof_content_pod)
        }
    }

    /**
     * Horizontal Selected PODs recyclerview adapter
     */
    private class RecyclerAdapter2 : RecyclerView.Adapter<RecyclerAdapter2.ViewHolder>() {
        var onItemRemove: (Schedule) -> Unit = {}
        var itemList = mutableListOf<Schedule>()

        override fun getItemCount() = itemList.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_select_pod_horizontal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = itemList[position]

                // set pod
                this.tv_podCd.text = data.podCode
                this.tv_podNm.text = data.podName

                this.iv_pod_close.setSafeOnClickListener {
                    onItemRemove(data)
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardRouteViewModel) : BofWizardRoutePodFragment {
            return BofWizardRoutePodFragment(viewModel)
        }
    }
}