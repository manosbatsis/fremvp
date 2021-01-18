package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_BOOKING_DASHBOARD
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_BUY_OFFER
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_CARGO_TRACKING
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_INVOICE
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_MARKET_COMMENTARY
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_MARKET_INDEX
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_MARKET_WATCH
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_SELL_OFFER
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_TRADE_MARKET
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_YOUR_BUY_OFFERS
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_YOUR_INVENTORY
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_YOUR_SELL_OFFERS

enum class MenuSelect constructor(
        val menuItem: String,
        val resourceId: Int
) {
    MENU_TRADE_MARKET(MENUITEM_TRADE_MARKET, R.id.tv_market),
    MENU_SELL_OFFER(MENUITEM_SELL_OFFER, R.id.tv_sell_offer),
    MENU_BUY_OFFER(MENUITEM_BUY_OFFER, R.id.tv_buy_offer),
    MENU_MARKET_WATCH(MENUITEM_MARKET_WATCH, R.id.tv_market_watch),
    MENU_MARKET_INDEX(MENUITEM_MARKET_INDEX, R.id.tv_market_index),
    MENU_MARKET_COMMENTARY(MENUITEM_MARKET_COMMENTARY, R.id.tv_market_commentary),
    MENU_YOUR_INVENTORY(MENUITEM_YOUR_INVENTORY, R.id.tv_inventory),
    MENU_YOUR_BUY_OFFERS(MENUITEM_YOUR_BUY_OFFERS, R.id.tv_your_buy_offers),
    MENU_YOUR_SELL_OFFERS(MENUITEM_YOUR_SELL_OFFERS, R.id.tv_your_sell_offers),
    MENU_BOOKING_DASHBOARD(MENUITEM_BOOKING_DASHBOARD, R.id.tv_booking_dashboard),
    MENU_CARGO_TRACKING(MENUITEM_CARGO_TRACKING, R.id.tv_cargo_tracking),
    MENU_FINANCE_PAY_COLLECT_PLAN(MENUITEM_FINANCE_PAY_COLLECT_PLAN, R.id.tv_pay_collect_plan),
    MENU_FINANCE_TRANSACTION_STATEMENT(MENUITEM_FINANCE_TRANSACTION_STATEMENT, R.id.tv_transaction_statement),
    MENU_FINANCE_INVOICE(MENUITEM_FINANCE_INVOICE, R.id.tv_invoice);
    companion object {
        fun getResourceId(menuItem: String) : MenuSelect? {
            for (item in values()) {
                if (menuItem == item.menuItem) {
                    return item
                }
            }
            return MENU_TRADE_MARKET
        }
    }
}