package com.cyberlogitec.freight9.lib.rx

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/*
 * Singleton instance
 * A simple rx bus that emits the data to the subscribers using Observable
 */
object RxBus {

    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

}