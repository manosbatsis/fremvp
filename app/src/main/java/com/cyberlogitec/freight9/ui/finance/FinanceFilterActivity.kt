package com.cyberlogitec.freight9.ui.finance

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.FilterDateType.FILTER_DATE_END
import com.cyberlogitec.freight9.config.FilterDateType.FILTER_DATE_START
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_DEAL
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_DUE
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_POD_ETA
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_POL_ETD
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod.FILTER_DATE_PERIOD_1M
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod.FILTER_DATE_PERIOD_3M
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod.FILTER_DATE_PERIOD_6M
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod.FILTER_DATE_PERIOD_CUSTOM
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_CASES
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_DATE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_INVOICE_TRANSACTION_TYPE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_ROUTE
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_STATUS
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_TRANSACTION_TYPE
import com.cyberlogitec.freight9.config.FinanceFilterSpinType.FILTER_SPIN_POD
import com.cyberlogitec.freight9.config.FinanceFilterSpinType.FILTER_SPIN_POL
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_INVOICE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.lib.model.finance.FinanceFilter
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.datepicker.date.DatePickerDialogFragment
import com.cyberlogitec.freight9.lib.ui.enums.FinanceSearchTypeFilter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getEngShortMonth
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_finance_filter.btn_apply
import kotlinx.android.synthetic.main.body_finance_filter.*
import kotlinx.android.synthetic.main.body_finance_filter.iv_route_pod
import kotlinx.android.synthetic.main.body_finance_filter.iv_route_pol
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_perioa_1m
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_period_3m
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_period_6m
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_period_custom
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_period_end
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_period_start
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_pod_eta
import kotlinx.android.synthetic.main.body_finance_filter.tv_date_pol_etd
import kotlinx.android.synthetic.main.body_finance_filter.tv_route_pod_selected
import kotlinx.android.synthetic.main.body_finance_filter.tv_route_podname_selected
import kotlinx.android.synthetic.main.body_finance_filter.tv_route_pol_selected
import kotlinx.android.synthetic.main.body_finance_filter.tv_route_polname_selected
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.joda.time.DateTime
import timber.log.Timber
import java.util.*


@RequiresActivityViewModel(value = FinanceFilterViewModel::class)
class FinanceFilterActivity : BaseActivity<FinanceFilterViewModel>() {

