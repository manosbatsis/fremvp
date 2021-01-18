package com.cyberlogitec.freight9.lib.ui.split

import android.view.View
import android.widget.FrameLayout
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_TITLE_HEIGHT_35
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_TITLE_HEIGHT_91
import com.cyberlogitec.freight9.lib.util.toPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.split_market_drag_top.view.*
import timber.log.Timber

class SplitScreen(private val splitUiData: SplitUiData,
                  private val listener: (SplitUiReceiveData) -> Unit,
                  private val slideOffsetListener: (Float) -> Unit) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var oldSlideStatus = BottomSheetBehavior.STATE_COLLAPSED

    init {
        try {
            bottomSheetBehavior = BottomSheetBehavior.from<FrameLayout>(splitUiData.view)
            initSplitScreen(splitUiData.splitDisplayCategory)
            initBottomSheetBehavior()
        }
        catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    private fun initSplitScreen(splitDisplayCategory: SplitDisplayCategory) {
        with(splitUiData.view) {
            when(splitDisplayCategory) {
                SplitDisplayCategory.LiveDealPrice -> {
                    tv_split_title.text = getResources().getText(R.string.split_market_live_deal_price)
                }
                SplitDisplayCategory.DealsByVoyageWeek -> {
                    tv_split_title.text = getResources().getText(R.string.split_market_deals_by_voyage_week)
                }
                SplitDisplayCategory.YourInventory -> {
                    tv_split_title.text = getResources().getText(R.string.split_market_your_inventory)
                }
                SplitDisplayCategory.YourOffersOnMarket -> {
                    tv_split_title.text = getResources().getText(R.string.split_market_your_offers_on_market)
                }
                SplitDisplayCategory.AllOffersOnMarket -> {
                    tv_split_title.text = getResources().getText(R.string.split_market_all_offers_on_market)
                }
            }
        }
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior.state = splitUiData.splitStatus
        oldSlideStatus = splitUiData.splitStatus

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                slideOffsetListener(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when(newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        oldSlideStatus = newState
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if(oldSlideStatus != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        oldSlideStatus = newState
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        oldSlideStatus = newState
                    }
                }
                changeUi()
                onProcessWhenCollapsed(newState)
            }
        })
        changeUi()
    }

    fun setHalfExpandRatio(ratio: Float) {
        Timber.d("test $ratio")
        bottomSheetBehavior.halfExpandedRatio = ratio

    }

    private fun changeUi() {
        when(oldSlideStatus) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                splitUiData.view.ll_split_title_collapse.visibility = View.GONE
                splitUiData.view.tv_split_title.visibility = View.VISIBLE
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                splitUiData.view.ll_split_title_collapse.visibility = View.VISIBLE
                splitUiData.view.tv_split_title.visibility = View.GONE
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                bottomSheetBehavior.peekHeight = SPLIT_TITLE_HEIGHT_35.toPx()
                splitUiData.view.ll_split_title_collapse.visibility = View.INVISIBLE
                splitUiData.view.tv_split_title.visibility = View.GONE
            }
        }

    }

    fun changeTitle(displayDisplayCategory: SplitDisplayCategory) {
        initSplitScreen(displayDisplayCategory)
    }

    private fun onProcessWhenCollapsed(newState: Int) {
        when(splitUiData.splitDisplayCategory) {
            SplitDisplayCategory.TradeMarketList -> {
                listener(SplitUiReceiveData(SplitUiEvent.EventSplitCollapsed,
                        SplitConst.SPLIT_UI_ZERO, SplitConst.SPLIT_UI_EMPTY_STRING, newState))
            }
            else -> {
                listener(SplitUiReceiveData(SplitUiEvent.EventSplitCollapsed,
                        SplitConst.SPLIT_UI_ZERO, SplitConst.SPLIT_UI_EMPTY_STRING, newState))
            }
        }
    }

    /**
     * Live Deal Price > Split UI 의 모든 Event 를 처리
     */
    private fun onClickLiveDealPrice() {
    }

    /**
     * Trade Market > Split UI 의 모든 Event 를 처리
     */
    private fun onClickTradeMarket() {
        with(splitUiData.view) {
            //            btn_trademarket_test.setOnClickListener {
//                listener(SplitUiReceiveData(SplitUiEvent.EventButtonClick, btn_trademarket_test.id, "btn_trademarket_test market !!!"))
//            }
        }
    }

    /**
     * Sell Offer > SofDraft > Split UI 의 모든 Event 를 처리
     */
    private fun onClickSellOfferDraft() {
        with(splitUiData.view) {
            //            btn_draft_test.setOnClickListener {
//                listener(SplitUiReceiveData(SplitUiEvent.EventButtonClick, btn_draft_test.id, "btn_draft_test draft !!!"))
//            }
        }
    }

}