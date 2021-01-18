package com.cyberlogitec.freight9.lib.rx

import com.cyberlogitec.freight9.lib.util.Quardruple
import io.reactivex.*
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import io.reactivex.subjects.Subject
import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * Created by Omjoon on 2017. 5. 29..
 */

enum class Parameter {
    CLICK, NULL, SUCCESS, EVENT
}

// View Click : ParameterClick
enum class ParameterClick {
    CLICK_CONDITION_DETAIL, CLICK_WHOLE_ROUTE, CLICK_PRICE_TABLE,
    CLICK_TERMS_MORE, CLICK_PAY_PLAN, CLICK_PERIOD_AND_VOLUME, CLICK_INVENTORY_VALUATION,
    CLICK_TITLE_LEFT, CLICK_TITLE_RIGHT_BTN, CLICK_TITLE_RIGHT_TV, CLICK_CLOSE, CLICK_FINISH,
    CLICK_SPIN_CONTAINER_TYPE, CLICK_QUANTITY_ENTER_BTN, CLICK_SELECT_BTN, CLICK_CONFIRM_BTN, CLICK_MAKE_BTN,
    CLICK_CARRIER_MORE, CLICK_JUMP_TO_OTHERS, CLICK_FAB, CLICK_TRANSACTION_STATEMENT, CLICK_OTHERS,
    CLICK_FILTER_FROM, CLICK_FILTER_TO, CLICK_FILTER_ALL,
    // use buy offer - route
    CLICK_TO_BOF_BACK, CLICK_TO_BOF_CLOSE, CLICK_TO_BOF_DONE,
    // search ui
    CLICK_SEARCH_INIT, CLICK_SEARCH_REMOVE, CLICK_SEARCH_TYPE, CLICK_SEARCH_POPUP
}

// View Any : Pair<ParameterAny, Any>
enum class ParameterAny {
    ANY_BATCH_INPUT_ET,             // Boolean
    ANY_TERM_AGREE,                 // Boolean
    ANY_ON_MARKET_CLOSED,           // Boolean
    ANY_VIEW_CHK,                   // Boolean
    ANY_DEALT_HISTORY_OPEN_CLOSED,  // Boolean
    ANY_JUMP_TO_OTHERS,             // enum, Object
    ANY_WEEK_TAB_INDEX,             // Int
    ANY_ITEM_INDEX,                 // Int
    ANY_SWIPE_INDEX,                // Int
    ANY_SWIPE_LEFT,                 // Int
    ANY_SWIPE_RIGHT,                // Int
    ANY_ITEM_OBJECT,                // Object
    ANY_SEARCH_GO_FILTER,           // OBject
    ANY_SEARCH_FILTER               // Object
}

fun <T> Observable<T>.handleToError(action: Subject<Throwable>? = null): Observable<T> = doOnError { action?.onNext(it) }
fun <T> Observable<T>.neverError(): Observable<T> = onErrorResumeNext { _: Throwable -> Observable.empty() }
fun <T> Observable<T>.neverError(action: Subject<Throwable>? = null): Observable<T> = handleToError(action).neverError()

fun <T> Single<T>.handleToError(action: Subject<Throwable>?): Single<T> = doOnError { action?.onNext(it) }
fun <T> Single<T>.neverError(): Maybe<T> = toMaybe().neverError()
fun <T> Single<T>.neverError(action: Subject<Throwable>? = null): Maybe<T>? = handleToError(action).neverError()

fun <T> Maybe<T>.handleToError(action: Subject<Throwable>? = null): Maybe<T> = doOnError { action?.onNext(it) }
fun <T> Maybe<T>.neverError(): Maybe<T> = onErrorResumeNext(onErrorComplete())
fun <T> Maybe<T>.neverError(action: Subject<Throwable>? = null): Maybe<T>? = handleToError(action).neverError()

fun Completable.handleToError(action: Subject<Throwable>? = null): Completable = doOnError { action?.onNext(it) }
fun Completable.neverError(): Completable = onErrorResumeNext { it.printStackTrace();Completable.never() }
fun Completable.neverError(action: Subject<Throwable>? = null): Completable = handleToError(action).neverError()

fun <T> Flowable<T>.handleToError(action: Subject<Throwable>? = null): Flowable<T> = doOnError { action?.onNext(it) }
fun <T> Flowable<T>.neverError(): Flowable<T> = onErrorResumeNext { _: Throwable -> Flowable.empty() }
fun <T> Flowable<T>.neverError(action: Subject<Throwable>? = null): Flowable<T> = handleToError(action).neverError()

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any> Observable<T1>.combineLatest(observable1: Observable<T2>
                                                                          , observable2: Observable<T3>
                                                                          , observable3: Observable<T4>): Observable<Quardruple<T1, T2, T3, T4>>
        = Observable.combineLatest(this, observable1, observable2, observable3, Function4 { p1, p2, p3, p4 -> Quardruple<T1, T2, T3, T4>(p1, p2, p3, p4) })

fun <T : Any, R : Any> Observable<T>.combineLatest(observable: Observable<R>): Observable<Pair<T, R>>
        = Observable.combineLatest(this, observable, BiFunction(::Pair))

fun <T : Any> T.toSingle(): Single<T> = Single.just(this)
fun <T : Any> Future<T>.toSingle(): Single<T> = Single.fromFuture(this)
fun <T : Any> Callable<T>.toSingle(): Single<T> = Single.fromCallable(this)
fun <T : Any> (() -> T).toSingle(): Single<T> = Single.fromCallable(this)