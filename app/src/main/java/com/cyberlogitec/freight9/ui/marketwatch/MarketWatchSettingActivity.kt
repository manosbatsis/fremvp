package com.cyberlogitec.freight9.ui.marketwatch

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.Chart
import com.cyberlogitec.freight9.lib.model.MovingAverage
import com.cyberlogitec.freight9.lib.util.Intents.Companion.MARKET_WATCH_CHART_SETTING
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import kotlinx.android.synthetic.main.act_market_watch_setting.*
import kotlinx.android.synthetic.main.item_chart_setting_ma.view.*
import kotlinx.android.synthetic.main.item_market_filter_container.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber

@RequiresActivityViewModel(value = MarketWatchViewModel::class)
class MarketWatchSettingActivity : BaseActivity<MarketWatchViewModel>() {
    private lateinit var chartSetting : Chart
    private lateinit var changeChartSetting : Chart

    enum class WatchUiType {
        CHART_SETTING_DEFAULT,  // chart type,
        CHART_SETING_MA         // chart type, interval, moving average
    }
    private val chartTypeAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    onSelectchanged = {changeChartSetting.chartType = it}
                }
    }
    private val intervalAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    onSelectchanged = {changeChartSetting.interVal = it}
                }
    }

    private val movingAverageRecyclerAdapter by lazy {
        MovingAverageRecyclerAdapter()
                .apply {
                    onRefreshItem = { position, movingAverage ->

                        this.notifyItemChanged(position)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("onCreate")

        setContentView(R.layout.act_market_watch_setting)

        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.color_0d0d0d)


        toolbarInit()

        initUi()
        // init recyclerview
        recyclerViewInit()

    }

    private fun initUi() {

        when(intent.extras["type"] as WatchUiType) {
            WatchUiType.CHART_SETTING_DEFAULT -> {
                ll_interval.visibility = View.GONE
                ll_interval_rcv.visibility = View.GONE
                ll_ma.visibility = View.GONE
                ll_ma_rcv.visibility = View.GONE

            }
            WatchUiType.CHART_SETING_MA -> {
                ll_interval.visibility = View.VISIBLE
                ll_interval_rcv.visibility = View.VISIBLE
                ll_ma.visibility = View.VISIBLE
                ll_ma_rcv.visibility = View.VISIBLE
            }
        }

        chartSetting = intent.extras[MARKET_WATCH_CHART_SETTING] as Chart
        changeChartSetting = chartSetting.copy()

        btn_apply.setSafeOnClickListener {
            setResult()
            finish()
        }

        btn_apply.isEnabled = true
        btn_apply.isClickable = true

    }

    private fun setResult() {
        if(changeChartSetting.movingAverage.isNullOrEmpty())
            changeChartSetting.movingAverage = ArrayList()
        changeChartSetting.movingAverage?.clear()

        changeChartSetting.movingAverage?.addAll(movingAverageRecyclerAdapter.datas)
        val newIntent = Intent()
        setResult(Activity.RESULT_OK, newIntent)
        newIntent.putExtra(MARKET_WATCH_CHART_SETTING, changeChartSetting)
    }

    private fun toolbarInit() {
        defaultbarInit(toolbar_common,menuType = MenuType.DEFAULT)
        toolbar_common.setBackgroundColor(getColor(R.color.color_0d0d0d))
        toolbar_right_btn.visibility = View.INVISIBLE
        toolbar_left_btn.setOnClickListener {
            setResult()
            finish()
        }
    }

    private fun recyclerViewInit() {
        val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager(this).orientation)


        recycler_view_market_chart_type.addItemDecoration(dividerItemDecoration)
        recycler_view_market_chart_type.apply {
            layoutManager = LinearLayoutManager(this@MarketWatchSettingActivity)
            adapter = this@MarketWatchSettingActivity.chartTypeAdapter
        }

        chartTypeAdapter.datas.addAll(resources.getStringArray(R.array.chart_type))
        if(chartSetting.storeId == MarketWatchIndexActivity::class.simpleName)
            chartTypeAdapter.datas.remove(resources.getString(R.string.market_watch_chart_candle))
        chartSetting.chartType?.let { chartTypeAdapter.setSelectedItem(it) }

        recycler_view_market_watch_interval.addItemDecoration(dividerItemDecoration)
        recycler_view_market_watch_interval.apply {
            layoutManager = LinearLayoutManager(this@MarketWatchSettingActivity)
            adapter = this@MarketWatchSettingActivity.intervalAdapter
        }
        if(chartSetting.intervalList.isNullOrEmpty())
            intervalAdapter.datas.addAll(resources.getStringArray(R.array.chart_interval))
        else
            intervalAdapter.datas.addAll(chartSetting.intervalList!!)
        chartSetting.interVal?.let { intervalAdapter.setSelectedItem(it) }

        recycler_view_market_watch_ma.addItemDecoration(dividerItemDecoration)
        recycler_view_market_watch_ma.apply {
            layoutManager = LinearLayoutManager(this@MarketWatchSettingActivity)
            adapter = this@MarketWatchSettingActivity.movingAverageRecyclerAdapter
        }

        if(chartSetting.movingAverage.isNullOrEmpty()) {
            movingAverageRecyclerAdapter.datas.add(MovingAverage(false, "5", "0", getColor(R.color.color_movingaverage_a9e13a)))
            movingAverageRecyclerAdapter.datas.add(MovingAverage(false, "10", "0", getColor(R.color.color_movingaverage_2caadc)))
            movingAverageRecyclerAdapter.datas.add(MovingAverage(false, "50", "0", getColor(R.color.color_movingaverage_ed749c)))
        }else {
            chartSetting.movingAverage?.let { movingAverageRecyclerAdapter.datas.addAll(it) }
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        private lateinit var tfRegular: Typeface
        private lateinit var tfBold: Typeface
        val datas = mutableListOf<String>()
        private var selectedItem: String = ""
        var onSelectchanged:(String) -> Unit = {}

        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            tfRegular = ResourcesCompat.getFont(parent.context, R.font.opensans_regular)!!
            tfBold = ResourcesCompat.getFont(parent.context, R.font.opensans_bold)!!
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_market_filter_container, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with(datas[position]) {
                    tv_item.text = this
                    if(tv_item.text.toString() == selectedItem) {
                        cb_select.isChecked = true
                        tv_item.isSelected = true
                        tv_item.typeface = tfBold
                    }else {
                        cb_select.isChecked = false
                        tv_item.typeface = tfRegular
                        tv_item.isSelected = false
                    }
                }
                this.setOnClickListener {
                    selectedItem = it.tv_item.text.toString()
                    onSelectchanged(selectedItem)
                    notifyDataSetChanged() }
            }
        }

        fun setSelectedItem(item: String) {
            selectedItem = item
        }
        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
    private class MovingAverageRecyclerAdapter : RecyclerView.Adapter<MovingAverageRecyclerAdapter.ViewHolder>() {
        private lateinit var tfRegular: Typeface
        private lateinit var tfBold: Typeface
        val datas = mutableListOf<MovingAverage>()
        var onRefreshItem:(position: Int, movingAverage: MutableList<MovingAverage>) -> Unit = { _, _ ->}
        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            tfRegular = ResourcesCompat.getFont(parent.context, R.font.opensans_regular)!!
            tfBold = ResourcesCompat.getFont(parent.context, R.font.opensans_bold)!!
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chart_setting_ma, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                when(position) {
                    0 -> {
                        cl_header_title.visibility = View.VISIBLE
                    }
                    else -> {
                        cl_header_title.visibility = View.GONE
                    }
                }
                with(datas[position]) {
                    when(this.selected) {
                        true -> {
                            et_offset.isFocusable = true
                            et_period.isFocusable = true
                            et_offset.isEnabled = true
                            et_period.isEnabled = true
                            when(position){
                                0->{
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_green_default_32_x_32, null))
                                }
                                1->{
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_blue_default_32_x_32, null))
                                }
                                2->{
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_pink_default_32_x_32, null))
                                }
                            }
                        }
                        false -> {
                            et_offset.isFocusable = false
                            et_period.isFocusable = false
                            et_offset.isEnabled = false
                            et_period.isEnabled = false
                            when(position) {
                                0 -> {
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_green_dim_32_x_32, null))
                                }
                                1 -> {
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_blue_dim_32_x_32, null))
                                }
                                2 -> {
                                    v_color.setImageDrawable(resources.getDrawable(R.drawable.img_ma_pink_dim_32_x_32, null))
                                }
                            }
                        }
                    }
                    sw_select.isChecked = this.selected!!
                    if (this.peroid != null) {
                        et_period.setText(this.peroid)
                    }
                    if (this.offset != null) {
                        et_offset.setText(this.offset)
                    }

                }
                this.setOnClickListener {
                    datas[holder.bindingAdapterPosition].selected = datas[position].selected?.not()

                    onRefreshItem(holder.bindingAdapterPosition, datas)
//                    notifyItemChanged(holder.adapterPosition)
                }
                et_period.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty())
                            datas[holder.bindingAdapterPosition].peroid = "0"
                        else
                            datas[holder.bindingAdapterPosition].peroid = s.toString().toFloat().toInt().toString()
                        //onRefreshItem(holder.adapterPosition)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                })
                et_offset.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if(s.isNullOrEmpty())
                            datas[holder.bindingAdapterPosition].offset = "0"
                        else
                            datas[holder.bindingAdapterPosition].offset = s.toString()
                        //onRefreshItem(holder.adapterPosition)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                })
            }
        }


        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
