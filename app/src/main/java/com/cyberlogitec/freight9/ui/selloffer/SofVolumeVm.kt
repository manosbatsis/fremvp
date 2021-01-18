package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofVolumeVm(context: Context) : BaseViewModel(context), VolumeInputs, VolumeOutputs  {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: VolumeInputs = this
    private val clickToCheckAll = PublishSubject.create<Boolean>()
    private val clickToMaxAll = PublishSubject.create<Parameter>()
    private val clickToVolAll = PublishSubject.create<String>()
    private val clickToVolEach = PublishSubject.create<String>()
    private val clickToEnter = PublishSubject.create<Int>()
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToNext = PublishSubject.create<Contract>()

    private val clickToConditionDetail = PublishSubject.create<Parameter>()
    private val clickToWholeRoute = PublishSubject.create<Parameter>()
    private val clickToPriceTable = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: VolumeOutputs = this
    private val onClickCheckAll = PublishSubject.create<Boolean>()
    private val onClickMaxAll = PublishSubject.create<Parameter>()
    private val onClickVolAll = PublishSubject.create<String>()
    private val onClickVolEach = PublishSubject.create<String>()
    private val onClickEnter = PublishSubject.create<Int>()
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickNext = PublishSubject.create<Contract>()
    private val onSuccessRefresh = PublishSubject.create<Contract>()

    private val onClickConditionDetail = PublishSubject.create<Parameter>()
    private val onClickWholeRoute = PublishSubject.create<Parameter>()
    private val onClickPriceTable = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getStringExtra(Intents.MSTR_CTRK_NR) }
                .map { Timber.d("f9: MSTR_CTRK_NR --> ${it}"); it }
                .filter{ it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( parentId )

        ///////////////////////////////////////////////////////////////////////////////////////////
        // refresh (masterContractNumber)
        // 라우트 (POL, POD) 표시 (inventory db 조회)

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { it.isNotEmpty() }
                .map { Timber.d("f9: parentId--> ${it}"); it }
                .flatMapMaybe { enviorment.apiTradeClient.getContract(it).handleToError(hideLoadingDialog).neverError() }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .map { contract ->
                    enviorment.currentUser.getCrcyCd()?.let {
                        contract.tradeCompanyCode = it
                    }
                    contract
                }
                .map { contract ->
                    contract.tradeRoleCode = "001"
                    contract
                }
                .map { contract ->
                    contract.masterContractLineItems?.filter{ it.deleteYn != "1'" }?.forEach { lineItem ->
                        lineItem.isChecked = false
                        contract.inventory?.let {
                            if ( it.deleteYn != "1") {
                                it.inventoryDetails?.filter{ it.deleteYn != "1" }?.find { it.baseYearWeek == lineItem.baseYearWeek }?.let {
                                    lineItem.remainderQty = it.remainderConfirmedQty
                                    lineItem.costPrice = 0.0f
                                    lineItem.offerPrice = 0
                                }
                            }
                        }
                    }
                    contract
                }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)
        
        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToNext
        //
        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)

        clickToCheckAll.bindToLifeCycle()
                .subscribe(onClickCheckAll)

        clickToMaxAll.bindToLifeCycle()
                .subscribe(onClickMaxAll)

        clickToVolAll.bindToLifeCycle()
                .subscribe(onClickVolAll)

        clickToVolEach.bindToLifeCycle()
                .subscribe(onClickVolEach)

        clickToEnter.bindToLifeCycle()
                .subscribe(onClickEnter)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToConditionDetail
        //
        parentId.compose<String> { clickToConditionDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ it.isNotEmpty() }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(onClickConditionDetail)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToWholeRoute
        //
        parentId.compose<String> { clickToWholeRoute.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ it.isNotEmpty() }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(onClickWholeRoute)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToPriceTable
        //
        parentId.compose<String> { clickToPriceTable.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ it.isNotEmpty() }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(onClickPriceTable)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }


    // interface : in
    override fun clickToCheckAll(parameter: Boolean) = clickToCheckAll.onNext(parameter)
    override fun clickToMaxAll(parameter: Parameter) = clickToMaxAll.onNext(parameter)
    override fun clickToVolAll(parameter: String) = clickToVolAll.onNext(parameter)
    override fun clickToVolEach(parameter: String) = clickToVolEach.onNext(parameter)
    override fun clickToEnter(parameter: Int) = clickToEnter.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToNext(parameter: Contract) = clickToNext.onNext(parameter)

    override fun clickToConditionDetail(parameter: Parameter) = clickToConditionDetail.onNext(parameter)
    override fun clickToWholeRoute(parameter: Parameter) = clickToWholeRoute.onNext(parameter)
    override fun clickToPriceTable(parameter: Parameter) = clickToPriceTable.onNext(parameter)

    // interface : out
    override fun onClickCheckAll() = onClickCheckAll
    override fun onClickMaxAll() = onClickMaxAll
    override fun onClickVolAll() = onClickVolAll
    override fun onClickVolEach() = onClickVolEach
    override fun onClickEnter() = onClickEnter
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickNext() : Observable<Contract> = onClickNext

    override fun onClickConditionDetail() : Observable<Parameter> = onClickConditionDetail
    override fun onClickWholeRoute() : Observable<Parameter> = onClickWholeRoute
    override fun onClickPriceTable() : Observable<Parameter> = onClickPriceTable
}

interface VolumeInputs {
    fun clickToCheckAll(parameter: Boolean)
    fun clickToMaxAll(parameter: Parameter)
    fun clickToVolAll(parameter: String)
    fun clickToVolEach(parameter: String)
    fun clickToEnter(parameter: Int)
    fun clickToDone(parameter: Parameter)
    fun clickToNext(parameter: Contract)

    fun clickToConditionDetail(parameter: Parameter)
    fun clickToWholeRoute(parameter: Parameter)
    fun clickToPriceTable(parameter: Parameter)
}

interface VolumeOutputs {
    fun onClickCheckAll(): Observable<Boolean>
    fun onClickMaxAll(): Observable<Parameter>
    fun onClickVolAll(): Observable<String>
    fun onClickVolEach(): Observable<String>
    fun onClickEnter(): Observable<Int>
    fun onClickNext(): Observable<Contract>
    fun onClickDone(): Observable<Parameter>
    fun onSuccessRefresh(): Observable<Contract>

    fun onClickConditionDetail() : Observable<Parameter>
    fun onClickWholeRoute() : Observable<Parameter>
    fun onClickPriceTable() : Observable<Parameter>
}