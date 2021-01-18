package com.cyberlogitec.freight9.ui.routeselect.select

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.ui.routeselect.featured.PreferredRouteEditActivity
import com.cyberlogitec.freight9.ui.routeselect.search.RouteSearchActivity
import kotlinx.android.synthetic.main.frag_route_select.view.*
import kotlinx.android.synthetic.main.frag_route_select_add.view.*

class InfiniteAdapter(val viewModel: RouteSelectViewModel, context: Context, itemList: MutableList<FeaturedRoute>, isInfinite: Boolean) : LoopingPagerAdapter<FeaturedRoute>(context, itemList, isInfinite) {
    final val VIEW_TYPE_ROUTE = 100
    final val VIEW_TYPE_ADD = 101
    final val VIEW_TYPE_FIRST = 102

    override fun getItemViewType(listPosition: Int): Int {
        if (itemList.get(listPosition).fromCode.equals("first")){
            return VIEW_TYPE_FIRST
        } else if (itemList.get(listPosition).fromCode.equals("plus")){
            return VIEW_TYPE_ADD
        } else {
            return VIEW_TYPE_ROUTE
        }
    }

    override fun inflateView(viewType: Int, container: ViewGroup?, listPosition: Int): View {
        when (viewType) {
            VIEW_TYPE_ADD-> return LayoutInflater.from(context).inflate(R.layout.frag_route_select_add, container, false)
        }
        return LayoutInflater.from(context).inflate(R.layout.frag_route_select, container, false)
    }

    override fun bindView(convertView: View?, listPosition: Int, viewType: Int) {
        convertView?.let {
            if (viewType == VIEW_TYPE_ROUTE) {
                convertView.tv_route_select_card_from.text = if(itemList.get(listPosition).fromCode.equals("")) "FROM" else itemList.get(listPosition).fromCode
                convertView.tv_route_select_card_from_detail.text = if(itemList.get(listPosition).fromDetail.equals("")) "City or Port" else itemList.get(listPosition).fromDetail
                convertView.tv_route_select_card_to.text = if(itemList.get(listPosition).toCode.equals("")) "FROM" else itemList.get(listPosition).toCode
                convertView.tv_route_select_card_to_detail.text = if(itemList.get(listPosition).toDetail.equals("")) "City or Port" else itemList.get(listPosition).toDetail
                convertView.tv_route_select_card_from.setOnClickListener(searchRoute(1, itemList.get(listPosition), listPosition))
                convertView.tv_route_select_card_from_detail.setOnClickListener(searchRoute(1, itemList.get(listPosition), listPosition))
                convertView.tv_route_select_card_to.setOnClickListener(searchRoute(2, itemList.get(listPosition), listPosition))
                convertView.tv_route_select_card_to_detail.setOnClickListener(searchRoute(2, itemList.get(listPosition), listPosition))
                convertView.btn_route_select.isSelected = (itemList.get(listPosition).priority != 0.toLong())
//                Timber.v("diver:/ bindview -> ${itemList.get(listPosition)}")

                convertView.btn_route_select.setOnClickListener {
//                    Timber.v("diver:/ validator: ${viewModel.currentPage} $listPosition")
                    if( viewModel.currentPage == listPosition){
                        if (it.isSelected) {
                            viewModel.inPuts.deleteFeaturedRoute(itemList.get(listPosition))
                            itemList.get(listPosition).priority = 0
                            it.isSelected = false
                        }
                        else {
                            if (itemList.size < 12) {
                                viewModel.inPuts.addFeatureRoute(itemList.get(listPosition))
                                itemList.maxBy { it.priority }.also {maxPriorityRoute->
                                    maxPriorityRoute?.let {
                                        itemList.get(listPosition).priority = it.priority + 1
                                    }
                                }
                                it.isSelected = true
                            } else {
                                Toast.makeText(context, "Featured Route can't over than 10!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            }
            else if (viewType == VIEW_TYPE_ADD) {
                if (itemList.size >= 12){
                    convertView.iv_route_select_add.setImageResource(R.drawable.ic_setting_default)
                }
                convertView.iv_route_select_add.setOnClickListener {
                    startActivity(context, Intent(context, PreferredRouteEditActivity::class.java), null)
                }
            }
            else {
                convertView.btn_route_select.visibility = View.GONE
                val dummyRoute = FeaturedRoute(null, "FROM", "City or Port", "TO", "City or Port", 0)
                convertView.tv_route_select_card_from.setOnClickListener(searchRoute(1, dummyRoute, listPosition))
                convertView.tv_route_select_card_from_detail.setOnClickListener(searchRoute(1, dummyRoute, listPosition))
                convertView.tv_route_select_card_to.setOnClickListener(searchRoute(2, dummyRoute, listPosition))
                convertView.tv_route_select_card_to_detail.setOnClickListener(searchRoute(2, dummyRoute, listPosition))
            }
        }
    }

    inner class searchRoute(tabPosition: Int,  val route: FeaturedRoute, val listPosition: Int) : View.OnClickListener {
        val position = tabPosition
        override fun onClick(v: View?) {
            val intent = Intent(context, RouteSearchActivity::class.java)
            val pageList = arrayListOf<String>(
                    context.getString(R.string.route_tab_featured),
                    context.getString(R.string.route_tab_porpol),
                    context.getString(R.string.route_tab_poddel)
            )
            intent.putExtra("tabPosition", position)
            intent.putExtra("fromCode", route.fromCode)
            intent.putExtra("fromDetail", route.fromDetail)
            intent.putExtra("toCode", route.toCode)
            intent.putExtra("toDetail", route.toDetail)
            intent.putExtra("pageList", pageList)

            if (viewModel.currentPage == listPosition){
                viewModel.inPuts.callOpenSearch(intent)
            }
        }
    }

}