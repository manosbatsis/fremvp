package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofPriceVm(context: Context) : BaseViewModel(context), PriceInputs, PriceOutputs  {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: PriceInputs = this
    private val clickToCheckAll = PublishSubject.create<Boolean>()
    private val clickToMaxAll = PublishSubject.create<Parameter>()
    private val clickToPriceAll = PublishSubject.create<String>()
    private val clickToPriceEach = PublishSubject.create<String>()
    private val clickToEnter = PublishSubject.create<Int>()
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToNext = PublishSubject.create<Contract>()

    private val clickToConditionDetail = PublishSubject.create<Parameter>()
    private val clickToWholeRoute = PublishSubject.create<Parameter>()
    private val clickToPriceTable = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: PriceOutputs = this
    private val onClickCheckAll = PublishSubject.create<Boolean>()
    private val onClickMaxAll = PublishSubject.create<Parameter>()
    private val onClickPriceAll = PublishSubject.create<String>()
    private val onClickPriceEach = PublishSubject.create<String>()
    private val onClickEnter = PublishSubject.create<Int>()
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickNext = PublishSubject.create<Contract>()
    private val onSuccessRefresh = PublishSubject.create<Contract>()

    private val onClickConditionDetail = PublishSubject.create<Contract>()
    private val onClickWholeRoute = PublishSubject.create<Contract>()
    private val onClickPriceTable = PublishSubject.create<Contract>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<Contract>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .map { Timber.d("f9: MSTR_CTRK --> ${it}"); it }
                .filter{ listOf(it).isNotEmpty() }
                .map {
                    it.masterContractLineItems?.map{ it.isChecked = false}
                    it
                }
                .neverError()
                .bindToLifeCycle()
                .subscribe( parentId )

        ///////////////////////////////////////////////////////////////////////////////////////////
        // refresh (masterContractNumber)

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .map { contract ->
                    contract.masterContractLineItems = contract.masterContractLineItems?.filter{ it.offerQty > 0 }
                    contract
                }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToNext

        clickToNext.bindToLifeCycle()
                .map { contract  ->
                    contract.masterContractLineItems = contract.masterContractLineItems?.filter{ it.offerPrice > 0 }
                    contract
                }
                .subscribe(onClickNext)

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)

        clickToCheckAll.bindToLifeCycle()
                .subscribe(onClickCheckAll)

        clickToMaxAll.bindToLifeCycle()
                .subscribe(onClickMaxAll)

        clickToPriceAll.bindToLifeCycle()
                .subscribe(onClickPriceAll)

        clickToPriceEach.bindToLifeCycle()
                .subscribe(onClickPriceEach)

        clickToEnter.bindToLifeCycle()
                .subscribe(onClickEnter)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // click header card tables

        parentId.compose<Contract> { clickToConditionDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickConditionDetail)

        parentId.compose<Contract> { clickToWholeRoute.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickWholeRoute)

        parentId.compose<Contract> { clickToPriceTable.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
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
    override fun clickToPriceAll(parameter: String) = clickToPriceAll.onNext(parameter)
    override fun clickToPriceEach(parameter: String) = clickToPriceEach.onNext(parameter)
    override fun clickToEnter(parameter: Int) = clickToEnter.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToNext(parameter: Contract) = clickToNext.onNext(parameter)

    override fun clickToConditionDetail(parameter: Parameter) = clickToConditionDetail.onNext(parameter)
    override fun clickToWholeRoute(parameter: Parameter) = clickToWholeRoute.onNext(parameter)
    override fun clickToPriceTable(parameter: Parameter) = clickToPriceTable.onNext(parameter)

    // interface : out
    override fun onClickCheckAll() = onClickCheckAll
    override fun onClickMaxAll() = onClickMaxAll
    override fun onClickPriceAll() = onClickPriceAll
    override fun onClickPriceEach() = onClickPriceEach
    override fun onClickEnter() = onClickEnter

    override fun onClickConditionDetail() : Observable<Contract> = onClickConditionDetail
    override fun onClickWholeRoute() : Observable<Contract> = onClickWholeRoute
    override fun onClickPriceTable() : Observable<Contract> = onClickPriceTable

    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh

    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickNext() : Observable<Contract> = onClickNext
}

interface PriceInputs {
    fun clickToCheckAll(parameter: Boolean)
    fun clickToMaxAll(parameter: Parameter)
    fun clickToPriceAll(parameter: String)
    fun clickToPriceEach(parameter: String)
    fun clickToEnter(parameter: Int)
    fun clickToDone(parameter: Parameter)

    fun clickToNext(parameter: Contract)

    fun clickToConditionDetail(parameter: Parameter)
    fun clickToWholeRoute(parameter: Parameter)
    fun clickToPriceTable(parameter: Parameter)
}

interface PriceOutputs {
    fun onClickCheckAll(): Observable<Boolean>
    fun onClickMaxAll(): Observable<Parameter>
    fun onClickPriceAll(): Observable<String>
    fun onClickPriceEach(): Observable<String>
    fun onClickEnter(): Observable<Int>
    fun onClickNext(): Observable<Contract>
    fun onClickDone(): Observable<Parameter>

    fun onClickConditionDetail() : Observable<Contract>
    fun onClickWholeRoute() : Observable<Contract>
    fun onClickPriceTable() : Observable<Contract>

    fun onSuccessRefresh(): Observable<Contract>
}