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

class SofPriceTableVm(context: Context) : BaseViewModel(context), PriceTableInputs, PriceTableOutputs {
    val inPuts: PriceTableInputs = this
    private val refresh = PublishSubject.create<Parameter>()

    val outPuts: PriceTableOutputs = this
    private val parentId = BehaviorSubject.create<Contract>()

    private val onSuccessRefresh = PublishSubject.create<Contract>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter{ listOf(it).isNotEmpty() }
                .neverError()
                .bindToLifeCycle()
                .subscribe( parentId )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )


    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun refresh(parameter: Parameter) = refresh.onNext(parameter)

    // outPuts
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
}

interface PriceTableInputs {
    fun refresh(parameter: Parameter)
}

interface PriceTableOutputs {
    fun onSuccessRefresh(): Observable<Contract>
}