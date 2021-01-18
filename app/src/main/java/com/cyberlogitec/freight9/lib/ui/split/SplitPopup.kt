package com.cyberlogitec.freight9.lib.ui.split

import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_TITLE_HEIGHT_80
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.toPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.split_popup_drag_top.view.*
import timber.log.Timber

class SplitPopup(private val splitUiData: SplitUiData,
                 private val listener: (SplitUiReceiveData) -> Unit) {

    private var isShowRouteTitle: Boolean = false
    private var routeName: String = ""
    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private var collapsing = true
    private var prevSlideOffset = SplitConst.SPLIT_SLIDE_COLLAPSED
    lateinit private var splitDisplayCategory: SplitDisplayCategory

    init {
        try {
            bottomSheetBehavior = BottomSheetBehavior.from<FrameLayout>(splitUiData.view)
            initSplitScreen()
            initBottomSheetBehavior()
        }
        catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    private fun initSplitScreen() {
        splitDisplayCategory = splitUiData.splitDisplayCategory
        with(splitUiData.view) {
            when(splitUiData.splitDisplayCategory) {

                SplitDisplayCategory.TradeMarketList -> {

                    onClickTradeMarket()
                }
                SplitDisplayCategory.TradeMarketOfferPrev -> {

                }
                SplitDisplayCategory.TradeMarketOfferRoute -> {

                }
            }
            changeTitle(splitUiData.splitDisplayCategory)
        }
    }

    private fun initBottomSheetBehavior() {
        if(splitUiData.splitStatus == SplitConst.SPLIT_UI_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            prevSlideOffset = 0.0f
        } else if(splitUiData.splitStatus == SplitConst.SPLIT_UI_ZERO){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            prevSlideOffset = 0.0f
        } else if(splitUiData.splitStatus == SplitConst.SPLIT_UI_HALF_EXPANDED){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            prevSlideOffset = 0.0f
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            prevSlideOffset = 1.0f
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                collapsing = if ((prevSlideOffset - slideOffset) > SplitConst.SPLIT_SLIDE_COLLAPSED) true else false

                Timber.d("f9: " + "STATE_"+ (prevSlideOffset - slideOffset) + " " + collapsing)

                if (collapsing) {
                    bottomSheetBehavior.peekHeight = SPLIT_TITLE_HEIGHT_80.toPx()
                    splitUiData.view.tv_split_title.visibility = View.GONE
                    splitUiData.view.tv_split_title_center.visibility = View.VISIBLE
                    if(isShowRouteTitle){
                        splitUiData.view.iv_carrier_logo.visibility = View.VISIBLE
                        splitUiData.view.tv_split_title_center.text = routeName
                    }else {
                        splitUiData.view.iv_carrier_logo.visibility = View.GONE
                        splitUiData.view.tv_split_title_center.text = splitUiData.title

                    }
                    when(splitDisplayCategory) {
                        SplitDisplayCategory.TradeMarketOfferPrev,
                        SplitDisplayCategory.TradeMarketOfferRoute -> {
                            splitUiData.view.cl_title.visibility = View.INVISIBLE
                        }
                        SplitDisplayCategory.TradeMarketCounterOfferList -> {
                            splitUiData.view.tv_split_title.visibility = View.GONE
                            splitUiData.view.tv_split_title_center_count.visibility = View.VISIBLE
                            splitUiData.view.tv_split_title_center.text = bottomSheet.resources.getString(R.string.counter_offers)
                        }
                    }
                }
                else {
                    splitUiData.view.tv_split_title.visibility = View.VISIBLE
                    splitUiData.view.tv_split_title_center.visibility = View.GONE
                    splitUiData.view.iv_carrier_logo.visibility = View.GONE

                    when(splitDisplayCategory) {
                        SplitDisplayCategory.TradeMarketOfferRoute,
                        SplitDisplayCategory.TradeMarketOfferPrev -> {
                            splitUiData.view.tv_split_title.visibility = View.GONE
                            splitUiData.view.cl_title.visibility = View.VISIBLE
                        }
                        SplitDisplayCategory.TradeMarketCounterOfferList -> {
                            splitUiData.view.tv_split_title.visibility = View.VISIBLE
                            splitUiData.view.tv_split_title_center_count.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("f9: " + "STATE_COLLAPSED")
                        prevSlideOffset = SplitConst.SPLIT_SLIDE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Timber.d("f9: " + "STATE_HIDDEN")
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("f9: " + "STATE_EXPANDED")
                        prevSlideOffset = SplitConst.SPLIT_SLIDE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Timber.d("f9: " + "STATE_DRAGGING")
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Timber.d("f9: " + "STATE_SETTLING " + collapsing)
                    }
                }
                onProcessWhenCollapsed(newState)
            }
        })
    }

    private fun onProcessWhenCollapsed(newState: Int) {
        when(splitUiData.splitDisplayCategory) {
            SplitDisplayCategory.TradeMarketList,SplitDisplayCategory.TradeMarketCounterOfferList -> {
                listener(SplitUiReceiveData(SplitUiEvent.EventSplitCollapsed,
                        SplitConst.SPLIT_UI_ZERO, SplitConst.SPLIT_UI_EMPTY_STRING,newState))
            }
        }
    }

    private fun onProcessWhenHidden(newState: Int) {
        listener(SplitUiReceiveData(SplitUiEvent.EventSplitCollapsed,
                SplitConst.SPLIT_UI_ZERO, SplitConst.SPLIT_UI_EMPTY_STRING,newState))
    }

    fun changeTitle(displayCategory: SplitDisplayCategory) {
        splitDisplayCategory = displayCategory
        with(splitUiData.view) {
            when (displayCategory) {
                SplitDisplayCategory.TradeMarketList -> {
                    tv_split_title.visibility = View.VISIBLE
                    tv_split_title.text = splitUiData.title
                    cl_title.visibility = View.INVISIBLE

                }
                SplitDisplayCategory.TradeMarketCounterOfferList -> {
                    tv_split_title.visibility = View.INVISIBLE
                    tv_split_title.text = splitUiData.title
                    iv_carrier_logo.visibility = View.INVISIBLE
                    tv_split_title_center.text = splitUiData.view.resources.getString(R.string.counter_offers)
                    cl_title.visibility = View.INVISIBLE
                    tv_split_title_center_count.visibility = View.VISIBLE

                }
                SplitDisplayCategory.TradeMarketOfferPrev -> {
                    tv_split_title.visibility = View.GONE
                    tv_split_title_center.visibility = View.GONE
                    cl_title.visibility = View.VISIBLE
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                    textView.typeface = ResourcesCompat.getFont(context, R.font.opensans_extrabold)!!
                    textView2.setTextColor(ContextCompat.getColor(context, R.color.greyish_brown))
                    textView2.typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)!!
                    vw_underbar_01.visibility = View.VISIBLE
                    vw_underbar_02.visibility = View.INVISIBLE
                }
                SplitDisplayCategory.TradeMarketOfferRoute -> {
                    tv_split_title.visibility = View.GONE
                    tv_split_title_center.visibility = View.GONE
                    cl_title.visibility = View.VISIBLE
                    textView.setTextColor(ContextCompat.getColor(context, R.color.greyish_brown))
                    textView.typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)!!
                    textView2.setTextColor(ContextCompat.getColor(context, R.color.white))
                    textView2.typeface = ResourcesCompat.getFont(context, R.font.opensans_extrabold)!!
                    vw_underbar_01.visibility = View.INVISIBLE
                    vw_underbar_02.visibility = View.VISIBLE
                }
            }

        }
    }

    fun setRouteTitle(carrier: String, route: String) {
        isShowRouteTitle = true
        routeName = route
        splitUiData.view.iv_carrier_logo.setImageResource(carrier.getCarrierIcon())
    }
    fun resetRouteTitle() {
        isShowRouteTitle = false

    }
    fun setCounterCount(count: Int) {

    }
    fun setHalfExpandRatio(ratio: Float) {
        bottomSheetBehavior.halfExpandedRatio = ratio
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
            /*btn_draft_test.setOnClickListener {
                listener(SplitUiReceiveData(SplitUiEvent.EventButtonClick, btn_draft_test.id, "btn_draft_test draft !!!"))
            }*/
        }
    }


}