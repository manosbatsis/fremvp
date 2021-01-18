package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofConditionDetailVm(context: Context) : BaseViewModel(context), PlanDetailInputs, PlanDetailOutputs  {
    val inPuts: PlanDetailInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickToPolSelect = PublishSubject.create<Parameter>()

    val outPuts: PlanDetailOutputs = this
    private val onClickMenu = PublishSubject.create<Parameter>()
    private val gotoPolSelect = PublishSubject.create<Parameter>()

    //private val onSuccessRefresh = PublishSubject.create<SofDraft>()
    //private val onSuccessRefresh2 = PublishSubject.create<List<SofDraftDetail>>()

    // intents
    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()


    init {

        intent.map { it.getStringExtra(Intents.OFER_GRP_NR) }
                .filter{ it.isNotEmpty() }
                .neverError()
                .bindToLifeCycle()
                .subscribe( parentId )

        // get draft from db and refresh ui  on resume
        /*
        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .flatMapMaybe { enviorment.sofDraftRepository.getSofDraft_oferGrpNr(it).handleToError(hideLoadingDialog).neverError() }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribeWith( onSuccessRefresh )
                .filter { it.oferGrpNr != null }
                .flatMapMaybe { enviorment.sofDraftDetailRepository.getSofDraftDetails_oferGrpNr(it.oferGrpNr!!).handleToError(hideLoadingDialog).neverError() }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh2 )
         */

        clickToMenu.bindToLifeCycle()
                .subscribe(onClickMenu)

        clickToPolSelect.bindToLifeCycle()
                .subscribe(gotoPolSelect)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // input
    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun clickToPolSelect(parameter: Parameter) = clickToPolSelect.onNext(parameter)

    // output
    override fun onClickMenu() : Observable<Parameter> = onClickMenu
    override fun gotoPolSelect() : Observable<Parameter> = gotoPolSelect
}

interface PlanDetailInputs {
    fun clickToMenu(parameter: Parameter)
    fun clickToPolSelect(parameter: Parameter)
}

interface PlanDetailOutputs {
    fun onClickMenu() : Observable<Parameter>
    fun gotoPolSelect() : Observable<Parameter>
}