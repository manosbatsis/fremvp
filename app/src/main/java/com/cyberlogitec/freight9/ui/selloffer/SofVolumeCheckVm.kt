package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofVolumeCheckVm(context: Context) : BaseViewModel(context), VolumeCheckInputs, VolumeCheckOutputs  {
    val inPuts: VolumeCheckInputs = this
    private val clickToEdit = PublishSubject.create<Parameter>()

    val outPuts: VolumeCheckOutputs = this
    private val onClickEdit = PublishSubject.create<String>()
    private val onSuccessRefresh = PublishSubject.create<Contract>()

    // intents
    private val parentId = BehaviorSubject.create<Contract>()
    private val refresh = PublishSubject.create<Parameter>()
    
    init {

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( parentId )

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        parentId.compose<Contract> { clickToEdit.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map{ it.masterContractNumber }
                .bindToLifeCycle()
                .subscribe(onClickEdit)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun clickToEdit(parameter: Parameter) = clickToEdit.onNext(parameter)

    // outPuts
    override fun onClickEdit() : Observable<String> = onClickEdit
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
}

interface VolumeCheckInputs {
    fun clickToEdit(parameter: Parameter)
}

interface VolumeCheckOutputs {
    fun onClickEdit() : Observable<String>
    fun onSuccessRefresh(): Observable<Contract>
}