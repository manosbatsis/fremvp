package com.cyberlogitec.freight9.ui.youroffers

import android.content.Intent
import android.os.Bundle
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.util.getCarrierCode
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_your_offers_swipe.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber


@RequiresActivityViewModel(value = YourOffersSwipeViewModel::class)
class YourOffersSwipeActivity : BaseActivity<YourOffersSwipeViewModel>() {

    private lateinit var tradeOfferWrapper: TradeOfferWrapper
    private lateinit var cell: Dashboard.Cell

    private var borList: BorList = BorList()
    private var fragmentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("f9: onCreate")

        setContentView(R.layout.act_your_offers_swipe)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Timber.v("f9: onBackPressed")

    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        // Activity onResume
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { pair ->
                        this.fragmentIndex = pair.first
                        // Dashboard로 BorList 구성 (Card link 에서 사용)
                        this.cell = pair.second
                        with(cell) {
                            borList.offerNumber = offerNumber
                            borList.cryrCd = getCarrierCode(carrierItem?.first()?.carrierCode)
                            borList.carrierCount = carrierItem?.let {
                                if (it.size > 1) it.size - 1 else 0
                            }
                            borList.locPolCd = headPolCode
                            borList.locPodCd = headPodCode
                            borList.locPolCnt = polCount
                            borList.locPodCnt = podCount
                            borList.locPolNm = headPolName
                            borList.locPodNm = headPodName
                        }
                    }
                }

        viewModel.outPuts.onSuccessRequestOfferInfoDetails()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            // getTradeOfferDetailTarget api 응답 데이터 원본
                            val orderTradeOfferDetail = it.body() as OrderTradeOfferDetail
                            borList.rdTermCode = orderTradeOfferDetail.offerRdTermCode
                            if (::cell.isInitialized) {
                                cell.lineItem?.let { lineItems ->
                                    lineItems.map { lineItem ->
                                        lineItem.offerPrices = orderTradeOfferDetail.offerLineItems.first().offerPrices
                                    }
                                    tradeOfferWrapper = TradeOfferWrapper(
                                            borList,
                                            orderTradeOfferDetail,
                                            mutableListOf(),
                                            lineItems.toMutableList())
                                    showFragment(fragmentIndex)
                                }
                            }
                        } else {
                            Timber.e("f9: Fail Request Offer Detail(Http)\n${it.errorBody()}")
                            return@subscribe
                        }
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                onBackPressed()
                            }
                            else -> {  }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                ParameterAny.ANY_SWIPE_INDEX -> {
                                    val tabIndex = second as Int
                                    showFragment(tabIndex)
                                }
                                ParameterAny.ANY_ITEM_OBJECT -> {
                                    val intent = second as Intent
                                    intent.setClass(this@YourOffersSwipeActivity,
                                            YourOffersDetailActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {  }
                            }
                        }
                    }
                }

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("--> f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    Timber.e("--> f9: error : $it")
                    showToast("Fail (Throwable)\n" + it.message)
                    finish()
                }
    }

    private fun initData() {

    }

    /**
     * activity view init
     */
    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                isEnableNavi = false)

        setListener()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        toolbar_right_btn.setSafeOnClickListener{
            Timber.d("f9: toolbar_right_btn clcick")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        ll_tab_01.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_SWIPE_INDEX, 0))
        }

        ll_tab_02.setSafeOnClickListener {
            clickViewParameterAny(Pair(ParameterAny.ANY_SWIPE_INDEX, 1))
        }
    }

    /**
     * Left, Right fragment show
     */
    private fun showFragment(index: Int) {
        setTabLayout(index)
        if (::tradeOfferWrapper.isInitialized) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment,
                            if (index == 0) {
                                YourOffersSwipePreviewFragment.newInstance(viewModel, tradeOfferWrapper)
                            } else {
                                YourOffersSwipeRouteFragment.newInstance(viewModel, tradeOfferWrapper)
                            })
                    .commit()
        }
    }

    private fun setTabLayout(index: Int) {
        tv_tab_01.setTextAppearance(
                if (index == 0) {
                    R.style.txt_opensans_eb_16_white
                } else {
                    R.style.txt_opensans_r_16_greyishbrown
        })

        tv_tab_02.setTextAppearance(
                if (index == 0) {
                    R.style.txt_opensans_r_16_greyishbrown
                } else {
                    R.style.txt_opensans_eb_16_white
                })

        vw_underbar_01.background = getDrawable(
                if (index == 0 ) {
                    R.color.blue_violet
                } else {
                    R.color.color_1d1d1d
                })

        vw_underbar_02.background = getDrawable(
                if (index == 0 ) {
                    R.color.color_1d1d1d
                } else {
                    R.color.blue_violet
                })
    }
}