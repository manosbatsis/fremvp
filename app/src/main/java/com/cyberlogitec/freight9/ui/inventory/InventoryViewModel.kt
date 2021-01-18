package com.cyberlogitec.freight9.ui.inventory

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class InventoryViewModel(context: Context) : BaseViewModel(context), InventoryInputs, InventoryOutputs  {

    val inPuts: InventoryInputs = this
//    private val clickViewCardLink = PublishSubject.create<ParameterClick>()
//    private val clickToMenu = PublishSubject.create<Parameter>()
//    private val clickToFinish = PublishSubject.create<Parameter>()
//    private val clickItem = PublishSubject.create<InventoryActivity.InventoryChange>()
//    private val clickRouteFilter = PublishSubject.create<Any>()
//    private val clickDetailGoMove = PublishSubject.create<InventoryDetailActivity.Goto>()
//    private val clickDetailItem = PublishSubject.create<InventoryDetails.InventoryDetail>()
//    private val clickDetailPopupItem = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val clickDetailJumpPopupFirstItem = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val clickDetailJumpPopupSecondItem = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val swipeToOfferDetail = PublishSubject.create<Long>()
    private val swipeToOfferRoute = PublishSubject.create<Long>()
    private val requestOfferDetail = PublishSubject.create<BorList>()
    private val requestOfferRoute = PublishSubject.create<Bor>()
    private val requestPortNm = PublishSubject.create<String>()
    private val requestCarrierName = PublishSubject.create<String>()

    private val requestInventory = PublishSubject.create<Intent>()
    private val requestInventoryDetail = PublishSubject.create<Pair<String, Int>>()
    private val requestMasterContract = PublishSubject.create<String>()
    private val requestPaymentPlan = PublishSubject.create<String>()

    val outPuts: InventoryOutputs = this
//    private val onClickViewCardLink = PublishSubject.create<ParameterClick>()
//    private val gotoMenu = PublishSubject.create<Parameter>()
//    private val gotoFinish = PublishSubject.create<Parameter>()
//    private val gotoYourTradeDashboard = PublishSubject.create<InventoryActivity.InventoryChange>()
//    private val gotoRouteFilter = PublishSubject.create<Any>()
//    private val gotoDetailGoMove = PublishSubject.create<InventoryDetailActivity.Goto>()
//    private val gotoDetailItem = PublishSubject.create<InventoryDetails.InventoryDetail>()
//    private val gotoDetailPopupGoMove = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val gotoDetailJumpPopupFirstItem = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val gotoDetailJumpPopupSecondItem = PublishSubject.create<InventoryDetailPopupActivity.Goto>()
    private val viewSplitPopupDetail = PublishSubject.create<Long>()
    private val viewSplitPopupRoute = PublishSubject.create<Long>()
    //private val refreshSplitPopupDeatil = PublishSubject.create<Bor>()
    //private val onSuccessRequestOfferDetail = PublishSubject.create<Bor>()
    private val onSuccessRequestOfferRoute = PublishSubject.create<Bor>()
    private val onSuccessRequestPortNm = PublishSubject.create<String>()
    private val onSuccessRequestCarrierName = PublishSubject.create<String>()
    private val onSuccessRefresh = PublishSubject.create<Parameter>()

    private val onSuccessRequestInventory = PublishSubject.create<InventoryList>()
    private val onSuccessRequestInventoryDetail = PublishSubject.create<Response<InventoryDetails>>()
    private val onSuccessRequestMasterContractWithInventory = PublishSubject.create<Response<MasterContractWithInventory>>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()

    private val refresh = PublishSubject.create<Parameter>()

    private var bor: Bor = Bor()

    init {

        /**
         * onResume 시 inventory list 요청
         */
        refresh.bindToLifeCycle()
                .subscribe(onSuccessRefresh)

//        clickViewCardLink.bindToLifeCycle()
//                .subscribe(onClickViewCardLink)

//        clickToMenu.bindToLifeCycle()
//                .subscribe(gotoMenu)

//        clickToFinish.bindToLifeCycle()
//                .subscribe(gotoFinish)

//        clickItem.bindToLifeCycle()
//                .subscribe(gotoYourTradeDashboard)

//        clickRouteFilter.bindToLifeCycle()
//                .subscribe(gotoRouteFilter)

//        clickDetailItem.bindToLifeCycle()
//                .subscribe(gotoDetailItem)

//        clickDetailGoMove.bindToLifeCycle()
//                .subscribe(gotoDetailGoMove)

//        clickDetailPopupItem.bindToLifeCycle()
//                .subscribe(gotoDetailPopupGoMove)

        clickDetailJumpPopupFirstItem.bindToLifeCycle()
                .subscribe(gotoDetailJumpPopupFirstItem)

        clickDetailJumpPopupSecondItem.bindToLifeCycle()
                .subscribe(gotoDetailJumpPopupSecondItem)

        swipeToOfferDetail.bindToLifeCycle()
                .subscribe(viewSplitPopupDetail)

        swipeToOfferRoute.bindToLifeCycle()
                .subscribe(viewSplitPopupRoute)

        /*requestOfferDetail.flatMapMaybe {
            bor.detailList = ArrayList()
            bor.routeList = ArrayList()
            bor.item = it

            refreshSplitPopupDeatil.onNext(bor)
            enviorment.apiClient.getBuyOrderDetails(it.oferGrpNr!!).neverError() }
                .filter {it.isNotEmpty()}
                .bindToLifeCycle()
                .subscribe {
                    bor.detailList = it
                    onSuccessRequestOfferDetail.onNext(bor)
                    requestOfferRoute.onNext(bor)

                }*/
        requestOfferRoute.flatMapMaybe {
            enviorment.apiTradeClient.postServiceRoute(it.getServiceRouteReques()).neverError() }
                .filter {it.isNotEmpty()}
                .bindToLifeCycle()
                .subscribe {
                    bor.routeList = it
                    onSuccessRequestOfferRoute.onNext(bor)
                }

        /**
         * port name 요청
         */
        requestPortNm
                .map { enviorment.portRepository.getPortNm(it) }
                .bindToLifeCycle()
                .subscribe { onSuccessRequestPortNm.onNext(it) }

        /**
         * carrier name 요청
         */
        requestCarrierName
                .flatMap { enviorment.carrierRepository.getCarrierName(it) }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (onSuccessRequestCarrierName)

        /**
         * RouteFilterActivity로 부터 전달받은 inventory item data 전달
         * 전달받은 inventory item에 대한 masterContract data 요청
         */
        requestInventory
                .map { it.getSerializableExtra(Intents.INVENTORY_LIST) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .neverError()
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestInventory.onNext(it as InventoryList)
                    requestMasterContract.onNext(it.masterContractNumber!!)
                }

        /**
         * Inventory Detail 요청
         */
        requestInventoryDetail
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getInventorySingleDetail(it.first, it.second)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestInventoryDetail.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * Master Contract 요청
         */
        requestMasterContract
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getMasterContractWithInventory(it)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestMasterContractWithInventory.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * Pyament plan data 요청
         */
        requestPaymentPlan
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getPaymentPlan(it)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestPaymentPlan.onNext(response)
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
    override fun onSuccessRefresh(): Observable<Parameter> = onSuccessRefresh

    override fun requestPaymentPlan(paymentPlanCode: String) = requestPaymentPlan.onNext(paymentPlanCode)
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan

    override fun requestPortNm(portCode: String) = requestPortNm.onNext(portCode)
    override fun onSuccessRequestPortNm(): Observable<String> = onSuccessRequestPortNm

    override fun requestCarrierName(carrierCode: String) = requestCarrierName.onNext(carrierCode)
    override fun onSuccessRequestCarrierName(): Observable<String> = onSuccessRequestCarrierName

//    override fun clickViewCardLink(parameterClick: ParameterClick) = clickViewCardLink.onNext(parameterClick)
//    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
//    override fun clickToFinish(parameter: Parameter) = clickToFinish.onNext(parameter)
//    override fun clickItem(inventoryChange: InventoryActivity.InventoryChange) = clickItem.onNext(inventoryChange)
//    override fun clickRouteFilter(data: Any) = clickRouteFilter.onNext(data)
//    override fun clickDetailGoMove(goto: InventoryDetailActivity.Goto) = clickDetailGoMove.onNext(goto)
//    override fun clickDetailItem(inventoryDetail: InventoryDetails.InventoryDetail) = clickDetailItem.onNext(inventoryDetail)
//    override fun clickDetailPopupItem(goto: InventoryDetailPopupActivity.Goto) = clickDetailPopupItem.onNext(goto)
    override fun clickDetailJumpPopupFirstItem(goto: InventoryDetailPopupActivity.Goto) = clickDetailJumpPopupFirstItem.onNext(goto)
    override fun clickDetailJumpPopupSecondItem(goto: InventoryDetailPopupActivity.Goto) = clickDetailJumpPopupSecondItem.onNext(goto)
    override fun swipeToOfferDetail(id: Long) = swipeToOfferDetail.onNext(id)
    override fun swipeToOfferRoute(id: Long) = swipeToOfferRoute.onNext(id)
    override fun requestOfferDetail(item: BorList) = requestOfferDetail.onNext(item)
    override fun requestOfferRoute(bor: Bor) = requestOfferRoute.onNext(bor)

//    override fun onClickViewCardLink(): Observable<ParameterClick> = onClickViewCardLink
//    override fun gotoMenu() : Observable<Parameter> = gotoMenu
//    override fun gotoFinish() : Observable<Parameter> = gotoFinish
//    override fun gotoYourTradeDashboard(): Observable<InventoryActivity.InventoryChange> = gotoYourTradeDashboard
//    override fun gotoRouteFilter(): Observable<Any> = gotoRouteFilter
//    override fun gotoDetailGoMove(): Observable<InventoryDetailActivity.Goto> = gotoDetailGoMove
//    override fun gotoDetailItem(): Observable<InventoryDetails.InventoryDetail> = gotoDetailItem
//    override fun gotoDetailPopupGoMove(): Observable<InventoryDetailPopupActivity.Goto> = gotoDetailPopupGoMove
    override fun gotoDetailJumpPopupFirstItem(): Observable<InventoryDetailPopupActivity.Goto> = gotoDetailJumpPopupFirstItem
    override fun gotoDetailJumpPopupSecondItem(): Observable<InventoryDetailPopupActivity.Goto> = gotoDetailJumpPopupSecondItem
    override fun viewSplitPopupDetail(): Observable<Long> = viewSplitPopupDetail
    override fun viewSplitPopupRoute(): Observable<Long> = viewSplitPopupRoute

    override fun requestInventory(intent: Intent) = requestInventory.onNext(intent)
    override fun requestInventoryDetail(pair: Pair<String, Int>)= requestInventoryDetail.onNext(pair)
    override fun requestMasterContract(masterContractNumber: String) = requestMasterContract.onNext(masterContractNumber)
    override fun onSuccessRequestInventory(): Observable<InventoryList> = onSuccessRequestInventory
    override fun onSuccessRequestInventoryDetail(): Observable<Response<InventoryDetails>> = onSuccessRequestInventoryDetail
    override fun onSuccessRequestMasterContractWithInventory(): Observable<Response<MasterContractWithInventory>>
            = onSuccessRequestMasterContractWithInventory
}

interface InventoryInputs {
    fun refresh(parameter: Parameter)

//    fun clickViewCardLink(parameterClick: ParameterClick)
//    fun clickToMenu(parameter: Parameter)
//    fun clickToFinish(parameter: Parameter)
//    fun clickItem(inventoryChange: InventoryActivity.InventoryChange)
//    fun clickRouteFilter(data: Any)
//    fun clickDetailGoMove(goto: InventoryDetailActivity.Goto)
//    fun clickDetailItem(inventoryDetail: InventoryDetails.InventoryDetail)
//    fun clickDetailPopupItem(goto: InventoryDetailPopupActivity.Goto)
    fun clickDetailJumpPopupFirstItem(goto: InventoryDetailPopupActivity.Goto)
    fun clickDetailJumpPopupSecondItem(goto: InventoryDetailPopupActivity.Goto)
    fun swipeToOfferDetail(id: Long)
    fun swipeToOfferRoute(id: Long)
    fun requestOfferDetail(item: BorList)
    fun requestOfferRoute(bor: Bor)
    fun requestPortNm(portCode: String)
    fun requestCarrierName(carrierCode: String)

    fun requestInventory(intent: Intent)
    fun requestInventoryDetail(pair: Pair<String, Int>)
    fun requestMasterContract(masterContractNumber: String)
    fun requestPaymentPlan(paymentPlanCode: String)
}

interface InventoryOutputs {
    fun onSuccessRefresh(): Observable<Parameter>

    fun onSuccessRequestPortNm() : Observable<String>
    fun onSuccessRequestCarrierName() : Observable<String>

    fun onSuccessRequestInventory() : Observable<InventoryList>
    fun onSuccessRequestInventoryDetail() : Observable<Response<InventoryDetails>>
    fun onSuccessRequestMasterContractWithInventory() : Observable<Response<MasterContractWithInventory>>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>

//    fun onClickViewCardLink() : Observable<ParameterClick>
//    fun gotoMenu() : Observable<Parameter>
//    fun gotoFinish() : Observable<Parameter>
//    fun gotoYourTradeDashboard(): Observable<InventoryActivity.InventoryChange>
//    fun gotoRouteFilter(): Observable<Any>
//    fun gotoDetailGoMove(): Observable<InventoryDetailActivity.Goto>
//    fun gotoDetailItem(): Observable<InventoryDetails.InventoryDetail>
//    fun gotoDetailPopupGoMove(): Observable<InventoryDetailPopupActivity.Goto>
    fun gotoDetailJumpPopupFirstItem(): Observable<InventoryDetailPopupActivity.Goto>
    fun gotoDetailJumpPopupSecondItem(): Observable<InventoryDetailPopupActivity.Goto>
    fun viewSplitPopupDetail(): Observable<Long>
    fun viewSplitPopupRoute(): Observable<Long>
}