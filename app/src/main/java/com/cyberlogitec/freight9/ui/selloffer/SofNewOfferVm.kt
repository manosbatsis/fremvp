package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofNewOfferVm(context: Context) : BaseViewModel(context), SofNewOfferInputs, SofNewOfferOutputs  {
    val inPuts: SofNewOfferInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickToCreate = PublishSubject.create<String>()

    val outPuts: SofNewOfferOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()
    private val gotoCreate = PublishSubject.create<String>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getStringExtra(Intents.MSTR_CTRK_NR) }
                .map {
                    Timber.d("f9: SofNewOfferVm : MSTR_CTRK_NR --> ${it}")
                    it
                }
                .bindToLifeCycle()
                .subscribe( parentId )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { masterContractNumber ->
                    Timber.d("f9: SofNewOfferVm : parentId--> ${masterContractNumber}");
                    if (masterContractNumber.isNotEmpty()) {
                        clickToCreate.onNext(masterContractNumber)
                    }
                    parentId.onNext(EmptyString)
                    masterContractNumber
                }
                .flatMapMaybe { enviorment.apiTradeClient.getPaymentPlans().handleToError(hideLoadingDialog).neverError() }
                .map{ Timber.d("f9: paymentPlans: ${it}")}
                .flatMapMaybe { enviorment.apiTradeClient.getPaymentTerm().handleToError(hideLoadingDialog).neverError() }
                .map{ Timber.d("f9: paymentTerm: ${it}")}
                .bindToLifeCycle()
                .subscribe()

        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        clickToCreate.bindToLifeCycle()
                .subscribe(gotoCreate)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun refresh(parameter: Parameter) = refresh.onNext(parameter)
    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun clickToCreate(masterContractNumber: String) = clickToCreate.onNext(masterContractNumber)

    // outPuts
    override fun gotoMenu() : Observable<Parameter> = gotoMenu
    override fun gotoCreate() : Observable<String> = gotoCreate
}

interface SofNewOfferInputs {
    fun refresh(parameter: Parameter)
    fun clickToMenu(parameter: Parameter)
    fun clickToCreate(masterContractNumber: String)
}

interface SofNewOfferOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoCreate() : Observable<String>
}