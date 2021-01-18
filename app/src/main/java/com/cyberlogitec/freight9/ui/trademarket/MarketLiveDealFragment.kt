package com.cyberlogitec.freight9.ui.trademarket

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
import com.cyberlogitec.freight9.lib.util.getWeek
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.frag_market_livedeal.*
import kotlinx.android.synthetic.main.item_market_livedeal.view.*
import timber.log.Timber
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MarketLiveDealFragment constructor(val viewModel: BaseViewModel): RxFragment() {

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


        recyclerViewInit()

        // + test datas
        for(x in 1..100){
            adapter.datas.add( LiveDeal(Calendar.getInstance().time, "201901", getRandom(1000f,5f).toInt(),getRandom(1000f,-500f).toInt(),getRandom(2000f,5f).toInt() ))
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

        recycler_view_market_deal_list.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketLiveDealFragment.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<LiveDeal>()
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_market_livedeal, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            with(holder.itemView) {
                val data = datas[position]
                val df: DateFormat = SimpleDateFormat("yy-MM-dd HH:mm:ss")
                tv_deal_date.text = df.format(data.date)
                //date visible/invisible
                if(position == 0){
                    tv_deal_date.visibility = View.VISIBLE
                }else {
                    if(data.date!! == datas[position -1].date)
                        tv_deal_date.visibility = View.INVISIBLE
                    else
                        tv_deal_date.visibility = View.VISIBLE
                }
                tv_deal_week.text = context.getWeek(data.week)
                tv_deal_price.text = "${currencyFormat.format(data.price)}/T"

                //거래 가격차이..
                tv_deal_price_diff.text = currencyFormat.format(abs(data.pricediff!!))
                when {
                    data.pricediff!! > 0 -> {
                        v_deal_up_down.visibility = View.VISIBLE
                        v_deal_normal.visibility = View.GONE
                        v_deal_up_down.background = getDrawable(context, R.drawable.ic_up_green)
                        tv_deal_price_diff.setTextColor(getColor(context, R.color.green_blue))

                    }
                    data.pricediff!! < 0 -> {
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
                tv_deal_teu.text = "${data.volume.toString()}T"
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketLiveDealFragment {
            return MarketLiveDealFragment(viewModel)
        }
    }

    data class LiveDeal (
            var date: Date?,
            var week: String?,
            var price: Int?,
            var pricediff: Int?,      //가격 차이를 어떻게 판단하나..
            var volume: Int?
    )

    private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }
}