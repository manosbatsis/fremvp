package com.cyberlogitec.freight9.ui.splash

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.lib.apistat.TokenResponse
import com.cyberlogitec.freight9.lib.model.Carrier
import com.cyberlogitec.freight9.lib.model.Container
import com.cyberlogitec.freight9.lib.model.Payment
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SplashViewModel(context: Context) : BaseViewModel(context), SplashInputs, SplashOutputs {
    final val PORT_LOAD_SIZE = 2000

    enum class LoadingParameter {
        LOADING_PORT, LOADING_SCHEDULE
    }

    val inPuts: SplashInputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val initializeData = PublishSubject.create<Parameter>()

    val outPuts: SplashOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Parameter>()

    var loginFlag = false

    companion object {
        val PRECARRIER_DATA = arrayListOf(
                Carrier("CMA", "CMA CGM", true),
                Carrier("COS", "COSCO SHIPPING Lines Co. Ltd", true),
                Carrier("EMC", "Evergreen Marine Corporation", true),
                Carrier("HLC", "Hapag Lloyd", true),
                Carrier("HMM", "Hyundai", true),
                Carrier("ISL", "Islamic Republic of Iran Shipping Lines", true),
                Carrier("KMD", "Korea Marine Transport Company", true),
                Carrier("MSC", "Mediterranean Shipping Company", true),
                Carrier("MSK", "Maersk", true),
                Carrier("ONE", "Ocean Network Express Pte. Ltd.", true),
                Carrier("PIL", "Pacific International Lines", true),
                Carrier("SIT", "SITC Container Lines Co., Ltd", true),
                Carrier("SLC", "SM Line Corporation, Seoul", true),
                Carrier("TSL", "T. S. Lines", true),
                Carrier("WHL", "Wan Hai Lines", true),
                Carrier("XPR", "X-Press Feeders", true),
                Carrier("YML", "Yang Ming Line", true),
                Carrier("ZIM", "Zim Israel Navigation Company", true)
        )

        val PRE_CONTAINER_TYPE_DATA = arrayListOf(
                Container("rdterm", "YY", "CY - CY", true),
                Container("rdterm", "YD", "CY - DOOR", false),
                Container("rdterm", "DY", "DOOR - CY", false),
                Container("rdterm", "DD", "DOOR - DOOR", false),
                Container("type", ContainerType.F_TYPE, "Dry", true),
                Container("type", ContainerType.R_TYPE, "Reefer", false),
                Container("type", ContainerType.E_TYPE, "Empty", false),
                Container("type", ContainerType.S_TYPE, "SOC", false),
                Container("size", "20'", "20ft", false),
                Container("size", "40'", "40ft", true),
                Container("size", "40'HC", "40ft High Cubic", false),
                Container("size", "45'HC", "45ft High Cubic", false)
        )
        val PRE_PAYMENT_DATA = arrayListOf(
                Payment("type", "P", "", "",
                        "", "", true),
                Payment("type", "C", "", "",
                        "", "", false),
                Payment("plan", "", "1", "10",
                        "80", "10", false),
                Payment("plan", "", "2", "30",
                        "60", "10", true),
                Payment("plan", "", "3", "50",
                        "40", "10", false)
        )
    }


    init {
        val checkDataAndStart = PublishSubject.create<Parameter>()
        val portValid = PublishSubject.create<Boolean>()
        val scheduleValid = PublishSubject.create<Boolean>()
        val dataValid = Observable.zip(portValid, scheduleValid, BiFunction { t1: Boolean, t2: Boolean -> t1 && t2 })

        dataValid.subscribe {
            if (it) onSuccessRefresh.onNext(Parameter.SUCCESS)
        }

        val portUpdate =
                Observable.fromCallable { enviorment.portRepository.deleteAll() }
                        .flatMap { enviorment.apiTradeClient.getPortDataByPage(PORT_LOAD_SIZE, 0).toObservable() }
                        .flatMap { Observable.range(0, it.totalPages) }
                        .concatMap { enviorment.apiTradeClient.getPortDataByPage(PORT_LOAD_SIZE, it).toObservable() }
                        .doOnComplete {
                            Timber.d("diver:/ port update complete, make base tree")
                            showLoadingDialog.onNext(Pair(Parameter.EVENT, LoadingParameter.LOADING_SCHEDULE))
                            enviorment.portRepository.initializeBaseTree()
                            portValid.onNext(true)
                        }

        val scheduleUpdate =
                Observable.fromCallable { enviorment.scheduleRepository.deleteAll() }
                        .flatMap { enviorment.apiTradeClient.getScheduleDataByPage(PORT_LOAD_SIZE, 0).toObservable() }
                        .flatMap { Observable.range(0, it.totalPages) }
                        .concatMap { enviorment.apiTradeClient.getScheduleDataByPage(PORT_LOAD_SIZE, it).toObservable() }
                        .doOnComplete {
                            Timber.d("diver:/ schedule update complete")
                            hideLoadingDialog.onNext( Throwable("OK"))
                            scheduleValid.onNext(true)
                        }

        refresh.bindToLifeCycle()
                .doOnNext {
                    enviorment.carrierRepository.storeCarriersInDb(PRECARRIER_DATA)
                    enviorment.containerRepository.storeContainersInDb(PRE_CONTAINER_TYPE_DATA)
                    enviorment.paymentRepository.storePaymentsInDb(PRE_PAYMENT_DATA)
                }.subscribeWith(checkDataAndStart)
                .doOnNext {
                    portValid.onNext(false)
                    scheduleValid.onNext(false)
                }.map { enviorment.portRepository.hasPortData() }
                .filter { it == false }
                .doOnNext {
                    portUpdate.subscribe {
                        Timber.d("diver:/ insert port data chunk seq::${it.requestPage}")
                        enviorment.portRepository.insertPortData(it.content)
                    }
                }.map { enviorment.scheduleRepository.hasScheduleData() }
                .filter { it == false }
                .doOnNext {
                    scheduleUpdate.subscribe {
                        Timber.d("diver:/ insert schedule data chunk seq::${it.requestPage}")
                        enviorment.scheduleRepository.insertScheduleData(it.content)
                    }
                }.subscribe {
                    Timber.v("diver:/ MDM Data Checked")
                    showLoadingDialog.onNext(Pair(Parameter.EVENT, LoadingParameter.LOADING_PORT))
                }

        checkDataAndStart
                .map { Timber.d("f9: checkDataAndStart 1") }
                .map {
                    enviorment.portRepository.hasPortData() && enviorment.scheduleRepository.hasScheduleData()
                }.map {
                    Timber.d("f9: checkDataAndStart 2 : hasPortData " + enviorment.portRepository.hasPortData()) ; it
                    Timber.d("f9: checkDataAndStart 2 : hasScheduleData " + enviorment.scheduleRepository.hasScheduleData()) ; it
                    Timber.d("f9: checkDataAndStart 2 :" + it) ; it
                }
                .filter { it == true }
                .map { Timber.d("f9: checkDataAndStart 3") }
                .bindToLifeCycle()
                .map { enviorment.currentUser.getRefreshToken() }
                .onErrorReturn {
                    Timber.d("f9: getRefreshToken error : " + it.message)
                    String.let { "" }
                }
                .doOnNext { refreshToken ->
                    Timber.v("diver:/ on splash viewmodel ${enviorment.currentUser.getRememberMe()}")
                    refreshToken?.let {
                        if (enviorment.currentUser.getRememberMe()) {
                            Observable.fromCallable { enviorment.apiTradeClient.getTokenRefresh(refreshToken) }
                                    .subscribe { it ->
                                        it.onErrorReturn {
                                            Timber.v("diver:/ ${it.localizedMessage} ${enviorment.currentUser.getRememberMe()} ")
                                            TokenResponse(access_token = null, expires_in = null, refresh_token = null, scope = null, token_type = null)
                                        }.subscribe { response ->
                                            Timber.v("diver:/ refresh process 1")
                                            if (response.access_token != null) {
                                                Timber.v("diver:/ refresh process 2")
                                                enviorment.currentUser.getUsrId()?.let { userId ->
                                                    enviorment.userRepository.getUserById(userId).subscribe { user ->
                                                        user.token = response.access_token
                                                        user.refresh = response.refresh_token
                                                        user.expiresin = response.expires_in
                                                        user.remember = true
                                                        enviorment.currentUser.login(user)
                                                        loginFlag = true
                                                    }
                                                }
                                            } else {
                                                Timber.v("diver:/ refresh process 3")
                                            }
                                        }
                                    }
                        }
                    }
                }
                .subscribe {
                    Timber.v("f9: checkDataAndStart: onSuccessRefresh")
                    enviorment.portRepository.initializeBaseTree()
                    onSuccessRefresh.onNext(Parameter.SUCCESS)
                }

        /*
        initData.bindToLifeCycle()
                .doOnNext {
                    enviorment.carrierRepository.storeCarriersInDb(PRECARRIER_DATA)
                    enviorment.containerRepository.storeContainersInDb(PRE_CONTAINER_TYPE_DATA)
                    enviorment.paymentRepository.storePaymentsInDb(PRE_PAYMENT_DATA)
                }.doOnNext {
                    enviorment.portRepository.deleteAll()
                }.subscribe {
                    enviorment.apiClient.getPortData().map {
                        Timber.v("diver:/ port size=${it.size}")
                        enviorment.portRepository.initialize(it)
                    }.subscribe()
                }
        */
        /*
        val simpleText = context.assets.open("port_data.json").bufferedReader().use { it.readText() }
        val portData = JsonParser().parse(simpleText) as JsonArray
        val portList = portData.map {
            Port(
                id = null,
                continentName = it.asJsonObject.get("cntiNm").asString,
                countryCode = it.asJsonObject.get("cntCd").asString,
                countryName = it.asJsonObject.get("cntNm").asString,
                subContinentCode = it.asJsonObject.get("sbCntiCd").asString,
                subContinentName = it.asJsonObject.get("sbCntiNm").asString,
                regionCode = it.asJsonObject.get("regnCd").asString,
                regionName = it.asJsonObject.get("regnNm").asString,
                portCode = it.asJsonObject.get("locCd").asString,
                portName = it.asJsonObject["locNm"].asString,
                isInland = (it.asJsonObject["portInldYn"].asString == "Y"),
                lastSelected = null
            )
        }
        enviorment.portRepository.initialize(portList)
        */

    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // in
    override fun initializeData(parameter: Parameter) = initializeData.onNext(parameter)

    // out
    override fun onSuccessRefresh(): Observable<Parameter> = onSuccessRefresh
}

interface SplashInputs {
    fun initializeData(parameter: Parameter)
}

interface SplashOutputs {
    fun onSuccessRefresh(): Observable<Parameter>
}

