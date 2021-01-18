package com.cyberlogitec.freight9.ui.routeselect.search

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.db.PortDao.PortMinimal
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.frag_route_search.*
import kotlinx.android.synthetic.main.item_route_search_continent.view.*
import kotlinx.android.synthetic.main.item_route_search_country.view.*
import kotlinx.android.synthetic.main.item_route_search_port.view.*
import me.texy.treeview.TreeNode
import me.texy.treeview.TreeView
import me.texy.treeview.base.BaseNodeViewBinder
import me.texy.treeview.base.BaseNodeViewFactory
import timber.log.Timber
import java.util.*

class PortSearchFragment(val viewModel: RouteSearchViewModel) : RxFragment() {
    var searchAdapter: SearchResultAdapter = SearchResultAdapter("")
    lateinit var treeView: TreeView

    private lateinit var searchResult: List<PortMinimal>
    private var currentResultPage = 1
    private var totalResultPage = 1
    private val resultPageDivider = 200

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_route_search, container, false)
        treeView = TreeView(viewModel.initPortTree, context!!, NodeViewFactory())
        treeView.setItemAnimator(DefaultItemAnimator())
        treeView.view.also {
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            (it as RecyclerView).addItemDecoration(TreeViewDecoration())
            it.addOnScrollListener(TreeViewScrollListenr())
        }
        treeView.collapseAll()
        treeView.allNodes
                .find { it.value.toString().equals("Recently") }
                .apply {
                    treeView.expandNode(this)
                }

        viewModel.outPuts.getPortTree()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.v("diver:/ draw treeview on ui")
                    activity?.runOnUiThread(Runnable {
                        container_search?.let { container_search.visibility = View.INVISIBLE }
                        container_tree?.let { container_tree.visibility = View.VISIBLE }
                    })
                }

        viewModel.outPuts.doSearchPort()
                .bindToLifecycle(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pair->
                    Timber.v("diver:/ call getSearchResult at ${Date()}")
                    pair.second.subscribe {
                        if (pair.first.isNotEmpty()){
                            Timber.v("route:/ search result cnt=${it.size}")
                            activity?.runOnUiThread {
                                container_tree?.let { container_tree.visibility = View.INVISIBLE }
                                container_search?.let { container_search.visibility = View.VISIBLE }

                                searchResult = it;
                                currentResultPage = 1
                                totalResultPage = it.size.div(resultPageDivider)
                                if (it.size.rem(resultPageDivider) > 0) {
                                    totalResultPage += 1
                                }

                                val toIndex = if (totalResultPage > 1) resultPageDivider-1 else searchResult.size
                                searchAdapter.keyword = pair.first
                                searchAdapter.clearData()
                                searchAdapter.setData(searchResult.subList(0, toIndex))
                                searchAdapter.notifyDataSetChanged()
                            }
                        } else {
                            activity?.runOnUiThread {
                                container_search?.let { container_search.visibility = View.INVISIBLE }
                                container_tree?.let { container_tree.visibility = View.VISIBLE }
                            }
                        }
                    }
                }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container_tree.removeAllViews()
        container_tree.addView(treeView.view)
        container_search.visibility = View.INVISIBLE
        container_tree.visibility = View.VISIBLE

        recycler_search_result.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManagerWrapper(this.context)
        }
        recycler_search_result.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    viewModel.inPuts.callFocusChange(false)
                }

                activity?.runOnUiThread {
                    // add result partial list when End of list
                    if (!recycler_search_result.canScrollVertically(1)) {
                        // from index of nextpage
                        val fromIndex = currentResultPage * resultPageDivider
                        currentResultPage += 1
                        var doNotifyDataSetChanged = true
                        when {
                            currentResultPage < totalResultPage -> {
                                val toIndex = fromIndex + resultPageDivider - 1
                                searchAdapter.setData(searchResult.subList(fromIndex, fromIndex + resultPageDivider - 1))
                            }
                            currentResultPage == totalResultPage -> {
                                val toIndex = searchResult.size - 1
                                searchAdapter.setData(searchResult.subList(fromIndex, searchResult.size - 1))
                            }
                            else -> {
                                doNotifyDataSetChanged = false
                            }
                        }
                        if(doNotifyDataSetChanged) {
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        container_search.visibility = View.INVISIBLE
        container_tree.visibility = View.VISIBLE
        treeView.collapseAll()
        treeView.allNodes
                .find { it.value.toString().equals("Recently") }
                .apply {
                    treeView.expandNode(this)
                }
    }

    inner class LinearLayoutManagerWrapper(context: Context) : LinearLayoutManager(context) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }

    inner class SearchResultAdapter(var keyword:String): RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>(){

        var results = mutableListOf<PortMinimal>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route_search_port, parent, false)
            return SearchResultViewHolder(view)
        }

        override fun getItemCount(): Int {
            return results.size
        }

        fun clearData() {
            this.results.clear()
        }

        fun setData(datas: List<PortMinimal>) {
            this.results.addAll(datas)
        }

        override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
            val data = results[position]

            if (data.portCd.startsWith(keyword, true)) {
                val builder = SpannableStringBuilder(data.portCd)
                builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.itemView.tv_port.text = builder
                holder.itemView.tv_port_detail.setText(data.portNm)
            }
            else {
                val builder = SpannableStringBuilder(data.portNm)
                builder.setSpan(ForegroundColorSpan(resources.getColor(R.color.blue_violet)), 0, keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.itemView.tv_port_detail.text = builder
                holder.itemView.tv_port.setText(data.portCd)
            }
            holder.itemView.setOnClickListener {
                holder.itemView.tv_port.setTextColor(resources.getColor(R.color.blue_violet))
                holder.itemView.tv_port_detail.setTextColor(resources.getColor(R.color.blue_violet))
                val selectRouteData = FeaturedRoute(
                        id = null,
                        fromCode = data.portCd,
                        fromDetail = data.portNm,
                        toCode = "",
                        toDetail = "",
                        priority = 0
                )
                Handler().postDelayed(object: Runnable{
                    override fun run() { viewModel.inPuts.callSelectItem(selectRouteData) }
                }, 200)
            }
        }
        inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    inner class TreeViewDecoration : RecyclerView.ItemDecoration(){
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val density = resources.displayMetrics!!.density
            if (position == 0) {
                outRect.top = (89 * density).toInt()
                outRect.bottom = 0
            }
        }
    }

    inner class TreeViewScrollListenr() : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                viewModel.inPuts.callFocusChange(false)
            }
            else if(newState == RecyclerView.SCROLL_STATE_IDLE){
                viewModel.inPuts.callFocusChange(false)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            when(recyclerView.scrollState) {
                RecyclerView.SCROLL_STATE_IDLE->{
//                    Timber.v("diver:/ idle, dy="+dy)
                }
                RecyclerView.SCROLL_STATE_DRAGGING->{
//                    Timber.v("diver:/ drag, dy=$dy")
                    if(dy > 40) {
                        viewModel.inPuts.callHideSearch(Parameter.EVENT)
                    }else if(dy < -10){
                        viewModel.inPuts.callShowSearch(Parameter.EVENT)
                    }
                }
            }
        }
    }

    inner class NodeViewFactory : BaseNodeViewFactory() {
        override fun getNodeViewBinder(view: View?, level: Int): BaseNodeViewBinder? {
            when(level){
                0-> return ContinentNodeBinder(view!!)
                1-> return CountryNodeBinder(view!!)
                2-> return PortNodeBinder(view!!)
            }
            return null
        }
    }

    inner class ContinentNodeBinder(itemView: View) : BaseNodeViewBinder(itemView) {
        override fun getLayoutId(): Int {
            return R.layout.item_route_search_continent
        }

        override fun bindView(treeNode: TreeNode?) {
            itemView.tv_continent.text = treeNode?.value.toString()
            if (treeNode?.value.toString().equals("Recently")){
                itemView.isClickable = false
                itemView.iv_continent_select_flag.visibility = View.INVISIBLE
            }
            itemView.iv_continent_select_flag.rotation = when(treeNode!!.isExpanded) {
                true-> 180f
                false-> 0f
            }
        }

        override fun onNodeToggled(treeNode: TreeNode?, expand: Boolean) {
            super.onNodeToggled(treeNode, expand)
            if (expand) {
                itemView.iv_continent_select_flag.animate().rotation(180f).setDuration(200).start();
                treeView.view.requestLayout()
            } else {
                itemView.iv_continent_select_flag.animate().rotation(0f).setDuration(200).start();
                viewModel.inPuts.callShowSearch(Parameter.CLICK)
                treeView.view.requestLayout()
            }
            viewModel.inPuts.callFocusChange(false)
        }
    }

    inner class CountryNodeBinder(itemView: View) : BaseNodeViewBinder(itemView) {
        override fun getLayoutId(): Int {
            return R.layout.item_route_search_country
        }

        override fun bindView(treeNode: TreeNode?) {
            itemView.tv_country.text = treeNode?.value.toString()
            itemView.iv_coutry_select_flag.rotation = when(treeNode!!.isExpanded) {
                true-> 180f
                false-> 0f
            }
        }

        override fun onNodeToggled(treeNode: TreeNode?, expand: Boolean) {
            super.onNodeToggled(treeNode, expand)
            if (expand) {
                itemView.iv_coutry_select_flag.animate().rotation(180f).setDuration(200).start();
                treeView.view.requestLayout()
            } else {
                itemView.iv_coutry_select_flag.animate().rotation(0f).setDuration(200).start();
                viewModel.inPuts.callShowSearch(Parameter.CLICK)
                treeView.view.requestLayout()
            }
            viewModel.inPuts.callFocusChange(false)
        }
    }

    inner class PortNodeBinder(itemView: View) : BaseNodeViewBinder(itemView) {
        override fun getLayoutId(): Int {
            return R.layout.item_route_search_port
        }

        override fun bindView(treeNode: TreeNode?) {
            val portData = treeNode?.value as PortMinimal
            itemView.tv_port.text = portData.portCd
            itemView.tv_port_detail.text = portData.portNm
            itemView.setOnClickListener {
                itemView.tv_port.setTextColor(resources.getColor(R.color.blue_violet))
                itemView.tv_port_detail.setTextColor(resources.getColor(R.color.blue_violet))
                val selectRouteData = FeaturedRoute(
                        id = null,
                        fromCode = portData.portCd,
                        fromDetail = portData.portNm,
                        toCode = "",
                        toDetail = "",
                        priority = 0
                )
                Handler().postDelayed(object: Runnable{
                    override fun run() { viewModel.inPuts.callSelectItem(selectRouteData) }
                }, 200)

            }
        }
    }
}