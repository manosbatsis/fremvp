package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.LocationTypeCode.POD
import com.cyberlogitec.freight9.config.LocationTypeCode.POL
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.toJson
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_bof_recent.*
import kotlinx.android.synthetic.main.appbar_bof_recent.*
import kotlinx.android.synthetic.main.body_bof_recent.*
import kotlinx.android.synthetic.main.item_bof_recent.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.util.*


@RequiresActivityViewModel(value = BofRecentVm::class)
class BofRecentAct : BaseActivity<BofRecentVm>() {

    // list view
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemClick = { position, isChecked -> viewModel.inPuts.clickToItem(Pair(position, isChecked)) }
                    onFilteredEnd = { size ->
                        Timber.d("f9: filtered size : $size")
                    }
                }
    }

    override fun onBackPressed() {
        Timber.v("onBackPressed --> prevent backpress")
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_recent)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_bof_recent,
                menuType = MenuType.CROSS,
                title = getString(R.string.buy_offer_select_route),
                isEnableNavi=false)

        // init recycler views
        recyclerViewInit()
        setListener()
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offers ->
                        Timber.d("f9: onSuccessRefresh")
                        Timber.d("f9: %s", offers.toJson())

                        et_search.setText("")
                        et_search.clearFocus()

                        adapter.datas.clear()
                        adapter.datas.addAll(offers)

                        adapter.allDatas.clear()
                        adapter.allDatas.addAll(offers)

                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onClickItem()
                .bindToLifecycle(this)
                .subscribe { value ->
                    Timber.d("f9: onClickItem")
                    Timber.d("f9: isChecked -> ${value.second}")
                    isCheckedItem(value.second, value.first)
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe { offer ->
                    Timber.d("f9: onClickNext")
                    if (offer.isChecked) {
                        startActivity(Intent(this, BofRouteAct::class.java).putExtra(Intents.OFFER, offer))
                    } else {
                        startActivity(Intent(this, BofLaneAct::class.java))
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog : $it")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }
    }

    private fun setListener() {
        appbar_bof_recent.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        btn_recent_next.setSafeOnClickListener {
            Timber.d("f9: btn_recent_next click")
            val offer2 = adapter.datas.find{ it.isChecked }
            if (offer2 != null) {
                viewModel.inPuts.clickToNext( offer2 )
            } else {
                viewModel.inPuts.clickToNext( Offer() )
            }
        }

        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isCheckedItem(false)
                adapter.filter.filter(p0.toString())
            }
        })
    }

    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofRecentAct)
            adapter = this@BofRecentAct.adapter
        }
    }

    private fun isCheckedItem(isChecked: Boolean, position: Int = -1) {
        adapter.datas.map { it.isChecked = false }
        if (position > -1) {
            adapter.datas[position].isChecked = isChecked
        }

        if (isChecked) {
            btn_recent_next.text = getString(R.string.select)
        } else {
            btn_recent_next.text = getString(R.string.skip)
        }
        adapter.notifyDataSetChanged()

    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

        var onItemClick: (Int, Boolean) -> Unit = { _, _ -> }
        var allDatas = mutableListOf<Offer>()
        var datas = mutableListOf<Offer>()
        var searchString: String = Constant.EmptyString
        var onFilteredEnd: (Int) -> Unit = { size -> Timber.d("f9 : filter result $size") }

        override fun getItemCount() = this.datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_recent, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Timber.d("f9: onBindViewHolder(position(${position}))")
            val data = datas[position]
            with(holder.itemView) {
                if (data.isChecked) {
                    ll_item_bof_recenet_cell.setBackgroundResource(R.drawable.bg_round_white_corner_blueviolet)
                } else {
                    ll_item_bof_recenet_cell.setBackgroundResource(R.drawable.bg_round_corner_white)
                }

                data.offerRoutes?.let { routes ->
                    val polList = routes.filter { it.locationTypeCode == POL }
                                                        .filter { listOf(it).isNotEmpty() }
                    polList.minBy { it.offerRegSeq }!!
                            .let {
                                tv_pol_name.text = it.locationCode
                                tv_pol_desc.text  = it.locationName
                                if (searchString.isNotEmpty()) {
                                    if (it.locationCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                        val startPos = it.locationCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                        val endPos = startPos + searchString.length
                                        val spanString = Spannable.Factory.getInstance().newSpannable(it.locationCode)
                                        spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                                startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        tv_pol_name.text = spanString
                                    }
                                }
                            }
                    polList.let { polListRoutes ->
                        val polCount = polListRoutes
                                .filter { it.locationTypeCode == POL }
                                .distinctBy { it.locationName }
                                .size
                        tv_pol_count.text = if (polCount > 1) "+" + (polCount - 1) else ""
                    }
                }

                data.offerRoutes?.let { routes ->
                    val podList = routes.filter { it.locationTypeCode == POD }
                                                        .filter { listOf(it).isNotEmpty() }
                    podList.minBy { it.offerRegSeq }!!
                            .let {
                                tv_pod_name.text = it.locationCode
                                tv_pod_desc.text = it.locationName
                                if (searchString.isNotEmpty()) {
                                    if (it.locationCode.toLowerCase(Locale.getDefault()).contains(searchString)) {
                                        val startPos = it.locationCode.toLowerCase(Locale.getDefault()).indexOf(searchString)
                                        val endPos = startPos + searchString.length
                                        val spanString = Spannable.Factory.getInstance().newSpannable(it.locationCode)
                                        spanString.setSpan(ForegroundColorSpan(context.getColor(R.color.blue_violet)),
                                                startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        tv_pod_name.text = spanString
                                    }
                                }
                            }
                    podList.let { podListRoutes ->
                        val podCount = podListRoutes
                                .filter { it.locationTypeCode == POD }
                                .distinctBy { it.locationName }
                                .size
                        tv_pod_count.text = if (podCount > 1) "+" + (podCount - 1) else ""
                    }
                }

                ll_item_bof_recenet_cell.setSafeOnClickListener {
                    Timber.d("f9: ll_item_bof_recent_cell Click: $position")
                    onItemClick(position, !data.isChecked)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence?.toString()?.toLowerCase(Locale.getDefault())
                    searchString = queryString.toString()
                    val filterResults = FilterResults()
                    if (queryString.isNullOrEmpty()) {
                        filterResults.values = allDatas
                    } else {
                        val searchedList = mutableListOf<Offer>()
                        for (data in allDatas) {
                            val polSearched = data.offerRoutes?.filter { route ->
                                                    route.locationTypeCode == POL
                                                }?.filter { listOf(it).isNotEmpty()
                                                }?.minBy {
                                                    it.offerRegSeq
                                                }?.locationCode?.toLowerCase(Locale.getDefault())?.contains(queryString)
                            val podSearched = data.offerRoutes?.filter { route ->
                                                    route.locationTypeCode == POD
                                                }?.filter { listOf(it).isNotEmpty()
                                                }?.minBy {
                                                    it.offerRegSeq
                                                }?.locationCode?.toLowerCase(Locale.getDefault())?.contains(queryString)

                            if (polSearched!! || podSearched!!) {
                                searchedList.add(data)
                            }
                        }
                        filterResults.values = searchedList
                    }
                    return filterResults
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    datas = (results!!.values as List<Offer>).toMutableList()
                    notifyDataSetChanged()
                    onFilteredEnd(datas.size)
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}