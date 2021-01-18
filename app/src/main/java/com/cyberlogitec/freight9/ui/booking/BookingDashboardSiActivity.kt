package com.cyberlogitec.freight9.ui.booking

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboardItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.act_market_watchweek.*
import kotlinx.android.synthetic.main.appbar_dashboard.*
import kotlinx.android.synthetic.main.toolbar_common.view.toolbar_left_btn
import kotlinx.android.synthetic.main.toolbar_common.view.toolbar_right_btn
import timber.log.Timber
import kotlin.collections.ArrayList

@RequiresActivityViewModel(value = BookingDashboardDetailViewModel::class)
class BookingDashboardSiActivity : BaseActivity<BookingDashboardDetailViewModel>() {

    private lateinit var bookingItem: BookingDashboardItem
    private var pageList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_booking_dashboard_si)
        (application as App).component.inject(this)

        initData()
        initView()
        initFragment()
        updateUi()
    }
    private fun initToolbar() {
        toolbar_booking_dashboard.setBackgroundColor(getColor(R.color.color_1d1d1d))

        defaultbarInit(toolbar_booking_dashboard,menuType = MenuType.CROSS, title = getString(R.string.booking_dashboard_si_doc))
        toolbar_booking_dashboard.setBackgroundColor(getColor(R.color.color_1d1d1d))
        toolbar_booking_dashboard.toolbar_left_btn.visibility = View.INVISIBLE

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_booking_dashboard.toolbar_right_btn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun initFragment() {
        pageList.addAll(resources.getStringArray(R.array.tab_bookingdashboard_si))
        //Attach Viewpager to tablayout
        viewPager2.adapter = ViewPagerAdapter(pageList,this)
        viewPager2.offscreenPageLimit = 3

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let { unselectedTab ->
                    ResourcesCompat.getFont(unselectedTab.parent!!.context, R.font.opensans_regular).also {
                        it?.let { setTabTitleTypeface(unselectedTab.position, it) }
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { selectedTab ->
                    ResourcesCompat.getFont(selectedTab.parent!!.context, R.font.opensans_extrabold).also {
                        it?.let { setTabTitleTypeface(selectedTab.position, it) }
                    }
                }
            }
        })

        TabLayoutMediator(tabLayout, viewPager2, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            (viewPager2.adapter as BookingDashboardSiActivity.ViewPagerAdapter).also {
                tab.text = it.pageList[position]
            }
        }).attach()

        //Tab Width 변경
        for (i in pageList.indices) {
            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }

    }

    private fun initData() {

        bookingItem = intent.extras["bookingitem"] as BookingDashboardItem

    }

    private fun initView() {
        initToolbar()
        setListener()
    }

    private fun setListener() {
    }


    private fun updateUi() {}

    fun setTabTitleTypeface(position: Int, type: Typeface){
        val tabLayout : LinearLayout = ((tabLayout.getChildAt(0) as ViewGroup)).getChildAt(position) as LinearLayout
        val tabTextView: TextView = tabLayout.getChildAt(1) as TextView
        tabTextView.typeface = type
    }

    inner class ViewPagerAdapter(val pageList: ArrayList<String>, fragment: FragmentActivity) : FragmentStateAdapter(fragment){
        private var fragments = mutableListOf<Fragment>()
        override fun getItemCount(): Int = pageList.size
        override fun createFragment(position: Int): Fragment {
            when(pageList[position]){
                resources.getString(R.string.booking_dashboard_si_parties)->{
                    val fragment = BookingDashboardConditionDetailFragment(viewModel, bookingItem)
                    fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.SI_PARTIES)
                    fragments.add(fragment)
                    return fragment
                }
                resources.getString(R.string.booking_dashboard_si_container)->{
                    val fragment = BookingDashboardContainerFragment(viewModel, bookingItem)
                    fragments.add(fragment)
                    return fragment
                }
                resources.getString(R.string.booking_dashboard_si_mark_desc)->{
                    val fragment = BookingDashboardConditionDetailFragment(viewModel, bookingItem)
                    fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.MARK)
                    fragments.add(fragment)
                    return fragment
                }
                else->{
                    val fragment = BookingDashboardConditionDetailFragment(viewModel, bookingItem)
                    fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.SI_PARTIES)
                    fragments.add(fragment)
                    return fragment
                }
            }
        }
    }
}
