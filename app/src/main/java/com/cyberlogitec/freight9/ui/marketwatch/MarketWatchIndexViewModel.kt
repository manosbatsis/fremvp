package com.cyberlogitec.freight9.ui.marketwatch

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Chart
import com.cyberlogitec.freight9.lib.model.MarketIndexChartList
import com.cyberlogitec.freight9.lib.model.MarketIndexList
import com.cyberlogitec.freight9.lib.model.PostMarketIndexChartRequest
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketWatchIndexViewModel(context: Context) : BaseViewModel(context), MarketIndexInputs, MarketIndexOutputs  {
    val inPuts: MarketIndexInputs = this

    private val clickToMenu = PublishSubject.create<Parameter>()
    private val storeChartSetting = PublishSubject.create<Chart>()
    private val storeIndexList = PublishSubject.create<List<MarketIndexList>>()
    private val loadChartSetting = PublishSubject.create<String>()
    private val requestIndexList = PublishSubject.create<Parameter>()
    private val requestChart = PublishSubject.create<PostMarketIndexChartRequest>()

    val outPuts: MarketIndexOutputs = this


    private val gotoMenu = PublishSubject.create<Parameter>()
    private val onSuccessLoadChartSetting = PublishSubject.create<Chart>()
    private val onSuccessRequestIndexList = PublishSubject.create<List<MarketIndexList>>()
    private val onSuccessRequestChartList = PublishSubject.create<MarketIndexChartList>()
    private val onFailLoadChartSetting = PublishSubject.create<Throwable>()
    private val onFailRequest = PublishSubject.create<Parameter>()

    init {

        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        storeIndexList.bindToLifeCycle()
                .subscribe{

                }
        storeChartSetting.bindToLifeCycle()
                .subscribe {
                    enviorment.marketChartSettingRepository.storeMarketChartSettingInDb(it)
                    onSuccessLoadChartSetting.onNext(it)
                }

        loadChartSetting.flatMapMaybe {
            enviorment.marketChartSettingRepository.getMarketChartSettingFromDb(it).doOnError {
                onFailLoadChartSetting.onNext(Throwable("FAIL"))
            }.neverError()
        }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessLoadChartSetting.onNext(it)
                }

        requestIndexList.flatMapMaybe {
            enviorment.apiStatClient.getMarketWatchIndexList().neverError() }
                .bindToLifeCycle()
                .subscribe {
                    enviorment.marketIndexListRepository.deleteAllIndex()
                    for(data in it)
                        enviorment.marketIndexListRepository.storeMarketIndexListInDb(data)
                    onSuccessRequestIndexList.onNext(it)

                }
        requestChart.flatMapMaybe {
            enviorment.apiStatClient.postMarketIndexChartList(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestChartList.onNext(it)
                }

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun storeChartSetting(chart: Chart) = storeChartSetting.onNext(chart)
    override fun loadChartSetting(simpleName: String) = loadChartSetting.onNext(simpleName)
    override fun requestIndexList(parameter: Parameter) = requestIndexList.onNext(parameter)
    override fun requestChart(item: PostMarketIndexChartRequest) = requestChart.onNext(item)

    override fun gotoMenu() : Observable<Parameter> = gotoMenu
    override fun onFailRequest(): Observable<Parameter> = onFailRequest
    override fun onSuccessLoadChartSetting(): Observable<Chart> = onSuccessLoadChartSetting
    override fun onSuccessRequestIndexList(): Observable<List<MarketIndexList>> = onSuccessRequestIndexList
    override fun onSuccessRequestChartList(): Observable<MarketIndexChartList> = onSuccessRequestChartList
    override fun onFailLoadChartSetting(): Observable<Throwable> = onFailLoadChartSetting
}

interface MarketIndexInputs {
    fun clickToMenu(parameter: Parameter)
    fun storeChartSetting(chart: Chart)
    fun loadChartSetting(simpleName: String)
    fun requestIndexList(parameter: Parameter)
    fun requestChart(item: PostMarketIndexChartRequest)
}

interface MarketIndexOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun onFailRequest(): Observable<Parameter>
    fun onSuccessLoadChartSetting(): Observable<Chart>
    fun onSuccessRequestIndexList(): Observable<List<MarketIndexList>>
    fun onSuccessRequestChartList(): Observable<MarketIndexChartList>
    fun onFailLoadChartSetting(): Observable<Throwable>

}