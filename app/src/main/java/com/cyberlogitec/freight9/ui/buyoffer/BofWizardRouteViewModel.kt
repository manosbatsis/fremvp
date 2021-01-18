package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.combineLatest
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_LANE
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_POD
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_POL
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_RECENTLY
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardRouteActivity.Companion.STEP_SELECT
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*

class BofWizardRouteViewModel(context: Context) : BaseViewModel(context), Inputs, Outputs {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: Inputs = this
    private val requestLoadRouteData = PublishSubject.create<Parameter>()
    private val requestLoadScheduleData = PublishSubject.create<Boolean>()
    private val requestLoadSchedulePartialData = PublishSubject.create<Schedule>()

    private val requestGoToOtherStep = PublishSubject.create<Pair<Int, Any?>>()

    private val stepRecently = BehaviorSubject.create<List<Offer>>()
    private val requestStepRecently = PublishSubject.create<Parameter>()

    private val stepLane = BehaviorSubject.create<List<Schedule>>()
    private val requestStepLane = PublishSubject.create<Parameter>()

    private val stepPol = BehaviorSubject.create<List<Schedule>>()
    private val requestStepPol = PublishSubject.create<Parameter>()
    private val requestStepPolInitList = PublishSubject.create<Parameter>()

    private val stepPod = BehaviorSubject.create<List<Schedule>>()
    private val requestStepPod = PublishSubject.create<Parameter>()
    private val requestStepPodInitList = PublishSubject.create<Parameter>()

    private val stepSelect = BehaviorSubject.create<List<Schedule>>()
    private val requestStepSelect = PublishSubject.create<Parameter>()
    private val requestStepSelectSkip = PublishSubject.create<Parameter>()

    private val requestGoToBuyVolume = PublishSubject.create<Offer>()

    private val stepBuyVolume = BehaviorSubject.create<Offer>()
    private val requestSaveToPreference = PublishSubject.create<Boolean>()

    private val requestSetDataStatus = PublishSubject.create<Pair<Int, Any?>>()

    private val requestSearchInit = PublishSubject.create<Parameter>()
    private val requestSearchFilter = PublishSubject.create<Pair<Boolean, String>>()
    private val requestSearchRemove = PublishSubject.create<String>()
    private val requestSearchSelectSet = PublishSubject.create<Pair<String, String>>()

    private val requestSearchLanePopup = PublishSubject.create<Parameter>()
    private val requestSearchPolPopup = PublishSubject.create<Parameter>()
    private val requestSearchPodPopup = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: Outputs = this
    private val onGoToOtherStep = PublishSubject.create<Pair<Int, Any?>>()

    private val onGoToStepRecently = PublishSubject.create<List<Offer>>()
    private val onGoToStepLane = PublishSubject.create<List<Schedule>>()
    private val onGoToStepPol = PublishSubject.create<List<Schedule>>()
    private val onStepPolInitList = PublishSubject.create<Parameter>()
    private val onGoToStepPod = PublishSubject.create<List<Schedule>>()
    private val onStepPodInitList = PublishSubject.create<Parameter>()
    private val onLoadSchedulePartialData = PublishSubject.create<List<Schedule>>()
    private val onGoToStepSelect = PublishSubject.create<Offer>()

    private val onGoToBuyVolume = PublishSubject.create<Offer>()

    private val onSetDataStatus = PublishSubject.create<Pair<Int, Any?>>()
    private val onSearchInit = PublishSubject.create<Parameter>()
    private val onSearchFilter = PublishSubject.create<Pair<Boolean, String>>()
    private val onSearchRemove = PublishSubject.create<String>()
    private val onSearchSelectSet = PublishSubject.create<Pair<String, String>>()

    private val onSearchLanePopup = PublishSubject.create<Parameter>()
    private val onSearchPolPopup = PublishSubject.create<Parameter>()
    private val onSearchPodPopup = PublishSubject.create<Parameter>()

