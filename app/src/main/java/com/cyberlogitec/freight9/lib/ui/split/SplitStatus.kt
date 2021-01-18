package com.cyberlogitec.freight9.lib.ui.split

import android.content.Context
import android.widget.FrameLayout

enum class SplitDisplayCategory {
    TradeMarketList,
    TradeMarketCounterOfferList,
    TradeMarketOfferRoute,
    TradeMarketOfferPrev,
    YourOffersOnMarket,
    LiveDealPrice,
    YourInventory,
    AllOffersOnMarket,
    DealsByVoyageWeek,
    CounterOffers
}

enum class SplitUiEvent {
    EventButtonClick,
    EventSwipeLeft,
    EventSwipeRight,
    EventListItemSelect,
    EventSplitCollapsed
}

class SplitConst {
    companion object {
        val SPLIT_TITLE_HEIGHT_35 = 35
        val SPLIT_TITLE_HEIGHT_80 = 80
        val SPLIT_TITLE_HEIGHT_91 = 91
        val SPLIT_TITLE_HEIGHT_136 = 136
        val SPLIT_UI_COLLAPSED = 0
        val SPLIT_UI_EXPANDED = 1
        val SPLIT_UI_HALF_EXPANDED = 2
        val SPLIT_UI_EMPTY_STRING = ""
        val SPLIT_UI_ZERO = -1
        val SPLIT_SLIDE_EXPANDED = 1.0f
        val SPLIT_SLIDE_HALF_EXPANDED = 0.78f
        val SPLIT_SLIDE_HALF = 0.5f
        val SPLIT_SLIDE_COLLAPSED = 0.0f
    }
}

data class SplitUiReceiveData (val splitUiEvent: SplitUiEvent,
                               val viewId: Int,
                               val t: Any,
                               val state: Int)

data class SplitUiData (val context: Context,
                        val splitDisplayCategory: SplitDisplayCategory,
                        val view: FrameLayout,
                        val splitStatus: Int,
                        val title: String)