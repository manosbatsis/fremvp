package com.cyberlogitec.freight9.ui.routeselect.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.hideKeyboard
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.startActivity
import com.cyberlogitec.freight9.ui.routeselect.featured.PreferredRouteEditActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_route_search.*
import timber.log.Timber

@RequiresActivityViewModel(value = RouteSearchViewModel::class)
class RouteSearchActivity : BaseActivity<RouteSearchViewModel>() {
    private lateinit var pageList: ArrayList<String>
    var isTabExpand = true
    var isSearchExpand = true
    var searchFlag = false
    var resultData = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.act_route_search)
        (application as App).component.inject(this)
        window.statusBarColor = getColor(R.color.black)
        val position = intent.getIntExtra("tabPosition", 0)
        pageList = intent.getStringArrayListExtra("pageList")

        when (pageList.size) {
            2-> {
                iv_route_search_back.visibility = View.VISIBLE
                tv_route_search_title.visibility = View.VISIBLE
                iv_route_search_done.visibility = View.INVISIBLE
            }
            3-> {
                iv_route_search_back.visibility = View.INVISIBLE
                tv_route_search_title.visibility = View.INVISIBLE
                iv_route_search_done.visibility = View.VISIBLE
            }
        }

        viewModel.outPuts.doShowSearch().bindToLifecycle(this).subscribe {
            if (isTabExpand and !isSearchExpand){
                isSearchExpand = true
                showSearch()
            }
        }

        viewModel.outPuts.doHideSearch().bindToLifecycle(this).subscribe {
            if (isTabExpand and isSearchExpand) {
                isSearchExpand = false
                collapseSearch()
            }
        }

        viewModel.outPuts.doFocusChange().bindToLifecycle(this).subscribe {
            when (it) {
                true-> {
                    if(isTabExpand){
                        collapse(container_pager)
                        expandBack()
                        iv_remove_text.visibility = View.VISIBLE
                        isTabExpand = false
                    }
                }
                false-> {
                    hideKeyboard()
                    et_route_search_search.clearFocus()
                }
            }
        }

        viewModel.outPuts.goToPreferredEdit().bindToLifecycle(this).subscribe {
            et_route_search_search.setText("")
            viewModel.inPuts.callFocusChange(false)
            startActivity(PreferredRouteEditActivity::class.java)
        }

        viewModel.outPuts.setRoute().bindToLifecycle(this).subscribe {
            when (tabLayout_route_select.getTabAt(tabLayout_route_select.selectedTabPosition)?.text) {
                resources.getString(R.string.route_tab_featured)-> {
                    resultData.putExtra("fromCode", it.fromCode)
                    resultData.putExtra("fromDetail", it.fromDetail)
                    resultData.putExtra("toCode", it.toCode)
                    resultData.putExtra("toDetail", it.toDetail)
                    setResult(Activity.RESULT_OK, resultData)
                    finish()
                }
                resources.getString(R.string.route_tab_porpol)-> {
                    resultData.putExtra("fromCode", it.fromCode)
                    resultData.putExtra("fromDetail", it.fromDetail)
                    viewModel.inPuts.callUpdatePortDate(it.fromCode)
                    if (!isTabExpand) tabExpand()

                    viewPager_route_search.currentItem = tabLayout_route_select.selectedTabPosition+1
                }
                resources.getString(R.string.route_tab_poddel)-> {
                    resultData.putExtra("toCode", it.fromCode)
                    resultData.putExtra("toDetail", it.fromDetail)
                    viewModel.inPuts.callUpdatePortDate(it.fromCode)
                    setResult(Activity.RESULT_OK, resultData)
                    finish()
                }
            }
        }

        resultData.putExtra("fromCode", intent.getStringExtra("fromCode"))
        resultData.putExtra("fromDetail", intent.getStringExtra("fromDetail"))
        resultData.putExtra("toCode", intent.getStringExtra("toCode"))
        resultData.putExtra("toDetail", intent.getStringExtra("toDetail"))

        //Customize Tablayout
        tabLayout_route_select.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                setTabTitleTypeface(tab!!.position, Typeface.NORMAL)
                tab?.let { unselectedTab ->
                    ResourcesCompat.getFont(unselectedTab.parent!!.context, R.font.opensans_regular).also {
                        it?.let {setTabTitleTypeface(unselectedTab.position, it)}
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.inPuts.callPortTree(Parameter.EVENT)
                viewModel.inPuts.callShowSearch(Parameter.EVENT)
                et_route_search_search.setText("")
                tab?.let { selectedTab ->
                    setContentOnSelectedPort(selectedTab.text.toString())
                    ResourcesCompat.getFont(selectedTab.parent!!.context, R.font.opensans_extrabold).also {
                        it?.let {setTabTitleTypeface(selectedTab.position, it)}
                    }
                }
            }
        })

        //Attach Viewpager to tablayout
        viewPager_route_search.adapter = ViewPagerAdapter(pageList,this)
        viewPager_route_search.currentItem = position
        viewPager_route_search.isUserInputEnabled = false

        TabLayoutMediator(tabLayout_route_select, viewPager_route_search,
                TabLayoutMediator.TabConfigurationStrategy { tab, tabPosition ->
            (viewPager_route_search.adapter as ViewPagerAdapter).also {
                tab.text = it.pageList[tabPosition]
            }
        }).attach()

        ResourcesCompat.getFont(this, R.font.opensans_extrabold).also {
            it?.let {setTabTitleTypeface(position, it)}
        }
