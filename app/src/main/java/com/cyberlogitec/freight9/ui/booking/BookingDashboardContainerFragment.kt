package com.cyberlogitec.freight9.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.booking.*
import com.cyberlogitec.freight9.lib.ui.enums.BookingDashboardWeightType
import com.cyberlogitec.freight9.lib.util.getContainerTypeSizeName
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.bottom_sheet_bookingdashboard_container.*
import kotlinx.android.synthetic.main.item_booking_container.view.*
import timber.log.Timber
import java.text.Format


class BookingDashboardContainerFragment(val viewModel: BaseViewModel, val item: BookingDashboardItem?): RxFragment() {

    private lateinit var bookingItem: BookingDashboardItem
    private var bookingDashboardWeightTypes: MutableList<BookingDashboardWeightType> = mutableListOf()

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
            View = inflater.inflate(R.layout.bottom_sheet_bookingdashboard_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        initView()
        setData(bookingItem)
    }

    private fun initView() {
        recyclerViewInit()
//        adapter.datas.add(Container(ContainerDate(1,"","","","",""), ContainerDimension("","","","",1),"","", ContainerMeasurement("","","","","","","","","","",1,"",""),"","",null,"",null,null,null,"","",1,"",false))
    }

    fun setData(data: BookingDashboardItem) {
        adapter.datas.clear()
        adapter.datas.addAll(data.container)
        adapter.bookingDashboardItem = data
        adapter.notifyDataSetChanged()
    }

    private fun recyclerViewInit() {
        rv_container.apply {
            layoutManager = LinearLayoutManager(this@BookingDashboardContainerFragment.context)
            adapter = this@BookingDashboardContainerFragment.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val datas = mutableListOf<Container>()
        lateinit var bookingDashboardItem: BookingDashboardItem
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_booking_container, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {

                with(datas[position]){

                    if(isExpand){
                        ll_expand.visibility = View.VISIBLE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_collapse_default_l))
                    }else {
                        ll_expand.visibility = View.GONE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_expand_default_l))
                    }
                    iv_expand.setOnClickListener {
                        isExpand = isExpand.not()
                        notifyDataSetChanged()
                    }

                    tv_container_id.text = "#${String.format("%02d", position +1)}"
                    tv_container_no.text = containerNo
                    tv_container_type.text = containerTypeSize.getContainerTypeSizeName()
                    tv_container_no_summary.text = containerNo ?: "-"
                    tv_comodity.text = bookingDashboardItem?.commodity?.firstOrNull()?.commoditySequenceNumber ?: "-"             // container와 commodity 연결 점이 확인 안됨 web은
                    val packages = bookingDashboardItem?.commodity?.firstOrNull()?.commodityNumberOfPackages
                    tv_packages.text = "${packages?.let { packages } ?: "0"} ${resources.getString(R.string.booking_dashboard_si_package)}"             // 40 packages
                    tv_weight                                                                      //0.2ton
                    tv_ventilization    //40 cbm
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel, bookingItem: BookingDashboardItem) : BookingDashboardContainerFragment {
            return BookingDashboardContainerFragment(viewModel, bookingItem)
        }
    }
}