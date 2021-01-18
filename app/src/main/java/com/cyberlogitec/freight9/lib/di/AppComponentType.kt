package com.cyberlogitec.freight9.lib.di

import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.Environment
import com.cyberlogitec.freight9.ui.booking.*
import com.cyberlogitec.freight9.ui.buyoffer.*
import com.cyberlogitec.freight9.ui.buyorder.BuyOrderActivity
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingActivity
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingDetailActivity
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingFilterActivity
import com.cyberlogitec.freight9.ui.finance.FinanceActivity
import com.cyberlogitec.freight9.ui.finance.FinanceFilterActivity
import com.cyberlogitec.freight9.ui.home.HomeActivity
import com.cyberlogitec.freight9.ui.inventory.InventoryActivity
import com.cyberlogitec.freight9.ui.inventory.InventoryDetailActivity
import com.cyberlogitec.freight9.ui.inventory.InventoryDetailPopupActivity
import com.cyberlogitec.freight9.ui.inventory.RouteFilterActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchIndexActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchSettingActivity
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchWeekActivity
import com.cyberlogitec.freight9.ui.member.LoginActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.routefilter.both.RouteFilterBothAct
import com.cyberlogitec.freight9.ui.routefilter.pod.RouteFilterPodAct
import com.cyberlogitec.freight9.ui.routefilter.pol.RouteFilterPolAct
import com.cyberlogitec.freight9.ui.routeselect.featured.PreferredRouteEditActivity
import com.cyberlogitec.freight9.ui.routeselect.search.RouteSearchActivity
import com.cyberlogitec.freight9.ui.routeselect.select.RouteSelectActivity
import com.cyberlogitec.freight9.ui.salesquota.SalesQuotaActivity
import com.cyberlogitec.freight9.ui.selloffer.*
import com.cyberlogitec.freight9.ui.sellorder.SellOrderActivity
import com.cyberlogitec.freight9.ui.splash.SplashActivity
import com.cyberlogitec.freight9.ui.trademarket.*
import com.cyberlogitec.freight9.ui.transaction.TransactionActivity
import com.cyberlogitec.freight9.ui.youroffers.*

interface AppComponentType {
    fun inject(app: App)

    fun inject(activity: SplashActivity)
    fun inject(activity: MenuActivity)
    fun inject(activity: HomeActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: MarketActivity)
    fun inject(activity: TransactionActivity)
    fun inject(activity: SalesQuotaActivity)

    // inventory
    fun inject(activity: InventoryActivity)
    fun inject(activity: InventoryDetailActivity)
    fun inject(activity: InventoryDetailPopupActivity)

    // sell-offer
    fun inject(activity: SofNewOfferAct)
    fun inject(activity: SofWizardActivity)
    fun inject(activity: SofContractAct)
    fun inject(activity: SofVolumeAct)
    fun inject(activity: SofPriceAct)
    fun inject(activity: SofConditionAct)
    fun inject(activity: SofMakeOfferAct)
    fun inject(activity: SofConditionTableAct)
    fun inject(activity: SofRouteGridAct)
    fun inject(activity: SofPriceTableAct)
    fun inject(activity:SofConditionDetailAct)
    fun inject(activity:SofVolumeCheckAct)
    fun inject(activity:SofPriceCheckAct)
    fun inject(activity:SofConditionCheckAct)

    // buy-order
    fun inject(activity: BuyOrderActivity)

    // buy-offer
    fun inject(activity: BofNewOfferAct)
    fun inject(activity: BofWizardRouteActivity)
    fun inject(activity: BofRecentAct)
    fun inject(activity: BofLaneAct)
    fun inject(activity: BofPolAct)
    fun inject(activity: BofPodAct)
    fun inject(activity: BofRouteAct)
    fun inject(activity: BofWizardActivity)
    fun inject(activity: BofVolumeAct)
    fun inject(activity: BofPriceAct)
    fun inject(activity: BofPriceCheckAct)
    fun inject(activity: BofMakeOfferAct)
    fun inject(activity: BofVolumeCheckAct)
    fun inject(activity: BofConditionAct)
    fun inject(activity: BofConditionCheckAct)
    fun inject(activity: BofCarrierAct)

    // sell-order
    fun inject(activity: SellOrderActivity)

    // route select
    fun inject(activity: RouteSearchActivity)
    fun inject(activity: RouteSelectActivity)
    fun inject(activity: PreferredRouteEditActivity)
    fun inject(activity: RouteFilterActivity)

    // route filter
    fun inject(activity: RouteFilterBothAct)
    fun inject(activity: RouteFilterPolAct)
    fun inject(activity: RouteFilterPodAct)

    // market
    fun inject(activity: MarketCarrierFilterActivity)
    fun inject(activity: MarketContainerFilterActivity)
    fun inject(activity: MarketPpdFilterActivity)

    // Your Offers
    fun inject(activity: YourOffersActivity)
    fun inject(activity: YourOffersSwipeActivity)
    fun inject(activity: YourOffersDetailActivity)
    fun inject(activity: YourOffersDetailPopupActivity)
    fun inject(activity: YourOffersHistoryActivity)
    fun inject(activity: YourOffersHistoryDetailActivity)
    fun inject(activity: YourOffersTransactionActivity)
    fun inject(activity: YourOffersTransactionFilterActivity)

    // market watch
    fun inject(activity: MarketWatchActivity)
    fun inject(activity: MarketWatchSettingActivity)
    fun inject(activity: MarketWatchWeekActivity)
    fun inject(activity: MarketWatchIndexActivity)

    // booking dashboard
    fun inject(activity: BookingDashboardActivity)
    fun inject(activity: BookingDashboardDetailActivity)
    fun inject(activity: BookingDashboardFilterActivity)
    fun inject(activity: BookingDashboardSiActivity)

    // Cargo tracking
    fun inject(activity: CargoTrackingActivity)
    fun inject(activity: CargoTrackingFilterActivity)
    fun inject(activity: CargoTrackingDetailActivity)

    // Finance
    fun inject(activity: FinanceActivity)
    fun inject(activity: FinanceFilterActivity)

    fun enviorment(): Environment

}