package com.cyberlogitec.freight9.ui.home

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchUIUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeItemTouchHelper
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeItemTouchListener
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.startActivity
import com.cyberlogitec.freight9.ui.buyorder.BuyOrderActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.sellorder.SellOrderActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.main.appbar_home.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = HomeViewModel::class)
class HomeActivity : BaseActivity<HomeViewModel>(), SwipeItemTouchListener {

    private var onSwipeItemLeft: (Int) -> Unit = {}
    private var onSwipeItemRight: (Int) -> Unit = {}

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onSwipeItemLeft = { viewModel.inPuts.swipeToLeft(it) }
                    onSwipeItemRight = { viewModel.inPuts.swipeToRight(it) }
                }
    }

    private val listFragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_home)

        (application as App).component.inject(this)

        // set status bar
        getWindow().statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar, menuType = MenuType.DEFAULT, title = "HOME")

        // draw route card view
        ////////////////////////////////////////////////////////////////////////////////////////////
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        for (x in 1..5) {
            val fragRouter = HomeRouteFragment.newInstance(viewModel)
            viewPagerAdapter.addFragment(fragRouter, "")

        }

        viewPager.adapter = viewPagerAdapter
        viewPager.clipToPadding = false
        viewPager.setPadding(200, 0, 200, 0)
        tabLayout.setupWithViewPager(viewPager, true)
        ////////////////////////////////////////////////////////////////////////////////////////////

        // wait click event (toolbar left button)
        toolbar_common.toolbar_left_btn.setOnClickListener {
            it?.let {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_common.toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }

        // init recyclerview
        recyclerViewInit()

        // + test datas
        // val test_msg = "Ever wondered how some graphic desingers always manage to"
        for (x in 1..10)
            adapter.datas.add(x.toString())
        adapter.notifyDataSetChanged()
        // + test datas


        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MenuActivity)")
                    startActivity(MenuActivity::class.java)
                }

        viewModel.outPuts.gotoBuyOrder()
                .bindToLifecycle(this)
                .subscribe{ startActivity(Intent(this, BuyOrderActivity::class.java).putExtra(Intents.TOPIC_ID, it)) }

        viewModel.outPuts.gotoSellOrder()
                .bindToLifecycle(this)
                .subscribe{ startActivity(Intent(this, SellOrderActivity::class.java).putExtra(Intents.TOPIC_ID, it)) }

    }

    class ViewPagerAdapter(fragmentManager: FragmentManager) :
            FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList : MutableList<androidx.fragment.app.Fragment> = ArrayList()
        private val titleList : MutableList<String> = ArrayList()

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: androidx.fragment.app.Fragment, title: String){
            fragmentList.add(fragment)
            titleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }
    }


    private fun recyclerViewInit() =
            recycler_view_home.apply {
                layoutManager = LinearLayoutManager(this@HomeActivity)

                // init swipe
                val swipeHelper = SwipeItemTouchHelper(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, this@HomeActivity)
                ItemTouchHelper(swipeHelper).attachToRecyclerView(recycler_view_home)

                adapter = this@HomeActivity.adapter
            }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<String>()

        var onClickItem: (Long) -> Unit = {}

        override fun getItemCount(): Int = datas.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))

        override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]
                setOnClickListener { onClickItem( datas.indexOf(data).toLong() ) }
            }
        }

        public class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    // SwipeItemTouchListener
    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        if(holder is RecyclerAdapter.ViewHolder) {
            val swipedIndex = holder.adapterPosition

            Timber.d("f9: onSwiped() -> swipedIndex: ${swipedIndex}, direction: ${direction}")

            when(direction) {
                ItemTouchHelper.LEFT -> {
                    Timber.d("f9: LEFT -> Temporary -> SellOrder")
                    onSwipeItemLeft(swipedIndex)
                }
                ItemTouchHelper.RIGHT -> {
                    Timber.d("f9: RIGHT -> Temporary -> BuyOrder")
                    onSwipeItemRight(swipedIndex)
                }
                else -> { Timber.d("f9: ELSE") }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onSelectedChanged(holder: RecyclerView.ViewHolder?, actionState: Int, uiUtil: ItemTouchUIUtil) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    //Timber.d("f9: onSelectedChanged> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onSelected(holder.container)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
            uiUtil: ItemTouchUIUtil
    ) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    //Timber.d("f9: onChildDraw> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onDraw(c, recyclerView, holder.container, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun onChildDrawOver(
            c: Canvas,
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder?,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
            uiUtil: ItemTouchUIUtil
    ) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if(holder is RecyclerAdapter.ViewHolder) {
                    //Timber.d("f9: onChildDrawOver> ItemTouchHelper.ACTION_STATE_SWIPE")
                    //uiUtil.onDrawOver(c, recyclerView, holder.container, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }
    }

    // SwipeItemTouchListener
    override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, uiUtil: ItemTouchUIUtil) {
        if(holder is RecyclerAdapter.ViewHolder) {
            Timber.d("f9: clearView> ItemTouchHelper.ACTION_STATE_SWIPE")
            //uiUtil.clearView(holder.container)
        }
    }


}