package com.cyberlogitec.freight9.ui.menu

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_INVOICE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.config.MenuFragment
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.booking.BookingDashboardActivity
import com.cyberlogitec.freight9.ui.buyoffer.BofNewOfferAct
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingActivity
import com.cyberlogitec.freight9.ui.finance.FinanceActivity
import com.cyberlogitec.freight9.ui.home.HomeActivity
import com.cyberlogitec.freight9.ui.inventory.RouteFilterActivity
import com.cyberlogitec.freight9.ui.marketcommentary.MarketCommentaryActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchIndexActivity
import com.cyberlogitec.freight9.ui.member.LoginActivity
import com.cyberlogitec.freight9.ui.salesquota.SalesQuotaActivity
import com.cyberlogitec.freight9.ui.selloffer.SofNewOfferAct
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.cyberlogitec.freight9.ui.transaction.TransactionActivity
import com.cyberlogitec.freight9.ui.youroffers.YourOffersActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_menu.*
import timber.log.Timber


@RequiresActivityViewModel(value = MenuViewModel::class)
class MenuActivity : BaseActivity<MenuViewModel>() {

    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
    private val listFragments = mutableListOf<Fragment>()

    override fun onBackPressed() {
        //Timber.v("onBackPressed --> prevent backpress")
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        //overridePendingTransition(R.anim.menu_left_to_right, R.anim.menu_right_to_left);
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkGoToMessageBox(intent!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_menu)
        (applicationContext as? App)?.component?.inject(this)

        // set full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // ViewModel 이벤트 처리 (클릭후 처리 결과 처리 + Frag 이벤트 포함)
        viewModel.outPuts.onCloseMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: onCloseMenu")
                    finish()
                }
        viewModel.outPuts.goLogin()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: goLogin")
                    finishAffinity()
                    startActivityWithFinish( Intent(this, LoginActivity::class.java))
                }
        viewModel.outPuts.gotoHome()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoHome")
                    startActivityWithFinish( Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }
        viewModel.outPuts.gotoTradeMarket()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoTradeMarket")
                    startActivityWithFinish( Intent(this, MarketActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }

        /**
         * go to sell offer
         */
        viewModel.outPuts.gotoSellOffer()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoSellOffer")
                    startActivityWithFinish( Intent(this, SofNewOfferAct::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.MSTR_CTRK_NR, EmptyString))
                }

        /**
         * go to buy offer
         */
        viewModel.outPuts.gotoBuyOffer()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoBuyOffer")
                    startActivityWithFinish( Intent(this, BofNewOfferAct::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }
        viewModel.outPuts.gotoMarketWatch()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoMarketWatch")
                    startActivityWithFinish( Intent(this, MarketWatchActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }
        viewModel.outPuts.gotoMarketIndex()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoMarketIndex")
                    startActivityWithFinish( Intent(this, MarketWatchIndexActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }
        viewModel.outPuts.gotoMarketCommentary()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoMarketCommentary")
                    startActivityWithFinish( Intent(this, MarketCommentaryActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }

        /**
         * go to inventory
         */
        viewModel.outPuts.gotoInventory()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoInventory")
                    startActivityWithFinish( Intent(this, RouteFilterActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }

        /**
         * go to buy offers
         * YOUR_OFFER_TYPE 으로 buy, sell 구분
         */
        viewModel.outPuts.gotoYourBuyOffers()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoYourOffers-Buy")
                    startActivityWithFinish( Intent(this, YourOffersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.YOUR_OFFER_TYPE, OFFER_TYPE_CODE_BUY))
                }

        /**
         * go to sell offers
         * YOUR_OFFER_TYPE 으로 buy, sell 구분
         */
        viewModel.outPuts.gotoYourSellOffers()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoYourOffers-Sell")
                    startActivityWithFinish( Intent(this, YourOffersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.YOUR_OFFER_TYPE, OFFER_TYPE_CODE_SELL))
                }

        viewModel.outPuts.gotoBookingDashboard()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: gotoBookingDashboard")
                    startActivityWithFinish( Intent(this, BookingDashboardActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }

        /**
         * go to cargo tracking
         */
        viewModel.outPuts.gotoCargoTracking()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: gotoCargoTracking")
                    startActivityWithFinish( Intent(this, CargoTrackingActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                }

        viewModel.outPuts.gotoPayCollectPlan()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoPayCollectPlan")
                    startActivityWithFinish( Intent(this, FinanceActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.FINANCE_TYPE, FINANCE_TYPE_PAY_COLLECT_PLAN))
                }
        viewModel.outPuts.gotoTransactionStatement()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: gotoTransactionStatement")
                    startActivityWithFinish( Intent(this, FinanceActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.FINANCE_TYPE, FINANCE_TYPE_TRANSACTION_STATEMENT))
                }
        viewModel.outPuts.gotoInvoice()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: gotoInvoice")
                    startActivityWithFinish( Intent(this, FinanceActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Intents.FINANCE_TYPE, FINANCE_TYPE_INVOICE))
                }
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh")

                    if (viewPagerAdapter.count > 0) {
                        checkGoToMessageBox(intent)
                        return@subscribe
                    }

                    // menu fragment
                    val menu01Fragment = Menu01Fragment.newInstance(viewModel, it)
                    val menu02Fragment = Menu02Fragment.newInstance(viewModel)

                    // draw menu view
                    viewPagerAdapter.addFragment(menu01Fragment, "")
                    viewPagerAdapter.addFragment(menu02Fragment, "")

                    menuViewPager.adapter = viewPagerAdapter
                    menuTabLayout.setupWithViewPager(menuViewPager, true)
                    checkGoToMessageBox(intent)
                }

        /**
         * go to message box
         */
        viewModel.outPuts.gotoMessage()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: gotoMessage")
                    // move message fragment
                    menuTabLayout.getTabAt(MenuFragment.MENU_MESSAGEBOX)!!.select()
                }

        menu_left_area.setSafeOnClickListener { finish() }

        ll_menu_signout.setSafeOnClickListener{
            Timber.d("f9: clickToLogout")
            val dialog = NormalTwoBtnDialog(getString(R.string.singout_cancel_title), getString(R.string.signout_cancel_desc),
                    getString(R.string.cancel), getString(R.string.ok))
            dialog.isCancelable = false
            dialog.setOnClickListener(View.OnClickListener {
                it?.let {
                    if (it.id == R.id.btn_right) { viewModel.inPuts.clickToLogout(Parameter.CLICK) }
                    dialog.dismiss()
                }
            })
            dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
        }

        menuViewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) { }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageSelected(position: Int) {
                if (position == MenuFragment.MENU_MESSAGEBOX) {
                    val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelNotifications()
                }
            }
        })
    }

    private fun checkGoToMessageBox(intent: Intent) {
        Timber.d("f9: Intent : $intent")
        if (intent.hasExtra(Intents.GOTO_MESSAGE_BOX)) {
            if (intent.getBooleanExtra(Intents.GOTO_MESSAGE_BOX, false)) {
                // GOTO MessageBox
                if (menuViewPager.adapter!!.count > 0) {
                    menuViewPager.setCurrentItem(MenuFragment.MENU_MESSAGEBOX)
                }
            }
        }
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager) :
            FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList : MutableList<Fragment> = ArrayList()
        private val titleList : MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String){
            fragmentList.add(fragment)
            titleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }
    }
}



