package com.cyberlogitec.freight9.ui.trademarket

import android.graphics.Typeface
import android.os.Bundle
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
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.lib.model.Container
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_market_container_filter.*
import kotlinx.android.synthetic.main.item_market_filter_container.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber

@RequiresActivityViewModel(value = MarketContainerFilterViewModel::class)
class MarketContainerFilterActivity : BaseActivity<MarketContainerFilterViewModel>() {

    private val rdTermAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }
    private val typeAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }
    private val sizeAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("onCreate")

        setContentView(R.layout.act_market_container_filter)

        (application as App).component.inject(this)

        // set status bar
        window.statusBarColor = getColor(R.color.color_0d0d0d)

        toolbarInit()


        btn_filter_container_apply.setOnClickListener {
            Timber.d("market_btn_filter_container_apply_click")
            val list = mutableListOf<Container>()
            list.addAll(rdTermAdapter.datas)
            list.addAll(sizeAdapter.datas)
            list.addAll(typeAdapter.datas)
            viewModel.inPuts.clickToApply(list)
        }

        viewModel.outPuts.gotoMarket()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("startActivity(MarketActivity)")
                    //startActivityWithFinish(MarketActivity::class.java)
                    finish()
                }
        viewModel.outPuts.refreshContainerRdterm()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("refreshContainerRdterm")
                    rdTermAdapter.datas.clear()
                    rdTermAdapter.datas.addAll(it)
                    rdTermAdapter.notifyDataSetChanged()
                }

        viewModel.outPuts.refreshContainerSize()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("refreshContainerSize")
                    sizeAdapter.datas.clear()
                    sizeAdapter.datas.addAll(it)
                    sizeAdapter.notifyDataSetChanged()
                }
        viewModel.outPuts.refreshContainerType()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("refreshContainerType")
                    typeAdapter.datas.clear()
                    typeAdapter.datas.addAll(it)
                    typeAdapter.notifyDataSetChanged()
                }

        // init recyclerview
        recyclerViewInit()

    }
    private fun toolbarInit() {
        defaultbarInit(toolbar_common,menuType = MenuType.CROSS)
        toolbar_common.setBackgroundColor(getColor(R.color.color_0d0d0d))
        toolbar_left_btn.visibility = View.INVISIBLE
        toolbar_right_btn.setOnClickListener {
            Timber.d("market_btn_filter_container_x_click")
            finish()
        }
    }

    private fun recyclerViewInit() {
        val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager(this).orientation)
        recycler_view_market_container_rdterm_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_container_rdterm_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketContainerFilterActivity)
            adapter = this@MarketContainerFilterActivity.rdTermAdapter
        }
        recycler_view_market_container_type_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_container_type_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketContainerFilterActivity)
            adapter = this@MarketContainerFilterActivity.typeAdapter
        }

        recycler_view_market_container_size_filter.addItemDecoration(dividerItemDecoration)
        recycler_view_market_container_size_filter.apply {
            layoutManager = LinearLayoutManager(this@MarketContainerFilterActivity)
            adapter = this@MarketContainerFilterActivity.sizeAdapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        private lateinit var tfRegular: Typeface
        private lateinit var tfBold: Typeface
        val datas = mutableListOf<Container>()
        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            tfRegular = ResourcesCompat.getFont(parent.context, R.font.opensans_regular)!!
            tfBold = ResourcesCompat.getFont(parent.context, R.font.opensans_bold)!!
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_market_filter_container, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with(datas[position]) {
                    when {
                        position == 0 -> {
                            space_top.visibility = View.VISIBLE
                            space_bottom.visibility = View.GONE
                        }
                        datas.size == position + 1 -> {
                            space_top.visibility = View.GONE
                            space_bottom.visibility = View.VISIBLE
                        }
                        else -> {
                            space_top.visibility = View.GONE
                            space_bottom.visibility = View.GONE
                        }
                    }
                    when(dataType) {
                        "type" -> {
                            when(code) {
                                ContainerType.F_TYPE -> { tv_item.text = resources.getString(R.string.filter_container_type_full) }
                                ContainerType.R_TYPE -> { tv_item.text = resources.getString(R.string.filter_container_type_rf) }
                                ContainerType.E_TYPE -> { tv_item.text = resources.getString(R.string.filter_container_type_empty) }
                                ContainerType.S_TYPE -> { tv_item.text = resources.getString(R.string.filter_container_type_soc) }
                            }
                        }
                        "size","rdterm" -> {
                            tv_item.text = fullname
                        }
                    }

                    if(selected!!) {
                        tv_item.typeface = tfBold
                    }else {
                        tv_item.typeface = tfRegular
                    }
                    tv_item.isSelected = selected!!
                    cb_select.isChecked = selected!!
                }

                rl_market_filter_item.setOnClickListener {
                    if(!datas[position].selected!!){
                        clearSelected()
                        datas[position].selected = !datas[position].selected!!
                        notifyDataSetChanged()
                    }
                }
            }
        }

        fun clearSelected() {
            for(data in datas) {
                data.selected = false
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
