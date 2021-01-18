package com.cyberlogitec.freight9.ui.inventory

import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.enums.ContainerItemTypes
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.route.RouteGridView
import com.cyberlogitec.freight9.lib.util.*
import kotlinx.android.synthetic.main.body_pol_pod_card.view.*
import kotlinx.android.synthetic.main.item_inventory_detail.view.*
import kotlinx.android.synthetic.main.popup_inventory_contract_preview.view.*
import timber.log.Timber
import java.lang.Math.abs
import java.text.NumberFormat
import java.util.*


class InventoryContractPreviewPopup(var view: View, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    private var tabList = mutableListOf<TabEnum>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val inventoryDetailAdapter by lazy {
        InventoryDetailRecyclerAdapter()
                .apply { }
    }

    init {
        currencyFormat.minimumFractionDigits = 0

        view.iv_inventory_contract_preview_close.setSafeOnClickListener {
            dismiss()
        }

        tabList.add(TabEnum.TAB_01)
        tabList.add(TabEnum.TAB_02)
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0 )

    }

    fun initValue(isList: Boolean, datas: Any? = null) {
        if (datas == null) {
            // 01
            make01DataDummy()

            // 02
            make02DataDummy()
            // container type 에 표시될 내용
            makeContainerType()
        }

        // 상단 Tab 생성
        makeTabList(isList)

        // View detail 버튼
        view.btn_inventory_contract_preview_view_detail.setSafeOnClickListener {
            view.context.startActivity(Intent(view.context, InventoryDetailActivity::class.java))
            dismiss()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun makeTabList(isList: Boolean) {
        for (tab in tabList) {
            val llItem = LinearLayout(view.context)
            llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            llItem.orientation = LinearLayout.VERTICAL
            llItem.gravity = Gravity.BOTTOM
            val margin = 12.toDp().toInt()
            val params = llItem.layoutParams as LinearLayout.LayoutParams
            params.setMargins(margin, 0, margin, 0)
            llItem.layoutParams = params

            llItem.addView(makeTabTextView(view.context.getString(tab.nameId)))
            llItem.addView(makeTabImageView())
            llItem.tag = tab.tagSeq

            llItem.setOnClickListener { clickTabProcess(tab) }
            view.ll_inventory_contract_preview_tab_horizontal.addView(llItem)
        }
        // 첫번째 Tab 초기화
        clickTabProcess(if (isList) tabList[0] else tabList[1])
    }

    private fun clickTabProcess(tab: TabEnum) {

        // 선택한 tab highlight, 그 외 tab dark
        makeSelectedTabList(tab)

        for (index in 0 until view.ll_inventory_contract_preview_tab_horizontal.childCount) {
            val childView = view.ll_inventory_contract_preview_tab_horizontal.getChildAt(index) as LinearLayout

            var textColorValue = R.color.greyish_brown
            var viewColorValue = R.color.black
            if (childView.tag as Int == tab.tagSeq) {
                textColorValue = R.color.colorWhite
                viewColorValue = R.color.purpley_blue
            }

            val subChildCount = childView.childCount
            for (subIndex in 0 until subChildCount) {
                val subChildView = childView.getChildAt(subIndex)
                when (subChildView) {
                    is TextView -> {
                        subChildView.setTextColor(ContextCompat.getColor(view.context, textColorValue))
                    }
                    is View -> {
                        subChildView.setBackgroundColor(view.context.getColor(viewColorValue))
                    }
                }
            }
        }
    }

    private fun makeTabTextView(title: String) : TextView {
        val textview = TextView(view.context)
        textview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textview.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        params.setMargins(0, 0, 0, 4.toDp().toInt())
        textview.layoutParams = params
        textview.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textview.setTextAppearance(R.style.txt_opensans_b_16_greyishbrown)
        textview.text = title
        return textview
    }

    private fun makeTabImageView() : View {
        val imageview = View(view.context)
        imageview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4.toDp().toInt())
        imageview.setBackgroundColor(view.context.getColor(R.color.black))
        return imageview
    }

    // Table, Grid tab 누른 경우
    private fun makeSelectedTabList(tab: TabEnum) {
        // TODO : 선택한 tab 에 대한 UI, Data setting
        view.ns_inventory_contract_preview.smoothScrollTo(0, 0)
        when(tab.index) {
            // 01
            0 -> {
                view.ll_inventory_contract_preview_tab_01.visibility = View.VISIBLE
                view.ll_inventory_contract_preview_tab_02.visibility = View.GONE
                make01View()
            }
            // 02
            1 -> {
                view.ll_inventory_contract_preview_tab_01.visibility = View.GONE
                view.ll_inventory_contract_preview_tab_02.visibility = View.VISIBLE
                make02View()
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun make01View() {
        with(view) {
            iv_carrier_more.visibility = View.INVISIBLE // For MVP : View.VISIBLE
            iv_carrier_more.setSafeOnClickListener {
                context.showToast("GO_MORE : Pending")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // TODO : sharePermissionCheck()
                    Timber.d("f9: TODO : sharePermissionCheck()")
                }
            }
            tv_pol_count.visibility = View.INVISIBLE
            tv_pod_count.visibility = View.INVISIBLE
            div_pol_pod.visibility = View.GONE
            ll_link_detail.visibility = View.GONE

            with(inventoryMain) {
                // top card
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text =
                        if (carrierCode.trim().isNotEmpty()) carrierCode else context.getString(R.string.all_carriers)
                tv_pol_name.text = locPolCd
                tv_pol_count.text = "+$locPolCnt"
                tv_pol_desc.text = locPolNm
                tv_pod_name.text = locPodCd
                tv_pod_count.text = "+$locPodCnt"
                tv_pod_desc.text = locPodNm
                // Recycler card
                inventoryDetailRecyclerViewInit()
                // bottom card
                tv_inventory_detail_total_value.text = currencyFormat.format(totalCostValue.toInt())
                tv_inventory_detail_f9_cost_value.text = currencyFormat.format(f9MarketValue.toInt())
                val diffValue = (f9MarketValue - totalCostValue).toInt()
                var diffSymbol = if (diffValue < 0) "- " else "+ "
                val diffAbsValue = abs(diffValue)
                if (diffValue > 0F) { diffSymbol = "+" }
                tv_inventory_detail_estimate_profit_value.text = "$diffSymbol${currencyFormat.format(diffAbsValue)}"
            }
        }
    }

    private fun make02View() {
        with(view) {
            gv_inventory_contract_preview_grid_view.resetView()   //재사용시 view 초기화 시켜줘야함
            // resetView 를 호출하면 portDataList 도 removeAll 되므로 다시 add 해줌
            make02DataDummy()
            gv_inventory_contract_preview_grid_view.mPor = "CNBOT"
            gv_inventory_contract_preview_grid_view.mDel = "DEL3"
            gv_inventory_contract_preview_grid_view.mViewType = RouteGridView.GridViewType.SELL_OFFER
            gv_inventory_contract_preview_grid_view.setData(portDataList)

            // create container type layout
            ll_inventory_contract_preview_condition_container_value.removeAllViews()
            ll_inventory_contract_preview_condition_container_value.addView(makeContainerTypeLayout())

            iv_order_carrier_more_grid.setSafeOnClickListener {
                context.showToast("GO_MORE : Pending")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Timber.d("f9: TODO : sharePermissionCheck()")
                }
            }
        }
    }

    private fun makeContainerTypeLayout() : LinearLayout {
        val llList = LinearLayout(view.context)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL

        for (containerData in containerItemTypes) {
            val llSub = LinearLayout(view.context)
            llSub.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            llSub.orientation = LinearLayout.HORIZONTAL
            llSub.gravity = Gravity.CENTER_VERTICAL
            val margin = 6.toDp().toInt()
            var params = llSub.layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, margin, 0, margin)
            llSub.layoutParams = params

            // 90dp
            val textviewLeft = TextView(view.context)
            textviewLeft.layoutParams = LinearLayout.LayoutParams(90.toDp().toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            textviewLeft.gravity = Gravity.START
            textviewLeft.includeFontPadding = false
            textviewLeft.setTextAppearance(R.style.txt_opensans_r_15_595959)
            textviewLeft.text = view.context.getString(containerData.containerName.nameMiddleId)

            llSub.addView(textviewLeft)

            // 0dp (weight 1)
            val textviewRight = TextView(view.context)
            textviewRight.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            params = textviewRight.layoutParams as LinearLayout.LayoutParams
            params.weight = 1.0f
            textviewRight.layoutParams = params
            textviewRight.gravity = Gravity.START
            textviewRight.includeFontPadding = false
            textviewRight.setTextAppearance(R.style.txt_opensans_r_15_595959)

            // container size with ", "
            val size = containerData.containerItems.size
            var rightContent = ""
            for ((index, containerItem) in containerData.containerItems.withIndex()) {
                val nameShort = view.context.getString(containerItem.nameShortId)
                rightContent += (if (index > -1 && index < size - 1) ("$nameShort, ") else nameShort)
            }
            textviewRight.text = rightContent

            llSub.addView(textviewRight)
            llList.addView(llSub)
        }
        return llList
    }

    /***********************************************************************************************
     * Inventory detail Recycler view init
     */
    private fun inventoryDetailRecyclerViewInit() {
        view.recycler_inventory_detail.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(view.context)
            adapter = inventoryDetailAdapter
        }
        setInventoryDetailRecyclerData()
    }

    private fun setInventoryDetailRecyclerData() {
        inventoryDetailAdapter.setData(inventoryDetailList)
        inventoryDetailAdapter.notifyDataSetChanged()
    }

    /***********************************************************************************************
     * Inventory Detail Recycler view adapter : Period, Volume
     */
    private class InventoryDetailRecyclerAdapter : RecyclerView.Adapter<InventoryDetailRecyclerAdapter.ViewHolder>() {

        val datas = mutableListOf<InventoryDetail>()

        fun setData(datas: List<InventoryDetail>) {
            this.datas.clear()
            this.datas.addAll(datas)
        }

//        fun getData(): List<InventoryDetail> {
//            return this.datas
//        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_detail, parent, false))

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]
                tv_inventory_detail_week.text = context.getWeek(data.bseYw)
                tv_inventory_detail_weekof.text = data.bseWeekFmDt.getYMDWithHypen()
                val ownedAmt = data.inStockAmt + data.onMarketAmt
                val totalAmt = ownedAmt + data.bookedAmt + data.soldAmt
                pv_inventory_detail_hgraph.progress = ownedAmt / totalAmt * 100.0F
                pv_inventory_detail_hgraph.progressAnimate()
                tv_inventory_detail_volume.text = context.getString(R.string.your_inventory_detail_owned_total_value,
                        context.getConvertedTeuValue(ownedAmt.toInt()),
                        context.getConvertedTeuValue(totalAmt.toInt()))
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    //----------------------------------------------------------------------------------------------

    private fun make01DataDummy() {
        makeInventoryMain()
        makeInventoryDetailDatas()
    }

    lateinit var portDataList: RouteDataList
    private fun make02DataDummy() {
        portDataList = RouteDataList()

        portDataList.add(RouteData("CNATX", "CNATX", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL1","DEL1"))
        portDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL2","DEL2"))
        portDataList.add(RouteData("CNBOT", "CNBOT", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL3","DEL3"))
        portDataList.add(RouteData("CNBSD", "CNBSD", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL11","DEL11"))
        portDataList.add(RouteData("CNBTN", "CNBTN", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL12","DEL12"))

        portDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL1", "CNSHAL1", "CNSHAD", "CNSHAD", "DEL2","DEL2"))
        portDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL1", "CNSHAL1", "CNSHAD", "CNSHAD", "DEL3","DEL3"))

        portDataList.add(RouteData("CNSH1", "CNSH1", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL12","DEL12"))
        portDataList.add(RouteData("CNSH1", "CNSH1", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL13","DEL13"))
        portDataList.add(RouteData("CNSH2", "CNSH2", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL14","DEL14"))
        portDataList.add(RouteData("CNSH3", "CNSH3", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL15","DEL15"))
        portDataList.add(RouteData("CNSH4", "CNSH4", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL16","DEL16"))
    }

    //----------------------------------------------------------------------------------------------

    private fun makeInventoryMain() {
        inventoryMain = InventoryMain("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01",
                "HLC", "CNSHA", "Shanghai, China", 3,
                "USLAX", "Los Angeles, US", 1, 28000F, 26000F)
    }

    private fun makeInventoryDetailDatas() {
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "CNSHA", "Shanghai, China", "201903", "20190113",
                22000F, 20000F, 50F, 25F, 23F, 22F, 15))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "USOAK", "Oakland, CA", "201904", "20190120",
                23000F, 20000F, 40F, 35F, 33F, 32F, 16))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "CNSHA", "Shanghai, China", "201905", "20190127",
                24000F, 20000F, 60F, 15F, 13F, 12F, 17))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "USOAK", "Oakland, CA", "201906", "20190210",
                25000F, 20000F, 20F, 55F, 13F, 50F, 18))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "CNSHA", "Shanghai, China", "201907", "20190217",
                26000F, 20000F, 15F, 60F, 50F, 30F, 19))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "USLAX", "Los Angeles, US", "201908", "20190224",
                24000F, 20000F, 55F, 30F, 30F, 20F, 20))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "CNSHA", "Shanghai, China", "201909", "20190301",
                23000F, 20000F, 40F, 25F, 20F, 30F, 21))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "USLAX", "Los Angeles, US", "201910", "20190308",
                22000F, 20000F, 50F, 35F, 20F, 40F, 22))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "CNSHA", "Shanghai, China", "201911", "20190315",
                21000F, 20000F, 60F, 30F, 30F, 15F, 23))
        inventoryDetailList.add(InventoryDetail("HLC", "HLC_MST_20190827_1", "MST-F9_HLC-201908-01", "USLAX", "Los Angeles, US", "201912", "20190322",
                20000F, 20000F, 50F, 30F, 60F, 25F, 24))
    }

    private fun makeContainerType() {
        val containerFItems = mutableListOf<ContainerItemTypes>()
        containerFItems.add(ContainerItemTypes.D_TYPE_20)
        containerFItems.add(ContainerItemTypes.D_TYPE_40)
        containerFItems.add(ContainerItemTypes.D_TYPE_45_HC)
        containerItemTypes.add(ContainerData(ContainerName.D_NAME, containerFItems))

        val containerRItems = mutableListOf<ContainerItemTypes>()
        containerRItems.add(ContainerItemTypes.R_TYPE_20)
        containerRItems.add(ContainerItemTypes.R_TYPE_40)
        containerRItems.add(ContainerItemTypes.R_TYPE_45_HC)
        containerItemTypes.add(ContainerData(ContainerName.R_NAME, containerRItems))

        val containerEItems = mutableListOf<ContainerItemTypes>()
        containerEItems.add(ContainerItemTypes.E_TYPE_20)
        containerEItems.add(ContainerItemTypes.E_TYPE_40)
        containerEItems.add(ContainerItemTypes.E_TYPE_45_HC)
        containerItemTypes.add(ContainerData(ContainerName.E_NAME, containerEItems))
    }

    //----------------------------------------------------------------------------------------------

    enum class TabEnum constructor(
            val index: Int,
            val nameId: Int,
            val tagSeq: Int
    ){
        TAB_01(0, R.string.your_inventory_contract_preview_01, layout_tab_tag_seq),
        TAB_02(1, R.string.your_inventory_contract_preview_02, layout_tab_tag_seq+1),
    }

    companion object {
        const val layout_tab_tag_seq = 1000000
    }

    /***********************************************************************************************
     * Inventory Main 에 표시될 data class
     */
    private lateinit var inventoryMain: InventoryMain
    data class InventoryMain(
            var dataOwnerPointerId: String = "",         // "HLC"
            var inventoryNumber: String = "",                 // "HLC_MST_20190827_1"
            var masterContractNumber: String = "",            // "MST-F9_HLC-201908-01"
            var carrierCode: String = "",
            var locPolCd: String = "",
            var locPolNm: String = "",
            var locPolCnt: Int = 0,
            var locPodCd: String = "",
            var locPodNm: String = "",
            var locPodCnt: Int = 0,
            var f9MarketValue: Float = 0F,
            var totalCostValue: Float = 0F
    )

    /***********************************************************************************************
     * Inventory detail adapter 에서 사용될 data class
     */
    private var inventoryDetailList = mutableListOf<InventoryDetail>()
    data class InventoryDetail(
            var dataOwnerPointerId: String = "",         // "HLC"
            var inventoryNumber: String = "",                 // "HLC_MST_20190827_1"
            var masterContractNumber: String = "",            // "MST-F9_HLC-201908-01"
            var polCd: String = "",                  // "JPUKB"
            var polName: String = "",                // "KOBE"
            var bseYw: String = "",                  // "201903"
            var bseWeekFmDt: String = "",           // "20190113"
            var f9MarketValue: Float = 0F,
            var unitCostValue: Float = 0F,
            var inStockAmt: Float = 0F,
            var onMarketAmt: Float = 0F,
            var bookedAmt: Float = 0F,
            var soldAmt: Float = 0F,
            var counterOffersCnt: Int = 0
    )

    /***********************************************************************************************
     * Inventory detail container type 에서 사용될 data class
     */
    private var containerItemTypes = mutableListOf<ContainerData>()
    data class ContainerData(
            var containerName: ContainerName = ContainerName.D_NAME,
            var containerItems: MutableList<ContainerItemTypes>
    )
}

