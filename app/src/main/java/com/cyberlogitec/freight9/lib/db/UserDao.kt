package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.User
import io.reactivex.Single


@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getUsers(): Single<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserById(email: String): Single<User>
}