package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.getCodeCount
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_RECENTLY
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_recent.*
import kotlinx.android.synthetic.main.item_bof_recent.view.*
import timber.log.Timber
import java.util.*

class BofWizardRouteRecentFragment constructor(val viewModel: BofWizardRouteViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    /**
     * Recently route list 에 대한 Recycler view의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemClick = { position, isChecked ->
                        isCheckedItem(isChecked, position)
                    }
                    onFilteredEnd = { size ->
                        Timber.d("f9: filtered size : $size")
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_route_recent, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("f9: onViewCreated")
        setRxOutputs()
        initData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("f9: onAttach")
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        Timber.d("f9: onDetach")
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        Timber.d("f9: onDestroyView")
        super.onDestroyView()
    }

    override fun onStart() {
        Timber.d("f9: onStart")
        super.onStart()
    }

    override fun onStop() {
        Timber.d("f9: onStop")
        super.onStop()
    }

    override fun onResume() {
        Timber.d("f9: onResume")
        super.onResume()
        viewModel.inPuts.requestLoadRouteData(Parameter.EVENT)
    }

    override fun onPause() {
        Timber.d("f9: onPause")
        super.onPause()
    }

    //----------------------------------------------------------------------------------------------

    /**
     * fragment data init
     */
    private fun initData() {
        recyclerViewInit()
        setListener()
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {

        viewModel.outPuts.onGoToStepRecently()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offers ->

                        bof_recent_root.scrollTo(0, 0)
                        viewModel.inPuts.requestSearchInit(Parameter.EVENT)

                        adapter.datas.clear()
                        adapter.datas.addAll(offers)

                        adapter.allDatas.clear()
                        adapter.allDatas.addAll(offers)

                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onSearchFilter()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { pair ->
                        isCheckedItem(pair.first)
                        adapter.filter.filter(pair.second)
                    }
                }
    }

    private fun setListener() {

    }

    /**
     * "Select Route" : Recyclerview <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@BofWizardRouteRecentFragment.context)
            adapter = this@BofWizardRouteRecentFragment.adapter
        }
    }

    /**
     * recently item 을 selected 한 경우 ui refresh
     */
    private fun isCheckedItem(isChecked: Boolean, position: Int = -1) {
        adapter.datas.map { it.isChecked = false }
        if (position > -1) {
            adapter.datas[position].isChecked = isChecked
        }
        adapter.notifyDataSetChanged()

        viewModel.inPuts.requestSetDataStatus(Pair(STEP_RECENTLY,
                Pair(isChecked, adapter.datas.find { it.isChecked })
        ))
    }

    /**
     * recyclerview adapter
     */
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
            val data = datas[position]
            with(holder.itemView) {
                if (data.isChecked) {
                    ll_item_bof_recenet_cell.setBackgroundResource(R.drawable.bg_round_white_corner_blueviolet)
                } else {
                    ll_item_bof_recenet_cell.setBackgroundResource(R.drawable.bg_round_corner_white)
                }

                data.offerRoutes?.let { routes ->
                    val polList = routes.filter { it.locationTypeCode == LocationTypeCode.POL }
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
                                .filter { it.locationTypeCode == LocationTypeCode.POL }
                                .distinctBy { it.locationName }
                                .size
                        tv_pol_count.text = polCount.getCodeCount(false)
                    }
                }

                data.offerRoutes?.let { routes ->
                    val podList = routes.filter { it.locationTypeCode == LocationTypeCode.POD }
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
                                .filter { it.locationTypeCode == LocationTypeCode.POD }
                                .distinctBy { it.locationName }
                                .size
                        tv_pod_count.text = podCount.getCodeCount(false)
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
                                route.locationTypeCode == LocationTypeCode.POL
                            }?.filter { listOf(it).isNotEmpty()
                            }?.minBy {
                                it.offerRegSeq
                            }?.locationCode?.toLowerCase(Locale.getDefault())?.contains(queryString)
                            val podSearched = data.offerRoutes?.filter { route ->
                                route.locationTypeCode == LocationTypeCode.POD
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

    //----------------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardRouteViewModel) : BofWizardRouteRecentFragment {
            return BofWizardRouteRecentFragment(viewModel)
        }
    }
}