package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.lib.apitrade.PostInventoryListRequest
import com.cyberlogitec.freight9.lib.apitrade.PostPortRouteRequest
import com.cyberlogitec.freight9.lib.model.InventoryList
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class SofContractVm(context: Context) : BaseViewModel(context), ContractInputs, ContractOutputs  {

    val inPuts: ContractInputs = this
    private val afterFeaturedRoute = PublishSubject.create<PostPortRouteRequest>()
    private val requestPolPodList = PublishSubject.create<Unit>()

    private val clickItem = PublishSubject.create<String>()
    private val swipeToLeft = PublishSubject.create<String>()
    private val swipeToRight = PublishSubject.create<String>()

    val outPuts: ContractOutputs = this
    private val goToNewVolume = PublishSubject.create<String>()
    private val onSuccessRefresh = PublishSubject.create<Response<List<InventoryList>>>()
    private val onSuccessRequestPolPodList = PublishSubject.create<Response<List<InventoryList>>>()

    private val gotoContractRoute = PublishSubject.create<String>()
    private val gotoConractVolume = PublishSubject.create<String>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getStringExtra(Intents.MSTR_CTRK_NR) }
                .map {
                    Timber.d("f9: SofContractVm : MSTR_CTRK_NR --> ${it}")
                    it
                }
                .bindToLifeCycle()
                .subscribe( parentId )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { masterContractNumber ->
                    Timber.d("f9: SofContractVm : parentId--> ${masterContractNumber}");
                    if (masterContractNumber.isNotEmpty()) {
                        clickItem.onNext(masterContractNumber)
                        parentId.onNext(EmptyString)
                    }
                    masterContractNumber
                }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe {
                    enviorment.apiTradeClient.postInventoryList(
                            PostInventoryListRequest(
                                    "",
                                    "",
                                    0.0f,
                                    "",
                                    "",
                                    "",""))
                            .handleToError(hideLoadingDialog).neverError()
                }
                .bindToLifeCycle()
                .subscribe(
                        { response ->
                            onSuccessRefresh.onNext(response)
                            if (response.isSuccessful) {
                                requestPolPodList.onNext(Unit)
                            } else {
                                hideLoadingDialog.onNext(Throwable("OK"))
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )


        ////////////////////////////////////////////////////////////////////////////////////////////
        // afterFeaturedRoute

        afterFeaturedRoute
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { enviorment.apiTradeClient.postInventoryList(PostInventoryListRequest(it.ipolCd, it.ipodCd, 0.0f, "","","","")).handleToError(hideLoadingDialog).neverError()}
                .bindToLifeCycle()
                .subscribe(
                        { response ->
                            onSuccessRefresh.onNext(response)
                            if (response.isSuccessful) {
                                requestPolPodList.onNext(Unit)
                            } else {
                                hideLoadingDialog.onNext(Throwable("OK"))
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

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

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickItem

        clickItem.bindToLifeCycle()
                .subscribe(goToNewVolume)

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // in
    override fun clickItem(parameter: String) = clickItem.onNext(parameter)
    override fun swipeToLeft(parameter: String) = swipeToLeft.onNext(parameter)
    override fun swipeToRight(parameter: String) = swipeToRight.onNext(parameter)
    override fun afterFeaturedRoute(parameter: PostPortRouteRequest) = afterFeaturedRoute.onNext(parameter)
    override fun requestPolPodList() = requestPolPodList.onNext(Unit)

    // out
    override fun goToNewVolume(): Observable<String> = goToNewVolume
    override fun onSuccessRefresh(): Observable<Response<List<InventoryList>>> = onSuccessRefresh
    override fun onSuccessRequestPolPodList(): Observable<Response<List<InventoryList>>> =
            onSuccessRequestPolPodList

    override fun gotoConractVolume(): Observable<String> = gotoConractVolume
    override fun gotoContractRoute(): Observable<String> = gotoContractRoute
}

interface ContractInputs {
    fun clickItem(parameter: String)
    fun swipeToLeft(parameter: String)
    fun swipeToRight(parameter: String)
    fun afterFeaturedRoute(parameter: PostPortRouteRequest)
    fun requestPolPodList()
}

interface ContractOutputs {
    fun goToNewVolume() : Observable<String>
    fun onSuccessRefresh(): Observable<Response<List<InventoryList>>>
    fun onSuccessRequestPolPodList() : Observable<Response<List<InventoryList>>>
    fun gotoContractRoute() : Observable<String>
    fun gotoConractVolume() : Observable<String>
}