package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.MarketWatchDealHistoryItems
import com.cyberlogitec.freight9.lib.util.toDate
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.frag_market_livedeal.*
import kotlinx.android.synthetic.main.item_market_livedeal.view.tv_deal_date
import kotlinx.android.synthetic.main.item_market_livedeal.view.tv_deal_price
import kotlinx.android.synthetic.main.item_market_livedeal.view.tv_deal_price_diff
import kotlinx.android.synthetic.main.item_market_livedeal.view.tv_deal_teu
import kotlinx.android.synthetic.main.item_market_livedeal.view.v_deal_normal
import kotlinx.android.synthetic.main.item_market_livedeal.view.v_deal_up_down
import kotlinx.android.synthetic.main.item_market_watch_dealhistory.view.*
import timber.log.Timber
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import kotlin.math.abs

class MarketWatchDealHistoryFragment constructor(val viewModel: BaseViewModel): RxFragment() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {

                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_market_livedeal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")


        initListener()
        recyclerViewInit()


    }

    private fun initListener() {
        when(viewModel) {
            is MarketWatchViewModel -> {
                viewModel.outPuts.refreshToBaseweek()
                        .bindToLifecycle(this)
                        .subscribe {
                            //to request deal history
                        }

                viewModel.outPuts.onSuccessRequestWeekDealHistory()
                        .bindToLifecycle(this)
                        .subscribe {
                            adapter.datas.clear()
                            if(it.cells != null)
                                adapter.datas.addAll(it.cells.sortedByDescending { it.timestamp })
                            adapter.notifyDataSetChanged()
                        }
            }
        }
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

        recycler_view_market_deal_list.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketWatchDealHistoryFragment.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<MarketWatchDealHistoryItems>()
        val currencyFormat: NumberFormat = NumberFormat.getNumberInstance()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            currencyFormat.minimumFractionDigits = 2
            currencyFormat.isGroupingUsed = true
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_market_watch_dealhistory, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            with(holder.itemView) {
                val data = datas[position]
                val df: DateFormat = SimpleDateFormat("HH:mm:ss")
                tv_deal_date.text = df.format(data.timestamp.toDate("yyyyMMddHHmmssSSSSSS"))
                tv_date.text = SimpleDateFormat("yyyy-MM-dd").format(data.timestamp.toDate("yyyyMMddHHmmssSSSSSS"))
                //date visible/invisible
                if(position == 0){
                    ll_date.visibility = View.VISIBLE
                }else {
                    if(data.timestamp.substring(0,8) == datas[position -1].timestamp.substring(0,8))
                        ll_date.visibility = View.GONE
                    else
                        ll_date.visibility = View.VISIBLE
                }
                tv_deal_price.text = "$${currencyFormat.format(data.dealPrice)}"

                //거래 가격차이..
                tv_deal_price_diff.text = currencyFormat.format(abs(data.priceChange!!))
                when {
                    data.priceChange > 0 -> {
                        v_deal_up_down.visibility = View.VISIBLE
                        v_deal_normal.visibility = View.GONE
                        v_deal_up_down.background = getDrawable(context, R.drawable.ic_up_green)
                        tv_deal_price_diff.setTextColor(getColor(context, R.color.green_blue))

                    }
                    data.priceChange < 0 -> {
                        v_deal_up_down.visibility = View.VISIBLE
                        v_deal_normal.visibility = View.GONE
                        v_deal_up_down.background = getDrawable(context, R.drawable.ic_down_blue)
                        tv_deal_price_diff.setTextColor(getColor(context, R.color.color_003aff))
                    }
                    else -> {
                        v_deal_up_down.visibility = View.GONE
                        v_deal_normal.visibility = View.VISIBLE
                        tv_deal_price_diff.setTextColor(getColor(context, R.color.white))
                    }
                }
                tv_deal_teu.text = "${if (data.dealQty >0) DecimalFormat("#,###").format(data.dealQty.toLong()) else ""}T"
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketWatchDealHistoryFragment {
            return MarketWatchDealHistoryFragment(viewModel)
        }
    }

    /*private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }*/
}