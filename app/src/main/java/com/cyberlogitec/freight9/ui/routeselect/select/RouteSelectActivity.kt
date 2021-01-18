package com.cyberlogitec.freight9.ui.routeselect.select

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_route_select.*
import timber.log.Timber

@RequiresActivityViewModel(value = RouteSelectViewModel::class)
class RouteSelectActivity : BaseActivity<RouteSelectViewModel>() {
    private lateinit var mAdapter: InfiniteAdapter
    private lateinit var routes: MutableList<FeaturedRoute>

    private var featuredRouteCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.act_route_select)
        (application as App).component.inject(this)
        window.statusBarColor = getColor(R.color.black)
        routes = mutableListOf()

        viewModel.outPuts.doOpenSearch()
                .bindToLifecycle(this)
                .subscribe {
                    startActivityForResult(it, 100)
                }

        viewModel.outPuts.doDeleteFeaturedRoute()
                .bindToLifecycle(this)
                .subscribe {
                    featuredRouteCount--
                    Timber.v("diver:/ current featured cnt=$featuredRouteCount")
                }

        viewModel.outPuts.doAddFeatureRoute()
                .bindToLifecycle(this)
                .subscribe {
                    featuredRouteCount++
                    Timber.v("diver:/ current featured cnt=$featuredRouteCount")
                }

        viewModel.outPuts.getFeaturedRoutes()
                .bindToLifecycle(this)
                .doOnNext {
                    Timber.v("route/ redraw RouteSelectActivity")
                }
                .subscribe {
                    routes.clear()
                    routes.add(FeaturedRoute(null, "first", "City or Port", "TO", "City or Port", 0))

                    val currentFromCode = intent.getStringExtra("fromCode")
                    val currentFromDetail = intent.getStringExtra("fromDetail")
                    val currentToCode = intent.getStringExtra("toCode")
                    val currentToDetail = intent.getStringExtra("toDetail")
                    var isCurrentRoutePosition = 2
                    var isCurrentRouteContained = false

                    Timber.v("diver:/ $currentFromCode, $currentToCode")

                    featuredRouteCount = it.size

                    for (route in it) {
                        if (route.fromCode == currentFromCode && route.toCode == currentToCode) {
                            isCurrentRouteContained = true
                        }
                        if (!isCurrentRouteContained) {
                            isCurrentRoutePosition++
                        }
                        routes.add(route)
                    }
                    if (!isCurrentRouteContained) {
                        isCurrentRoutePosition = 2
                        if ((currentFromCode != "FROM" && currentFromCode != "") || (currentToCode != "TO" && currentToCode != "")){
                            routes.add(isCurrentRoutePosition-1, FeaturedRoute(null, currentFromCode, currentFromDetail, currentToCode, currentToDetail, 0))
                        }else{
                            isCurrentRoutePosition = 1
                        }
                    }
                    routes.add(FeaturedRoute(null, "plus", "City or Port", "TO", "City or Port", 0))

                    mAdapter.setItemList(routes)
                    viewPager_route_select.reset()

                    if (routes.size == 2){
                        viewPager_route_select.setCurrentItem(1)
                    } else {
                        viewPager_route_select.setCurrentItem(isCurrentRoutePosition)
                    }
                    indicator_route_select.createDotPanel(routes.size, R.drawable.indicator_route_default, R.drawable.indicator_route_selected, isCurrentRoutePosition-1)

                    if (tv_route_select_search_btn != null && (currentFromCode == "FROM" || currentToCode == "TO")) {
                        tv_route_select_search_btn.setBackgroundColor(getColor(R.color.greyish_brown))
                        tv_route_select_search_btn.isClickable = false
                    } else {
                        tv_route_select_search_btn.setBackgroundColor(getColor(R.color.blue_violet))
                        tv_route_select_search_btn.isClickable = true
                    }
                }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        mAdapter = InfiniteAdapter(viewModel, this, routes, true)
        viewPager_route_select.adapter = mAdapter
        viewPager_route_select.clipToPadding = false
        viewPager_route_select.setIndicatorSmart(true)
        viewPager_route_select.setPadding((screenWidth * 0.15).toInt(), 0, (screenWidth * 0.15).toInt(), 0)

        viewPager_route_select.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                viewModel.currentPage = position-1
                indicator_route_select.selectDot(position-1)

                val current = mAdapter.getItem(position-1)
                current?.let {
                    if (it.fromCode.equals("first") || it.fromCode.equals("plus")){
                        tv_route_select_search_btn.visibility = View.INVISIBLE
                        tv_route_select_search_btn.setBackgroundColor(getColor(R.color.greyish_brown))
                        tv_route_select_search_btn.isClickable = false
                    } else if(it.fromCode.equals("FROM") || it.toCode.equals("TO")){
                        tv_route_select_search_btn.visibility = View.INVISIBLE
                        tv_route_select_search_btn.setBackgroundColor(getColor(R.color.greyish_brown))
                        tv_route_select_search_btn.isClickable = false
                    } else {
                        tv_route_select_search_btn.visibility = View.VISIBLE
                        tv_route_select_search_btn.setBackgroundColor(getColor(R.color.blue_violet))
                        tv_route_select_search_btn.isClickable = true
                    }
                }
            }
        })

        iv_route_select_done.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressed()
        }

        tv_route_select_search_btn.setOnClickListener {
            if (viewPager_route_select.currentItem == 1 || viewPager_route_select.currentItem == routes.size) {
                Toast.makeText(applicationContext, "Please Select Route!", Toast.LENGTH_SHORT).show()
            } else {
                Timber.d("f9: Activity.RESULT_OK ###")

                val selectRoute = (mAdapter.getItem(viewPager_route_select.currentItem-1))
                Toast.makeText(applicationContext, "Request To Server:/ ${selectRoute.fromCode} - ${selectRoute.toCode}", Toast.LENGTH_SHORT).show()
                intent.putExtra("selectFromCode", selectRoute.fromCode)
                intent.putExtra("selectFromDetail", selectRoute.fromDetail)
                intent.putExtra("selectToCode", selectRoute.toCode)
                intent.putExtra("selectToDetail", selectRoute.toDetail)

                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        viewModel.inPuts.callFeaturedRoute(Parameter.NULL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            intent.putExtra("fromCode", data!!.getStringExtra("fromCode"))
            intent.putExtra("fromDetail", data.getStringExtra("fromDetail"))
            intent.putExtra("toCode", data.getStringExtra("toCode"))
            intent.putExtra("toDetail", data.getStringExtra("toDetail"))
            viewModel.inPuts.callFeaturedRoute(Parameter.NULL)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        Timber.v("route:/ call resume")
        viewModel.inPuts.callFeaturedRoute(Parameter.NULL)
    }
}