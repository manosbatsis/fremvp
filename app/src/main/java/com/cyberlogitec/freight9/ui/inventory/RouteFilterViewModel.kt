package com.cyberlogitec.freight9.ui.inventory

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.apitrade.PostInventoryListRequest
import com.cyberlogitec.freight9.lib.model.InventoryDetails
import com.cyberlogitec.freight9.lib.model.InventoryList
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class RouteFilterViewModel(context: Context) : BaseViewModel(context), RouteFilterInputs, RouteFilterOutputs  {

    val inPuts: RouteFilterInputs = this
    private val requestInventoryList = PublishSubject.create<PostInventoryListRequest>()
    private val requestPolPodList = PublishSubject.create<Unit>()
    private val requestInventoryDetail = PublishSubject.create<String>()

    val outPuts: RouteFilterOutputs = this
    private val onSuccessRequestInventoryList = PublishSubject.create<Response<List<InventoryList>>>()
    private val onSuccessRequestPolPodList = PublishSubject.create<Response<List<InventoryList>>>()
    private val onSuccessRequestInventoryDetail = PublishSubject.create<Response<InventoryDetails>>()
    private val onSuccessRefresh = PublishSubject.create<Parameter>()

    private val refresh = PublishSubject.create<Parameter>()

    init {

        /**
         * onResume 시 inventory list 요청
         */
        refresh.bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        /**
         * request invneoty list
         * request pol, pod list
         */
        requestInventoryList
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.postInventoryList(it)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestInventoryList.onNext(response)
                            if (response.isSuccessful) {
                                requestPolPodList.onNext(Unit)
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * request pol, pod list
         */
        requestPolPodList
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getInventoryListPolPod()
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestPolPodList.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    override fun refresh(parameter: Parameter) = refresh.onNext(parameter)
    override fun requestInventoryList(inventoryRequest: PostInventoryListRequest) =
            requestInventoryList.onNext(inventoryRequest)
    override fun requestPolPodList() = requestPolPodList.onNext(Unit)
    override fun requestInventoryDetail(masterContractNumber: String) =
            requestInventoryDetail.onNext(masterContractNumber)

    override fun onSuccessRefresh(): Observable<Parameter> = onSuccessRefresh
    override fun onSuccessRequestInventoryList(): Observable<Response<List<InventoryList>>> =
            onSuccessRequestInventoryList
    override fun onSuccessRequestPolPodList(): Observable<Response<List<InventoryList>>> =
            onSuccessRequestPolPodList
    override fun onSuccessRequestInventoryDetail(): Observable<Response<InventoryDetails>> =
            onSuccessRequestInventoryDetail
}

interface RouteFilterInputs {
    fun refresh(parameter: Parameter)
    fun requestInventoryList(inventoryRequest: PostInventoryListRequest)
    fun requestPolPodList()
    fun requestInventoryDetail(masterContractNumber: String)
}

interface RouteFilterOutputs {
    fun onSuccessRefresh(): Observable<Parameter>
    fun onSuccessRequestInventoryList() : Observable<Response<List<InventoryList>>>
    fun onSuccessRequestPolPodList() : Observable<Response<List<InventoryList>>>
    fun onSuccessRequestInventoryDetail() : Observable<Response<InventoryDetails>>
}