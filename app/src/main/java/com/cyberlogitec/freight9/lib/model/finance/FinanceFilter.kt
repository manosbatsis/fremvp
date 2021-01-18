package com.cyberlogitec.freight9.lib.model.finance

import android.content.Context
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.FinanceFilterDate.FILTER_DATE_DEAL
import com.cyberlogitec.freight9.config.FinanceFilterDatePeriod.FILTER_DATE_PERIOD_6M
import com.cyberlogitec.freight9.config.FinanceFilterMoveType.FILTER_SCROLL_TO_TOP
import java.io.Serializable

data class FinanceFilter (
        // Transaction statement : Default = ture(items), false(description)
        var isDefaultSearchType: Boolean = true,
        var searchValue: String = "",
        // Status : Pay/Collect plan
        var payCollectPlanStatus: PayCollectPlanStatus = PayCollectPlanStatus(),
        // Transaction Type : Transaction statement
        var transactionStatementType: TransactionStatementType = TransactionStatementType(),
        // Transaction Type : Invoice
        var transactionInvoiceType: TransactionInvoiceType = TransactionInvoiceType(),
        // Transaction cases : Transaction statement
        var transactionCases: TransactionCases = TransactionCases(),
        // Route : Pay/Collect plan
        var routePolCode: String = "",   // defalut : ""(All POLs)
        var routePolList: MutableList<Route> = mutableListOf(),
        var routePodCode: String = "",   // default : ""(All PODs)
        var routePodList: MutableList<Route> = mutableListOf(),
        // Date : All finance kind
        var dateType: Int = FILTER_DATE_DEAL,               // defalut : FILTER_DATE_DEAL
        var datePeriodType: Int = FILTER_DATE_PERIOD_6M,    // defalut : FILTER_DATE_PERIOD_6M
        var dateStarts: Date = Date(),
        var dateEnds: Date = Date(),
        // Collect Plan : Pay/Collect Plan
        var collectPlan: CollectPlan = CollectPlan(),
        // For Scroll To XXX
        var scrollType: Int = FILTER_SCROLL_TO_TOP
): Serializable
{
    /*
    * Pay/Collect Plan
    * */
    data class PayCollectPlanStatus(
            var isCollected: Boolean = true,
            var isUncollected: Boolean = true,
            var isCollectedUnconfirmed: Boolean = true
    ): Serializable {

        private fun getCheckedCount(): Int {
            var checkedCount = 0
            checkedCount += if (isCollected) 1 else 0
            checkedCount += if (isUncollected) 1 else 0
            checkedCount += if (isCollectedUnconfirmed) 1 else 0
            return checkedCount
        }

        fun getBubbleDisplayText(context: Context) = when(getCheckedCount()) {
            0 -> EmptyString
            1 -> {
                val resId: Int = when {
                    isCollected -> {
                        R.string.finance_filter_status_collected
                    }
                    isUncollected -> {
                        R.string.finance_filter_status_uncollected
                    }
                    else -> {
                        R.string.finance_filter_status_collected_unconfirmed
                    }
                }
                context.getString(resId).toString()
            }
            3 -> {
                context.getString(R.string.finance_filter_pay_collect_status_bubble_all).toString()
            }
            else -> {
                val count = getCheckedCount()
                when {
                    isCollected -> {
                        context.getString(R.string.finance_filter_status_collected) + " +" + (count-1)
                    }
                    isUncollected -> {
                        context.getString(R.string.finance_filter_status_uncollected) + " +" + (count-1)
                    }
                    else -> {
                        context.getString(R.string.finance_filter_status_collected_unconfirmed) + " +" + (count-1)
                    }
                }
            }
        }
    }

    /*
    * Pay/Collect Plan
    * */
    data class Route(
            var code: String = "",
            var name: String = "",
            var isSelected: Boolean = false
    ): Serializable

    /*
    * Pay/Collect Plan
    * */
    data class Date(
            var month: Int = 0,        // 1 ~ 12
            var day: Int = 0,          // 1 ~
            var year: Int = 0          // 1 ~
    ): Serializable

    /*
    * Pay/Collect Plan
    * */
    data class CollectPlan(
            var isPrepaid: Boolean = true,
            var isCollect: Boolean = false
    ): Serializable {

        private fun getCheckedCount(): Int {
            var checkedCount = 0
            checkedCount += if (isPrepaid) 1 else 0
            checkedCount += if (isCollect) 1 else 0
            return checkedCount
        }

        fun getBubbleDisplayText(context: Context) = when(getCheckedCount()) {
            1 -> {
                val resId: Int = when {
                    isPrepaid -> {
                        R.string.finance_filter_pay_collect_plan_prepaid
                    }
                    else -> {
                        R.string.finance_filter_pay_collect_plan_collect
                    }
                }
                context.getString(resId).toString()
            }
            2 -> {
                context.getString(R.string.finance_filter_pay_collect_plan_bubble_all).toString()
            }
            else -> { EmptyString }
        }
    }

    /*
    * Transaction statement
    * */
    data class TransactionStatementType(
            var isInitialPayment: Boolean = true,
            var isMidTermPayment: Boolean = true,
            var isRemainderPayment: Boolean = true
    ): Serializable {

        private fun getCheckedCount(): Int {
            var checkedCount = 0
            checkedCount += if (isInitialPayment) 1 else 0
            checkedCount += if (isMidTermPayment) 1 else 0
            checkedCount += if (isRemainderPayment) 1 else 0
            return checkedCount
        }

        fun getBubbleDisplayText(context: Context) = when(getCheckedCount()) {
            0 -> EmptyString
            1 -> {
                val resId: Int = when {
                    isInitialPayment -> {
                        R.string.finance_filter_transaction_statement_type_init
                    }
                    isMidTermPayment -> {
                        R.string.finance_filter_transaction_statement_type_mid
                    }
                    else -> {
                        R.string.finance_filter_transaction_statement_type_remain
                    }
                }
                context.getString(resId).toString()
            }
            3 -> {
                context.getString(R.string.finance_filter_transaction_type_bubble_all).toString()
            }
            else -> {
                val count = getCheckedCount()
                when {
                    isInitialPayment -> {
                        context.getString(R.string.finance_filter_transaction_statement_type_init) + " +" + (count-1)
                    }
                    isMidTermPayment -> {
                        context.getString(R.string.finance_filter_transaction_statement_type_mid) + " +" + (count-1)
                    }
                    else -> {
                        context.getString(R.string.finance_filter_transaction_statement_type_remain) + " +" + (count-1)
                    }
                }
            }
        }
    }

    /*
    * Invoice
    * */
    data class TransactionInvoiceType(
            var isUnPaid: Boolean = true,
            var isUnconfirmed: Boolean = true
    ): Serializable {

        private fun getCheckedCount(): Int {
            var checkedCount = 0
            checkedCount += if (isUnPaid) 1 else 0
            checkedCount += if (isUnconfirmed) 1 else 0
            return checkedCount
        }

        fun getBubbleDisplayText(context: Context) = when(getCheckedCount()) {
            1 -> {
                val resId: Int = when {
                    isUnPaid -> {
                        R.string.finance_filter_invoice_transaction_type_unpaid
                    }
                    else -> {
                        R.string.finance_filter_invoice_transaction_type_unconfirmed
                    }
                }
                context.getString(resId).toString()
            }
            2 -> {
                context.getString(R.string.finance_filter_invoice_type_bubble_all).toString()
            }
            else -> { EmptyString }
        }
    }

    /*
    * Transaction statement
    * */
    data class TransactionCases(
            var isAmountIn: Boolean = true,
            var isAmountOut: Boolean = true,
            var isSpaceIn: Boolean = true,
            var isSpaceOut: Boolean = true
    ): Serializable {

        private fun getCheckedCount(): Int {
            var checkedCount = 0
            checkedCount += if (isAmountIn) 1 else 0
            checkedCount += if (isAmountOut) 1 else 0
            checkedCount += if (isSpaceIn) 1 else 0
            checkedCount += if (isSpaceOut) 1 else 0
            return checkedCount
        }

        fun getBubbleDisplayText(context: Context) = when(getCheckedCount()) {
            0 -> EmptyString
            1 -> {
                val resId: Int = when {
                    isAmountIn -> {
                        R.string.finance_filter_transaction_statement_cases_amount_in
                    }
                    isAmountOut -> {
                        R.string.finance_filter_transaction_statement_cases_amount_out
                    }
                    isSpaceIn -> {
                        R.string.finance_filter_transaction_statement_cases_space_in
                    }
                    else -> {
                        R.string.finance_filter_transaction_statement_cases_space_out
                    }
                }
                context.getString(resId).toString()
            }
            4 -> {
                context.getString(R.string.finance_filter_transaction_cases_bubble_all).toString()
            }
            else -> {
                val count = getCheckedCount()
                when {
                    isAmountIn -> {
                        context.getString(R.string.finance_filter_transaction_statement_cases_amount_in) + " +" + (count-1)
                    }
                    isAmountOut -> {
                        context.getString(R.string.finance_filter_transaction_statement_cases_amount_out) + " +" + (count-1)
                    }
                    isSpaceIn -> {
                        context.getString(R.string.finance_filter_transaction_statement_cases_space_in) + " +" + (count-1)
                    }
                    else -> {
                        context.getString(R.string.finance_filter_transaction_statement_cases_space_out) + " +" + (count-1)
                    }
                }
            }
        }
    }
}

data class FinanceSelectValue(
        var index: Int = 0,
        var moveType: Int = FILTER_SCROLL_TO_TOP,
        var value: String = "",
        var detail: String = ""
)