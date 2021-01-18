package com.cyberlogitec.freight9.lib.db

import androidx.room.*
import com.cyberlogitec.freight9.lib.model.Message
import com.cyberlogitec.freight9.lib.model.User
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface MessageDao {

    @Query("SELECT * FROM messages")
    fun getMessages(): Single<List<Message>>

    @Insert
    fun insert(message: Message)

    @Insert
    fun insertAll(messages: List<Message>)

    @Delete
    fun deleteMessage(message: Message): Completable

    @Query("DELETE FROM messages WHERE msgSeq = :msgSeq ")
    fun deleteMessage(msgSeq: Long): Single<Int>

    @Query("DELETE FROM messages")
    fun deleteAll(): Completable
}