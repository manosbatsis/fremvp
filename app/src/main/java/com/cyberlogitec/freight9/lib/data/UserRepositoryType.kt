package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.User
import io.reactivex.Observable

interface UserRepositoryType {

    fun storeUserInDb(user: User)

    fun storeUsersInDb(users: List<User>)

    fun getUsersFromDb(): Observable<List<User>>

    fun getUserById(email: String): Observable<User>

}