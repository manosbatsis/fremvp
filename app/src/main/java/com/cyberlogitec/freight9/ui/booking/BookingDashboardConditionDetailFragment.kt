package com.cyberlogitec.freight9.ui.booking

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboardItem
import com.cyberlogitec.freight9.lib.model.booking.PartyInfo
import com.cyberlogitec.freight9.lib.util.getCargoTrackingDetailData
import com.cyberlogitec.freight9.lib.util.getCarrierCodeToF9
import com.cyberlogitec.freight9.lib.util.getContainerSizeName
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingScheduleAdapter
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.bottom_sheet_bookingdashboard_condition_detail.*
import timber.log.Timber
import java.util.*


class BookingDashboardConditionDetailFragment(val viewModel: BaseViewModel, val item: BookingDashboardItem?): RxFragment() {

    private lateinit var bookingItem: BookingDashboardItem

    init {
        if (item != null) {
            bookingItem = item
        }
    }
    enum class BookingDashboardUiType {
        CHARGE_PAYPLAN, TRACKING, VESSEL_SCHEDULE, CHARGE, PARTIES, SI_PARTIES, MARK
    }

    private var uiType = BookingDashboardUiType.CHARGE_PAYPLAN
    // TODO : remark for dummy data
    //private var cargoTrackingStatusDetailList: MutableList<CargoTrackingStatusDetail> = mutableListOf()

