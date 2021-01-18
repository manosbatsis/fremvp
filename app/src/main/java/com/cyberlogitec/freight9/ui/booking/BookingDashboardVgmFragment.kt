package com.cyberlogitec.freight9.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboardItem
import com.cyberlogitec.freight9.lib.model.booking.Container
import com.cyberlogitec.freight9.lib.ui.enums.BookingDashboardWeightType
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.getContainerTypeSizeName
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.bottom_sheet_bookingdashboard_vgm.*
import kotlinx.android.synthetic.main.item_booking_vgm.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import timber.log.Timber


class BookingDashboardVgmFragment(val viewModel: BaseViewModel, val item: BookingDashboardItem?): RxFragment() {

    private lateinit var bookingItem: BookingDashboardItem
    private var bookingDashboardWeightTypes: MutableList<BookingDashboardWeightType> = mutableListOf()
    private var selectedWeightTypes = BookingDashboardWeightType.TYPE_KGM

    init {
        if (item != null) {
            bookingItem = item
        }
    }
    private val adapter: RecyclerAdapter by lazy {
        RecyclerAdapter()
                .apply {
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_bookingdashboard_vgm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        initView()
    }

    private fun initView() {
        recyclerViewInit()
        bookingDashboardWeightTypes.add(BookingDashboardWeightType.TYPE_KGM)
        bookingDashboardWeightTypes.add(BookingDashboardWeightType.TYPE_LBR)
        ll_booking_no.setSafeOnClickListener { showNumberTypeDialog() }
    }

    private fun showNumberTypeDialog() {
        val spinDataList = mutableListOf<TextItem>()
        for (numberType in bookingDashboardWeightTypes) {
            spinDataList.add(TextItem(getString(numberType.id),
                    numberType.code == selectedWeightTypes.code,
                    numberType.code))
        }

        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = context?.let { BottomSheetDialog(it) }

        dialog?.setCancelable(true)
        dialog?.setContentView(view)

        var selectedNumberType: BookingDashboardWeightType = BookingDashboardWeightType.TYPE_KGM
        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                selectedNumberType = bookingDashboardWeightTypes
                        .find { it.code == spinDataList[index]._index } ?: BookingDashboardWeightType.TYPE_KGM
            }
        })

        dialog?.btn_done?.setOnClickListener {
            dialog?.hide()
            selectedWeightTypes = selectedNumberType
            adapter.bookingDashboardWeightTypes = selectedWeightTypes
            refreshList()
        }

        view.picker.setItems(spinDataList)
        view.picker.index = spinDataList.first { it._isSelected }._index
        dialog?.show()
    }

    private fun refreshList() {
        adapter.notifyDataSetChanged()
    }

    fun setData(data: BookingDashboardItem) {
        adapter.datas.clear()
        adapter.datas.addAll(data.container)
        adapter.bookingDashboardItem = data
        adapter.notifyDataSetChanged()
    }

    private fun recyclerViewInit() {
        rv_vgm.apply {
            layoutManager = LinearLayoutManager(this@BookingDashboardVgmFragment.context)
            adapter = this@BookingDashboardVgmFragment.adapter
        }
        adapter.bookingDashboardWeightTypes = selectedWeightTypes
        adapter.datas.addAll(bookingItem.container)
        adapter.notifyDataSetChanged()
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val datas = mutableListOf<Container>()
        lateinit var bookingDashboardItem: BookingDashboardItem
        lateinit var bookingDashboardWeightTypes: BookingDashboardWeightType
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_booking_vgm, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {

                with(datas[position]){

                    containerNo.let { tv_container_no.text = containerNo }
                    containerTypeSize.let { tv_container_type.text = containerTypeSize.getContainerTypeSizeName() }

                    if(isExpand) {
                        ll_colapse.visibility = View.GONE
                        ll_expand.visibility = View.VISIBLE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_collapse_default_l))
                    }else {
                        ll_colapse.visibility = View.VISIBLE
                        ll_expand.visibility = View.GONE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_expand_default_l))
                    }

                    iv_expand.setOnClickListener {
                        if(isExpand) {
                            ll_colapse.visibility = View.VISIBLE
                            ll_expand.visibility = View.GONE
                            iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_expand_default_l))

                        }else {
                            ll_colapse.visibility = View.GONE
                            ll_expand.visibility = View.VISIBLE
                            iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_collapse_default_l))
                        }
                        isExpand = isExpand.not()
                    }

                    when(bookingDashboardWeightTypes) {
                        BookingDashboardWeightType.TYPE_KGM -> {
                            tv_total_weight_unit.text = context?.getString(R.string.booking_dashboard_vgm_unit_kg)
                            tv_total_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_kg)
                            tv_container_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_kg)
                            tv_cargo_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_kg)
                        }
                        BookingDashboardWeightType.TYPE_LBR -> {
                            tv_total_weight_unit.text = context?.getString(R.string.booking_dashboard_vgm_unit_pound)
                            tv_total_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_pound)
                            tv_container_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_pound)
                            tv_cargo_weight_unit_expand.text = context?.getString(R.string.booking_dashboard_vgm_unit_pound)
                        }
                    }

                    if(containerMeasurement?.containerWeightUnit?.toUpperCase() == (context?.getString(bookingDashboardWeightTypes.id))?.toUpperCase()){
                        containerMeasurement?.containerWeight?.let { tv_total_weight.text = containerMeasurement.containerWeight
                            tv_total_weight_expand.text = containerMeasurement.containerWeight
                        }
                    }else {
                        when(containerMeasurement?.containerWeightUnit){
                            "KGM".toUpperCase() -> {
                                containerMeasurement?.containerWeight?.let {
                                    tv_total_weight.text = "${containerMeasurement.containerWeight.toLong()*2.20462}"
                                    tv_total_weight_expand.text = "${containerMeasurement.containerWeight.toLong()*2.20462}"}
                            }
                            "LBR".toUpperCase() -> {
                                containerMeasurement?.containerWeight?.let {
                                    tv_total_weight.text = "${containerMeasurement.containerWeight.toLong()*0.453592}"
                                    tv_total_weight_expand.text = "${containerMeasurement.containerWeight.toLong()*0.453592}"}
                            }
                        }
                    }

                    ///??? 기준이 뭔지 모름....
                    tv_container_weight_expand


                    //???
                    tv_cargo_weight_expand

                    //???

                    tv_booking_no
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel, bookingItem: BookingDashboardItem) : BookingDashboardVgmFragment {
            return BookingDashboardVgmFragment(viewModel, bookingItem)
        }
    }
}