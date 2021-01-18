package com.cyberlogitec.freight9.ui.sellorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.InventoryList
import com.cyberlogitec.freight9.lib.util.getCarrierCode
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.getCodeCount
import com.cyberlogitec.freight9.lib.util.getWeek
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.frag_order_contract_selector.*
import kotlinx.android.synthetic.main.frag_order_contract_selector.view.*
import timber.log.Timber

class OrderContractFragment constructor(val viewModel: SellOrderViewModel, val index: Int,
                                        val inventory: InventoryList?, private val useDummy: Boolean) : RxFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_order_contract_selector, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.v("f9: onViewCreated")

        // 첫번째 페이지에 "Sleect a Contract from Your Inventory" 표시. Your Inventory 값은 ""
        view.ll_order_contract_intro.visibility = if (index == 0) View.VISIBLE else View.GONE
        view.ll_order_contract.visibility = if (index == 0) View.GONE else View.VISIBLE
        var cardViewColor = if (index == 0) R.color.greyish_brown else R.color.white
        view.cv_order_contract.setCardBackgroundColor(view.context.getColor(cardViewColor))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Timber.v("f9: onActivityCreated")

        /**
         *
         * [data]
         *
         * iv_order_contract_carrier_logo / tv_order_contract_carrier_name / tv_order_contract_carrier_twk
         * tv_order_pol_contract_name / tv_order_pol_contract_name_count / tv_order_pol_contract_desc
         * tv_order_pod_contract_name / tv_order_pod_contract_name_count / tv_order_pod_contract_desc
         * tv_order_pol_pod_contract_detail_partial / tv_order_pol_pod_contract_detail_week
         */

        if (useDummy) {
            iv_order_contract_carrier_logo.setImageResource("".getCarrierIcon(false))
            tv_order_contract_carrier_name.text = getString(R.string.all_carriers)
            tv_order_contract_carrier_count.text = ""

            tv_order_contract_carrier_twk.text = "100-500"

            tv_order_pol_contract_name.text = "CNSHA"
            tv_order_pol_contract_name_count.text = ""
            tv_order_pol_contract_desc.text = "Shanghai, Shanghai"

            tv_order_pod_contract_name.text = "DEHAM"
            tv_order_pod_contract_name_count.text = ""
            tv_order_pod_contract_desc.text = "Hamburg, HH"

            tv_order_pol_pod_period.text = "W01-W30"
        } else {
            if (index > 0) {
                with(inventory!!) {
                    iv_order_contract_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                    tv_order_contract_carrier_name.text = activity!!.getCarrierCode(carrierCode)
                    tv_order_contract_carrier_count.text = carrierCount.getCodeCount()

                    tv_order_contract_carrier_twk.text = "${minQty?.toFloat()?.toInt()}-${maxQty?.toFloat()?.toInt()}"

                    tv_order_pol_contract_name.text = polCode
                    tv_order_pol_contract_name_count.text = polCount.getCodeCount()
                    tv_order_pol_contract_desc.text = polName

                    tv_order_pod_contract_name.text = podCode
                    tv_order_pod_contract_name_count.text = podCount.getCodeCount()
                    tv_order_pod_contract_desc.text = podName

                    tv_order_pol_pod_period.text = activity!!.getWeek(minYearWeek) + "-" + activity!!.getWeek(maxYearWeek)
                }
            }
        }
    }

    fun getInventoryData(): InventoryList? = inventory

    //fun getInventoryFragmentIndex(): Int = index

    companion object {
        @JvmStatic
        fun newInstance(viewModel: SellOrderViewModel, index: Int, inventory: InventoryList?, useDummy: Boolean = false): OrderContractFragment {
            return OrderContractFragment(viewModel, index, inventory, useDummy)
        }
    }
}