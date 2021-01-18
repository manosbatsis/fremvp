package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.UserDao
import com.cyberlogitec.freight9.lib.model.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class UserRepository(val userDao: UserDao): UserRepositoryType {

    override fun storeUserInDb(user: User) {
        Observable.fromCallable { userDao.insert(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${user} users from API in DB...")
                }
    }

    override fun storeUsersInDb(users: List<User>) {
        Observable.fromCallable { userDao.insertAll(users) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${users.size} users from API in DB...")
                }
    }

    override fun getUsersFromDb(): Observable<List<User>> {
        return userDao.getUsers()
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} users from DB...")
                }
    }

    override fun getUserById(email: String): Observable<User> {
        return userDao.getUserById(email)
                .toObservable()
    }

}