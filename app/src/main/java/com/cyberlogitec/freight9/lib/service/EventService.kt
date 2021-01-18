package com.cyberlogitec.freight9.lib.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.config.Channel
import com.cyberlogitec.freight9.config.PushEventType.EVENT_TYPE_OFFER_CANCELED_FAIL
import com.cyberlogitec.freight9.config.PushEventType.EVENT_TYPE_OFFER_CANCELED_PASS
import com.cyberlogitec.freight9.config.PushEventType.EVENT_TYPE_OFFER_CREATED_PASS
import com.cyberlogitec.freight9.config.PushEventType.EVENT_TYPE_OFFER_DEALT_PASS
import com.cyberlogitec.freight9.lib.apistat.TokenResponse
import com.cyberlogitec.freight9.lib.model.Message
import com.cyberlogitec.freight9.lib.model.NetworkStatus
import com.cyberlogitec.freight9.lib.model.RxBusEvent
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.EVENT_INTERNET
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.EVENT_NETWORK
import com.cyberlogitec.freight9.lib.rx.RxBus
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.member.LoginActivity
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import io.reactivex.disposables.CompositeDisposable
import rx.Subscription
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.text.SimpleDateFormat
import io.reactivex.schedulers.Schedulers as ioReactivexScheduler
import rx.schedulers.Schedulers as rxSchedulers


class EventService : Service() {

    private var stompClient: StompClient ? = null
    //private var header: MutableList<StompHeader> = ArrayList()

    private var wakeLock: PowerManager.WakeLock? = null

    private val hName = "name"
    private val hAuthorization = "Authorization"
    private var responseTopic = "/queue/reply/"
    private var response: String = ""
    private var compositeDisposable: CompositeDisposable? = null
    private var isStompSocketExceptionError: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("f9: onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Timber.d("f9: using an intent with action $action")
            when (action) {
                Actions.START_SERVICE.name -> { startService() }
                Actions.STOP_SERVICE.name -> { stopService() }
                Actions.PUSH_NOTIFICATION.name -> { processMessage(intent.getSerializableExtra(Actions.PUSH_NOTIFICATION.name)) }
                else -> Timber.d("f9: This should never happen. No action in the received intent")
            }
        }
        else {
            Timber.d("f9: with a null intent. It has been probably restarted by the system.")
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        subscribeNetwork()
        Timber.d("f9: onCrate EventService")
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectStomp()
        safelyUnsubscribe()
    }

    private fun startService() {
        if (getServiceState(this) == ServiceState.STOPPED) {
            Timber.d("f9: Starting the foreground service task")

            startForeground(Channel.NOTIFICATION_SERVICE_ID, createNotification())
            setServiceState(this, ServiceState.STARTED)

            // we need this lock so our service gets not affected by Doze Mode
            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
                    .run {
                        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EventService::lock")
                                .apply { acquire() }
                    }
        }
        createStompClient()
    }

    private fun stopService() {
        Timber.d("f9: Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Timber.d("f9: Service stopped without being started: ${e.message}")
        }
        setServiceState(this, ServiceState.STOPPED)
    }

