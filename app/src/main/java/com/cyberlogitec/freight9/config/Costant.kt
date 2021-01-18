package com.cyberlogitec.freight9.config

object Constant {
    const val DB_NAME = "f9.db"

    const val CONNECT_TIMEOUT   = 180L     // seconds
    const val READ_TIMEOUT      = 60L        // seconds
    const val WRITE_TIMEOUT     = 60L       // seconds

    const val CLICK_INTERVAL    = 2000     // mili-seconds
    const val MAX_VOLUME        = 999      // T
    const val MAX_PRICE_PCT     = 99999      // Percent

    const val EmptyString       = ""
}

//object ContainerType {
//    const val F_TYPE = "A"  // A : Full Dry
//    const val R_TYPE = "B"  // B : Reefer
//    const val E_TYPE = "C"  // C : Empty
//    const val S_TYPE = "D"  // D : SOC
//}


object MenuItem {
    const val MENUITEM_TRADE_MARKET = "TRACE_MARKET"
    const val MENUITEM_SELL_OFFER = "SELL_OFFER"
    const val MENUITEM_BUY_OFFER = "BUY_OFFER"
    const val MENUITEM_MARKET_WATCH = "MARKET_WATCH"
    const val MENUITEM_MARKET_INDEX = "MARKET_INDEX"
    const val MENUITEM_MARKET_COMMENTARY = "MARKET_COMMENTARY"
    const val MENUITEM_YOUR_INVENTORY = "YOUR_INVENTORY"
    const val MENUITEM_YOUR_BUY_OFFERS = "YOUR_BUY_OFFERS"
    const val MENUITEM_YOUR_SELL_OFFERS = "YOUR_SELL_OFFERS"
    const val MENUITEM_BOOKING_DASHBOARD = "booking_dashboard"
    const val MENUITEM_CARGO_TRACKING = "cargo_tracking"
    const val MENUITEM_FINANCE_PAY_COLLECT_PLAN = "finance_pay_collect_plan"
    const val MENUITEM_FINANCE_TRANSACTION_STATEMENT = "finance_transaction_statement"
    const val MENUITEM_FINANCE_INVOICE = "finance_invoice"
}

object MenuFragment {
    const val MENU_LIST = 0
    const val MENU_MESSAGEBOX = 1
}

object Channel {
    // Freight9, freight9_channel, freight9_channel_trade
    const val CHANNEL_SERVICE_ID    = "freight9_channel_stomp"
    const val CHANNEL_SERVICE_NAME  = "Freight9 Trade"
    const val CHANNEL_SERVICE_DESC  = "Freight9 trade service."
    const val CHANNEL_PUSH_ID       = "freight9_channel_push"
    const val CHANNEL_PUSH_NAME     = "Freight9 Push"
    const val CHANNEL_PUSH_DESC     = "Freight9 push service."
    const val NOTIFICATION_SERVICE_ID = 2020
    const val NOTIFICATION_PUSH_GROUP_ID = 2021
    const val NOTIFICATION_PUSH_GROUP = "com.cyberlogitec.freight9.trade.message"
}

object LocationTypeCode {
    const val POR = "01"
    const val POL = "02"
    const val POD = "03"
    const val DEL = "04"
}

object RdTermCode {
    const val CY_CY = "01"
    const val CY_DR = "02"
    const val DR_CY = "03"
    const val DR_DR = "04"
}

object PaymentPlanCode {
    const val PS = "01PS"   // 1: pay standard
    const val PF = "01PF"   // 2: pay first
    const val PL = "01PL"   // 3: pay later
}

object PaymentTermCode {
    const val PPD = "01"
    const val CCT = "02"
}

object ContainerType {
    const val F_TYPE = "01"  // A : Full Dry
    const val R_TYPE = "02"  // B : Reefer
    const val E_TYPE = "03"  // C : Empty
    const val S_TYPE = "04"  // D : SOC
}

object ContainerSizeType {
    const val N20FT_TYPE = "01"  // 1 : 20ft
    const val N40FT_TYPE = "02"  // 2 : 40ft
    const val N45FT_TYPE = "03"  // 3 : 45ft
    const val N45HC_TYPE = "04"  // 4 : 45hc
}

object StompStatusCode {
    const val CONNECTED = "01"
    const val DISCONNECTED = "02"
    const val RECEIVING = "03"
}

object SellOffer {
    const val SELL_OFFER_WIZARD = true          // use wizard style : true / else : false
    const val SELL_OFFER_NO_DRAFT = true        // no draft : true / draft : false
}

object BuyOffer {
    const val BUY_OFFER_ROUTE_WIZARD = true     // use wizard style : true / else : false
    const val BUY_OFFER_WIZARD = true           // use wizard style : true / else : false
    const val BUY_OFFER_NO_DRAFT = true         // no draft : true / draft : false
}

object PushEventType {
    const val EVENT_TYPE_OFFER_CANCELED_PASS = "TradeOfferCanceled"
    const val EVENT_TYPE_OFFER_CANCELED_FAIL = "TradeOfferCancelFailed"
    const val EVENT_TYPE_OFFER_CREATED_PASS = "TradeOfferCreated"
    const val EVENT_TYPE_OFFER_DEALT_PASS = "TradeOfferDealt"
}
