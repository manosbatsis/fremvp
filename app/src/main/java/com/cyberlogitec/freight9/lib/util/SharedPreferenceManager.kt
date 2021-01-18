package com.cyberlogitec.freight9.lib.util

import android.content.Context
import android.content.SharedPreferences
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.Schedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.annotations.Async
import timber.log.Timber

class SharedPreferenceManager(val context: Context?) {

    val prefs: SharedPreferences? by lazy { context?.getSharedPreferences("common", Context.MODE_PRIVATE) }

    var lastLogin: Long?
        get() = prefs?.getLong("lastLogin", 0L)
        set(value) {
            prefs?.edit()?.let {
                it.putLong("lastLogin", value!!)
                it.commit()
            }
        }

    var userId: String?
        get() = prefs?.getString("userId", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("userId")
                else
                    it.putString("userId", value)
                it.commit()
            }
        }

    var token: String?
        get() = prefs?.getString("token", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("token")
                else
                    it.putString("token", value)
                it.commit()
            }
        }

    var email: String?
        get() = prefs?.getString("email", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("email")
                else
                    it.putString("email", value)
                it.commit()
            }
        }

    var name: String?
        get() = prefs?.getString("name", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("name")
                else
                    it.putString("name", value)
                it.commit()
            }
        }

    var organization: String?
        get() = prefs?.getString("organization", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("organization")
                else
                    it.putString("organization", value)
                it.commit()
            }
        }

    var remember: Boolean
        get() {
            if (prefs != null){
                return prefs!!.getBoolean("remember", false)
            } else {
                return false
            }
        }
        set(value) {
            prefs?.edit()?.let {
                it.putBoolean("remember", value)
                it.commit()
            }
        }

    var refresh: String?
        get() = prefs?.getString("refresh", null)
        set(value) {
            prefs?.edit()?.let {
                if (value == null)
                    it.remove("refresh")
                else
                    it.putString("refresh", value)
                it.commit()
            }
        }

    var expiresin: Long?
        get() = prefs?.getLong("expiresin", 0)
        set(value) {
            prefs?.edit()?.let {
                it.putLong("expiresin", value!!)
                it.commit()
            }
        }

    var offers: ArrayList<Offer>?
        get() {
            val json = prefs?.getString("offer_lists", "[]")
            return  Gson().fromJson(json, object: TypeToken<MutableList<Offer>>() {}.type) as ArrayList<Offer>
        }
        set(value) {
            prefs?.edit()?.let {
                val json = Gson().toJson(value)
                it.putString("offer_lists", json)
                it.apply()
            }
        }

}