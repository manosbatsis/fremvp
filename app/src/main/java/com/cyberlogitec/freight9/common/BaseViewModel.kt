package com.cyberlogitec.freight9.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.annotation.CallSuper
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.*
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.*
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresActivityViewModel(val value: KClass<out BaseViewModel>)

abstract class RootViewModel {
    abstract fun onCreate(context: Context, savedInstanceState: Bundle?)
    abstract fun onResume(view: BaseActivity<BaseViewModel>)
    abstract fun onDestroy()
}

open class BaseViewModel(context: Context) : RootViewModel() {

    private val viewChange = PublishSubject.create<BaseActivity<BaseViewModel>>()
    val view: Observable<BaseActivity<BaseViewModel>> = viewChange
    protected val enviorment = (context.applicationContext as App).component.enviorment()

    val error: PublishSubject<Throwable> = PublishSubject.create<Throwable>()
    val intent: PublishSubject<Intent> = PublishSubject.create<Intent>()

    val rxEventFinish: PublishSubject<Int> = PublishSubject.create<Int>()
    val rxEventInternet: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    val rxEventNetwork: PublishSubject<NetworkStatus> = PublishSubject.create<NetworkStatus>()
    val rxEventOfferDiscard: PublishSubject<Message> = PublishSubject.create<Message>()
    val rxEventOfferRefresh: PublishSubject<Message> = PublishSubject.create<Message>()

    val showLoadingDialog: PublishSubject<Any> = PublishSubject.create<Any>()
    val hideLoadingDialog: PublishSubject<Throwable> = PublishSubject.create<Throwable>()

    val onClickViewParameterClick = PublishSubject.create<ParameterClick>()
    val onClickViewParameterAny = PublishSubject.create<kotlin.Pair<ParameterAny, Any>>()

    @CallSuper
    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        error.bindToLifeCycle()
                .subscribe {
                    println("f9: error : ${it}")
                }
    }

    fun intent(intent: Intent) = this.intent.onNext(intent)

    fun clickViewParameterClick(parameterClick: ParameterClick) {
        onClickViewParameterClick.onNext(parameterClick)
    }

    fun clickViewParameterAny(parameterPair: kotlin.Pair<ParameterAny, Any>) {
        onClickViewParameterAny.onNext(parameterPair)
    }

    fun rxBusEvent(rxBusEvent: RxBusEvent) {
        when(rxBusEvent.code) {
            RxBusEvent.EVENT_FINISH -> {
                rxEventFinish.onNext(rxBusEvent.sent as Int)
            }
            RxBusEvent.EVENT_INTERNET -> {
                rxEventInternet.onNext(rxBusEvent.sent as Boolean)
            }
            RxBusEvent.EVENT_NETWORK -> {
                rxEventNetwork.onNext(rxBusEvent.sent as NetworkStatus)
            }
            RxBusEvent.EVENT_OFFER_DISCARD -> {
                rxEventOfferDiscard.onNext(rxBusEvent.sent as Message)
            }
            RxBusEvent.EVENT_OFFER_REFRESH -> {
                rxEventOfferRefresh.onNext(rxBusEvent.sent as Message)
            }
        }
    }

    @CallSuper
    override fun onResume(view: BaseActivity<BaseViewModel>) {
        onTakeView(view)
    }

    @CallSuper
    override fun onDestroy() {
        viewChange.onComplete()
    }

    private fun onTakeView(view: BaseActivity<BaseViewModel>) {
        viewChange.onNext(view)
    }

    fun <T> bindToLifecycle(): LifecycleTransformer<T> =
            LifecycleTransformer(view.switchMap { v -> v.lifecycle().map { e -> Pair.create(v, e) } }
                    .filter { ve -> isFinished(ve.first, ve.second) })

    fun <T> Observable<T>.bindToLifeCycle(): Observable<T> = compose(bindToLifecycle())
    fun <T> Flowable<T>.bindToLifeCycle(): Flowable<T> = compose(bindToLifecycle())
    fun <T> Single<T>.bindToLifeCycle(): Single<T> = compose(bindToLifecycle())
    fun <T> Maybe<T>.bindToLifeCycle(): Maybe<T> = compose(bindToLifecycle())
    fun Completable.bindToLifeCycle(): Completable = compose(bindToLifecycle<Any>())

    private fun isFinished(view: BaseActivity<*>, event: ActivityEvent): Boolean =
            event === ActivityEvent.DESTROY && (view as BaseActivity).isFinishing
}