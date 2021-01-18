package com.cyberlogitec.freight9.common

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.util.sendNotification
import com.cyberlogitec.freight9.ui.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class F9FirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FirebaseService"

    /**
     * FirebaseInstanceIdService is deprecated.
     * this is new on firebase-messaging:17.1.0
     */
    override fun onNewToken(token: String) {
        Timber.d("f9: new Token: $token")
        sendRegistrationToServer(token)
    }

    /**
     * this method will be triggered every time there is new FCM Message.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("f9: From: %s", remoteMessage.from)

        if(remoteMessage.notification != null) {
            Timber.d("f9: Notification Message Body: ${remoteMessage.notification?.body}")
            sendNotification(remoteMessage.notification!!)
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Timber.d("f9: Firebase token : $token")
    }

    private fun sendNotification(notification: RemoteMessage.Notification) {
        val notificationManager =
                ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
                        as NotificationManager
        notificationManager.sendNotification(notification.title!!, notification.body!!, applicationContext)
    }
}
