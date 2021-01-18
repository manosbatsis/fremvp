package com.cyberlogitec.freight9.viewmodels

import android.content.Context
import android.widget.Toast
import com.auth0.android.jwt.JWT
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.User
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class LoginViewModel(context: Context) : BaseViewModel(context), LoginViewModelInPuts, LoginViewModelOutPuts {

    val inPuts: LoginViewModelInPuts = this
    private val email = PublishSubject.create<String>()
    private val password = PublishSubject.create<String>()
    private val loginClick = PublishSubject.create<Parameter>()
    private val toggleRememberMe = PublishSubject.create<Boolean>()

    val outPuts: LoginViewModelOutPuts = this
    private val loginSuccess = PublishSubject.create<Boolean>()
    private val setLoginButtonIsEnabled = PublishSubject.create<Boolean>()

    init {
        Timber.v("f9: init")

        val emailAndPassword: Observable<Pair<String, String>> = Observables.combineLatest(email, password)
        val isValid = emailAndPassword.map { it.first.isNotEmpty() && it.second.isNotEmpty() }

        isValid.bindToLifeCycle()
                .subscribe(setLoginButtonIsEnabled)

        toggleRememberMe.bindToLifeCycle()
                .subscribe {
                    enviorment.currentUser.rememberUser(it)
                }

        /* ASIS: Login With id & pass
        emailAndPassword
                .compose<Pair<String, String>> { loginClick.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .map { User(email = it.first, password = it.second, id = null, token = null, username = null) }
                .flatMapMaybe { submit(it)  }
                .bindToLifeCycle()
                .subscribe {
                    Timber.d("f9: login(${it})")

                    // add user to preference
                    enviorment.currentUser.login(it)

                    // add user to db
                    enviorment.userRepository.storeUserInDb(it)

                    loginSuccess.onNext(true)
                }
         */

        emailAndPassword
                .compose<Pair<String, String>> { loginClick.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .bindToLifeCycle()
                .subscribe { loginData->
                    enviorment.apiTradeClient.getToken(loginData.first, loginData.second).map {
                        Timber.d("diver:/ access_token --> ${it.access_token}")
                        Toast.makeText(context, "Login Success!", Toast.LENGTH_SHORT).show()
                        val organization = JWT(it.access_token!!).getClaim("organization").asString()
                        Timber.v("organization:/ ${organization}")
                        User(
                            email = loginData.first,
                            password = loginData.second,
                            token = if (it.refresh_token.isNullOrEmpty()) it.access_token else it.refresh_token,
                            username = loginData.first,
                            organization = organization,
                            refresh = it.refresh_token,
                            expiresin = it.expires_in,
                            remember = enviorment.currentUser.getRememberMe())
                    }.onErrorReturn {
                        Toast.makeText(context, "Login Failed..", Toast.LENGTH_SHORT).show()
                        hideLoadingDialog.onNext( Throwable("OK"))
                        User(
                            email = loginData.first,
                            password = loginData.second,
                            token = "",
                            username = loginData.first,
                            organization = loginData.first,
                            refresh = "",
                            expiresin = 0,
                            remember = false)
                    }.filter {
                        it.token != ""
                    }.subscribe {loginUser->
                        enviorment.userRepository.getUsersFromDb().subscribe {userList->
                            Timber.v("diver:/ user list size=${userList.size}")
                            if(userList.filter { it.email == loginUser.email }.size == 0) {
                                enviorment.userRepository.storeUserInDb(loginUser)
                            }
                        }
                        enviorment.currentUser.login(loginUser)
                        loginSuccess.onNext(true)
                        hideLoadingDialog.onNext( Throwable("OK"))
                    }
                }

        // Temporary : allow login always (with temp id & pass)
//        emailAndPassword
//                .compose<Pair<String, String>> { loginClick.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
//                .throttleFirst(1000, TimeUnit.MILLISECONDS)
//                .map { User(email = "ONE@ONE.COM", password = "password", id = 1, token = "0123456789", username = "ONE") }
//                //.flatMapMaybe { submit(it)  }
//                .bindToLifeCycle()
//                .subscribe {
//                    Timber.d("f9: login(${it})")
//
//                    // add user to preference
//                    //enviorment.currentUser.login(it)
//
//                    // add user to db
//                    enviorment.userRepository.storeUserInDb(it)
//                    loginSuccess.onNext(true)
//                }
    }

    fun getRememberMe(): Boolean? {
        return enviorment.currentUser.getRememberMe()
    }

    private fun submit(user: User) = enviorment.apiTradeClient.logIn(user).neverError()
    override fun id(email: String) = this.email.onNext(email)
    override fun password(password: String) = this.password.onNext(password)
    override fun loginClick(paramter: Parameter) = loginClick.onNext(paramter)
    override fun toggleRememberMe(value: Boolean) = toggleRememberMe.onNext(value)
    override fun loginSuccess(): Observable<Boolean> = loginSuccess
    override fun setLoginButtonIsEnabled(): Observable<Boolean> = setLoginButtonIsEnabled
}

interface LoginViewModelInPuts {
    fun id(email: String)
    fun password(password: String)
    fun loginClick(paramter: Parameter)
    fun toggleRememberMe(value: Boolean)
}

interface LoginViewModelOutPuts {
    fun loginSuccess(): Observable<Boolean>
    fun setLoginButtonIsEnabled(): Observable<Boolean>
}

