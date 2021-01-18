package com.cyberlogitec.freight9.common

import android.app.Application
import android.content.Intent
import android.os.Build
import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.lib.di.AppComponent
import com.cyberlogitec.freight9.lib.di.AppModule
import com.cyberlogitec.freight9.lib.di.DaggerAppComponent
import com.cyberlogitec.freight9.lib.service.Actions
import com.cyberlogitec.freight9.lib.service.EventService
import com.cyberlogitec.freight9.lib.service.ServiceState
import com.cyberlogitec.freight9.lib.service.getServiceState
import com.facebook.stetho.Stetho
import timber.log.Timber

class App : Application() {

    lateinit var component: AppComponent
        private set

    companion object {
        lateinit var instance: App
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        this.component = DaggerAppComponent.builder().appModule(AppModule(this)).build()

        if (BuildConfig.DEBUG) {
            // Timber Initialize
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())

            // Stetho Initialize
            Stetho.initializeWithDefaults(this)
        }
    }
}