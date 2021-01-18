package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofRecentVm(context: Context) : BaseViewModel(context), RecentInputs, RecentOutputs {
    val inPuts: RecentInputs = this
    private val clickToNext = PublishSubject.create<Offer>()
    private val clickToItem = PublishSubject.create<Pair<Int, Boolean>>()

    val outPuts: RecentOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<List<Offer>>()
    private val onClickNext = PublishSubject.create<Offer>()
    private val onClickItem = PublishSubject.create<Pair<Int, Boolean>>()

    init {

        refresh.bindToLifeCycle()
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map{ SharedPreferenceManager(context).offers }
                .map { Timber.d("f9: pref->offers: ${it}"); it }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter{ listOf(it).isNotEmpty() }
                .subscribe(onSuccessRefresh)

        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        clickToItem.bindToLifeCycle()
                .subscribe(onClickItem)

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")

        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToNext(parameter: Offer) = clickToNext.onNext(parameter)
    override fun clickToItem(value: Pair<Int, Boolean>) = clickToItem.onNext(value)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<Offer>> = onSuccessRefresh
    override fun onClickNext(): Observable<Offer> = onClickNext
    override fun onClickItem(): Observable<Pair<Int, Boolean>> = onClickItem
}

interface RecentInputs {
    fun clickToNext(parameter: Offer)
    fun clickToItem(value: Pair<Int, Boolean>)
}

interface RecentOutputs {
    fun onSuccessRefresh(): Observable<List<Offer>>
    fun onClickNext(): Observable<Offer>
    fun onClickItem(): Observable<Pair<Int, Boolean>>
}