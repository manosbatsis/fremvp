package com.cyberlogitec.freight9.ui.routeselect.search

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.frag_featured_route.*
import kotlinx.android.synthetic.main.item_featured_route.view.*
import timber.log.Timber

class FeatureRouteSearchFragment(val viewModel: RouteSearchViewModel) : RxFragment() {

    private lateinit var mAdapter: FeaturedRouteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_featured_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = FeaturedRouteAdapter()
        recycler_featured_route.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        viewModel.outPuts.doSearchRoute()
                .bindToLifecycle(this)
                .subscribe { pair->
                    Timber.v("diver:/ search keyword: "+pair.first)
                    pair.second.doOnNext {
                        if (pair.first.length > 0) {
                            Timber.v("diver:/ search route cnt="+it.size)
                            mAdapter.update(it, pair.first)
                            activity?.runOnUiThread(Runnable {
                                tv_btn_edit.visibility = View.INVISIBLE

                            })
                        }
                    }.subscribe {
                        if (pair.first.length <= 0){
                            activity?.runOnUiThread {
                                tv_btn_edit.visibility = View.VISIBLE
                            }
                            viewModel.inPuts.callFeaturedRoute(Parameter.EVENT)
                        }
                    }
                }

        viewModel.outPuts.getFeaturedRoutes()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.v("route:/route load  on fragment, size="+it.size)
                    activity?.runOnUiThread {
                        mAdapter.routeList = it
                        mAdapter.keyword = ""
                        mAdapter.notifyDataSetChanged()
                    }
                }
        viewModel.inPuts.callFeaturedRoute(Parameter.NULL)

        recycler_featured_route.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    viewModel.inPuts.callFocusChange(false)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    Timber.v("diver:/ drag, dy="+dy)
                    if(dy > 40) {
                        viewModel.inPuts.callHideSearch(Parameter.EVENT)
                    }else if(dy < -10){
                        viewModel.inPuts.callShowSearch(Parameter.EVENT)
                    }
                }
            }
        })

        tv_btn_edit.setOnClickListener {
            Timber.v("diver:/ onClick Edit")
            viewModel.clickPreferredEdit(Parameter.CLICK)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.inPuts.callFeaturedRoute(Parameter.NULL)
    }

    inner class FeaturedRouteAdapter() : RecyclerView.Adapter<FeaturedRouteAdapter.FeatureRouteViewHolder>(){
        var routeList = listOf<FeaturedRoute>()
        var keyword: String = ""

        fun update(list: List<FeaturedRoute>, keyword: String){
            this.routeList = list
            this.keyword = keyword
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureRouteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_featured_route, parent, false)
            view.setOnClickListener {
                viewModel.inPuts.callFocusChange(false)
            }
            return FeatureRouteViewHolder(view)
        }

        override fun getItemCount(): Int {
            return routeList.size
        }

        override fun onBindViewHolder(holder: FeatureRouteViewHolder, position: Int) {
            val data = routeList[position]
            holder.itemView.tv_featured_route_from_code.setText(data.fromCode)
            holder.itemView.tv_featured_route_from_detail.setText(data.fromDetail)
            holder.itemView.tv_featured_route_to_code.setText(data.toCode)
            holder.itemView.tv_featured_route_to_detail.setText(data.toDetail)

            if (keyword.isNotEmpty()){
                if (data.fromCode.startsWith(keyword, true)){
                    val builder = SpannableStringBuilder(data.fromCode)
                    builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.itemView.tv_featured_route_from_code.text = builder
                }
                if (data.fromDetail!!.startsWith(keyword, true)){
                    val builder = SpannableStringBuilder(data.fromDetail)
                    builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.itemView.tv_featured_route_from_detail.text = builder
                }
                if (data.toCode.startsWith(keyword, true)){
                    val builder = SpannableStringBuilder(data.toCode)
                    builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.itemView.tv_featured_route_to_code.text = builder
                }
                if (data.toDetail!!.startsWith(keyword, true)){
                    val builder = SpannableStringBuilder(data.toDetail)
                    builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.itemView.tv_featured_route_to_detail.text = builder
                }
            }

            holder.itemView.setOnClickListener {
                val selectRoute = FeaturedRoute(null, data.fromCode, data.fromDetail, data.toCode, data.toDetail, 0)
                viewModel.inPuts.callSelectItem(selectRoute)
            }
        }
        inner class FeatureRouteViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}



