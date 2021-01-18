package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferRoute
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.combineLatest
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.cartesianProduct
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofPodVm(context: Context) : BaseViewModel(context), PodInputs, PodOutputs {
    val inPuts: PodInputs = this
    private val clickToItemCheck = PublishSubject.create<Int>()
    private val clickToItemRemove = PublishSubject.create<Schedule>()
    private val clickToNext = PublishSubject.create<Parameter>()
    private val schedules = BehaviorSubject.create<List<Schedule>>()
    private val clickToSearch = PublishSubject.create<Parameter>()
    private val requestSchedules = PublishSubject.create<Schedule>()

    val outPuts: PodOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val parentId = BehaviorSubject.create<List<Schedule>>()

    private val onSuccessLoadSchedules = PublishSubject.create<List<Schedule>>()
    private val onSuccessRefresh = PublishSubject.create<List<Schedule>>()

    private val onClickItemCheck = PublishSubject.create<Int>()
    private val onClickItemRemove = PublishSubject.create<Schedule>()
    private val onClickNext = PublishSubject.create<Offer>()
    private val onClickSearch = PublishSubject.create<Parameter>()


    init {

        val newOffer = BehaviorSubject.create<Offer>()
        val newOfferAndSchedules: Observable<Pair<Offer, List<Schedule>>> = newOffer.combineLatest(schedules)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent (parentId)

        intent.map { it.getSerializableExtra(Intents.SCHEDULE_LIST) as List<Schedule> }
                .filter{ it.isNotEmpty() }
                .map {
                    it.map{ it.isPodChecked = false }
                    it
                }
                .bindToLifeCycle()
                .subscribe(parentId)

        intent.map { Offer(offerTypeCode = "B") }
                .map { it.offerRoutes = mutableListOf<OfferRoute>(); it }
                .bindToLifeCycle()
                .subscribe(newOffer)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh (onSuccessRefresh)

        parentId.compose<List<Schedule>> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 })  }
                .filter { it.isNotEmpty() }
                .map {
                    it.map { it.isPodChecked = false }
                    it
                }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        ////////////////////////////////////////////////////////////////////////////////////////////

        requestSchedules.bindToLifeCycle()
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe {
                    enviorment.scheduleRepository.loadSchedules(it.serviceLaneCode, it.polCode)
                        .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe ( onSuccessLoadSchedules )

        clickToItemCheck.bindToLifeCycle()
                .subscribe(onClickItemCheck)

        clickToItemRemove.bindToLifeCycle()
                .subscribe(onClickItemRemove)

        newOfferAndSchedules.compose<Pair<Offer, List<Schedule>>> { clickToNext.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ it.second.isNotEmpty() && listOf(it.first).isNotEmpty() }
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
                        val pols = schedules2.filter{ it.isPolChecked == true }.distinctBy { it.polCode }
                        val pods = schedules2.filter{ it.isPodChecked == true }.distinctBy { it.podCode }

                        val polXpod = cartesianProduct(pols, pods)
                        val offerRoutes = mutableListOf<OfferRoute>()

                        polXpod.forEachIndexed { idx, item ->
                            Timber.d("f9: polXpod-> idx: ${idx}, polCode=${item.first.polCode}, podCode=${item.second.podCode}")

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
                    val offers3 = SharedPreferenceManager(context).offers!!
                    offers3.add(0, offer2)
                    SharedPreferenceManager(context).offers = offers3

                    offer2
                }
                .bindToLifeCycle()
                .subscribe(onClickNext)

        clickToSearch.bindToLifeCycle()
                .subscribe(onClickSearch)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToItemCheck(parameter: Int) = clickToItemCheck.onNext(parameter)
    override fun clickToItemRemove(parameter: Schedule) = clickToItemRemove.onNext(parameter)
    override fun clickToNext(parameter: Parameter) = clickToNext.onNext(parameter)
    override fun schedules(parameter: List<Schedule>) = schedules.onNext(parameter)
    override fun clickToSearch(parameter: Parameter) = clickToSearch.onNext(parameter)
    override fun requestSchedules(schedule: Schedule) = requestSchedules.onNext(schedule)

    // interface : out
    override fun onSuccessLoadSchedules(): Observable<List<Schedule>> = onSuccessLoadSchedules
    override fun onSuccessRefresh(): Observable<List<Schedule>> = onSuccessRefresh
    override fun onClickItemCheck(): Observable<Int> = onClickItemCheck
    override fun onClickItemRemove(): Observable<Schedule> = onClickItemRemove
    override fun onClickNext(): Observable<Offer> = onClickNext
    override fun onClickSearch(): Observable<Parameter> = onClickSearch
}

interface PodInputs {
    fun clickToItemCheck(parameter: Int)
    fun clickToItemRemove(parameter: Schedule)
    fun clickToNext(parameter: Parameter)
    fun clickToSearch(parameter: Parameter)

    fun schedules(parameter: List<Schedule>)
    fun requestSchedules(schedule: Schedule)
}

interface PodOutputs {
    fun onSuccessLoadSchedules(): Observable<List<Schedule>>
    fun onSuccessRefresh(): Observable<List<Schedule>>
    fun onClickItemCheck(): Observable<Int>
    fun onClickItemRemove(): Observable<Schedule>
    fun onClickNext(): Observable<Offer>
    fun onClickSearch(): Observable<Parameter>
}