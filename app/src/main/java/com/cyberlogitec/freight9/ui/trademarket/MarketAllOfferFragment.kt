package com.cyberlogitec.freight9.ui.trademarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.google.android.material.tabs.TabLayout
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.frag_market_myoffer.*
import kotlinx.android.synthetic.main.item_market_my_offer.view.*

import timber.log.Timber

class MarketAllOfferFragment constructor(val viewModel: BaseViewModel): RxFragment() {

    private val buyOfferAdapter by lazy {
        RecyclerAdapter()
                .apply {

                }
    }
    private val sellOfferAdapter by lazy {
        RecyclerAdapter()
                .apply {

                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_market_alloffer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")


        recyclerViewInit()
        initTabLayout()

        // + test datas
        for(x in 1..20){
            buyOfferAdapter.datas.add( x.toString() )
            sellOfferAdapter.datas.add( x.toString() )
        }
        buyOfferAdapter.notifyDataSetChanged()
        sellOfferAdapter.notifyDataSetChanged()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    private fun initTabLayout() {
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                changeOfferList(tab!!.position)
            }

        })
    }

    private fun changeOfferList(position: Int) {
        when(position) {
            0 -> { rv_buyoffers.visibility=View.VISIBLE
                rv_selloffers.visibility = View.INVISIBLE }
            1 -> { rv_buyoffers.visibility=View.INVISIBLE
                rv_selloffers.visibility = View.VISIBLE }
        }

    }

    private fun recyclerViewInit() {

        rv_buyoffers.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketAllOfferFragment.buyOfferAdapter
        }
        rv_selloffers.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketAllOfferFragment.sellOfferAdapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_market_my_offer, parent, false))

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]
                tv_weeks.visibility = View.GONE
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketAllOfferFragment {
            return MarketAllOfferFragment(viewModel)
        }
    }


}