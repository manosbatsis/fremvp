package com.cyberlogitec.freight9.ui.buyoffer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.toDp
import com.cyberlogitec.freight9.ui.buyoffer.BofRouteSearchPopup.Companion.CONTENT
import com.cyberlogitec.freight9.ui.buyoffer.BofRouteSearchPopup.Companion.HEADER
import com.cyberlogitec.freight9.ui.buyoffer.BofRouteSearchPopup.Companion.SUB_HEADER
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_pod.*
import kotlinx.android.synthetic.main.appbar_bof_pod.*
import kotlinx.android.synthetic.main.body_bof_pod.*
import kotlinx.android.synthetic.main.item_bof_content_pod.view.*
import kotlinx.android.synthetic.main.item_bof_select_pod_horizontal.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber


@RequiresActivityViewModel(value = BofPodVm::class)
class BofPodAct : BaseActivity<BofPodVm>() {

    lateinit var intentSchedules: List<Schedule>

    // list view
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemCheck = { viewModel.inPuts.clickToItemCheck(it) }
                }
    }

    // adapter2: week selector
    private val adapter2 by lazy {
        RecyclerAdapter2()
                .apply {
                    onItemRemove = { viewModel.inPuts.clickToItemRemove(it) }
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

        setContentView(R.layout.act_bof_pod)
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
        defaultbarInit(appbar_bof_pod,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_select_route),
                isEnableNavi = false)

        // init recycler views
        recyclerViewInit()
        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        intentSchedules = it
                        adapter.itemList.clear()
                        adapter2.itemList.clear()

                        for (schedule in intentSchedules.filter{it.isPolChecked}) {
                            viewModel.inPuts.requestSchedules(schedule)
                        }
                    }
                }

        viewModel.outPuts.onSuccessLoadSchedules()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        val items = getItems(it)

                        for (item in items) {
                            if (adapter.itemList.filter {adapterItem ->
                                        (adapterItem.title == item.title && adapterItem.type == item.type
                                                && (item.type == HEADER || item.type == SUB_HEADER)) ||
                                                (item.type == CONTENT && adapterItem.schedule != null && item.schedule != null
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

                        setButtonEnable()
                    }
                }

        viewModel.outPuts.onClickItemCheck()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickItemChecked --> $it")
                    it?.let { position ->
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
                    }
                    setButtonEnable()
                }

        viewModel.outPuts.onClickItemRemove()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickItemRemove --> $it")
                    it?.let {
                        adapter2.itemList.remove( it )
                        adapter2.notifyDataSetChanged()

                        val podCd = it.podCode
                        adapter.itemList
                                .find{ it.schedule?.podCode == podCd }?.schedule?.let {
                                    it.isPodChecked = false
                                    it.isPolChecked = false
                                }
                        adapter.notifyDataSetChanged()
                    }
                    setButtonEnable()
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickNext")
                    it?.let {
                        startActivity(Intent(this, BofRouteAct::class.java).putExtra(Intents.OFFER, it))
                    }
                }

        viewModel.outPuts.onClickSearch()
                .bindToLifecycle(this)
                .subscribe {
                    showSearchPopup()
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

    private fun setListener() {
        // wait click event (toolbar left button)
        toolbar_left_btn.setSafeOnClickListener {
            it.let {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        btn_pod_next.setSafeOnClickListener {
            Timber.d("f9: btn_pod_next click")
            var schedules = adapter.itemList
                    .filter { it.schedule != null }
                    .map { it.schedule!! }.toMutableList()

            schedules.addAll(intentSchedules)
            viewModel.inPuts.schedules( schedules )
            viewModel.inPuts.clickToNext( Parameter.CLICK )
        }

        ll_search.setSafeOnClickListener {
            viewModel.inPuts.clickToSearch(Parameter.CLICK)
        }
    }

    private fun setButtonEnable() {
        adapter.itemList.count{ it.schedule?.isPodChecked == true }.let { cnt ->
            btn_pod_next.isEnabled = cnt > 0
            ll_bof_pod_horizontal_list.visibility = if (btn_pod_next.isEnabled) View.VISIBLE else View.GONE
        }
    }

    private fun getItems(schedules: List<Schedule>): List<BofRouteSearchPopup.Item> {
        val items = mutableListOf<BofRouteSearchPopup.Item>()

        val podRegionNames = schedules.distinctBy{ it.podRegionName }.map{ it.podRegionName }
        Timber.d("f9: \n31 -> it.podRegionName -> $podRegionNames")

        for ((headerIndex, x) in podRegionNames.withIndex()) {
            Timber.d("f9: \n31 -> x -> $x")
            val header = BofRouteSearchPopup.Item.Builder()
                    .type(HEADER)
                    .title(x)
                    .schedule( null )
                    .isOpened(x == podRegionNames.first())
                    .build()
            items.add(header)

            val subRegionNames = schedules.filter{ it.podRegionName == x }.map { it.podSubRegionName }.distinct()
            Timber.d("f9: \n31 -> subRegionNames -> $subRegionNames")

            for (y in subRegionNames) {
                Timber.d("f9: \n31-> x:${ x }, y:${ y }")

                val subHeader = BofRouteSearchPopup.Item.Builder()
                        .type(SUB_HEADER)
                        .title(y)
                        .schedule( null )
                        .isOpened(x == podRegionNames.first())
                        .isSubOpened(x == podRegionNames.first())
                        .build()
                items.add(subHeader)

                val podSchedules = schedules.filter{ it.podRegionName == x }.filter{ it.podSubRegionName == y }.distinctBy { it.podCode }
                Timber.d("f9: \n31-> podSchedules.podCode --> ${ podSchedules.map{it.podCode} }")

                for (z in podSchedules) {
                    Timber.d("f9: \n31-> x:${ x }, y:${ y }, z.podCode --> ${ z.podCode }")

                    val content = BofRouteSearchPopup.Item.Builder()
                            .type(CONTENT)
                            .title(null)
                            .schedule( z )
                            .isOpened(headerIndex == 0) // open cond is same as subheader
                            .isSubOpened(headerIndex == 0) // open cond is same as subheader
                            .build()
                    items.add(content)
                }
            }
        }
        Timber.d("f9: \nitems.count :${ items.count() }")
        return items
    }

    private var popupWindow: PopupWindow? = null
    @SuppressLint("InflateParams")
    private fun showSearchPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_bof_route_search, null)
        popupWindow = BofRouteSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true, ::onSearchItemClick)
        (popupWindow as BofRouteSearchPopup).initValue(BofRouteSearchPopup.DataKind.KIND_POD, adapter.itemList)
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

            setButtonEnable()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofPodAct)
            adapter = this@BofPodAct.adapter
        }

        recycler_view_selected_ports.apply {
            layoutManager = LinearLayoutManager(this@BofPodAct, RecyclerView.HORIZONTAL, false)
            adapter = this@BofPodAct.adapter2
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var onItemCheck: (Int) -> Unit = {}
        var itemList = mutableListOf<BofRouteSearchPopup.Item>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            var holder: RecyclerView.ViewHolder? = null

            when(viewType) {
                HEADER -> holder =
                        HeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_header_pod, parent, false)
                        )
                SUB_HEADER -> holder =
                        SubHeaderViewHolder(
                                inflater.inflate(R.layout.item_bof_sub_header_pod, parent, false)
                        )
                CONTENT -> holder =
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
                    HEADER -> {
                        bindHeader(holder as HeaderViewHolder, item)
                    }
                    SUB_HEADER -> {
                        bindSubHeader(holder as SubHeaderViewHolder, item)
                    }
                    CONTENT -> {
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
                    Timber.d("f9: open -> close")
                    item.isOpened = false
                    shrinkContents(holder.bindingAdapterPosition + 1, HEADER)
                } else {
                    Timber.d("f9: close -> open")
                    item.isOpened = true
                    expandContents(holder.bindingAdapterPosition + 1, HEADER)
                }

                notifyItemRangeChanged(holder.bindingAdapterPosition, 1)
            }
        }

        private fun bindSubHeader(holder: SubHeaderViewHolder, item: BofRouteSearchPopup.Item) {

            resizeSubHeader(holder, item.isOpened, item.isSubOpened)

            Timber.d("f9: bindSubHeader -> position: ${holder.bindingAdapterPosition}, item.isSubOpened: ${item.isSubOpened}")

            if(item.isSubOpened) {
                holder.arrow.setImageResource(R.drawable.default_white_up)
            } else {
                holder.arrow.setImageResource(R.drawable.selected_white_down)
            }

            item.title?.let {
                holder.title.text = it
            }

            holder.backgroundContainer.setSafeOnClickListener {
                Timber.d("f9: bindSubHeader -> click")
                if(item.isSubOpened) {
                    Timber.d("f9: open -> close")
                    item.isSubOpened = false
                    shrinkContents(holder.bindingAdapterPosition + 1, SUB_HEADER)
                } else {
                    Timber.d("f9: close -> open")
                    item.isSubOpened = true
                    expandContents(holder.bindingAdapterPosition + 1, SUB_HEADER)
                }
                Timber.d("f9: bindSubHeader -> item.isOpened: ${item.isOpened}")
                notifyItemRangeChanged(holder.bindingAdapterPosition, 1)
            }
        }

        private fun expandContents(startPosition: Int, type: Int) {
            Timber.d("f9: expandContents: $startPosition")

            var endPosition = startPosition
            when(type) {
                HEADER -> {
                    while ((endPosition < itemList.size) && (itemList[endPosition].type == CONTENT || itemList[endPosition].type == SUB_HEADER)) {
                        itemList[endPosition].isOpened = true
                        itemList[endPosition].isSubOpened = true
                        endPosition++
                    }
                }
                SUB_HEADER -> {
                    while ((endPosition < itemList.size) && (itemList[endPosition].type == CONTENT)) {
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
            Timber.d("f9: shrinkContents: $startPosition")

            var endPosition = startPosition
            when(type) {
                HEADER -> {
                    while ( (endPosition < itemList.size )  && (itemList[endPosition].type == CONTENT || itemList[endPosition].type == SUB_HEADER) ) {
                        itemList[endPosition].isOpened = false
                        itemList[endPosition].isSubOpened = false
                        endPosition++
                    }
                }
                SUB_HEADER -> {
                    while ( (endPosition < itemList.size )  && (itemList[endPosition].type == CONTENT) ) {
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
                Timber.d("f9: click: ${holder.bindingAdapterPosition}")
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

    // Pod selector adapter
    private class RecyclerAdapter2 : RecyclerView.Adapter<RecyclerAdapter2.ViewHolder>() {
        var onItemRemove: (Schedule) -> Unit = {}
        var itemList = mutableListOf<Schedule>()

        override fun getItemCount() = itemList.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_select_pod_horizontal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Timber.d("f9: RecyclerAdapter2: onBindViewHolder(position(${position}))")

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
}