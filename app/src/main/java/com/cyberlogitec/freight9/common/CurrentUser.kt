package com.cyberlogitec.freight9.common

import com.cyberlogitec.freight9.lib.model.User
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import timber.log.Timber

data class CurrentUser(var user: User? = null, val sharedPreferenceManager: SharedPreferenceManager) {

    val id: Long?
        get() = user?.id

    init {
//        if (user == null)
//            if (sharedPreferenceManager.email != null && sharedPreferenceManager.name != null && sharedPreferenceManager.token != null)
//                user = User(id = sharedPreferenceManager.userId?.toLong(), email = sharedPreferenceManager.email, username = sharedPreferenceManager.name, token = sharedPreferenceManager.token)

//        if (user == null)
//            user = User(id = 1, email = "test@test.com", username = "ONE", token = "0123456789")
    }
    fun rememberUser(value: Boolean) {
        sharedPreferenceManager.remember = value
    }

    fun getRememberMe(): Boolean {
        return sharedPreferenceManager.remember
    }

    fun getRefreshToken(): String? {
        return sharedPreferenceManager.refresh
    }

    fun getCurrentUser():User?{
        return user
    }

    fun getCrcyCd(): String? {
        return sharedPreferenceManager.organization
    }

    fun getUsrId(): String? {
        return sharedPreferenceManager.email
    }

    fun getExpiresIn(): Long? {
        return sharedPreferenceManager.expiresin
    }

    fun isFirstLogin(): Boolean {
        var result = true
        sharedPreferenceManager.lastLogin?.let {
            result = it == 0L
        }
        return result
    }

    fun login(user: User) {
        this.user = user

        sharedPreferenceManager.userId = user.id?.toString()
        Timber.d("f9: login --> userId: ${sharedPreferenceManager.userId}")

        sharedPreferenceManager.email = user.email
        sharedPreferenceManager.name = user.username
        sharedPreferenceManager.token = user.token
        sharedPreferenceManager.organization = user.organization
        sharedPreferenceManager.refresh = user.refresh
        sharedPreferenceManager.remember = user.remember
        sharedPreferenceManager.expiresin = user.expiresin
        sharedPreferenceManager.lastLogin = System.currentTimeMillis()
    }

    fun logout() {
        user = null
        sharedPreferenceManager.userId = null
        sharedPreferenceManager.email = null
        sharedPreferenceManager.name = null
        sharedPreferenceManager.token = null
        sharedPreferenceManager.organization = null
        sharedPreferenceManager.refresh = null
        sharedPreferenceManager.remember = false
        sharedPreferenceManager.expiresin = 0L
    }
}