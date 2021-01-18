package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Message
import io.reactivex.Completable
import io.reactivex.Observable

interface MessageRepositoryType {

    fun storeMessageInDb(message: Message)

    fun storeMessagesInDb(messages: List<Message>)

    fun getMessagesFromDb(): Observable<List<Message>>

    fun deleteMessageFromDb(message: Message): Completable

    fun deleteMessageFromDb(msgSeq: Long): Observable<Int>

    fun deleteMessagesFromDb(): Completable

}