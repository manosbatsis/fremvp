package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.DashboardOfferWeekDetail
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_DASHBOARD_ITEM_DETAIL
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_NUMBER
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersDetailPopupViewModel(context: Context) : BaseViewModel(context),
        YourOffersDetailPopupInputs, YourOffersDetailPopupOutputs  {

    val inPuts: YourOffersDetailPopupInputs = this

    val outPuts: YourOffersDetailPopupOutputs = this
    private val onSuccessRefresh = PublishSubject.create<DashboardOfferWeekDetail>()

    // intents
    private val parentId = BehaviorSubject.create<Intent>()
    private val refreshByApi = PublishSubject.create<Parameter>()

    init {
        intent.bindToLifeCycle()
                .subscribe(parentId)

        /**
         * Get Offers week detail From Call Api
         */
        parentId.compose<Intent> { refreshByApi.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { intent ->
                    val offerNumber = intent.getStringExtra(YOUR_OFFER_NUMBER) as String
                    val lineItem = intent.getSerializableExtra(YOUR_OFFER_DASHBOARD_ITEM_DETAIL) as Dashboard.Cell.LineItem
                    val offerWeek = lineItem.baseYearWeek
                    enviorment.apiTradeClient.getDashboardOfferWeekDetail(offerNumber, offerWeek)
                            .handleToError(hideLoadingDialog)
                            .neverError()
                }
                .map { dashboardOfferWeekDetail ->
                    hideLoadingDialog.onNext(Throwable("OK"))
                    dashboardOfferWeekDetail
                }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            if (response.isSuccessful) {
                                onSuccessRefresh.onNext(response.body() as DashboardOfferWeekDetail)
                            } else {
                                error.onNext(Throwable(response.errorBody().toString()))
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refreshByApi.onNext(Parameter.EVENT)
    }

    override fun onSuccessRefresh(): Observable<DashboardOfferWeekDetail> = onSuccessRefresh
}

interface YourOffersDetailPopupInputs {

}

interface YourOffersDetailPopupOutputs {
    fun onSuccessRefresh(): Observable<DashboardOfferWeekDetail>
}