    /**
     * stomp client create & connect (only receive push message)
     */
    private fun createStompClient(doDisconnect: Boolean = true) {

        Timber.d("f9: createStompClient : " + if (doDisconnect) "doDisconnect" else "not doDisconnect")

        if (doDisconnect) {
            val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelNotifications()
            disconnectStomp()
        } else {
            stompClient?.let {
                if (it.isConnected) {
                    Timber.d("f9: createStompClient : stompClient is connected")
                    if (compositeDisposable == null) {
                        createDispResponseTopic(it)
                    }
                    return
                } else {
                    Timber.d("f9: createStompClient : stompClient is not connected")
                }
            }
        }

        Timber.e("f9: createStompClient : stompClient will be created...")

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.WS_URL).apply {
            connectStompClient(this)
        }
    }

    private fun getStompHeader() : List<StompHeader> {
        // Header 설정
        val share = SharedPreferenceManager(this)
        val header: MutableList<StompHeader> = ArrayList()
        header.add(StompHeader(hName, share.name))
        header.add(StompHeader(hAuthorization, share.token))
        Timber.d("f9: getStompHeader : header : $header")
        return header
    }

    private fun connectStompClient(stompClient: StompClient) {
        resetSubscriptions(true)
        stompClient.let {
            createDispLifeCycle(it)
            createDispResponseTopic(it)
            it.withClientHeartbeat(10000).withServerHeartbeat(10000)
            it.connect(getStompHeader())
        }
        Timber.e("f9: connectStompClient : stompClient connect...")
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    private fun createDispLifeCycle(stompClient: StompClient) {
        Timber.d("f9: createDispLifeCycle - add lifecycle to compositeDisposable")
        compositeDisposable?.add(stompClient.lifecycle()
                .subscribeOn(ioReactivexScheduler.io())
                .observeOn(ioReactivexScheduler.io())
                .subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            setMessage("stomp connection opened", true)
                            isStompSocketExceptionError = false
                        }
                        LifecycleEvent.Type.ERROR -> {
                            isStompSocketExceptionError = true
                            setMessage("stomp connection error \n${lifecycleEvent.exception}",true)
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            setMessage("stomp connection closed", true)
                            // isSocketException 이 아닌 경우에 closed 되면 reCreateStompClient 호출
                            if (isConnectedToInternet && !isStompSocketExceptionError) {
                                Timber.d("f9: createDispLifeCycle - No StompSocketException. call reCreateStompClient")
                                reCreateStompClient(networkStatus)
                            }
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                            setMessage("stomp failed server heartbeat", true)
                            if (isConnectedToInternet) {
                                reCreateStompClient(networkStatus)
                            }
                        }
                    }
                })
    }

    /**
     * add topic & subscribe
     */
    private fun createDispResponseTopic(stompClient: StompClient) {
        Timber.d("f9: createDispResponseTopic - add topic to compositeDisposable")
        compositeDisposable?.add(stompClient.topic(responseTopic + SharedPreferenceManager(this).name)
                .subscribeOn(ioReactivexScheduler.io())
                .observeOn(ioReactivexScheduler.io())
                .doOnError { t -> Timber.e("f9: topic : doOnError ${t}") }
                .subscribe({ stompMessage: StompMessage ->
                    processMessage(stompMessage.payload.fromJson<Message>())
                })
                { throwable: Throwable? -> Timber.e("f9: topic : Error on subscribe topic \n${throwable}") })
    }

    /**
     * check valid token
     */
    private fun checkAccessTokenValid() {
        /**
         * access token 이 expired 되었는지 체크
         * expired 되었으면 access token 재발급 후 createStompClient 에서 stomp header 재구성 됨
         */
        val share = SharedPreferenceManager(this)
        val isExpired = System.currentTimeMillis() >= (share.lastLogin!!.plus(share.expiresin!! * 1000))

        // For test by jgkim
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val expireDate = format.format(share.lastLogin!!.plus(share.expiresin!! * 1000))
        //Toast.makeText(this, "Expire at : [$expireDate]", Toast.LENGTH_LONG).show()
        Timber.d("f9: token expired : $isExpired - $expireDate")

        if (isExpired) {
            val environment = (this.applicationContext as App).component.enviorment()
            environment.currentUser.getRefreshToken()?.let {
                environment.apiTradeClient.getTokenRefresh(it)
                        .onErrorReturn { throwable ->
                            Timber.d("f9 : checkAccessTokenValid : error = ${throwable.message}")
                            TokenResponse(access_token = null, expires_in = null, refresh_token = null, scope = null, token_type = null)
                        }
                        .subscribe { response : TokenResponse ->
                            if (response.access_token != null) {
                                environment.currentUser.getUsrId()?.let { userId ->
                                    environment.userRepository.getUserById(userId).subscribe { user ->
                                        user.token = response.access_token
                                        user.refresh = response.refresh_token
                                        user.expiresin = response.expires_in
                                        user.remember = true
                                        environment.currentUser.login(user)
                                        Timber.d("f9: checkAccessTokenValid : users : $user")
                                        createStompClient(false)
                                    }
                                }
                            } else {
                                // TODO : error(Network) 발생으로 token 이 invalid 한 경우.
                                //  재 로그인 시도? call createStompCliet?, stop Service? (정책 필요)
                                Timber.d("f9 : checkAccessTokenValid : go LoginActivity")
                                startActivity(Intent(this, LoginActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            }
                        }
            }
        } else {
            createStompClient(false)
        }
    }

    /**
     * reconnect stomp client
     */
    private fun reCreateStompClient(networkStatus: NetworkStatus?) {
        Handler(Looper.getMainLooper()).postDelayed({
            disconnectStomp()
            if (isNetworkConnected(networkStatus)) {
                Timber.d("f9: call checkAccessTokenValid")
                checkAccessTokenValid()
            } else {
                Timber.d("f9: call nothing")
            }
        }, 1000)
    }

    /**
     * disconnect stomp client
     */
    private fun disconnectStomp() {
        resetSubscriptions(false)
        stompClient?.disconnect()
        stompClient = null
        Timber.d("f9: disconnectStomp : stomp disconnected")
    }

    /**
     * stomp subscribe init
     */
    private fun resetSubscriptions(isReset: Boolean) {
        compositeDisposable?.let {
            it.clear()
            Timber.e("f9: compositeDisposable disposed. cleared")
        }
        if (isReset) {
            compositeDisposable = CompositeDisposable()
            Timber.e("f9: compositeDisposable reInit")
        }
    }

    /**
     * display formatted log message
     */
    private fun setMessage(msg: String, isReset: Boolean) {
        Timber.e("f9: stomp message : $msg")
        if(isReset) response = (msg + "\n")
        else response += (msg + "\n")
    }

    /**
     * process received push message
     */
    @Synchronized
    private fun processMessage(message: Any?) {
        when(message) {
            is Message -> {
                Timber.d("f9: processMessage : push message - $message")

                processOfferCancelCreatedMessage(message)

                // DB에 저장
                message.readYn = "N"    // 최초저장시 "N"
                App.instance.component.enviorment().messageRepository.storeMessageInDb(message)

                // Notification 발생
                val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val title = message.messageTitle
                val body = message.errorMessage
                notificationManager.sendNotification(title!!, body!!, applicationContext)

                // TODO : For MVP Test : 연달아서 메시지 수신된 경우 delay를 줘서 ui 적으로 보여주기 위함
                Thread.sleep(1000)
            }
            else -> throw IllegalArgumentException("Invalid message")
        }
    }

    /**
     * offer create pass, dealt pass 시 처리
     * offer cancel 에 대한 pass, fail 시 처리
     */
    private fun processOfferCancelCreatedMessage(message: Any?) {
        when(message) {
            is Message -> {
                message.eventType.let { eventType ->
                    if (eventType.equals(EVENT_TYPE_OFFER_CREATED_PASS, true) ||
                            eventType.equals(EVENT_TYPE_OFFER_DEALT_PASS, true)) {
                        // YourOffersActivity 에서 처리
                        Timber.d("f9: [RxBusEvent.EVENT_OFFER_REFRESH]")
                        RxBus.publish(RxBusEvent(RxBusEvent.EVENT_OFFER_REFRESH, message))
                    } else if (eventType.equals(EVENT_TYPE_OFFER_CANCELED_PASS, true) ||
                            eventType.equals(EVENT_TYPE_OFFER_CANCELED_FAIL, true)) {
                        // YourOffersDetailActivity, BofWizardActivity, SofWizardActivity 에서 처리
                        Timber.d("f9: [RxBusEvent.EVENT_OFFER_DISCARD]")
                        RxBus.publish(RxBusEvent(RxBusEvent.EVENT_OFFER_DISCARD, message))
                    }
                }
            }
        }
    }

    /**
     * endless service 실행
     */
    private fun createNotification(): Notification {
        // STOMP Service channel 생성 (need for O, P, Q)
        createNotificationTradeChannel()

//        val pendingIntent: PendingIntent = Intent(this, SplashActivity::class.java)
//                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                .putExtra(GOTO_MESSAGE_BOX, true)
//                .let { notificationIntent ->
//                    PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//                }

        return NotificationCompat.Builder(this, Channel.CHANNEL_SERVICE_ID)
                .setContentTitle(getString(R.string.notification_service_channel_title))
                .setContentText(getString(R.string.notification_service_channel_text))
                .setContentIntent(null)                // not use pendingIntent
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()
    }

    private var connectivitySub: Subscription? = null
    private var internetSub: Subscription? = null
    private var isConnectedToInternet: Boolean = false
    private var networkStatus: NetworkStatus? = null

    /**
     * check network connected status
     */
    private fun isNetworkConnected(networkStatus: NetworkStatus?) = networkStatus?.let { it ->
        it.state == NetworkInfo.State.CONNECTED
    } ?: false

    /**
     * subscribe network status
     * send broadcasting network status
     */
    private fun subscribeNetwork() {
        if (connectivitySub == null) {
            connectivitySub = ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                    .subscribeOn(rxSchedulers.io())
                    .observeOn(rxSchedulers.io())
                    .subscribe { connectivity ->

                        val networkStatus = NetworkStatus(
                                connectivity.state,
                                connectivity.type,
                                connectivity.name)

                        Timber.d("f9: subscribeNetwork : Network = $connectivity")

                        this.networkStatus?.let {
                            // Wifi <-> Mobile 간 connect changed 인 경우.
                            if (networkStatus.state == NetworkInfo.State.CONNECTED) {
                                /*
                                * CONNECTED 상태인 경우에만 reCreateStompClient 이 호출
                                * */
                                Timber.d("f9: call reCreateStompClient to "
                                        + if (networkStatus.type == 0) "[MOBILE]" else "[WIFI]")

                                reCreateStompClient(networkStatus)
                            }
                        } ?: Timber.d("f9: init started. not call reCreateStompClient!!!")

                        this.networkStatus = networkStatus
                        RxBus.publish(RxBusEvent(EVENT_NETWORK, networkStatus))
                    }
        }

        if (internetSub == null) {
            internetSub = ReactiveNetwork.observeInternetConnectivity()
                    .subscribeOn(rxSchedulers.io())
                    .observeOn(rxSchedulers.io())
                    .subscribe { isConnectedToInternet ->
                        Timber.d("f9: subscribeNetwork : Connection = $isConnectedToInternet")
                        this.isConnectedToInternet = isConnectedToInternet
                        RxBus.publish(RxBusEvent(EVENT_INTERNET, this.isConnectedToInternet))
                    }
        }
    }

    private fun safelyUnsubscribe() {
        safelyUnsubscribe(connectivitySub)
        safelyUnsubscribe(internetSub)
    }

    private fun safelyUnsubscribe(subscription: Subscription?) {
        if (subscription != null && !subscription.isUnsubscribed) {
            subscription.unsubscribe()
        }
    }
}