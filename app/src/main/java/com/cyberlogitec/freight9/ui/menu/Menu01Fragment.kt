package com.cyberlogitec.freight9.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import com.cyberlogitec.freight9.lib.model.User
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.ui.enums.MenuSelect
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.item_menu_01.*
import timber.log.Timber

class Menu01Fragment constructor(val viewModel:MenuViewModel, var user:User?): RxFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.item_menu_01, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.v("f9: onViewCreated")

        // hide action bar
        // (activity as AppCompatActivity).supportActionBar?.hide()

        viewModel.outPuts.menuItem()
                .bindToLifecycle(this)
                .subscribe { setSelectedMenuColor(view, it) }

        /**
         * click message box
         */
        iv_menu_message.setSafeOnClickListener {
            Timber.d("f9: clickToMessage")
            viewModel.inPuts.clickToMessage(Parameter.CLICK)
        }
        tv_market.setSafeOnClickListener{
            Timber.d("f9: clickToMarket")
            setSelectedMenuColor(view, MENUITEM_TRADE_MARKET)
            viewModel.inPuts.clickToMarket(Parameter.CLICK)
        }

        /**
         * click sell offer
         */
        tv_sell_offer.setSafeOnClickListener{
            Timber.d("f9: clickToSellOffer")
            setSelectedMenuColor(view, MENUITEM_SELL_OFFER)
            viewModel.inPuts.clickToSellOffer(Parameter.CLICK)
        }

        /**
         * click buy offer
         */
        tv_buy_offer.setSafeOnClickListener{
            Timber.d("f9: clickToBuyOffer")
            setSelectedMenuColor(view, MENUITEM_BUY_OFFER)
            viewModel.inPuts.clickToBuyOffer(Parameter.CLICK)
        }

        tv_market_watch.setSafeOnClickListener{
            Timber.d("f9: clickToMarketWatch")
            setSelectedMenuColor(view, MENUITEM_MARKET_WATCH)
            viewModel.inPuts.clickToMarketWatch(Parameter.CLICK)
        }
        tv_market_index.setSafeOnClickListener{
            Timber.d("f9: clickToMarketIndex")
            setSelectedMenuColor(view, MENUITEM_MARKET_INDEX)
            viewModel.inPuts.clickToMarketIndex(Parameter.CLICK)
        }
        tv_market_commentary.setSafeOnClickListener {
            Timber.d("f9: clickToMarketCommentary")
            setSelectedMenuColor(view, MENUITEM_MARKET_COMMENTARY)
            viewModel.inPuts.clickToMarketCommentary(Parameter.CLICK)
        }

        /**
         * click inventory
         */
        tv_inventory.setSafeOnClickListener{
            Timber.d("f9: clickToInventory")
            setSelectedMenuColor(view, MENUITEM_YOUR_INVENTORY)
            viewModel.inPuts.clickToInventory(Parameter.CLICK)
        }

        /**
         * click buy offers
         */
        tv_your_buy_offers.setSafeOnClickListener{
            Timber.d("f9: clickToYourBuyOffers")
            setSelectedMenuColor(view, MENUITEM_YOUR_BUY_OFFERS)
            viewModel.inPuts.clickToYourBuyOffers(Parameter.CLICK)
        }

        /**
         * click sell offers
         */
        tv_your_sell_offers.setSafeOnClickListener{
            Timber.d("f9: clickToYourSellOffers")
            setSelectedMenuColor(view, MENUITEM_YOUR_SELL_OFFERS)
            viewModel.inPuts.clickToYourSellOffers(Parameter.CLICK)
        }
        tv_booking_dashboard.setSafeOnClickListener{
            Timber.d("f9: clickToBookingDashboard")
            setSelectedMenuColor(view, MENUITEM_BOOKING_DASHBOARD)
            viewModel.inPuts.clickToBookingDashboard(Parameter.CLICK)
        }

        /**
         * click cargo tracking
         */
        tv_cargo_tracking.setSafeOnClickListener{
            Timber.d("f9: clickToCargoTracking")
            setSelectedMenuColor(view, MENUITEM_CARGO_TRACKING)
            viewModel.inPuts.clickToCargoTracking(Parameter.CLICK)
        }

        tv_pay_collect_plan.setSafeOnClickListener{
            Timber.d("f9: clickToPayCollectPlan click")
            setSelectedMenuColor(view, MENUITEM_FINANCE_PAY_COLLECT_PLAN)
            viewModel.inPuts.clickToPayCollectPlan(Parameter.CLICK)
        }
        tv_transaction_statement.setSafeOnClickListener{
            Timber.d("f9: clickToTransactionStatement")
            setSelectedMenuColor(view, MENUITEM_FINANCE_TRANSACTION_STATEMENT)
            viewModel.inPuts.clickToTransactionStatement(Parameter.CLICK)
        }
        tv_invoice.setSafeOnClickListener{
            Timber.d("f9: clickToInvoice")
            setSelectedMenuColor(view, MENUITEM_FINANCE_INVOICE)
            viewModel.inPuts.clickToInvoice(Parameter.CLICK)
        }
        tv_logout.setSafeOnClickListener{
            Timber.d("f9: clickToLogout")

            val dialog = NormalTwoBtnDialog(getString(R.string.singout_cancel_title), getString(R.string.signout_cancel_desc))
            dialog.isCancelable = false
            dialog.setOnClickListener(View.OnClickListener {
                it?.let {
                    if (it.id == R.id.btn_right) {
                        viewModel.inPuts.clickToLogout(Parameter.CLICK)
                    }

                    dialog.dismiss()
                }
            })
            dialog.show(this.parentFragmentManager, dialog.CLASS_NAME)
        }

        tv_organization.text = if (user?.organization.isNullOrEmpty()) "Unknown Organization" else user?.organization
        tv_user_email.text = if (user?.email.isNullOrEmpty()) "Unknown User" else user?.email
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Timber.v("f9: onActivityCreated")
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: MenuViewModel, user: User?) : Menu01Fragment {
            val fragment = Menu01Fragment(viewModel, user)
            return fragment
        }
    }

    /**
     * 현재 선택된 menu item의 ui 처리 (selected, unselected)
     */
    private fun setSelectedMenuColor(view: View, menuItem: String) {
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_TRADE_MARKET)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_TRADE_MARKET)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_SELL_OFFER)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_SELL_OFFER)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_BUY_OFFER)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_BUY_OFFER)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_MARKET_WATCH)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_MARKET_INDEX)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_MARKET_COMMENTARY)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_INVENTORY)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_INVENTORY)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_BUY_OFFERS)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_BUY_OFFERS)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_SELL_OFFERS)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_YOUR_SELL_OFFERS)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_BOOKING_DASHBOARD)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_BOOKING_DASHBOARD)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_CARGO_TRACKING)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_FINANCE_PAY_COLLECT_PLAN)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_FINANCE_TRANSACTION_STATEMENT)!!.resourceId)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.very_light_pink))
        view.findViewById<TextView>(MenuSelect.getResourceId(MENUITEM_FINANCE_INVOICE)!!.resourceId)
                .setBackgroundColor(activity!!.getColor(R.color.color_1a1a1a))
        if (!menuItem.isNullOrEmpty()) {
            view.findViewById<TextView>(MenuSelect.getResourceId(menuItem)!!.resourceId)
                    .setTextColor(ContextCompat.getColor(activity!!, R.color.purpley_blue))
            view.findViewById<TextView>(MenuSelect.getResourceId(menuItem)!!.resourceId)
                    .setBackgroundColor(activity!!.getColor(R.color.color_0d0d0d))
        }
    }
}