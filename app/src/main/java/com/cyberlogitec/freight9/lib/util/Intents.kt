package com.cyberlogitec.freight9.lib.util

import android.app.Activity
import android.content.Intent
import com.cyberlogitec.freight9.R

class Intents {
    companion object {
        const val MSTR_CTRK_NR = "mstrCtrkNr"
        const val MSTR_CTRK_NR_2 = "mstrCtrkNr_2"
        const val CRYR_CD = "cryrCd"
        const val OFER_GRP_NR = "oferGrpNr"
        const val TOPIC_ID = "topicId"
        const val OFFER_ITEM = "offer_item"
        const val GOTO_MESSAGE_BOX = "goto_message_box"
        const val GOTO_EVENT_SERVICE_RUN = "goto_event_service_run"
        const val MENU_ITEM = "menu_item"
        const val FROM = "from"
        const val TO = "to"
        const val INVENTORY_LIST = "inventory_list"
        const val MSTR_CTRK = "mstrCtrk"
        const val OFFER = "offer"
        const val OFFER_BY_MADE_CONDITION = "offer_by_made_condition"
        const val OFFER_DISCARD = "offer_discard"
        const val OFFER_MAKE_STEP = "offer_make_step"
        const val SCHEDULE_LIST = "schedule_list"
        const val CARRIER_LIST = "carrier_list"
        const val VIEW_MODEL = "view_model"
        // For Sell Offer wizard
        const val SELL_OFFER_STEP = "sell_offer_step"
        // For Buy Offer wizard
        const val BUY_OFFER_STEP = "buy_offer_step"
        // For Dashboard
        const val YOUR_OFFER_TYPE = "your_offer_type"
        const val YOUR_OFFER_NUMBER = "your_offer_number"
        const val YOUR_OFFER_CHANGE_SEQ = "your_offer_change_seq"
        const val YOUR_OFFER_FRAG_INDEX = "your_offer_frag_index"
        const val YOUR_OFFER_DASHBOARD_ITEM = "your_offer_dashboard_item"
        const val YOUR_OFFER_DASHBOARD_ITEM_DETAIL = "your_offer_dashboard_item_detail"
        const val YOUR_OFFER_HISTORY_ITEM = "your_offer_history_item"
        const val YOUR_OFFER_TRADE_WRAPPER_INFO = "your_offer_trade_wrapper_info"
        const val MARKET_WATCH_CHART_SETTING = "market_watch_chart_setting"
        // Finance
        const val FINANCE_TYPE = "finance_type"
        const val FINANCE_FILTER = "finance_filter"
        // For Tracking Dashboard
        const val CARGO_TRACKING_FILTER = "cargo_tracking_filter"
        const val CARGO_TRACKING_ENTRY_OTHER = "cargo_tracking_ENTRY_OTHER"
        const val CARGO_TRACKING_DATA = "cargo_tracking_data"
        // For Booking Dashboard
        const val BOOKING_DASHBOARD_FILTER = "booking_dashboard_filter"
        const val BOOKING_DASHBOARD_FILTER_MOVE = "booking_dashboard_filter_move"
    }
}

fun Activity.startActivityWithFinish(`class`: Class<*>) = startActivity(Intent(this, `class`)).run { finish() }

fun Activity.startActivityWithFinish(intent: Intent) = startActivity(intent).run { finish() }

fun Activity.startActivity(`class`: Class<*>) = startActivity(Intent(this, `class`))

fun Activity.startMenuActivity(menuItem: String = "", `class`: Class<*>) {
    val intent = Intent(this, `class`)
    intent.putExtra(Intents.MENU_ITEM, menuItem)
    startActivity(intent)
    //overridePendingTransition(R.anim.menu_enter, R.anim.menu_exit);
}

fun Activity.startMenuActivityWithIntent(menuItem: String = "", `class`: Class<*>, attachIntent: Intent) {
    val intent = Intent(this, `class`)
    intent.putExtra(Intents.MENU_ITEM, menuItem)
    if (attachIntent.hasExtra(Intents.GOTO_MESSAGE_BOX)) {
        intent.putExtra(Intents.GOTO_MESSAGE_BOX, attachIntent.getBooleanExtra(Intents.GOTO_MESSAGE_BOX, false))
    }
    startActivity(intent)
    //overridePendingTransition(R.anim.menu_enter, R.anim.menu_exit);
}

fun Activity.startMenuActivityWithFinish(menuItem: String = "", `class`: Class<*>) {
    val intent = Intent(this, `class`)
    intent.putExtra(Intents.MENU_ITEM, menuItem)
    startActivity(intent)
    finish()
    //overridePendingTransition(R.anim.menu_enter, R.anim.menu_exit);
}

fun Activity.startActivityForResult(`class`: Class<*>, requestCode: Int) = startActivityForResult(Intent(this, `class`), requestCode)

fun Intent.getLongExtra(key: String) = getLongExtra(key, -1L)

fun Intent.getIntExtra(key: String) = getIntExtra(key, -1)
