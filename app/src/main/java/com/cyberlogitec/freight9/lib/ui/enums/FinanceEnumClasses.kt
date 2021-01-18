package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.FinanceFilterDate
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod
import com.cyberlogitec.freight9.config.FinanceFilterMoveType
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_INVOICE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_NONE
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.FinanceType.FINANCE_TYPE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_INVOICE
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_PAY_COLLECT_PLAN
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_FINANCE_TRANSACTION_STATEMENT
import com.cyberlogitec.freight9.lib.model.finance.FinanceFilter
import java.io.Serializable

enum class FinanceTypeEnum constructor(
        val typeCode: String,
        val titleResId: Int,
        val menuId: String,
        var financeSearchTypeFilter: FinanceSearchTypeFilter
) {
    TYPE_NONE(FINANCE_TYPE_NONE,
            -1,
            EmptyString,
            FinanceSearchTypeFilter()
    ),
    TYPE_PAY_COLLECT_PLAN(
            FINANCE_TYPE_PAY_COLLECT_PLAN,
            R.string.menu_finance_pay_collect_plan,
            MENUITEM_FINANCE_PAY_COLLECT_PLAN,
            FinanceSearchTypeFilter(
                    FINANCE_TYPE_PAY_COLLECT_PLAN,
                    false,
                    R.string.finance_search_hint_search_buyer_seller,
                    -1,
                    FinanceFilter())
    ),
    TYPE_TRANSACTION_STATEMENT(
            FINANCE_TYPE_TRANSACTION_STATEMENT,
            R.string.menu_finance_transaction_statement,
            MENUITEM_FINANCE_TRANSACTION_STATEMENT,
            FinanceSearchTypeFilter(
                    FINANCE_TYPE_TRANSACTION_STATEMENT,
                    true,
                    R.string.finance_search_hint_search_items,
                    R.string.finance_search_hint_search_description,
                    FinanceFilter())
    ),
    TYPE_INVOICE(
            FINANCE_TYPE_INVOICE,
            R.string.menu_finance_invoice,
            MENUITEM_FINANCE_INVOICE,
            FinanceSearchTypeFilter(
                    FINANCE_TYPE_INVOICE,
                    false,
                    R.string.finance_search_hint_search_issuer,
                    -1,
                    FinanceFilter())
    );

    companion object {
        fun getFinanceTypeEnum(typeCode: String): FinanceTypeEnum {
            for (financeType in values()) {
                if (financeType.typeCode == typeCode) {
                    // financeType filter 초기화
                    with(financeType.financeSearchTypeFilter.financeFilters) {
                        isDefaultSearchType = true
                        searchValue = ""
                        payCollectPlanStatus = FinanceFilter.PayCollectPlanStatus()
                        transactionStatementType = FinanceFilter.TransactionStatementType()
                        transactionInvoiceType = FinanceFilter.TransactionInvoiceType()
                        transactionCases = FinanceFilter.TransactionCases()
                        routePolCode = ""
                        routePolList = mutableListOf()
                        routePodCode = ""
                        routePodList = mutableListOf()
                        dateType = FinanceFilterDate.FILTER_DATE_DEAL
                        datePeriodType = FinanceFilterDatePeriod.FILTER_DATE_PERIOD_6M
                        dateStarts = FinanceFilter.Date()
                        dateEnds = FinanceFilter.Date()
                        collectPlan = FinanceFilter.CollectPlan()
                        scrollType = FinanceFilterMoveType.FILTER_SCROLL_TO_TOP
                    }

                    return financeType
                }
            }
            return TYPE_PAY_COLLECT_PLAN
        }
    }
}

data class FinanceSearchTypeFilter constructor(
        val typeCode: String = FINANCE_TYPE_PAY_COLLECT_PLAN,
        var isSearchTypeVisible: Boolean = false,
        var searchHintResIdDefault: Int = -1,
        var searchHintResIdExtra: Int = -1,
        var financeFilters: FinanceFilter = FinanceFilter()
): Serializable