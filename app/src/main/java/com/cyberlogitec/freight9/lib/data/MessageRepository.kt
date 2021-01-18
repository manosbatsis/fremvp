package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.MessageDao
import com.cyberlogitec.freight9.lib.model.Message
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MessageRepository(val messageDao: MessageDao): MessageRepositoryType {

    override fun storeMessageInDb(message: Message) {
        Observable.fromCallable { messageDao.insert(message) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${message} message from API in DB...")
                }
    }

    override fun storeMessagesInDb(messages: List<Message>) {
        Observable.fromCallable { messageDao.insertAll(messages) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${messages.size} messages from API in DB...")
                }
    }

    override fun getMessagesFromDb(): Observable<List<Message>> {
        return messageDao.getMessages()
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} messages from DB...")
                }
    }

    override fun deleteMessageFromDb(message: Message): Completable =
            messageDao.deleteMessage(message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun deleteMessageFromDb(msgSeq: Long): Observable<Int> =
            messageDao.deleteMessage(msgSeq)
                    .toObservable()
                    .doOnNext {
                        Timber.d("f9: " + "Delete ${it} from DB...")
                    }

    override fun deleteMessagesFromDb(): Completable =
            messageDao.deleteAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
}