    private val cargoTrackingScheduleAdapter by lazy {
        CargoTrackingScheduleAdapter()
                .apply {
                    // Do nothing
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_bookingdashboard_condition_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        initView()
        updateUi()
    }

    private fun initView() {
        updateUiType(uiType)
        recyclerViewInit()
    }

    private fun recyclerViewInit() {
        recycler_cargo_tracking_schedule_list.apply {
            layoutManager = LinearLayoutManager(this@BookingDashboardConditionDetailFragment.context)
            adapter = this@BookingDashboardConditionDetailFragment.cargoTrackingScheduleAdapter
        }
        setRecyclerData()
    }

    // TODO : Cargo Tracking (Set Dummy Data (index : 0 ~ 7))
    private fun setRecyclerData() {
        cargoTrackingScheduleAdapter.setMarginApply(false)
        cargoTrackingScheduleAdapter.setBackground(false)
        cargoTrackingScheduleAdapter.setData(ArrayList(getCargoTrackingDetailData())[0].statusDetailList)
        cargoTrackingScheduleAdapter.notifyDataSetChanged()
    }

    fun setUiType(type:BookingDashboardUiType){
        uiType = type
    }
    fun setBookingItem(item:BookingDashboardItem) {
        bookingItem = item
        updateUiData()

    }
    fun updateUi() {
        updateUiData()
        updateUiType(uiType)
        Handler().post {
            ns_contents.scrollTo(0,0)
            //ns_contents.fullScroll(View.FOCUS_UP)
        }
    }
    private fun updateUiType(type:BookingDashboardUiType) {
        cv_cargo_tracking.visibility = View.GONE
        cv_condition.visibility = View.GONE
        cv_charge.visibility = View.GONE
        cv_payplan.visibility = View.GONE
        ll_parties.visibility = View.GONE
        cv_vessel.visibility = View.GONE
        ll_mark.visibility = View.GONE
        when(type) {
            BookingDashboardUiType.CHARGE_PAYPLAN -> {
                cv_charge.visibility = View.VISIBLE
                cv_payplan.visibility = View.VISIBLE
            }
            BookingDashboardUiType.TRACKING -> {
                cv_cargo_tracking.visibility = View.VISIBLE
                cv_condition.visibility = View.VISIBLE
            }
            BookingDashboardUiType.VESSEL_SCHEDULE -> { cv_vessel.visibility = View.VISIBLE }
            BookingDashboardUiType.CHARGE -> { cv_charge.visibility = View.VISIBLE }
            BookingDashboardUiType.PARTIES -> {
                ll_parties.visibility = View.VISIBLE
                cv_parties_additional.visibility = View.GONE}
            BookingDashboardUiType.SI_PARTIES -> {
                ll_parties.visibility = View.VISIBLE
                cv_parties_additional.visibility = View.VISIBLE}
            BookingDashboardUiType.MARK -> {
                ll_mark.visibility = View.VISIBLE
            }

        }
    }

    private fun updateUiData() {
        if(!this.isAdded || !:: bookingItem.isInitialized)
            return
        // tracking

        // condition
        //-rdterm
        var rdterm: String = ""
        bookingItem?.booking?.receiveTerm?.let {
            when(bookingItem?.booking?.receiveTerm) {
                "Y" -> {rdterm = "CY"}
                "D" -> {rdterm = "DOOR"}
                "S" -> {rdterm = "CFS"}
            }
        }
        rdterm += "-"
        bookingItem?.booking?.deliveryTerm?.let {
            when(bookingItem?.booking?.deliveryTerm) {
                "Y" -> {rdterm += "CY"}
                "D" -> {rdterm += "DOOR"}
                "C" -> {rdterm += "CFS"}
            }
        }

        tv_rdterm.text = rdterm
        //-dry container
        //container code 22G0 2: length, 2: height, G0: container type
        var emptyContainer: String = ""
        for(item in bookingItem?.container?.filter { !it.containerTypeSize.contains("R",true)}?.distinctBy{it.containerTypeSize}) {
            if(emptyContainer.isNotEmpty())
                emptyContainer += ", "

            emptyContainer += item.containerTypeSize.getContainerSizeName()
        }
        tv_container.text = emptyContainer

        var refContainer: String = ""
        for(item in bookingItem?.container?.filter { it.containerTypeSize.contains("R",true)}?.distinctBy{it.containerTypeSize}) {
            if(refContainer.isNotEmpty())
                refContainer += ", "

            refContainer += item.containerTypeSize.getContainerSizeName()
        }
        tv_rf_container.text = refContainer

        //payplan
        tv_payplan.text = "TBD"
        //carrier
        tv_carrier.text = context?.getCarrierCodeToF9(bookingItem?.party?.carrierInfo?.partyInfo?.partyCode ?: "-")

        //vessle schedule
        bookingItem.transport?.let {

            val transport = bookingItem.transport.firstOrNull()
            tv_vs_pol_code.text = transport?.transportLocation?.portOfLoadingLocationCode
            tv_vs_pol_name.text = transport?.transportLocation?.portOfLoadingLocationName
            tv_vs_pol_eta.text = "-"
            tv_vs_pol_etb.text = "-"
            tv_vs_pol_etd.text = "-"

            tv_vs_pod_code.text = transport?.transportLocation?.portOfDischargeLocationCode
            tv_vs_pod_name.text = transport?.transportLocation?.portOfDischargeLocationName
            tv_vs_pod_eta.text = "-"
            tv_vs_pod_etb.text = "-"

        }

        //charge
        //freight
        tv_freight_price
        //freight detail charge by container
        //ll_charge_container_holder.addView()

        tv_price_lowsurphur
        tv_price_terminal_handlingr
        tv_price_port_service

        tv_price_total

        //parties
        tv_shipper_name.text = bookingItem?.party?.consignorInfo?.partyInfo?.partyName ?: "-"
        tv_shipper_detail.text = makeAddress(bookingItem?.party?.consignorInfo?.partyInfo)

        tv_consignee_name.text = bookingItem?.party?.consigneeInfo?.partyInfo?.partyName ?: "-"
        tv_consignee_detail.text = makeAddress(bookingItem?.party?.consigneeInfo?.partyInfo)

        tv_freight_forwarder_name.text = bookingItem?.party?.freightForwarderInfo?.partyInfo?.partyName ?: "-"
        tv_freight_forwarder_detail.text = makeAddress(bookingItem?.party?.freightForwarderInfo?.partyInfo)

        tv_notify_name.text = bookingItem?.party?.firstNotifyPartyInfo?.partyInfo?.partyName ?: "-"
        tv_notify_detail.text = makeAddress(bookingItem?.party?.firstNotifyPartyInfo?.partyInfo)

        //additional info
        tv_invoice_ref_no.text = bookingItem?.booking?.bookingReferenceNo?.invoiceNo ?: "-"
        tv_purchase_order_no.text = bookingItem?.booking?.bookingReferenceNo?.orderNo ?: "-"
        tv_export_ref_no.text = bookingItem?.booking?.bookingReferenceNo?.exportReferenceNo ?: "-"
        tv_eori_no  //Economic Operators Registration and Identification number

        // transportstagecode == 10
        tv_precarriageby.text = bookingItem?.transport?.find { it.transportStageCode == "10" }?.vesselName ?: "-"

        tv_mark.text = """FFFFFFFFFFFFFFFFFFFFFFRRRRRRRRRRRRRRRRR   EEEEEEEEEEEEEEEEEEEEEEIIIIIIIIII      GGGGGGGGGGGGGHHHHHHHHH     HHHHHHHHHTTTTTTTTTTTTTTTTTTTTTTT     999999999
F::::::::::::::::::::FR::::::::::::::::R  E::::::::::::::::::::EI::::::::I   GGG::::::::::::GH:::::::H     H:::::::HT:::::::::::::::::::::T   99:::::::::99
F::::::::::::::::::::FR::::::RRRRRR:::::R E::::::::::::::::::::EI::::::::I GG:::::::::::::::GH:::::::H     H:::::::HT:::::::::::::::::::::T 99:::::::::::::99
FF::::::FFFFFFFFF::::FRR:::::R     R:::::REE::::::EEEEEEEEE::::EII::::::IIG:::::GGGGGGGG::::GHH::::::H     H::::::HHT:::::TT:::::::TT:::::T9::::::99999::::::9
 F:::::F       FFFFFF  R::::R     R:::::R  E:::::E       EEEEEE  I::::I G:::::G       GGGGGG  H:::::H     H:::::H  TTTTTT  T:::::T  TTTTTT9:::::9     9:::::9
 F:::::F               R::::R     R:::::R  E:::::E               I::::IG:::::G                H:::::H     H:::::H          T:::::T        9:::::9     9:::::9
 F::::::FFFFFFFFFF     R::::RRRRRR:::::R   E::::::EEEEEEEEEE     I::::IG:::::G                H::::::HHHHH::::::H          T:::::T         9:::::99999::::::9
 F:::::::::::::::F     R:::::::::::::RR    E:::::::::::::::E     I::::IG:::::G    GGGGGGGGGG  H:::::::::::::::::H          T:::::T          99::::::::::::::9
 F:::::::::::::::F     R::::RRRRRR:::::R   E:::::::::::::::E     I::::IG:::::G    G::::::::G  H:::::::::::::::::H          T:::::T            99999::::::::9
 F::::::FFFFFFFFFF     R::::R     R:::::R  E::::::EEEEEEEEEE     I::::IG:::::G    GGGGG::::G  H::::::HHHHH::::::H          T:::::T                 9::::::9
 F:::::F               R::::R     R:::::R  E:::::E               I::::IG:::::G        G::::G  H:::::H     H:::::H          T:::::T                9::::::9
 F:::::F               R::::R     R:::::R  E:::::E       EEEEEE  I::::I G:::::G       G::::G  H:::::H     H:::::H          T:::::T               9::::::9
FF:::::::FF           RR:::::R     R:::::REE::::::EEEEEEEE:::::EII::::::IIG:::::GGGGGGGG::::GHH::::::H     H::::::HH      TT:::::::TT            9::::::9
F::::::::FF           R::::::R     R:::::RE::::::::::::::::::::EI::::::::I GG:::::::::::::::GH:::::::H     H:::::::H      T:::::::::T           9::::::9
F::::::::FF           R::::::R     R:::::RE::::::::::::::::::::EI::::::::I   GGG::::::GGG:::GH:::::::H     H:::::::H      T:::::::::T          9::::::9
FFFFFFFFFFF           RRRRRRRR     RRRRRRREEEEEEEEEEEEEEEEEEEEEEIIIIIIIIII      GGGGGG   GGGGHHHHHHHHH     HHHHHHHHH      TTTTTTTTTTT         99999999

"""
        tv_desc_container.text = ""     //container full/empty : containerFullEmptyIndicator
        tv_bldraft_no.text = ""
        tv_bldraft_desc.text = ""
        tv_packages.text = ""

    }
    private fun makeAddress(info: PartyInfo): String {
        var address = ""

        info?.partyPostalCode?.let {  address += info?.partyPostalCode}
        info?.partyAddress1?.let { address += ", ${info?.partyAddress1}"}
        info?.partyAddress2?.let { address += ", ${info?.partyAddress2}"}
        info?.partyContactTelephone?.let { address += "\n ${info?.partyContactTelephone}"}

        return if(address.isNullOrEmpty()) "-" else address
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel, bookingItem: BookingDashboardItem) : BookingDashboardConditionDetailFragment {
            return BookingDashboardConditionDetailFragment(viewModel, bookingItem)
        }
    }
}