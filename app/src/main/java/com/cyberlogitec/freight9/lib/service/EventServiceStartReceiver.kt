package com.cyberlogitec.freight9.lib.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber

class EventServiceStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, EventService::class.java).also {
                it.action = Actions.START_SERVICE.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Timber.d("f9: Starting the service in >=26 Mode from a BroadcastReceiver")
                    //context.startForegroundService(it)
                    return
                }
                Timber.d("f9: Starting the service in < 26 Mode from a BroadcastReceiver")
                //context.startService(it)
            }
        }
    }
}