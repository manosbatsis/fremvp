package com.cyberlogitec.freight9.ui.trademarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.frag_market_inventorylist.*

import timber.log.Timber

class MarketInventoryFragment constructor(val viewModel: BaseViewModel): RxFragment() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {

                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_market_inventorylist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")


        recyclerViewInit()

        // + test datas
        for(x in 1..20){
            adapter.datas.add( x.toString() )
        }
        adapter.notifyDataSetChanged()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    private fun recyclerViewInit() {

        recycler_view_market_inventory_list.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketInventoryFragment.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_market_inventory, parent, false))

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]

            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketInventoryFragment {
            return MarketInventoryFragment(viewModel)
        }
    }


}