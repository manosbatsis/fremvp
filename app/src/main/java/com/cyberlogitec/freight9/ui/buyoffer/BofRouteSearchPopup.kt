package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.lib.model.Schedule
import kotlinx.android.synthetic.main.item_bof_route_search.view.*
import kotlinx.android.synthetic.main.popup_bof_route_search.view.*
import timber.log.Timber
import java.util.*


class BofRouteSearchPopup(var view: View, width: Int, height: Int, focusable: Boolean,
                          onSearchItemClick: ((Item) -> Unit)) :
        PopupWindow(view, width, height, focusable) {

    private var datas = mutableListOf<Item>()
    private var dataKind: DataKind = DataKind.KIND_LANE
    private var im = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private val adapter by lazy {
        RecyclerAdapter()
                .apply { onClickItem = { selectedItem ->
                    onSearchItemClick(selectedItem)
                    dismiss()
                }}
    }

    init {
        this.isTouchable = true
        view.et_search.requestFocus()
        view.et_search.showSoftInputOnFocus = true

        view.iv_search_back.setOnClickListener {
            it?.let {
                dismiss()
            }
        }
        view.view_recycler_bg.setOnClickListener {
            it?.let {
                dismiss()
            }
        }

        view.iv_search_clear.setOnClickListener {
            it?.let {
                view.et_search.setText(Constant.EmptyString)
            }
        }

        view.et_search.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showHideKeypad(true)
                v.performClick()
            }
            true
        }

        view.et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { adapter.filter.filter(p0.toString()) }
        })

        view.recycler.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = this@BofRouteSearchPopup.adapter
        }

        view.recycler.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    showHideKeypad(false)
                }
            }
        })
    }

    override fun dismiss() {
        super.dismiss()
        showHideKeypad(false)
    }

    private fun showHideKeypad(isShow: Boolean) {
        if (isShow) {
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            im.showSoftInput(view.et_search, 0)
        } else {
            im.hideSoftInputFromWindow(view.et_search.windowToken, 0)
        }
    }

    fun initValue(dataKind: DataKind, datas: List<Item>) {
        this.dataKind = dataKind
        showHideKeypad(true)
        setData(datas)
    }

    private fun setData(datas: List<Item>) {
        this.datas = datas.filter { it.type == CONTENT }.toMutableList()
        adapter.setDataKind(this.dataKind)
        adapter.allDatas.addAll(this.datas)
        adapter.datas.addAll(this.datas)
        adapter.notifyDataSetChanged()
    }

    class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(), Filterable {

        val allDatas = mutableListOf<Item>()
        var datas = mutableListOf<Item>()
        var onClickItem: (Item) -> Unit = { selectedItem -> Timber.d("f9 : $selectedItem") }
        var onFilteredEnd: (Int) -> Unit = { size -> Timber.d("f9 : filter result $size") }
        var searchString: String = Constant.EmptyString
        private var dataKind: DataKind = DataKind.KIND_LANE

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_route_search, parent, false))
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(datas[position]) {
                setUiData(holder, this)
            }
            holder.itemView.setOnClickListener {
                onClickItem(datas[position])
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence?.toString()?.toLowerCase(Locale.getDefault())
                    searchString = queryString.toString()
                    val filterResults = FilterResults()
                    filterResults.values = if (queryString.isNullOrEmpty())
                        allDatas
                    else {
                        allDatas.filter { item ->
                            when(dataKind) {
                                DataKind.KIND_LANE -> {
                                    (item.schedule?.serviceLaneCode?.toLowerCase(Locale.getDefault())!!.contains(queryString)
                                            || item.schedule.serviceLaneName.toLowerCase(Locale.getDefault()).contains(queryString))
                                }
                                DataKind.KIND_POL -> {
                                    (item.schedule?.polCode?.toLowerCase(Locale.getDefault())!!.contains(queryString)
                                            || item.schedule.polName.toLowerCase(Locale.getDefault()).contains(queryString))
                                }
                                DataKind.KIND_POD -> {
                                    (item.schedule?.podCode?.toLowerCase(Locale.getDefault())!!.contains(queryString)
                                            || item.schedule.podName.toLowerCase(Locale.getDefault()).contains(queryString))
                                }
                            }
                        }.toMutableList()
                    }
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    datas = (results!!.values as List<Item>).toMutableList()
                    notifyDataSetChanged()
                    onFilteredEnd(datas.size)
                }
            }
        }

        private fun setUiData(holder: ViewHolder, item: Item) {
            with(holder.itemView) {
                item.schedule?.let { schedule ->
                    var code = Constant.EmptyString
                    var name = Constant.EmptyString
                    when(dataKind) {
                        DataKind.KIND_LANE -> {
                            code = schedule.serviceLaneCode
                            name = schedule.serviceLaneName
                        }
                        DataKind.KIND_POL -> {
                            code = schedule.polCode
                            name = schedule.polName
                        }
                        DataKind.KIND_POD -> {
                            code = schedule.podCode
                            name = schedule.podName
                        }
                    }
                    tv_cd.text = code
                    tv_desc.text = name
                    if (searchString.isNotEmpty()) {
                        if (code.toLowerCase(Locale.getDefault()).contains(searchString)) {
                            val startPos = code.toLowerCase(Locale.getDefault()).indexOf(searchString)
                            val endPos = startPos + searchString.length
                            val spanString = Spannable.Factory.getInstance().newSpannable(code)
                            spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                    startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            tv_cd.text = spanString
                        }
                        if (name.toLowerCase(Locale.getDefault()).contains(searchString)) {
                            val startPos = name.toLowerCase(Locale.getDefault()).indexOf(searchString)
                            val endPos = startPos + searchString.length
                            val spanString = Spannable.Factory.getInstance().newSpannable(name)
                            spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                    startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            spanString.setSpan(StyleSpan(Typeface.BOLD),
                                    startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            tv_desc.text = spanString
                        }
                    }
                }
            }
        }

        fun setDataKind(dataKind: DataKind) {
            this.dataKind = dataKind
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    enum class DataKind {
        KIND_LANE,
        KIND_POL,
        KIND_POD
    }

    companion object {
        const val HEADER = 0
        const val SUB_HEADER = 1
        const val CONTENT = 2
    }

    class Item(internal val type: Int,
               internal val title: String?,
               internal val schedule: Schedule?,
               internal var isOpened: Boolean,
               internal var isSubOpened: Boolean) {

        data class Builder(
                private var type: Int = 0,
                private var title: String? = null,
                private var schedule: Schedule? = null,
                private var isOpened: Boolean = true,
                private var isSubOpened: Boolean = true) {

            fun type(type: Int) = apply { this.type = type }
            fun title(title: String?) = apply { this.title = title }
            fun schedule(schedule: Schedule?) = apply { this.schedule = schedule }
            fun isOpened(isOpened: Boolean) = apply { this.isOpened = isOpened }
            fun isSubOpened(isSubOpened: Boolean) = apply { this.isSubOpened = isSubOpened }
            fun build() = Item(
                    type,
                    title,
                    schedule,
                    isOpened,
                    isSubOpened
            )
        }
    }
}

