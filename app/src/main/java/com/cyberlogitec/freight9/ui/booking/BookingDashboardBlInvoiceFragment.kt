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
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.bottom_sheet_bookingdashboard_blinvoice.*
import kotlinx.android.synthetic.main.item_booking_invoice.view.*
import timber.log.Timber


class BookingDashboardBlInvoiceFragment(val viewModel: BaseViewModel, val item: BookingDashboardItem?): RxFragment() {

    private lateinit var bookingItem: BookingDashboardItem

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
            View = inflater.inflate(R.layout.bottom_sheet_bookingdashboard_blinvoice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        initView()
    }

    private fun initView() {
        recyclerViewInit()
    }

    fun setData(data: BookingDashboardItem) {
        adapter.datas.clear()
        adapter.datas.addAll(data.container)
        adapter.bookingDashboardItem = data
        adapter.notifyDataSetChanged()
    }

    private fun recyclerViewInit() {
        rv_invoice.apply {
            layoutManager = LinearLayoutManager(this@BookingDashboardBlInvoiceFragment.context)
            adapter = this@BookingDashboardBlInvoiceFragment.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val datas = mutableListOf<Container>()
        lateinit var bookingDashboardItem: BookingDashboardItem
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_booking_invoice, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {
                val subAdapter = SubRecyclerAdapter()
                rv_invoice_item.layoutManager = LinearLayoutManager(context)
                rv_invoice_item.adapter = subAdapter
                subAdapter.datas.add("test")
                subAdapter.datas.add("test1")
                subAdapter.datas.add("test2")
                subAdapter.notifyDataSetChanged()

                with(datas[position]){
                    if(isExpand) {
                        ll_expand.visibility = View.VISIBLE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_collapse_default_l))
                    }else {
                        ll_expand.visibility = View.GONE
                        iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_expand_default_l))
                    }

                    iv_expand.setOnClickListener {
                        if(isExpand) {
                            ll_expand.visibility = View.GONE
                            iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_expand_default_l))

                        }else {
                            ll_expand.visibility = View.VISIBLE
                            iv_expand.setImageDrawable(context.getDrawable(R.drawable.btn_collapse_default_l))
                        }
                        isExpand = isExpand.not()
                    }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    class SubRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val datas = mutableListOf<String>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_booking_invoice_price, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            with(holder.itemView) {

                with(datas[position]){


                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel, bookingItem: BookingDashboardItem) : BookingDashboardBlInvoiceFragment {
            return BookingDashboardBlInvoiceFragment(viewModel, bookingItem)
        }
    }
}