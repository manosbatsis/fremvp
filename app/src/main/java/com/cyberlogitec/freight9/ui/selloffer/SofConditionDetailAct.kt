package com.cyberlogitec.freight9.ui.selloffer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.startActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_sof_condition_detail.*
import kotlinx.android.synthetic.main.body_sof_condition_detail.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber


@RequiresActivityViewModel(value = SofConditionDetailVm::class)
class SofConditionDetailAct : BaseActivity<SofConditionDetailVm>() {

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    //onClickItem = { viewModel.inPuts.clickTopic(it) }
                    //onLikeTopic = { viewModel.inPuts.likeTopic(it) }
                }
    }

    private var selectedContainderIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_condition_detail)

        (application as App).component.inject(this)

        // set status bar
        getWindow().statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_sof_plan_detail, menuType = MenuType.CROSS, title = "Set Collect Plan", isEnableNavi = false)

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")

            //viewModel.inPuts.clickToMenu(Parameter.CLICK)
            onBackPressed()
        }

        lo_pol.setSafeOnClickListener {
            Timber.d("f9: lo_pol click")
            viewModel.inPuts.clickToPolSelect(Parameter.CLICK)
        }

        // init recyclerview
        recyclerViewInit()

        // + test datas
        //val test_msg = "Ever wondered how some graphic desingers always manage to"
        for (x in 1..20)
            adapter.datas.add( x.toString() )

        adapter.notifyDataSetChanged()
        // - test datas

        // receive ViewModel event (onClickMenu)
        viewModel.outPuts.onClickMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startActivity(MenuActivity::class.java)
                }

        viewModel.outPuts.gotoPolSelect()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: popupPolSelect")

                    val containerList = listOf(
                            TextItem("KRPUS BUSAN, Republic Of Korea", true)
                            , TextItem("USOAK OAKLAND, CA")
                            , TextItem("CNSHA SHANGHAI, PR China")
                            , TextItem("CNNGB NINGBO")
                            , TextItem("USLO1 USLAX BLOCK STOWAGE")
                            , TextItem("USLO2 USLAX BLOCK STOWAGE")
                            , TextItem("USLO3 USLAX BLOCK STOWAGE")
                            , TextItem("USLO4 USLAX BLOCK STOWAGE")
                            , TextItem("USLO5 USLAX BLOCK STOWAGE")
                            , TextItem("USLO6 USLAX BLOCK STOWAGE")
                    )

                    val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
                    val dialog = BottomSheetDialog(this)

                    dialog.setCancelable(false)
                    dialog.setContentView(view)

                    view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
                    view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
                        override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                            view.picker.setSelected(index)
                            selectedContainderIndex = index
                        }
                    })

                    dialog.btn_done.setSafeOnClickListener {
                        dialog.hide()
                    }

                    view.picker.setItems(containerList)
                    view.picker.index = selectedContainderIndex

                    dialog.show()
                }
    }


    private fun recyclerViewInit() {
        recycler_view_sell_offer_plan_detail.apply {
            layoutManager = LinearLayoutManager(this@SofConditionDetailAct)
            adapter = this@SofConditionDetailAct.adapter
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        companion object {
            const val HEADER = 0
            const val CONTENT = 1
            const val FOOTER = 2
        }

        val datas = mutableListOf<String>()
        var onClickItem: (Long) -> Unit = {}

        override fun getItemCount(): Int {
            val size = datas.count() + 2 // header & footer
            return size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //Timber.d("f9: onCreateViewHolder(viewType: ${viewType})")

            when(viewType) {
                HEADER -> return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sof_header_plan_detail, parent, false))
                CONTENT -> return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sof_content_plan_detail, parent, false))
                FOOTER -> return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sof_footer_plan_detail, parent, false))
                else -> throw IllegalStateException("Item type unspecified.")
            }
        }

        override fun getItemViewType(position: Int): Int {
            //Timber.d("f9: getItemViewType(position: ${position})")

            when(position) {
                0 -> return HEADER
                datas.size + 1 -> return FOOTER
                else -> return CONTENT
            }
            //return super.getItemViewType(position)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //Timber.d("f9: onBindViewHolder(position(${position}))")

            if(holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                with(holder.itemView) {
                    //val data = datas[position]
                    //setOnClickListener { onClickItem(data.id) }
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }


}