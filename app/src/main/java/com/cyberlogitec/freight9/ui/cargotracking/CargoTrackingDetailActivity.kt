package com.cyberlogitec.freight9.ui.cargotracking

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingStatusItem
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_cargo_tracking_detail.*
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_container.view.*
import kotlinx.android.synthetic.main.toolbar_cargo_tracking_detail.*
import timber.log.Timber

@RequiresActivityViewModel(value = CargoTrackingDetailViewModel::class)
class CargoTrackingDetailActivity : BaseActivity<CargoTrackingDetailViewModel>() {

    private var cargoTrackingData: MutableList<CargoTrackingStatusItem> = mutableListOf()

    private val cargoTrackingContainerAdapter by lazy {
        CargoTrackingContainerAdapter()
                .apply {
                    onClickItem = { position ->
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, position))
                    }
                }
    }

    private val cargoTrackingScheduleAdapter by lazy {
        CargoTrackingScheduleAdapter()
                .apply {
                    // Do nothing
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_cargo_tracking_detail)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun setRxOutputs() {

        viewModel.outputs.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        if (intent.hasExtra(Intents.CARGO_TRACKING_ENTRY_OTHER)) {
                            val isCallFromOther = intent.getBooleanExtra(Intents.CARGO_TRACKING_ENTRY_OTHER, false)
                            setToolbar(isCallFromOther)
                        }

                        if (cargoTrackingData.isEmpty()) {
                            if (intent.hasExtra(Intents.CARGO_TRACKING_DATA)) {
                                cargoTrackingData = intent.getSerializableExtra(Intents.CARGO_TRACKING_DATA) as MutableList<CargoTrackingStatusItem>
                                cargoTrackingContainerAdapter.datas = cargoTrackingData
                                cargoTrackingContainerAdapter.notifyDataSetChanged()

                                val selectedIndex = cargoTrackingData.indexOfFirst { it.isSelected }
                                clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, selectedIndex))
                            }
                        }
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_TITLE_RIGHT_BTN, ParameterClick.CLICK_TITLE_LEFT -> {
                                finish()
                            }
                            else -> {  }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                // go Tracking detail
                                ParameterAny.ANY_ITEM_INDEX -> {
                                    val position = second as Int

                                    cargoTrackingData.map { it.isSelected = false }
                                    cargoTrackingData[position].isSelected = true
                                    cargoTrackingContainerAdapter.notifyDataSetChanged()

                                    cargoTrackingScheduleAdapter.setMarginApply(true)
                                    cargoTrackingScheduleAdapter.setBackground(true)
                                    cargoTrackingScheduleAdapter.setData(cargoTrackingData[position].statusDetailList)
                                    cargoTrackingScheduleAdapter.notifyDataSetChanged()
                                    Handler().postDelayed({
                                        recycler_cargo_tracking_schedule_list.scrollToPosition(0)
                                    }, 150)
                                }
                                else -> {  }
                            }
                        }
                    }
                }
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        setToolbar()
        recyclerViewInit()
        setListener()
    }

    private fun setToolbar(isCallFromOther: Boolean = false) {
        // set custom toolbar
        defaultbarInit(toolbar_cargo_tracking_detail,
                menuType = if (isCallFromOther) {
                    MenuType.CROSS
                } else {
                    MenuType.POPUP
                },
                title = getString(R.string.menu_cargo_tracking),
                isEnableNavi = !isCallFromOther)
    }

    private fun recyclerViewInit() {
        recycler_cargo_tracking_container_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@CargoTrackingDetailActivity.cargoTrackingContainerAdapter
        }

        recycler_cargo_tracking_schedule_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@CargoTrackingDetailActivity.cargoTrackingScheduleAdapter
        }
    }

    private fun setListener() {

        // on click toolbar right button
        toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        // on click toolbar left button
        toolbar_left_btn.setOnClickListener {
            Timber.d("f9: toolbar_left_btn click")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_LEFT)
        }
    }

    /***********************************************************************************************
     * RecyclerAdapter
     */
    private class CargoTrackingContainerAdapter : RecyclerView.Adapter<CargoTrackingContainerAdapter.ViewHolder>() {

        private lateinit var context: Context
        private lateinit var tfRegular: Typeface
        private lateinit var tfExtraBold: Typeface

        var datas = mutableListOf<CargoTrackingStatusItem>()
        var onClickItem: (Int) -> Unit = { _: Int -> }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            ResourcesCompat.getFont(context, R.font.opensans_regular)?.let{ tfRegular = it }
            ResourcesCompat.getFont(context, R.font.opensans_extrabold)?.let { tfExtraBold = it }
            return ViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.item_cargo_tracking_detail_container, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                tv_container_no.text = datas[position].containerNo
                tv_container_no.typeface = if (datas[position].isSelected) {
                    tfExtraBold
                } else {
                    tfRegular
                }
                tv_container_no.setBackgroundColor(context.getColor(if (datas[position].isSelected) {
                    R.color.color_f3f4f9
                } else {
                    R.color.colorWhite
                }))
                setSafeOnClickListener { onClickItem(position) }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}