    private var financeSearchTypeFilter: FinanceSearchTypeFilter = FinanceSearchTypeFilter()
    private lateinit var tfRegular: Typeface
    private lateinit var tfExtraBold: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_finance_filter)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initData() {
        ResourcesCompat.getFont(this, R.font.opensans_regular)?.let{ tfRegular = it }
        ResourcesCompat.getFont(this, R.font.opensans_extrabold)?.let { tfExtraBold = it }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                title = getString(R.string.finance_filters),
                isEnableNavi = false)

        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        financeSearchTypeFilter = intent.getSerializableExtra(Intents.FINANCE_FILTER)
                                as FinanceSearchTypeFilter
                        Handler().postDelayed({
                            var yPosition = 0
                            when(financeSearchTypeFilter.financeFilters.scrollType) {
                                FILTER_SCROLL_TO_TOP,
                                FILTER_SCROLL_TO_STATUS,
                                FILTER_SCROLL_TO_TRANSACTION_TYPE,
                                FILTER_SCROLL_TO_INVOICE_TRANSACTION_TYPE -> {
                                    yPosition = 0
                                }
                                FILTER_SCROLL_TO_CASES -> {
                                    yPosition = ll_filter_transaction_statement_cases.top
                                }
                                FILTER_SCROLL_TO_ROUTE -> {
                                    yPosition = ll_filter_route.top
                                }
                                FILTER_SCROLL_TO_DATE -> {
                                    yPosition = ll_filter_date.top
                                }
                                FILTER_SCROLL_TO_COLLECT_PLAN -> {
                                    yPosition = ll_filter_pay_collect_plan.top
                                }
                            }
                            sv_finance_filter_root.smoothScrollTo(0, yPosition)

                        },100)
                        setUi()
                    }
                }

        viewModel.outPuts.onClickToClose()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        onBackPressed()
                    }
                }

        viewModel.outPuts.onClickToSpinDialog()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { spinType ->
                        showSpinPolPodDialog(spinType)
                    }
                }

        viewModel.outPuts.onClickToSpinCalendarDialog()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { spinType ->
                        // show when only Custom
                        if (financeSearchTypeFilter.financeFilters.datePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
                            showSpinCalendarDialog(spinType)
                        }
                    }
                }

        viewModel.outPuts.onClickToDateType()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { dateType ->
                        setClickFilterDateType(dateType)
                    }
                }

        viewModel.outPuts.onClickToDatePeriodType()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { datePeriodType ->
                        setClickFilterDatePeriodType(datePeriodType)
                    }
                }

        viewModel.outPuts.onClickToApply()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        val intent = Intent()
                        intent.putExtra(Intents.FINANCE_FILTER, getFilterData())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

        viewModel.outPuts.onClickToSwitch()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { type ->
                        when(type) {
                            FINANCE_TYPE_PAY_COLLECT_PLAN -> {
                                setAllPayCollectStatus()
                                setAllPayCollectPlan()
                            }
                            FINANCE_TYPE_TRANSACTION_STATEMENT -> {
                                setAllTransactionStatementType()
                                setAllTransactionStatementCase()
                            }
                            else -> {
                                setAllInvoiceTransactionType()
                            }
                        }
                        setApplyButton(financeSearchTypeFilter.typeCode)
                    }
                }

        viewModel.outPuts.onClickToStatusClearSelectAll()
                .bindToLifecycle(this)
                .subscribe {
                    processStatusClearSelectAll()
                }

        viewModel.outPuts.onClickToTypeClearSelectAll()
                .bindToLifecycle(this)
                .subscribe {
                    processTypeClearSelectAll()
                }

        viewModel.outPuts.onClickToCasesClearSelectAll()
                .bindToLifecycle(this)
                .subscribe {
                    processCasesClearSelectAll()
                }

        viewModel.outPuts.onClickToInvoiceTypeClearSelectAll()
                .bindToLifecycle(this)
                .subscribe {
                    processInvoiceTypeClearSelectAll()
                }

        viewModel.outPuts.onClickToCollectPlanClearSelectAll()
                .bindToLifecycle(this)
                .subscribe {
                    processCollectPlanClearSelectAll()
                }
    }

    private fun setListener() {

        // title "X" button
        toolbar_right_btn.setSafeOnClickListener {
            viewModel.inPuts.clickToClose(Parameter.CLICK)
        }

        // APPLY button
        btn_apply.setSafeOnClickListener {
            viewModel.inPuts.clickToApply(Parameter.CLICK)
        }

        /*
         * ROUTE
         */
        // Route : POL - Spin Control (ALL POLs item 포함)
        iv_route_pol.setSafeOnClickListener {
            viewModel.inPuts.clickToSpinDialog(FILTER_SPIN_POL)
        }

        // Route : POD - Spin Control (ALL PODs item 포함)
        iv_route_pod.setSafeOnClickListener {
            viewModel.inPuts.clickToSpinDialog(FILTER_SPIN_POD)
        }

        /*
         * DATE
         */
        // Date : Deal
        tv_date_deal.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_DEAL)
        }

        tv_date_due.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_DUE)
        }

        // Date : POL ETD
        tv_date_pol_etd.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_POL_ETD)
        }

        // Date : POD ETA
        tv_date_pod_eta.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_POD_ETA)
        }

        // Date : 1M
        tv_date_perioa_1m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_1M)
        }

        // Date : 3M
        tv_date_period_3m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_3M)
        }

        // Date : 6M
        tv_date_period_6m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_6M)
        }

        // Date : Custom
        tv_date_period_custom.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_CUSTOM)
        }

        // Date : Start
        tv_date_period_start.setOnClickListener {
            viewModel.inPuts.clickToSpinCalendarDialog(FILTER_DATE_START)
        }

        // Date : End
        tv_date_period_end.setOnClickListener {
            viewModel.inPuts.clickToSpinCalendarDialog(FILTER_DATE_END)
        }

        // Clear <-> Select
        tv_status_clear_select_all.setOnClickListener {
            viewModel.inPuts.clickToStatusClearSelectAll(Parameter.CLICK)
        }

        tv_transaction_type_clear_select_all.setOnClickListener {
            viewModel.inPuts.clickToTypeClearSelectAll(Parameter.CLICK)
        }

        tv_transaction_cases_clear_select_all.setOnClickListener {
            viewModel.inPuts.clickToCasesClearSelectAll(Parameter.CLICK)
        }

        tv_invoice_transaction_type_clear_select_all.setOnClickListener {
            viewModel.inPuts.clickToInvoiceTypeClearSelectAll(Parameter.CLICK)
        }

        tv_pay_collect_plan_clear_select_all.setOnClickListener {
            viewModel.inPuts.clickToCollectPlanClearSelectAll(Parameter.CLICK)
        }

        /* Switch check */
        // Collect Plan Status
        switch_status_collected.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_PAY_COLLECT_PLAN)
        }
        switch_status_uncollected.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_PAY_COLLECT_PLAN)
        }
        switch_status_collected_unconfirmed.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_PAY_COLLECT_PLAN)
        }

        // Collect Plan
        switch_pay_collect_plan_prepaid.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_PAY_COLLECT_PLAN)
        }
        switch_pay_collect_plan_collect.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_PAY_COLLECT_PLAN)
        }

        // Transaction Type
        switch_transaction_statement_type_init.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }
        switch_transaction_statement_type_mid.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }
        switch_transaction_statement_type_remain.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }

        // Cases
        switch_transaction_statement_case_amount_in.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }
        switch_transaction_statement_case_amount_out.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }
        switch_transaction_statement_case_space_in.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }
        switch_transaction_statement_case_space_out.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_TRANSACTION_STATEMENT)
        }

        // Invoice Type
        switch_invoice_transaction_type_unpaid.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_INVOICE)
        }
        switch_invoice_transaction_type_unconfirmed.setOnCheckedChangeListener { _, _ ->
            viewModel.inPuts.clickToSwitch(FINANCE_TYPE_INVOICE)
        }
    }

    private fun setUi() {
        setLayoutPerTypeFilter()
        setPayCollectStatus()
        setTransactionStatementType()
        setTransactionStatementCase()
        setInvoiceTransactionType()
        setPayCollectPlan()
        setRoute()
        setDate()
    }

    private fun setLayoutPerTypeFilter(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        when(typeFilter.typeCode) {
            FINANCE_TYPE_PAY_COLLECT_PLAN -> {
                ll_filter_pay_collect_status.visibility = View.VISIBLE
                ll_filter_transaction_statement_transaction_type.visibility = View.GONE
                ll_filter_transaction_statement_cases.visibility = View.GONE
                ll_filter_invoice_transaction_type.visibility = View.GONE
                ll_filter_route.visibility = View.VISIBLE
                ll_filter_date.visibility = View.VISIBLE
                ll_filter_pay_collect_plan.visibility = View.VISIBLE
            }
            FINANCE_TYPE_TRANSACTION_STATEMENT -> {
                ll_filter_pay_collect_status.visibility = View.GONE
                ll_filter_transaction_statement_transaction_type.visibility = View.VISIBLE
                ll_filter_transaction_statement_cases.visibility = View.VISIBLE
                ll_filter_invoice_transaction_type.visibility = View.GONE
                ll_filter_route.visibility = View.GONE
                ll_filter_date.visibility = View.VISIBLE
                ll_filter_pay_collect_plan.visibility = View.GONE
            }
            else -> {
                ll_filter_pay_collect_status.visibility = View.GONE
                ll_filter_transaction_statement_transaction_type.visibility = View.GONE
                ll_filter_transaction_statement_cases.visibility = View.GONE
                ll_filter_invoice_transaction_type.visibility = View.VISIBLE
                ll_filter_route.visibility = View.GONE
                ll_filter_date.visibility = View.VISIBLE
                ll_filter_pay_collect_plan.visibility = View.GONE
            }
        }
    }

    // Pay/Collect Plan
    private fun setPayCollectStatus(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            switch_status_collected.isChecked = payCollectPlanStatus.isCollected
            switch_status_uncollected.isChecked = payCollectPlanStatus.isUncollected
            switch_status_collected_unconfirmed.isChecked = payCollectPlanStatus.isCollectedUnconfirmed
        }
        setAllPayCollectStatus()
        setApplyButton(typeFilter.typeCode)
    }

    // Transaction Statement
    private fun setTransactionStatementType(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            switch_transaction_statement_type_init.isChecked = transactionStatementType.isInitialPayment
            switch_transaction_statement_type_mid.isChecked = transactionStatementType.isMidTermPayment
            switch_transaction_statement_type_remain.isChecked = transactionStatementType.isRemainderPayment
        }
        setAllTransactionStatementType()
        setApplyButton(typeFilter.typeCode)
    }

    // Transaction Statement
    private fun setTransactionStatementCase(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            switch_transaction_statement_case_amount_in.isChecked = transactionCases.isAmountIn
            switch_transaction_statement_case_amount_out.isChecked = transactionCases.isAmountOut
            switch_transaction_statement_case_space_in.isChecked = transactionCases.isSpaceIn
            switch_transaction_statement_case_space_out.isChecked = transactionCases.isSpaceOut
        }
        setAllTransactionStatementCase()
        setApplyButton(typeFilter.typeCode)
    }

    // Invoice
    private fun setInvoiceTransactionType(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            switch_invoice_transaction_type_unpaid.isChecked = transactionInvoiceType.isUnPaid
            switch_invoice_transaction_type_unconfirmed.isChecked = transactionInvoiceType.isUnconfirmed
        }
        setAllInvoiceTransactionType()
        setApplyButton(typeFilter.typeCode)
    }

    // Pay/Collect Plan
    private fun setPayCollectPlan(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            switch_pay_collect_plan_prepaid.isChecked = collectPlan.isPrepaid
            switch_pay_collect_plan_collect.isChecked = collectPlan.isCollect
        }
        setAllPayCollectPlan()
        setApplyButton(typeFilter.typeCode)
    }

    // Pay/Collect Plan
    private fun setRoute(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        with(typeFilter.financeFilters) {
            val polCode = if (routePolCode.isEmpty()) {
                getString(R.string.finance_filters_route_pol_all)
            } else {
                routePolCode
            }
            tv_route_pol_selected.text = polCode
            if (routePolList.isNotEmpty()) {
                tv_route_polname_selected.text = routePolList.first { it.code == polCode }.name
            }

            val podCode = if (routePodCode.isEmpty()) {
                getString(R.string.finance_filters_route_pod_all)
            } else {
                routePodCode
            }
            tv_route_pod_selected.text = podCode
            if (routePodList.isNotEmpty()) {
                tv_route_podname_selected.text = routePodList.first { it.code == podCode }.name
            }
        }
    }

    // Pay/Collect Plan, Transaction Statement, Invoice
    private fun setDate(typeFilter: FinanceSearchTypeFilter = financeSearchTypeFilter) {
        setClickFilterDateType(financeSearchTypeFilter.financeFilters.dateType)

        val calendar = Calendar.getInstance()
        with(typeFilter.financeFilters) {
            var yearStarts= dateStarts.year
            var monthStarts = dateStarts.month
            var dayStarts = dateStarts.day
            if (yearStarts < 1 || monthStarts < 1 || dayStarts < 1) {
                yearStarts = calendar.get(Calendar.YEAR)
                monthStarts = calendar.get(Calendar.MONTH) + 1
                dayStarts = calendar.get(Calendar.DAY_OF_MONTH)
                dateStarts.year = yearStarts
                dateStarts.month = monthStarts
                dateStarts.day = dayStarts
            }
            tv_date_period_start.text = "${getEngShortMonth(monthStarts)} $dayStarts, $yearStarts"

            var yearEnds = dateEnds.year
            var monthEnds = dateEnds.month
            var dayEnds = dateEnds.day
            if (yearEnds < 1 || monthEnds < 1 || dayEnds < 1) {
                yearEnds = calendar.get(Calendar.YEAR)
                monthEnds = calendar.get(Calendar.MONTH) + 1
                dayEnds = calendar.get(Calendar.DAY_OF_MONTH)
                dateEnds.year = yearEnds
                dateEnds.month = monthEnds
                dateEnds.day = dayEnds
            }
            tv_date_period_end.text = "${getEngShortMonth(monthEnds)} $dayEnds, $yearEnds"
        }
        setClickFilterDatePeriodType(typeFilter.financeFilters.datePeriodType)
    }

    private fun getFilterData(): FinanceSearchTypeFilter {
        with(financeSearchTypeFilter.financeFilters) {

            getSwitchValue()

            val selectedPolCode = if (routePolList.isNotEmpty()) {
                routePolList.first { it.isSelected }.code
            } else {
                EmptyString
            }
            routePolCode = if (selectedPolCode == getString(R.string.finance_filters_route_pol_all)
                    || selectedPolCode.isEmpty()) {
                EmptyString
            } else {
                selectedPolCode
            }

            val selectedPodCode = if (routePodList.isNotEmpty()) {
                routePodList.first { it.isSelected }.code
            } else {
                EmptyString
            }
            routePodCode = if (selectedPodCode == getString(R.string.finance_filters_route_pod_all)
                    || selectedPodCode.isEmpty()) {
                EmptyString
            } else {
                selectedPodCode
            }
        }
        return financeSearchTypeFilter
    }

    private fun getSwitchValue() {
        getPayCollectStatus()
        getTransactionStatementType()
        getTransactionStatementCase()
        getInvoiceTransactionType()
        getPayCollectPlan()
    }

    private fun getPayCollectStatus() {
        with(financeSearchTypeFilter.financeFilters.payCollectPlanStatus) {
            isCollected = switch_status_collected.isChecked
            isUncollected = switch_status_uncollected.isChecked
            isCollectedUnconfirmed = switch_status_collected_unconfirmed.isChecked
        }
    }

    private fun getTransactionStatementType() {
        with(financeSearchTypeFilter.financeFilters.transactionStatementType) {
            isInitialPayment = switch_transaction_statement_type_init.isChecked
            isMidTermPayment = switch_transaction_statement_type_mid.isChecked
            isRemainderPayment = switch_transaction_statement_type_remain.isChecked
        }
    }

    private fun getTransactionStatementCase() {
        with(financeSearchTypeFilter.financeFilters.transactionCases) {
            isAmountIn = switch_transaction_statement_case_amount_in.isChecked
            isAmountOut = switch_transaction_statement_case_amount_out.isChecked
            isSpaceIn = switch_transaction_statement_case_space_in.isChecked
            isSpaceOut = switch_transaction_statement_case_space_out.isChecked
        }
    }

    private fun getInvoiceTransactionType() {
        with(financeSearchTypeFilter.financeFilters.transactionInvoiceType) {
            isUnPaid = switch_invoice_transaction_type_unpaid.isChecked
            isUnconfirmed = switch_invoice_transaction_type_unconfirmed.isChecked
        }
    }

    private fun getPayCollectPlan() {
        with(financeSearchTypeFilter.financeFilters.collectPlan) {
            isPrepaid = switch_pay_collect_plan_prepaid.isChecked
            isCollect = switch_pay_collect_plan_collect.isChecked
        }
    }

    private fun setAllPayCollectStatus() {
        val linkText = if (isUnCheckedAllStatusSwitch()) {
            getString(R.string.finance_filters_select_all)
        } else {
            getString(R.string.finance_filters_clear_all)
        }

        val content = SpannableString(linkText)
        content.setSpan(UnderlineSpan(), 0, linkText.length, 0)
        tv_status_clear_select_all.text = content
    }

    private fun setAllTransactionStatementType() {
        val linkText = if (isUnCheckedAllTypeSwitch()) {
            getString(R.string.finance_filters_select_all)
        } else {
            getString(R.string.finance_filters_clear_all)
        }

        val content = SpannableString(linkText)
        content.setSpan(UnderlineSpan(), 0, linkText.length, 0)
        tv_transaction_type_clear_select_all.text = content
    }

    private fun setAllTransactionStatementCase() {
        val linkText = if (isUnCheckedAllCasesSwitch()) {
            getString(R.string.finance_filters_select_all)
        } else {
            getString(R.string.finance_filters_clear_all)
        }

        val content = SpannableString(linkText)
        content.setSpan(UnderlineSpan(), 0, linkText.length, 0)
        tv_transaction_cases_clear_select_all.text = content
    }

    private fun setAllInvoiceTransactionType() {
        val linkText = if (isUnCheckedAllInvoiceTypeSwitch()) {
            getString(R.string.finance_filters_select_all)
        } else {
            getString(R.string.finance_filters_clear_all)
        }

        val content = SpannableString(linkText)
        content.setSpan(UnderlineSpan(), 0, linkText.length, 0)
        tv_invoice_transaction_type_clear_select_all.text = content
    }

    private fun setAllPayCollectPlan() {
        val linkText = if (isUnCheckedAllCollectPlanSwitch()) {
            getString(R.string.finance_filters_select_all)
        } else {
            getString(R.string.finance_filters_clear_all)
        }

        val content = SpannableString(linkText)
        content.setSpan(UnderlineSpan(), 0, linkText.length, 0)
        tv_pay_collect_plan_clear_select_all.text = content
    }

    private fun setApplyButton(typeCode: String) {
        btn_apply.isEnabled = when(typeCode) {
            FINANCE_TYPE_PAY_COLLECT_PLAN -> !((isUnCheckedAllStatusSwitch() && isUnCheckedAllCollectPlanSwitch())
                        || (isUnCheckedAllStatusSwitch() && !isUnCheckedAllCollectPlanSwitch())
                        || (!isUnCheckedAllStatusSwitch() && isUnCheckedAllCollectPlanSwitch()))
            FINANCE_TYPE_TRANSACTION_STATEMENT -> !((isUnCheckedAllTypeSwitch() && isUnCheckedAllCasesSwitch())
                    || (isUnCheckedAllTypeSwitch() && !isUnCheckedAllCasesSwitch())
                    || (!isUnCheckedAllTypeSwitch() && isUnCheckedAllCasesSwitch()))
            else -> !isUnCheckedAllInvoiceTypeSwitch()
        }
    }

    private fun setClickFilterDateType(filterDateType: Int = FILTER_DATE_DEAL) {
        val normalTextColor = getColor(R.color.greyish_brown)
        val selectedTextColor = getColor(R.color.white)
        val normalTextTypeface = tfRegular
        val selectedTextTypeface = tfExtraBold

        tv_date_deal.typeface = if (filterDateType == FILTER_DATE_DEAL) selectedTextTypeface else normalTextTypeface
        tv_date_deal.setTextColor(if (filterDateType == FILTER_DATE_DEAL) selectedTextColor else normalTextColor)
        if (filterDateType == FILTER_DATE_DEAL) {
            tv_date_deal.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_left_top_bottom)
        } else {
            tv_date_deal.setBackgroundColor(getColor(R.color.white))
        }

        tv_date_due.typeface = if (filterDateType == FILTER_DATE_DUE) selectedTextTypeface else normalTextTypeface
        tv_date_due.setTextColor(if (filterDateType == FILTER_DATE_DUE) selectedTextColor else normalTextColor)
        tv_date_due.setBackgroundColor(getColor(if (filterDateType == FILTER_DATE_DUE) R.color.greyish_brown else R.color.white))

        tv_date_pol_etd.typeface = if (filterDateType == FILTER_DATE_POL_ETD) selectedTextTypeface else normalTextTypeface
        tv_date_pol_etd.setTextColor(if (filterDateType == FILTER_DATE_POL_ETD) selectedTextColor else normalTextColor)
        tv_date_pol_etd.setBackgroundColor(getColor(if (filterDateType == FILTER_DATE_POL_ETD) R.color.greyish_brown else R.color.white))

        tv_date_pod_eta.typeface = if (filterDateType == FILTER_DATE_POD_ETA) selectedTextTypeface else normalTextTypeface
        tv_date_pod_eta.setTextColor(if (filterDateType == FILTER_DATE_POD_ETA) selectedTextColor else normalTextColor)
        if (filterDateType == FILTER_DATE_POD_ETA) {
            tv_date_pod_eta.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_right_top_bottom)
        } else {
            tv_date_pod_eta.setBackgroundColor(getColor(R.color.white))
        }

        financeSearchTypeFilter.financeFilters.dateType = filterDateType
    }

    private fun setClickFilterDatePeriodType(filterDatePeriodType: Int = FILTER_DATE_PERIOD_CUSTOM) {
        val normalTextColor = getColor(R.color.greyish_brown)
        val selectedTextColor = getColor(R.color.white)
        val normalTextTypeface = tfRegular
        val selectedTextTypeface = tfExtraBold

        tv_date_perioa_1m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) selectedTextTypeface else normalTextTypeface
        tv_date_perioa_1m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) selectedTextColor else normalTextColor)
        if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) {
            tv_date_perioa_1m.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_left_top)
        } else {
            tv_date_perioa_1m.setBackgroundColor(getColor(R.color.white))
        }

        tv_date_period_3m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) selectedTextTypeface else normalTextTypeface
        tv_date_period_3m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) selectedTextColor else normalTextColor)
        tv_date_period_3m.setBackgroundColor(getColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) R.color.greyish_brown else R.color.white))

        tv_date_period_6m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) selectedTextTypeface else normalTextTypeface
        tv_date_period_6m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) selectedTextColor else normalTextColor)
        tv_date_period_6m.setBackgroundColor(getColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) R.color.greyish_brown else R.color.white))

        tv_date_period_custom.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) selectedTextTypeface else normalTextTypeface
        tv_date_period_custom.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) selectedTextColor else normalTextColor)
        if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
            tv_date_period_custom.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_right_top)
        } else {
            tv_date_period_custom.setBackgroundColor(getColor(R.color.white))
        }

        financeSearchTypeFilter.financeFilters.datePeriodType = filterDatePeriodType
        setDatePeriodDuration(filterDatePeriodType)
    }

    private fun setDatePeriodDuration(filterDatePeriodType: Int) {
        // Set today ~ 1M, 3M, 6M and disable Picker
        with(financeSearchTypeFilter.financeFilters) {
            val startYear = dateStarts.year
            val startMonth = dateStarts.month
            val startDay = dateStarts.day

            val calendar = Calendar.getInstance()
            val startDateTime = DateTime(startYear, startMonth, startDay, 0, 0, 0, 0)
            calendar.time = startDateTime.toDate()

            when(filterDatePeriodType) {
                FILTER_DATE_PERIOD_1M -> {
                    calendar.add(Calendar.MONTH, 1)
                }
                FILTER_DATE_PERIOD_3M -> {
                    calendar.add(Calendar.MONTH, 3)
                }
                FILTER_DATE_PERIOD_6M -> {
                    calendar.add(Calendar.MONTH, 6)
                }
                else -> {  }
            }

            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH) + 1
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
                year = dateEnds.year
                month = dateEnds.month
                day = dateEnds.day
            }

            val selectedDotDate = "${getEngShortMonth(month)} $day, $year"
            tv_date_period_end.text = selectedDotDate
            dateEnds = FinanceFilter.Date(month, day, year)
        }
    }

    private fun setStatusSwitchCheck(isChecked: Boolean) {
        switch_status_collected.isChecked = isChecked
        switch_status_uncollected.isChecked = isChecked
        switch_status_collected_unconfirmed.isChecked = isChecked
    }

    private fun isUnCheckedAllStatusSwitch() = !switch_status_collected.isChecked &&
            !switch_status_uncollected.isChecked &&
            !switch_status_collected_unconfirmed.isChecked

    private fun setTypeSwitchCheck(isChecked: Boolean) {
        switch_transaction_statement_type_init.isChecked = isChecked
        switch_transaction_statement_type_mid.isChecked = isChecked
        switch_transaction_statement_type_remain.isChecked = isChecked
    }

    private fun isUnCheckedAllTypeSwitch() = !switch_transaction_statement_type_init.isChecked &&
            !switch_transaction_statement_type_mid.isChecked &&
            !switch_transaction_statement_type_remain.isChecked

    private fun setCasesSwitchCheck(isChecked: Boolean) {
        switch_transaction_statement_case_amount_in.isChecked = isChecked
        switch_transaction_statement_case_amount_out.isChecked = isChecked
        switch_transaction_statement_case_space_in.isChecked = isChecked
        switch_transaction_statement_case_space_out.isChecked = isChecked
    }

    private fun isUnCheckedAllCasesSwitch() = !switch_transaction_statement_case_amount_in.isChecked &&
            !switch_transaction_statement_case_amount_out.isChecked &&
            !switch_transaction_statement_case_space_in.isChecked &&
            !switch_transaction_statement_case_space_out.isChecked

    private fun setInvoiceTypeSwitchCheck(isChecked: Boolean) {
        switch_invoice_transaction_type_unpaid.isChecked = isChecked
        switch_invoice_transaction_type_unconfirmed.isChecked = isChecked
    }

    private fun isUnCheckedAllInvoiceTypeSwitch() = !switch_invoice_transaction_type_unpaid.isChecked &&
            !switch_invoice_transaction_type_unconfirmed.isChecked

    private fun setCollectPlanSwitchCheck(isChecked: Boolean) {
        switch_pay_collect_plan_prepaid.isChecked = isChecked
        switch_pay_collect_plan_collect.isChecked = isChecked
    }

    private fun isUnCheckedAllCollectPlanSwitch() = !switch_pay_collect_plan_prepaid.isChecked &&
            !switch_pay_collect_plan_collect.isChecked

    private fun processStatusClearSelectAll() {
        if (isUnCheckedAllStatusSwitch()) {
            // Select all
            setStatusSwitchCheck(true)
        } else {
            // Clear all
            setStatusSwitchCheck(false)
        }
        setAllPayCollectStatus()
        setApplyButton(financeSearchTypeFilter.typeCode)
    }

    private fun processTypeClearSelectAll() {
        if (isUnCheckedAllTypeSwitch()) {
            // Select all
            setTypeSwitchCheck(true)
        } else {
            // Clear all
            setTypeSwitchCheck(false)
        }
        setAllTransactionStatementType()
        setApplyButton(financeSearchTypeFilter.typeCode)
    }

    private fun processCasesClearSelectAll() {
        if (isUnCheckedAllCasesSwitch()) {
            // Select all
            setCasesSwitchCheck(true)
        } else {
            // Clear all
            setCasesSwitchCheck(false)
        }
        setAllTransactionStatementCase()
        setApplyButton(financeSearchTypeFilter.typeCode)
    }

    private fun processInvoiceTypeClearSelectAll() {
        if (isUnCheckedAllInvoiceTypeSwitch()) {
            // Select all
            setInvoiceTypeSwitchCheck(true)
        } else {
            // Clear all
            setInvoiceTypeSwitchCheck(false)
        }
        setAllInvoiceTransactionType()
        setApplyButton(financeSearchTypeFilter.typeCode)
    }

    private fun processCollectPlanClearSelectAll() {
        if (isUnCheckedAllCollectPlanSwitch()) {
            // Select all
            setCollectPlanSwitchCheck(true)
        } else {
            // Clear all
            setCollectPlanSwitchCheck(false)
        }
        setAllPayCollectPlan()
        setApplyButton(financeSearchTypeFilter.typeCode)
    }

    /**
     * TODO
     * pair.first  : FILTER_DATE_START or FILTER_DATE_END
     * pair.second : Date set to Spin Calendar
     */
    private fun showSpinCalendarDialog(spinType: Int) {
        val datePickerDialogFragment = DatePickerDialogFragment()

        with(financeSearchTypeFilter.financeFilters) {
            val year = if (spinType == FILTER_DATE_START) {
                dateStarts.year
            } else {
                dateEnds.year
            }

            val month = if (spinType == FILTER_DATE_START) {
                dateStarts.month
            } else {
                dateEnds.month
            }

            val day = if (spinType == FILTER_DATE_START) {
                dateStarts.day
            } else {
                dateEnds.day
            }

            if (year > 0 && month > 0 && day > 0) {
                datePickerDialogFragment.setSelectedDate(year, month, day)
            }
            datePickerDialogFragment.setOnDateChooseListener(object : DatePickerDialogFragment.OnDateChooseListener {
                override fun onDateChoose(year: Int, month: Int, day: Int) {
                    val selectedDotDate = "${getEngShortMonth(month)} $day, $year"
                    if (spinType == FILTER_DATE_START) {
                        tv_date_period_start.text = selectedDotDate
                        dateStarts = FinanceFilter.Date(month, day, year)
                    } else {
                        tv_date_period_end.text = selectedDotDate
                        dateEnds = FinanceFilter.Date(month, day, year)
                    }
                }
            })
        }
        datePickerDialogFragment.show(this.supportFragmentManager, "DatePickerDialogFragment")
    }

    private fun showSpinPolPodDialog(spinType: Int = FILTER_SPIN_POL) {

        val spinDataList = mutableListOf<TextItem>()
        if (spinType == FILTER_SPIN_POL) {
            for ((index, routePolData) in financeSearchTypeFilter.financeFilters.routePolList.withIndex()) {
                if (index == 0) {
                    routePolData.code = getString(R.string.finance_filters_route_pol_all)
                }
                spinDataList.add(TextItem(routePolData.code + " " + routePolData.name, routePolData.isSelected, index))
            }
        } else {
            for ((index, routePodData) in financeSearchTypeFilter.financeFilters.routePodList.withIndex()) {
                if (index == 0) {
                    routePodData.code = getString(R.string.finance_filters_route_pod_all)
                }
                spinDataList.add(TextItem(routePodData.code + " " + routePodData.name, routePodData.isSelected, index))
            }
        }

        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(true)
        dialog.setContentView(view)

        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                if (spinType == FILTER_SPIN_POL) {
                    financeSearchTypeFilter.financeFilters.routePolList.map { it.isSelected = false }
                    financeSearchTypeFilter.financeFilters.routePolList[index].isSelected = true
                } else {
                    financeSearchTypeFilter.financeFilters.routePodList.map { it.isSelected = false }
                    financeSearchTypeFilter.financeFilters.routePodList[index].isSelected = true
                }
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            if (spinType == FILTER_SPIN_POL) {
                tv_route_pol_selected.text = financeSearchTypeFilter.financeFilters.routePolList.first { it.isSelected }.code
                tv_route_polname_selected.text = financeSearchTypeFilter.financeFilters.routePolList.first { it.isSelected }.name
            } else {
                tv_route_pod_selected.text = financeSearchTypeFilter.financeFilters.routePodList.first { it.isSelected }.code
                tv_route_podname_selected.text = financeSearchTypeFilter.financeFilters.routePodList.first { it.isSelected }.name
            }
        }

        view.picker.setItems(spinDataList)
        view.picker.index = if (spinType == FILTER_SPIN_POL) {
            financeSearchTypeFilter.financeFilters.routePolList.indexOf(financeSearchTypeFilter.financeFilters.routePolList.first { it.isSelected })
        } else {
            financeSearchTypeFilter.financeFilters.routePodList.indexOf(financeSearchTypeFilter.financeFilters.routePodList.first { it.isSelected })
        }

        dialog.show()
    }
}