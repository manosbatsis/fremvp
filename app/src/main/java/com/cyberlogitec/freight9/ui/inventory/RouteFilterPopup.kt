package com.cyberlogitec.freight9.ui.inventory

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import kotlinx.android.synthetic.main.item_route_filter_select.view.*
import kotlinx.android.synthetic.main.popup_route_filter.view.*
import timber.log.Timber
import java.util.*


class RouteFilterPopup(var view: View, width: Int, height: Int, focusable: Boolean,
                       onRouteSelectClick: ((Int, RouteFromTo, RouteAdapterData) -> Unit)) :
        PopupWindow(view, width, height, focusable) {

    private lateinit var routeFromTo: RouteFromTo
    private var currentEditText: View
    private var im = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private val adapter by lazy {
        RecyclerRouteSelectAdapter()
                .apply { onClickItem = { position, routeSelect, routeAdapterData ->
                    onRouteSelectClick(position, routeSelect, routeAdapterData)
                    dismiss()
                }
                onFilteredEnd = {size ->
                    view.ll_recycler_route_select.visibility = if (size > 0) View.VISIBLE else View.GONE
                }}
    }

    init {
        currentEditText = view.et_route_filter_from
        this.isTouchable = true

        view.ll_route_filter_top.setOnClickListener {
            it?.let {
                dismiss()
            }
        }

        view.fl_route_filter_from_to.setOnClickListener {
            it?.let {
                dismiss()
            }
        }

        view.view_recycler_route_bg.setOnClickListener {
            it?.let {
                dismiss()
            }
        }

        view.et_route_filter_from.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                currentEditText = view.et_route_filter_from
                showHideKeypad(true)
                v.performClick()
            }
            true
        }

        view.et_route_filter_from.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { adapter.filter.filter(p0.toString()) }
        })

        view.et_route_filter_to.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                currentEditText = view.et_route_filter_to
                showHideKeypad(true)
                v.performClick()
            }
            true
        }

        view.et_route_filter_to.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { adapter.filter.filter(p0.toString()) }
        })

        view.recycler_route_select.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = this@RouteFilterPopup.adapter
        }

        view.recycler_route_select.addOnScrollListener(object: RecyclerView.OnScrollListener(){
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
        if (this.routeFromTo == RouteFromTo.ALL) return
        if (isShow) {
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            im.showSoftInput(currentEditText, 0)
        } else {
            im.hideSoftInputFromWindow(currentEditText.windowToken, 0)
        }
    }

    fun initValue(routeSelect: RouteFromTo, routeAdapterData: List<RouteAdapterData>) {
        this.routeFromTo = routeSelect
        when(routeSelect) {
            RouteFromTo.FROM -> {
                currentEditText = view.et_route_filter_from
                view.et_route_filter_to.visibility = View.INVISIBLE
                view.et_route_filter_from.visibility = View.VISIBLE
                view.lottie_route_filter_all.visibility = View.INVISIBLE
                view.et_route_filter_from.requestFocus()
                view.et_route_filter_from.showSoftInputOnFocus = true
                showHideKeypad(true)
            }
            RouteFromTo.TO -> {
                currentEditText = view.et_route_filter_to
                view.et_route_filter_to.visibility = View.VISIBLE
                view.et_route_filter_from.visibility = View.INVISIBLE
                view.lottie_route_filter_all.visibility = View.INVISIBLE
                view.et_route_filter_to.requestFocus()
                view.et_route_filter_to.showSoftInputOnFocus = true
                showHideKeypad(true)
            }
            RouteFromTo.ALL -> {
                view.lottie_route_filter_all.visibility = View.VISIBLE
                view.et_route_filter_to.visibility = View.INVISIBLE
                view.et_route_filter_from.visibility = View.INVISIBLE
                view.lottie_route_filter_all.apply {
                    repeatCount = 1
                    playAnimation()
                }
            }
            else -> { }
        }
        setData(routeSelect, routeAdapterData)
    }

    private fun setData(routeSelect: RouteFromTo, routeAdapterData: List<RouteAdapterData>) {
        adapter.setRouteSelect(routeSelect)
        adapter.allDatas.addAll(routeAdapterData)
        adapter.datas.addAll(routeAdapterData)
        adapter.notifyDataSetChanged()
    }

    class RecyclerRouteSelectAdapter : RecyclerView.Adapter<RecyclerRouteSelectAdapter.ViewHolder>(), Filterable {

        val allDatas = mutableListOf<RouteAdapterData>()
        var datas = mutableListOf<RouteAdapterData>()
        var onClickItem: (Int, RouteFromTo, RouteAdapterData) -> Unit =
                { position, routeSelect, routeAdapterData ->
                    Timber.d("f9 : $position, $routeSelect, $routeAdapterData")
                }
        var onFilteredEnd: (Int) -> Unit = { size -> Timber.d("f9 : filter result $size") }
        var searchString: String = ""
        private lateinit var routeFromTo: RouteFromTo

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_route_filter_select, parent, false))
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            with(datas[position]) {
                setUiVisible(holder, this)
                setUiData(holder, this)
            }

            holder.itemView.setOnClickListener {
                onClickItem(position, routeFromTo, datas[position])
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
                        allDatas.filter {
                            it.polOrPorPortCode.toLowerCase(Locale.getDefault()).contains(queryString)
                                    || it.podOrDelPortCode.toLowerCase(Locale.getDefault()).contains(queryString)
                        }
                    }
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    datas = (results!!.values as List<RouteAdapterData>).toMutableList()
                    notifyDataSetChanged()
                    onFilteredEnd(datas.size)
                }
            }
        }

        fun setRouteSelect(routeFromTo: RouteFromTo) {
            this.routeFromTo = routeFromTo
        }

        private fun setUiVisible(holder: ViewHolder, routeAdapterData: RouteAdapterData) {
            Timber.d("f9 : $routeAdapterData")
            with(holder.itemView) {
                when (routeFromTo) {
                    RouteFromTo.FROM -> {
                        ll_inventory_route_item_value_oneside.visibility = View.VISIBLE
                        ll_inventory_route_item_value_all.visibility = View.GONE
                    }
                    RouteFromTo.ALL -> {
                        ll_inventory_route_item_value_oneside.visibility = View.GONE
                        ll_inventory_route_item_value_all.visibility = View.VISIBLE
                    }
                    RouteFromTo.TO -> {
                        ll_inventory_route_item_value_oneside.visibility = View.VISIBLE
                        ll_inventory_route_item_value_all.visibility = View.GONE
                    }
                    else -> { }
                }
            }
        }

        private fun setUiData(holder: ViewHolder, routeAdapterData: RouteAdapterData) {
            with(holder.itemView) {
                val oneSide = routeFromTo == RouteFromTo.FROM || routeFromTo == RouteFromTo.TO
                with(routeAdapterData) {
                    when (oneSide) {
                        // From or To
                        true -> {
                            if (polOrPorPortKind == PortKind.ALL || podOrDelPortKind == PortKind.ALL) {
                                // opensans-bold
                                val bold = ResourcesCompat.getFont(context, R.font.opensans_bold)
                                tv_inventory_route_item_polpod_code.typeface = bold
                                tv_inventory_route_item_polpod_code.text = context.getString(R.string.route_filter_all)
                                tv_inventory_route_item_polpod_name.text = ""
                            } else {
                                // opensans-extrabold
                                val extraBold = ResourcesCompat.getFont(context, R.font.opensans_extrabold)
                                tv_inventory_route_item_polpod_code.typeface = extraBold
                                if (searchString.isNotEmpty()) {
                                    if (routeFromTo == RouteFromTo.FROM) {
                                        tv_inventory_route_item_polpod_code.text = polOrPorPortCode
                                        if (polOrPorPortCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                            val startPos = polOrPorPortCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                            val endPos = startPos + searchString.length
                                            val spanString = Spannable.Factory.getInstance().newSpannable(polOrPorPortCode)
                                            spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                                    startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                            tv_inventory_route_item_polpod_code.text = spanString
                                        }
                                    } else {
                                        tv_inventory_route_item_polpod_code.text = podOrDelPortCode
                                        if (podOrDelPortCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                            val startPos = podOrDelPortCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                            val endPos = startPos + searchString.length
                                            val spanString = Spannable.Factory.getInstance().newSpannable(podOrDelPortCode)
                                            spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                                    startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                            tv_inventory_route_item_polpod_code.text = spanString
                                        }
                                    }
                                } else {
                                    if (routeFromTo == RouteFromTo.FROM) {
                                        tv_inventory_route_item_polpod_code.text = polOrPorPortCode
                                    } else {
                                        tv_inventory_route_item_polpod_code.text = podOrDelPortCode
                                    }
                                }
                                if (routeFromTo == RouteFromTo.FROM) {
                                    tv_inventory_route_item_polpod_name.text = polOrPorPortName
                                } else {
                                    tv_inventory_route_item_polpod_name.text = podOrDelPortName
                                }
                            }
                        }
                        // ->
                        else -> {
                            if (polOrPorPortKind == PortKind.ALL || podOrDelPortKind == PortKind.ALL) {
                                tv_inventory_route_item_polcode.text = context.getString(R.string.route_filter_all)
                                tv_inventory_route_item_polname.text = context.getString(R.string.route_filter_port_or_city)
                                tv_inventory_route_item_podcode.text = context.getString(R.string.route_filter_all)
                                tv_inventory_route_item_podname.text = context.getString(R.string.route_filter_port_or_city)
                            } else {
                                if (searchString.isNotEmpty()) {
                                    tv_inventory_route_item_polcode.text = polOrPorPortCode
                                    tv_inventory_route_item_podcode.text = podOrDelPortCode
                                    tv_inventory_route_item_polname.visibility = View.VISIBLE
                                    tv_inventory_route_item_podname.visibility = View.VISIBLE
                                    if (polOrPorPortCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                        val startPos = polOrPorPortCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                        val endPos = startPos + searchString.length
                                        val spanString = Spannable.Factory.getInstance().newSpannable(polOrPorPortCode)
                                        spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                                startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        tv_inventory_route_item_polcode.text = spanString
                                    }
                                    if (podOrDelPortCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                        val startPos = podOrDelPortCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                        val endPos = startPos + searchString.length
                                        val spanString = Spannable.Factory.getInstance().newSpannable(podOrDelPortCode)
                                        spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        tv_inventory_route_item_podcode.text = spanString
                                    }
                                } else {
                                    tv_inventory_route_item_polcode.text = polOrPorPortCode
                                    tv_inventory_route_item_podcode.text = podOrDelPortCode
                                }
                                tv_inventory_route_item_polname.text = polOrPorPortName
                                tv_inventory_route_item_podname.text = podOrDelPortName
                            }
                        }
                    }
                }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }


    enum class RouteFromTo {
        FROM,
        ALL,
        TO,
        INIT
    }

    data class RouteData(
            val portKind: PortKind,
            val index: Int = -1,
            val portCode: String = "",
            val portName: String = ""
    )

    enum class PortKind {
        NONE,
        ALL,
        POL,
        POR,
        POD,
        DEL
    }

    data class RouteAdapterData(
            val polOrPodIndex: Int,
            val polOrPorPortKind: PortKind,
            val polOrPorPortCode: String,
            val polOrPorPortName: String,
            val podOrDelIndex: Int,
            val podOrDelPortKind: PortKind,
            val podOrDelPortCode: String,
            val podOrDelPortName: String
    )
}