//        setTabTitleTypeface(position, Typeface.BOLD)


        //Tab Width 변경
        for (i in pageList.indices) {
            val layout = ((tabLayout_route_select.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }

        //Selected Route Delete Action
        iv_route_search_selected_port_delete.setOnClickListener {
            if (isSearchExpand){
                container_route_search_selected_port.visibility = View.GONE
                et_route_search_search.requestFocus()
                viewModel.inPuts.callFocusChange(true)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(et_route_search_search, 0)
            }
        }

        //Done Button Action
        iv_route_search_done.setOnClickListener {
            if (validateResult()) setResult(Activity.RESULT_OK, resultData)
            else showToast("Invalid Route!")
            finish()
        }

        //Back Button Action
        iv_route_search_back.setOnClickListener {
            if (validateResult()) setResult(Activity.RESULT_OK, resultData)
            else showToast("Invalid Route!")
            finish()
        }

        //Back Button Action
        iv_back_to_default.setOnClickListener {
            tabExpand()
        }

        //Remove Button Action
        iv_remove_text.setOnClickListener {
            et_route_search_search.setText("")
        }

        //화면 영역 터치시 Focus Clear 및 키보드 내림
        viewPager_route_search.setOnClickListener {
            viewModel.inPuts.callFocusChange(false)
        }

        //EditText Focus에 따른 Animation 적용
        et_route_search_search.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                (v as EditText).also { viewModel.inPuts.callFocusChange(hasFocus) }
            }
        }

        // Search 버튼 선택시 Focus Clear 및 키보드 내림
        (et_route_search_search as TextView).setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    viewModel.inPuts.callFocusChange(false)
                }
                return true
            }
        })

        et_route_search_search.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Timber.v("diver:/ "+s!!.length.toString())
                val validator = pageList.size - viewPager_route_search.currentItem
                if (validator == 3){
                    viewModel.inPuts.callSearchRoute(s.toString())
                } else {
                    viewModel.inPuts.callSearchPort(s.toString())
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.inPuts.callShowSearch(Parameter.EVENT)
    }

    inner class ViewPagerAdapter(val pageList: List<String>, fragment: FragmentActivity) : FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = pageList.size
        override fun createFragment(position: Int): Fragment {
            when(pageList.get(position)){
                resources.getString(R.string.route_tab_featured)->return FeatureRouteSearchFragment(viewModel)
                else->return PortSearchFragment(viewModel)
            }
        }
    }

    fun setTabTitleTypeface(position: Int, type: Typeface){
        val tabLayout : LinearLayout = ((tabLayout_route_select.getChildAt(0) as ViewGroup)).getChildAt(position) as LinearLayout
        val tabTextView: TextView = tabLayout.getChildAt(1) as TextView
        tabTextView.typeface = type
    }

    fun setContentOnSelectedPort(tabText: String){
        when (tabText){
            resources.getString(R.string.route_tab_featured)->{
                container_route_search_selected_port.visibility = View.GONE
            }
            resources.getString(R.string.route_tab_porpol)-> {
                val portCode = resultData.getStringExtra("fromCode")
                if (portCode == "FROM" || portCode == null){
                    container_route_search_selected_port.visibility = View.GONE
                }
                else{
                    when(viewModel.checkPort(resultData.getStringExtra("fromCode"))){
                        true-> tv_route_search_selected_port_type.text = "POL"
                        false-> tv_route_search_selected_port_type.text = "POR"
                    }
                    tv_route_search_selected_port.text = resultData.getStringExtra("fromCode")
                    tv_route_search_selected_port_detail.text = resultData.getStringExtra("fromDetail")
                    container_route_search_selected_port.visibility = View.VISIBLE
                }
            }
            resources.getString(R.string.route_tab_poddel)-> {
                when(viewModel.checkPort(resultData.getStringExtra("toCode"))){
                    true-> tv_route_search_selected_port_type.text = "POD"
                    false-> tv_route_search_selected_port_type.text = "DEL"
                }
                val portCode = resultData.getStringExtra("toCode")
                if (portCode == "TO" || portCode == null){
                    container_route_search_selected_port.visibility = View.GONE
                }
                else{
                    tv_route_search_selected_port.text = resultData.getStringExtra("toCode")
                    tv_route_search_selected_port_detail.text = resultData.getStringExtra("toDetail")
                    container_route_search_selected_port.visibility = View.VISIBLE
                }
            }
        }
    }

    fun validateResult(): Boolean{
        var validFrom: Boolean
        var validTo: Boolean
        when (resultData.getStringExtra("fromCode")){
            "FROM", "", null-> validFrom = false
            else-> validFrom = true
        }
        when (resultData.getStringExtra("toCode")){
            "TO", "", null-> validTo = false
            else-> validTo = true
        }
        return (validFrom || validTo)
    }

    fun tabExpand(){
        expand(container_pager)
        collapseBack()
        iv_remove_text.visibility = View.INVISIBLE
        hideKeyboard()
        et_route_search_search.clearFocus()
        isTabExpand = true
    }

    fun expandBack() {
        val view = iv_back_to_default
        val targetWidth = 37
        val targetHeight = 37

        view.layoutParams.width = 0
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animation = object: Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                when(interpolatedTime.toInt() == 1){
                    true->{
                        view.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                        view.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    }
                    false->{
                        view.layoutParams.height = (targetHeight * interpolatedTime).toInt()
                        view.layoutParams.width = (targetWidth * interpolatedTime).toInt()
                    }
                }
                view.requestLayout()
            }
            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        animation.duration = (targetWidth / view.context.resources.displayMetrics.density).toLong() * 2
        view.startAnimation(animation)
    }

    fun collapseBack() {
        val view = iv_back_to_default
        val initialWidth = view.measuredWidth
        val animation = object: Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                when(interpolatedTime.toInt() == 1){
                    true->view.visibility = View.GONE
                    false->{
                        view.layoutParams.width = initialWidth - (initialWidth * interpolatedTime).toInt()
                        view.requestLayout()
                    }
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        animation.duration = (initialWidth / view.context.resources.displayMetrics.density).toLong()
        view.startAnimation(animation)
    }

    fun showSearch(){
        container_route_search.visibility = View.VISIBLE
        val animation = TranslateAnimation(0f, 0f, -container_route_search.height.toFloat(), 0f)
        animation.duration = 200
        animation.fillAfter = true
        container_route_search.startAnimation(animation)
    }

    fun collapseSearch(){
        container_route_search.visibility=View.GONE
        val animation = TranslateAnimation(0f, 0f, 0f, -container_route_search.height.toFloat())
        animation.duration = 200
        animation.fillAfter = true
        container_route_search.startAnimation(animation)
    }

    fun expand(view: View) {
        val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animation = object: Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                when(interpolatedTime.toInt() == 1){
                    true->view.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    false->view.layoutParams.height = (targetHeight * interpolatedTime).toInt()
                }
                view.requestLayout()
            }
            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        animation.duration = (targetHeight / view.context.resources.displayMetrics.density).toLong() * 2
        view.startAnimation(animation)
    }

    fun collapse(view: View) {
        val initialHeight = view.measuredHeight
        val animation = object: Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                when(interpolatedTime.toInt() == 1){
                    true->view.visibility = View.GONE
                    false->{
                        view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                        view.requestLayout()
                    }
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        animation.duration = (initialHeight / view.context.resources.displayMetrics.density).toLong()
        view.startAnimation(animation)
    }
}

