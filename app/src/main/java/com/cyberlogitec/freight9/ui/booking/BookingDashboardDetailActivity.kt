package com.cyberlogitec.freight9.ui.booking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboardItem
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingDetailActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_booking_dashboard_detail.*
import kotlinx.android.synthetic.main.appbar_dashboard.*
import kotlinx.android.synthetic.main.body_booking_dashboard_detail.*
import kotlinx.android.synthetic.main.item_booking_dashboard.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

@RequiresActivityViewModel(value = BookingDashboardDetailViewModel::class)
class BookingDashboardDetailActivity : BaseActivity<BookingDashboardDetailViewModel>() {

    private var popupWindow: PopupWindow? = null

    lateinit var fragment: BookingDashboardConditionDetailFragment
    lateinit var fragmentVgm: BookingDashboardVgmFragment
    lateinit var fragmentInvoice: BookingDashboardBlInvoiceFragment
    private lateinit var bookingItem: BookingDashboardItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_booking_dashboard_detail)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
        initFragment()
        updateUi()
    }

    private fun initFragment() {
        fragment = BookingDashboardConditionDetailFragment(viewModel, bookingItem)
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_detail_body, fragment)
                .commit()
        fragment.setBookingItem(bookingItem)

        fragmentVgm = BookingDashboardVgmFragment(viewModel, bookingItem)
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_detail_body_vgm, fragmentVgm)
                .commit()
        fragmentVgm.setData(bookingItem)

        fragmentInvoice = BookingDashboardBlInvoiceFragment(viewModel, bookingItem)
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_detail_body_invoice, fragmentInvoice)
                .commit()
        fragmentInvoice.setData(bookingItem)

    }

    override fun onDestroy() {


        super.onDestroy()
    }

    override fun onBackPressed() {
        if (popupWindow != null) {
            removePopup()
        }else if(ll_detail_holder.visibility == View.VISIBLE){
            ll_detail_holder.visibility = View.INVISIBLE

        } else {
            super.onBackPressed()
        }
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    private fun setRxOutputs() {
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startActivity(MenuActivity::class.java)
                }
    }

    private fun initData() {

        bookingItem = intent.extras["bookingitem"] as BookingDashboardItem

    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        defaultbarInit(toolbar_booking_dashboard, menuType = MenuType.CROSS, title = getString(R.string.booking_dashboard_your_booking),isEnableNavi = false)

        // TODO : Request Something

        setListener()
    }

    private fun setListener() {
        toolbar_booking_dashboard.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        rl_cargo_tracking.setSafeOnClickListener {
            callCargoTracking()
        }

        iv_expand.setSafeOnClickListener {
            if(ll_expand.visibility == View.VISIBLE) {
                ll_expand.visibility = View.GONE
                iv_expand.setImageDrawable(getDrawable(R.drawable.btn_expand_default_l))
            }
            else {
                ll_expand.visibility = View.VISIBLE
                iv_expand.setImageDrawable(getDrawable(R.drawable.btn_collapse_default_l))
            }
        }
        iv_arror_back.setSafeOnClickListener { ll_detail_holder.visibility = View.INVISIBLE }
        iv_condition_details.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.booking_dashboard_condition_details)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.VISIBLE
            ll_detail_body_vgm.visibility = View.GONE
            ll_detail_body_invoice.visibility = View.GONE
            fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.TRACKING)
            fragment.updateUi()
        }
        iv_vessel_schedule.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.booking_dashboard_vessel_schedule)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.VISIBLE
            ll_detail_body_vgm.visibility = View.GONE
            ll_detail_body_invoice.visibility = View.GONE
            fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.VESSEL_SCHEDULE)
            fragment.updateUi()
        }
        iv_charge.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.booking_dashboard_charge)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.VISIBLE
            ll_detail_body_vgm.visibility = View.GONE
            ll_detail_body_invoice.visibility = View.GONE
            fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.CHARGE)
            fragment.updateUi()
        }
        iv_parties.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.booking_dashboard_parties)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.VISIBLE
            ll_detail_body_vgm.visibility = View.GONE
            ll_detail_body_invoice.visibility = View.GONE
            fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.PARTIES)
            fragment.updateUi()
        }
        rl_si_doc.setSafeOnClickListener {
            startActivity(Intent(this, BookingDashboardSiActivity::class.java).putExtra("bookingitem", bookingItem))
        }
        rl_vgm.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.booking_dashboard_vgm)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.GONE
            ll_detail_body_vgm.visibility = View.VISIBLE
            ll_detail_body_invoice.visibility = View.GONE
        }
        rl_bl_invoice.setSafeOnClickListener {
            tv_header_title.text = getString(R.string.menu_finance_invoice)
            ll_detail_holder.visibility = View.VISIBLE
            ll_detail_body.visibility = View.GONE
            ll_detail_body_vgm.visibility = View.GONE
            ll_detail_body_invoice.visibility = View.VISIBLE
        }

    }

    // TODO : Call cargo tracking activity (set dummy data)
    private fun callCargoTracking() {
        startActivity(Intent(this, CargoTrackingDetailActivity::class.java)
                .putExtra(Intents.CARGO_TRACKING_ENTRY_OTHER, true)
                .putExtra(Intents.CARGO_TRACKING_DATA, ArrayList(getCargoTrackingDetailData())))
    }

    private fun updateUi() {

        tv_booking_no_title.text = bookingItem?.booking?.messageNumber

        val party = bookingItem.party
        if(party != null) {
            tv_carrier_name.text = getCarrierCodeToF9(bookingItem.party.carrierInfo.partyInfo.partyCode)
            iv_carrier_logo.setImageResource(getCarrierCodeToF9(bookingItem.party.carrierInfo.partyInfo.partyCode).getCarrierIcon())
        }

        bookingItem.transport?.let {
            val transport = bookingItem.transport.firstOrNull()
            tv_vvd.text = transport?.vesselName
            tv_pol_cd.text = transport?.transportLocation?.portOfLoadingLocationCode
            tv_pol_name.text = transport?.transportLocation?.portOfLoadingLocationName

            tv_pod_cd.text = transport?.transportLocation?.portOfDischargeLocationCode
            tv_pod_name.text = transport?.transportLocation?.portOfDischargeLocationName

            val df: DateFormat = SimpleDateFormat("yy-MM-dd HH:mm")

            runCatching { tv_etd_time.text = " '${df.format(transport?.transportDate?.estimatedDepartureDate?.toDate("yyyyMMdd"))}" }
                    .onFailure { tv_etd_time.text = " -" }
            runCatching { tv_eta_time.text = " '${df.format(transport?.transportDate?.estimatedArrivalDate?.toDate("yyyyMMdd"))}" }
                    .onFailure { tv_eta_time.text = " -" }

        }

        tv_booking_no.text = bookingItem?.booking?.messageNumber ?: "-"
        tv_bl_no.text = bookingItem?.booking?.bookingReferenceNo?.billOfLadingNo ?: "-"
        val container = bookingItem?.container.firstOrNull()
        var containerCount = 0
        containerCount = bookingItem?.container.size
        if(containerCount > 0)
            tv_container_no.text = "${container?.containerNo} ${if(containerCount > 1)"+${containerCount -1}" else ""}"
        else
            tv_container_no.text = "-"

        tv_consignee.text = bookingItem.party?.consigneeInfo?.partyInfo?.partyName ?: "-"

        tv_shipper.text = bookingItem.party?.consignorInfo?.partyInfo?.partyName ?: "-"

        //cutoff time
        // si &doc
        // eta -48hour
        var sidocDate = "20201022"          // test code
        calDateCutoff(sidocDate, v_sidoc_status, v_sidoc_title, v_sidoc_date)

        //b/l draft

        var blDraft = "20201023"          // test code
        calDateCutoff(blDraft, v_bl_status, v_bl_title, v_bl_date)

        //vgm

        var vgmDraft = "20201024"          // test code
        calDateCutoff(vgmDraft, v_vgm_status, v_vgm_title, v_vgm_date)

        //advanced manifest
        var advmani = "20201025"          // test code
        calDateCutoff(advmani, v_advance_status, v_advance_title, v_advance_date)

        //-----

    }

    private fun calDateCutoff(date: String, vStatus: View, vTitle: TextView, vDate: TextView) {
        var diff: Long = -1
        val format = SimpleDateFormat("yyyyMMdd")

        vDate.text = "${date.substring(2,4)}-${date.substring(4,6)}-${date.substring(6,8)} 12:00:00"

        val oriDate =  format.parse(date)
        if(oriDate.compareTo(Calendar.getInstance().time) == -1){
            diff = -1
        }else{

            val calDate = oriDate.time - Calendar.getInstance().time.time
            diff = abs(calDate / (60*60*1000))
        }

        when {
            diff <= 0 -> {
                vStatus.background = getDrawable(R.drawable.bg_circle_8_f0f0f0)
                vTitle.setTextColor(getColor(R.color.color_bfbfbf))
                vDate.setTextColor(getColor(R.color.color_bfbfbf))
            }
            diff < 24 -> {
                vStatus.background = getDrawable(R.drawable.bg_circle_8_ff4444)
                vTitle.setTextColor(getColor(R.color.color_4c4c4c))
                vDate.setTextColor(getColor(R.color.color_4c4c4c))
            }
            diff <= 72 -> {
                vStatus.background = getDrawable(R.drawable.bg_circle_8_ffa241)
                vTitle.setTextColor(getColor(R.color.color_4c4c4c))
                vDate.setTextColor(getColor(R.color.color_4c4c4c))
            }
            else -> {
                vStatus.background = getDrawable(R.drawable.bg_circle_8_3bd090)
                vTitle.setTextColor(getColor(R.color.color_4c4c4c))
                vDate.setTextColor(getColor(R.color.color_4c4c4c))
            }
        }
    }

}