    private val onSuccessSaveToPreference = PublishSubject.create<Boolean>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        val newOffer = BehaviorSubject.create<Offer>()
        val newOfferAndSchedules: Observable<Pair<Offer, List<Schedule>>> = newOffer.combineLatest(stepSelect)

        // laod route data from shared preference
        requestLoadRouteData
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { SharedPreferenceManager(context).offers }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter{ listOf(it).isNotEmpty() }
                .subscribe {
                    // init to STEP_RECENTLY step
                    requestGoToOtherStep.onNext(Pair(STEP_RECENTLY, it))
                }

        // Load schedule data from DB
        requestLoadScheduleData
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { Pair(it, enviorment.scheduleRepository.loadAllSchedules().handleToError(hideLoadingDialog).neverError()) }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { pair ->
                    pair.second.subscribe { schedules ->
                        if (pair.first) {
                            // Select > Lane
                            stepLane.onNext(schedules)
                        } else {
                            // init to STEP_LANE step (Recently > Lane)
                            requestGoToOtherStep.onNext(Pair(STEP_LANE, schedules))
                        }
                    }
                }

        requestLoadSchedulePartialData
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe {
                    enviorment.scheduleRepository.loadSchedules(it.serviceLaneCode, it.polCode)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe {
                    onLoadSchedulePartialData.onNext(it)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Next Step, Back Step

        requestGoToOtherStep
                .map { onGoToOtherStep.onNext(it); it }
                .bindToLifeCycle()
                .subscribe { pair ->
                    when (pair.first) {
                        STEP_RECENTLY -> {
                            pair.second?.let {
                                if (it is List<*>) {
                                    val offers = it as List<Offer>
                                    val clonedOffers = offers.map { it.copy() }
                                    clonedOffers.map { it.isChecked = false }
                                    stepRecently.onNext(clonedOffers)
                                }
                            }
                            // Fragment 에서 onGoToStepRecently 으로 받는다
                            requestStepRecently.onNext(Parameter.EVENT)
                        }
                        STEP_LANE -> {
                            pair.second?.let {
                                val schedules = it as List<Schedule>
                                val clonedSchedules = schedules.map { it.copy() }
                                stepLane.onNext(clonedSchedules)
                            }
                            // Fragment 에서 onGoToStepLane 으로 받는다
                            requestStepLane.onNext(Parameter.EVENT)
                        }
                        STEP_POL -> {
                            pair.second?.let {
                                val schedules = it as List<Schedule>
                                val clonedSchedules = schedules.map { it.copy() }
                                clonedSchedules.map { it.isPolChecked = false }
                                stepPol.onNext(clonedSchedules)
                            }
                            // Fragment 에서 onGoToStepPol 으로 받는다
                            requestStepPol.onNext(Parameter.EVENT)
                        }
                        STEP_POD -> {
                            pair.second?.let {
                                val schedules = it as List<Schedule>
                                val clonedSchedules = schedules.map { it.copy() }
                                clonedSchedules.map { it.isPodChecked = false }
                                stepPod.onNext(clonedSchedules)
                            }
                            // Fragment 에서 onGoToStepPod 으로 받는다
                            requestStepPod.onNext(Parameter.EVENT)
                        }
                        STEP_SELECT -> {
                            // Offer 를 만들어서 전달
                            pair.second?.let {
                                if (it is List<*>) {
                                    // Pod > Select
                                    val schedules = it as List<Schedule>
                                    stepSelect.onNext(schedules)

                                    val offer = Offer(offerTypeCode = OFFER_TYPE_CODE_BUY)
                                    offer.offerRoutes = mutableListOf()
                                    newOffer.onNext(offer)

                                    // Fragment 에서 onGoToStepSelect 으로 받는다
                                    requestStepSelectSkip.onNext(Parameter.EVENT)
                                } else {
                                    // Recent > Select
                                    val offer = it as Offer
                                    newOffer.onNext(offer)

                                    // Fragment 에서 onGoToStepSelect 으로 받는다
                                    requestStepSelect.onNext(Parameter.EVENT)
                                }
                            }
                        }
                    }
                }

        requestGoToBuyVolume
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { offer ->
                    // SELECT > Buy Volume 으로 이동 시 Preference 에 저장
                    requestSaveToPreference.onNext(false)

                    // deep copy 후 Buy Volume 으로 전달
                    val offerDeepCopy = offer.toJson().fromJson<Offer>()
                    offerDeepCopy
                }
                .map { offerDeepCopy ->

                    val thisYearN = getThisYearNumber()
                    val thisWeekN = getTodayWeekNumber()
                    val thisMaxWeekN = Calendar.getInstance().getYearWeeks(thisYearN)
                    val offerLineItems = mutableListOf<OfferLineItem>()

                    // this year weeks
                    for (i in thisWeekN .. thisMaxWeekN) {
                        val baseYearWeek = String.format("%4d%02d", thisYearN, i)
                        offerLineItems.add(OfferLineItem(baseYearWeek = baseYearWeek))
                    }

                    // next year weeks
                    for (j in 1 until thisWeekN) {
                        val baseYearWeek = String.format("%4d%02d", thisYearN + 1, j)
                        offerLineItems.add(OfferLineItem(baseYearWeek = baseYearWeek))
                    }

                    offerDeepCopy.offerLineItems = offerLineItems
                    offerDeepCopy
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe(onGoToBuyVolume)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Move step : 각 fragment 에서 subscribe - onGoToStepRecently, onGoToStepLane, ...

        stepRecently.compose<List<Offer>> { requestStepRecently.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { offers ->
                    val clonedOffers = offers.map { it.copy() }
                    clonedOffers
                }
                .bindToLifeCycle()
                .subscribe(onGoToStepRecently)

        stepLane.compose<List<Schedule>> { requestStepLane.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { schedules ->
                    val clonedSchedules = schedules.map { it.copy() }
                    clonedSchedules
                }
                .bindToLifeCycle()
                .subscribe(onGoToStepLane)

        stepPol.compose<List<Schedule>> { requestStepPol.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { schedules ->
                    val clonedSchedules = schedules.map { it.copy() }
                    clonedSchedules
                }
                .bindToLifeCycle()
                .subscribe(onGoToStepPol)

        stepPod.compose<List<Schedule>> { requestStepPod.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { schedules ->
                    val clonedSchedules = schedules.map { it.copy() }
                    clonedSchedules
                }
                .bindToLifeCycle()
                .subscribe(onGoToStepPod)

        newOffer.compose<Offer> { requestStepSelect.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .map { offer ->
                    stepBuyVolume.onNext(offer)
                    offer
                }
                .bindToLifeCycle()
                .subscribe(onGoToStepSelect)

        newOfferAndSchedules.compose<Pair<Offer, List<Schedule>>> {
            requestStepSelectSkip.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .filter { it.second.isNotEmpty() && listOf(it.first).isNotEmpty() }
                .map { pair ->
                    pair.second.find { it.isPolChecked && it.isPodChecked }?.let { schedule ->
                        // it.second
                        pair.first.serviceLaneCode = schedule.serviceLaneCode
                        pair.first.serviceLaneName = schedule.serviceLaneName
                    }
                    pair
                }
                .map { pair ->
                    pair.second.let { schedules2 ->
                        val pols = schedules2.filter{ it.isPolChecked }.distinctBy { it.polCode }
                        val pods = schedules2.filter{ it.isPodChecked }.distinctBy { it.podCode }

                        val polXpod = cartesianProduct(pols, pods)
                        val offerRoutes = mutableListOf<OfferRoute>()

                        polXpod.forEachIndexed { idx, item ->
                            offerRoutes.add( OfferRoute(offerRegSeq = idx+1, locationCode = item.first.polCode, locationTypeCode = LocationTypeCode.POR, locationName = item.first.polName) )
                            offerRoutes.add( OfferRoute(offerRegSeq = idx+1, locationCode = item.first.polCode, locationTypeCode = LocationTypeCode.POL, locationName = item.first.polName) )
                            offerRoutes.add( OfferRoute(offerRegSeq = idx+1, locationCode = item.second.podCode, locationTypeCode = LocationTypeCode.POD, locationName = item.second.podName) )
                            offerRoutes.add( OfferRoute(offerRegSeq = idx+1, locationCode = item.second.podCode, locationTypeCode = LocationTypeCode.DEL, locationName = item.second.podName) )
                        }
                        pair.first.offerRoutes = offerRoutes
                    }
                    pair
                }
                .map { pair ->
                    enviorment.currentUser.getCrcyCd()?.let {
                        pair.first.tradeCompanyCode = it
                    }
                    pair
                }
                .map { pair ->
                    pair.first.tradeRoleCode = "001"
                    pair
                }
                .map { it.first }
                .map { offer2 ->
                    // SELECT step 으로 이동 시 stepBuyVolume 에 offer 넣어놓는다
                    stepBuyVolume.onNext(offer2)
                    offer2
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe(onGoToStepSelect)

        stepBuyVolume.compose<Pair<Boolean, Offer>> { requestSaveToPreference.withLatestFrom(it,
                BiFunction { isFinish, offer ->
                    Pair(isFinish, offer)
                })}
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { pair ->
                    // Recently 에서 item 선택 후 "SELECT" 로 넘어온 경우 isChecked 가 true 이므로 false로 설정 후 중복 check
                    pair.second.isChecked = false

                    // 중복 offer 제거 후 Preference에 set
                    val previousOffers = SharedPreferenceManager(context).offers!!
                    previousOffers.remove(pair.second)
                    previousOffers.add(0, pair.second)
                    SharedPreferenceManager(context).offers = previousOffers

                    // stepRecently 를 update
                    val clonedOffers = previousOffers.map { it.copy() }
                    clonedOffers.map { it.isChecked = false }
                    stepRecently.onNext(clonedOffers)

                    pair.first
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe { isFinish ->
                    // Preference 에 저장 이후 처리에 사용
                    onSuccessSaveToPreference.onNext(isFinish)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Set Buttom Button Status

        requestSetDataStatus
                .bindToLifeCycle()
                .subscribe {
                    onSetDataStatus.onNext(it)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Set POL, POD Init Listview

        requestStepPolInitList
                .bindToLifeCycle()
                .subscribe {
                    onStepPolInitList.onNext(it)
                }

        requestStepPodInitList
                .bindToLifeCycle()
                .subscribe {
                    onStepPodInitList.onNext(it)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Search Area Status
        /**
         * Recently : init search
         */
        requestSearchInit
                .bindToLifeCycle()
                .subscribe { onSearchInit.onNext(it) }
        /**
         * Recently : Listview filter
         */
        requestSearchFilter
                .bindToLifeCycle()
                .subscribe { onSearchFilter.onNext(it) }

        /**
         * Lane : remove schedule
         */
        requestSearchRemove
                .bindToLifeCycle()
                .subscribe { onSearchRemove.onNext(it) }

        /**
         * Lane : set schedule
         */
        requestSearchSelectSet
                .bindToLifeCycle()
                .subscribe { onSearchSelectSet.onNext(it) }

        /**
         * Lane search popup
         */
        requestSearchLanePopup
                .bindToLifeCycle()
                .subscribe { onSearchLanePopup.onNext(it) }

        /**
         * Pol search popup
         */
        requestSearchPolPopup
                .bindToLifeCycle()
                .subscribe { onSearchPolPopup.onNext(it) }

        /**
         * Pod search popup
         */
        requestSearchPodPopup
                .bindToLifeCycle()
                .subscribe { onSearchPodPopup.onNext(it) }
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // interface : in
    override fun requestLoadRouteData(parameter: Parameter) = requestLoadRouteData.onNext(parameter)
    override fun requestLoadScheduleData(isLoadOnly: Boolean) = requestLoadScheduleData.onNext(isLoadOnly)
    override fun requestLoadSchedulePartialData(schedule: Schedule) = requestLoadSchedulePartialData.onNext(schedule)

    override fun requestGoToOtherStep(pair: Pair<Int, Any?>) = requestGoToOtherStep.onNext(pair)

    override fun stepRecently(offers: List<Offer>) = stepRecently.onNext(offers)
    override fun requestStepRecently(parameter: Parameter) = requestStepRecently.onNext(parameter)

    override fun stepLane(schedules: List<Schedule>) = stepLane.onNext(schedules)
    override fun requestStepLane(parameter: Parameter) = requestStepLane.onNext(parameter)

    override fun stepPol(schedules: List<Schedule>) = stepPol.onNext(schedules)
    override fun requestStepPol(parameter: Parameter) = requestStepPol.onNext(parameter)
    override fun requestStepPolInitList(parameter: Parameter) = requestStepPolInitList.onNext(parameter)

    override fun stepPod(schedules: List<Schedule>) = stepPod.onNext(schedules)
    override fun requestStepPod(parameter: Parameter) = requestStepPod.onNext(parameter)
    override fun requestStepPodInitList(parameter: Parameter) = requestStepPodInitList.onNext(parameter)

    override fun stepSelect(schedules: List<Schedule>) = stepSelect.onNext(schedules)
    override fun requestStepSelect(parameter: Parameter) = requestStepSelect.onNext(parameter)

    override fun stepBuyVolume(offer: Offer) = stepBuyVolume.onNext(offer)
    override fun requestGoToBuyVolume(offer: Offer) = requestGoToBuyVolume.onNext(offer)

    override fun requestSaveToPreference(isFinish: Boolean) = requestSaveToPreference.onNext(isFinish)

    override fun requestSetDataStatus(pair: Pair<Int, Any?>) = requestSetDataStatus.onNext(pair)

    override fun requestSearchInit(parameter: Parameter) = requestSearchInit.onNext(parameter)
    override fun requestSearchFilter(pair: Pair<Boolean, String>) = requestSearchFilter.onNext(pair)
    override fun requestSearchRemove(serviceLaneCode: String) = requestSearchRemove.onNext(serviceLaneCode)
    override fun requestSearchSelectSet(pair: Pair<String, String>) = requestSearchSelectSet.onNext(pair)

    override fun requestSearchLanePopup(parameter: Parameter) = requestSearchLanePopup.onNext(parameter)
    override fun requestSearchPolPopup(parameter: Parameter) = requestSearchPolPopup.onNext(parameter)
    override fun requestSearchPodPopup(parameter: Parameter) = requestSearchPodPopup.onNext(parameter)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // interface : out
    override fun onGoToOtherStep() : Observable<Pair<Int, Any?>> = onGoToOtherStep

    override fun onGoToStepRecently() : Observable<List<Offer>> = onGoToStepRecently
    override fun onGoToStepLane() : Observable<List<Schedule>> = onGoToStepLane
    override fun onGoToStepPol() : Observable<List<Schedule>> = onGoToStepPol
    override fun onStepPolInitList() : Observable<Parameter> = onStepPolInitList
    override fun onGoToStepPod() : Observable<List<Schedule>> = onGoToStepPod
    override fun onStepPodInitList() : Observable<Parameter> = onStepPodInitList
    override fun onGoToStepSelect() : Observable<Offer> = onGoToStepSelect

    override fun onGoToBuyVolume() : Observable<Offer> = onGoToBuyVolume

    override fun onLoadSchedulePartialData(): Observable<List<Schedule>> = onLoadSchedulePartialData

    override fun onSetDataStatus(): Observable<Pair<Int, Any?>> = onSetDataStatus
    override fun onSearchInit(): Observable<Parameter> = onSearchInit
    override fun onSearchFilter(): Observable<Pair<Boolean, String>> = onSearchFilter
    override fun onSearchRemove(): Observable<String> = onSearchRemove
    override fun onSearchSelectSet(): Observable<Pair<String, String>> = onSearchSelectSet

    override fun onSearchLanePopup(): Observable<Parameter> = onSearchLanePopup
    override fun onSearchPolPopup(): Observable<Parameter> = onSearchPolPopup
    override fun onSearchPodPopup(): Observable<Parameter> = onSearchPodPopup

    override fun onSuccessSaveToPreference(): Observable<Boolean> = onSuccessSaveToPreference
}

interface Inputs {
    // Load route data in Recently
    fun requestLoadRouteData(parameter: Parameter)
    // Load schedule data in Lane
    fun requestLoadScheduleData(isLoadOnly: Boolean)
    // Load schedule partial data in Pod
    fun requestLoadSchedulePartialData(schedule: Schedule)
    // Move other fragment
    fun requestGoToOtherStep(pair: Pair<Int, Any?>)
    // Recently
    fun stepRecently(offers: List<Offer>)
    fun requestStepRecently(parameter: Parameter)
    // Lane
    fun stepLane(schedules: List<Schedule>)
    fun requestStepLane(parameter: Parameter)
    // Pol
    fun stepPol(schedules: List<Schedule>)
    fun requestStepPol(parameter: Parameter)
    fun requestStepPolInitList(parameter: Parameter)
    // Pod
    fun stepPod(schedules: List<Schedule>)
    fun requestStepPod(parameter: Parameter)
    fun requestStepPodInitList(parameter: Parameter)
    // Select
    fun stepSelect(schedules: List<Schedule>)
    fun requestStepSelect(parameter: Parameter)
    // Set Buy Volume
    fun stepBuyVolume(offer: Offer)
    fun requestGoToBuyVolume(offer: Offer)
    // Save to preference
    fun requestSaveToPreference(isFinish: Boolean)
    // Set selected data
    fun requestSetDataStatus(pair: Pair<Int, Any?>)
    // Search area action
    fun requestSearchInit(parameter: Parameter)
    fun requestSearchFilter(pair: Pair<Boolean, String>)
    fun requestSearchRemove(serviceLaneCode: String)
    fun requestSearchSelectSet(pair: Pair<String, String>)
    // Search popup
    fun requestSearchLanePopup(parameter: Parameter)
    fun requestSearchPolPopup(parameter: Parameter)
    fun requestSearchPodPopup(parameter: Parameter)
}

interface Outputs {
    // Move other fragment
    fun onGoToOtherStep() : Observable<Pair<Int, Any?>>
    // Recently
    fun onGoToStepRecently() : Observable<List<Offer>>
    // Lane
    fun onGoToStepLane() : Observable<List<Schedule>>
    // Pol
    fun onGoToStepPol() : Observable<List<Schedule>>
    fun onStepPolInitList() : Observable<Parameter>
    // Pod
    fun onGoToStepPod() : Observable<List<Schedule>>
    fun onStepPodInitList() : Observable<Parameter>
    fun onLoadSchedulePartialData() : Observable<List<Schedule>>
    // Select
    fun onGoToStepSelect() : Observable<Offer>
    // Set Buy Volume
    fun onGoToBuyVolume() : Observable<Offer>
    // Set Selected Data
    fun onSetDataStatus() : Observable<Pair<Int, Any?>>
    // Search area action
    fun onSearchInit() : Observable<Parameter>
    fun onSearchFilter() : Observable<Pair<Boolean, String>>
    fun onSearchRemove() : Observable<String>
    fun onSearchSelectSet() : Observable<Pair<String, String>>
    // Search popup
    fun onSearchLanePopup() : Observable<Parameter>
    fun onSearchPolPopup() : Observable<Parameter>
    fun onSearchPodPopup() : Observable<Parameter>

    // Save to preference
    fun onSuccessSaveToPreference() : Observable<Boolean>
}