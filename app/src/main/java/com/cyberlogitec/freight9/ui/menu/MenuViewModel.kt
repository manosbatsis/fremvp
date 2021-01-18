package com.cyberlogitec.freight9.ui.menu

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Message
import com.cyberlogitec.freight9.lib.model.User
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MenuViewModel(context: Context) : BaseViewModel(context), MenuViewModelInputs, MenuViewModelOutPuts {

    val inPuts: MenuViewModelInputs = this
    private val clickToCloseMenu = PublishSubject.create<Parameter>()
    private val clickToHome = PublishSubject.create<Parameter>()
    private val clickToMessage = PublishSubject.create<Parameter>()
    private val clickToMarket = PublishSubject.create<Parameter>()
    private val clickToSellOffer = PublishSubject.create<Parameter>()
    private val clickToBuyOffer = PublishSubject.create<Parameter>()
    private val clickToMarketWatch = PublishSubject.create<Parameter>()
    private val clickToMarketIndex = PublishSubject.create<Parameter>()
    private val clickToMarketCommentary = PublishSubject.create<Parameter>()
    private val clickToInventory = PublishSubject.create<Parameter>()
    private val clickToYourBuyOffers = PublishSubject.create<Parameter>()
    private val clickToYourSellOffers = PublishSubject.create<Parameter>()
    private val clickToBookingDashboard = PublishSubject.create<Parameter>()
    private val clickToCargoTracking = PublishSubject.create<Parameter>()
    private val clickToPayCollectPlan = PublishSubject.create<Parameter>()
    private val clickToTransactionStatement = PublishSubject.create<Parameter>()
    private val clickToInvoice = PublishSubject.create<Parameter>()
    private val clickToLogout = PublishSubject.create<Parameter>()
    private val clickClearAll = PublishSubject.create<Parameter>()
    private val getMessages = PublishSubject.create<Parameter>()
    private val insertMessages = PublishSubject.create<List<Message>>()
    private val deleteMessage = PublishSubject.create<Long>()
    private val deleteAllMessages = PublishSubject.create<Parameter>()

    val outPuts: MenuViewModelOutPuts = this
    private val onCloseMenu = PublishSubject.create<Parameter>()
    private val gotoMessage = PublishSubject.create<Parameter>()
    private val gotoHome = PublishSubject.create<Parameter>()
    private val gotoTradeMarket = PublishSubject.create<Parameter>()
    private val gotoSellOffer = PublishSubject.create<Parameter>()
    private val gotoBuyOffer = PublishSubject.create<Parameter>()
    private val gotoMarketWatch = PublishSubject.create<Parameter>()
    private val gotoMarketIndex = PublishSubject.create<Parameter>()
    private val gotoMarketCommentary = PublishSubject.create<Parameter>()
    private val gotoInventory = PublishSubject.create<Parameter>()
    private val gotoYourBuyOffers = PublishSubject.create<Parameter>()
    private val gotoYourSellOffers = PublishSubject.create<Parameter>()
    private val gotoBookingDashboard = PublishSubject.create<Parameter>()
    private val gotoCargoTracking = PublishSubject.create<Parameter>()
    private val gotoPayCollectPlan = PublishSubject.create<Parameter>()
    private val gotoTransactionStatement = PublishSubject.create<Parameter>()
    private val gotoInvoice = PublishSubject.create<Parameter>()
    private val goLogin = PublishSubject.create<Parameter>()
    private val onSuccessClearAll = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<User>()
    private val onSuccessGetMessages = PublishSubject.create<List<Message>>()
    private val onSuccessInsertMessages = PublishSubject.create<Boolean>()
    private val onSuccessDeleteMessage = PublishSubject.create<Int>()
    private val onSuccessDeleteAllMessages = PublishSubject.create<Boolean>()

    private val menuItem = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        clickClearAll.bindToLifeCycle()
                .subscribe(onSuccessClearAll)

        clickToCloseMenu.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(onCloseMenu)

        clickToHome.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoHome)

        clickToMessage.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoMessage)

        clickToMarket.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoTradeMarket)

        clickToSellOffer.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoSellOffer)

        clickToBuyOffer.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoBuyOffer)

        clickToMarketWatch.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoMarketWatch)

        clickToMarketIndex.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoMarketIndex)

        clickToMarketCommentary.map { Parameter.EVENT}
                .bindToLifeCycle()
                .subscribe(gotoMarketCommentary)

        clickToInventory.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoInventory)

        clickToYourBuyOffers.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoYourBuyOffers)

        clickToYourSellOffers.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoYourSellOffers)

        clickToBookingDashboard.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoBookingDashboard)

        clickToCargoTracking.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoCargoTracking)

        clickToPayCollectPlan.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoPayCollectPlan)

        clickToTransactionStatement.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoTransactionStatement)

        clickToInvoice.map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(gotoInvoice)

        /* Asis:
        clickToLogout.flatMapSingle { enviorment.apiClient.logOut().neverError().toSingle{}  }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .doOnNext { enviorment.currentUser.logout() }
                .subscribe(goLogin)
         */

        // Temporary: allow logout always
        clickToLogout
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .doOnNext { enviorment.currentUser.logout() }
                .subscribe(goLogin)

        intent.map { it.getStringExtra(Intents.MENU_ITEM) }
                .filter{ it.isNotEmpty() }
                .neverError()
                .bindToLifeCycle()
                .subscribe( menuItem )

        refresh.map { enviorment.currentUser.user }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        /**
         * load push message
         */
        getMessages
                .flatMap { enviorment.messageRepository.getMessagesFromDb() }
                .bindToLifeCycle()
                .subscribe (onSuccessGetMessages)

        /**
         * not used
         */
        insertMessages
                .map { enviorment.messageRepository.storeMessagesInDb(it) }
                .bindToLifeCycle()
                .subscribe { onSuccessInsertMessages.onNext(true) }

        /**
         * delete selected push message
         */
        deleteMessage
                .flatMap { enviorment.messageRepository.deleteMessageFromDb(it) }
                .bindToLifeCycle()
                .subscribe (onSuccessDeleteMessage)

        /**
         * delete all push message
         */
        deleteAllMessages
                .flatMapSingle { enviorment.messageRepository.deleteMessagesFromDb().neverError().toSingle { } }
                .bindToLifeCycle()
                .subscribe { onSuccessDeleteAllMessages.onNext(true) }
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    override fun clickToMessage(parameter: Parameter) = clickToMessage.onNext(parameter)
    override fun clickClearAll(parameter: Parameter) = clickClearAll.onNext(parameter)
    override fun clickToCloseMenu(parameter: Parameter) = clickToCloseMenu.onNext(parameter)
    override fun clickToHome(parameter: Parameter) = clickToHome.onNext(parameter)
    override fun clickToMarket(parameter: Parameter) = clickToMarket.onNext(parameter)
    override fun clickToSellOffer(parameter: Parameter) = clickToSellOffer.onNext(parameter)
    override fun clickToBuyOffer(parameter: Parameter) = clickToBuyOffer.onNext(parameter)
    override fun clickToMarketWatch(parameter: Parameter) = clickToMarketWatch.onNext(parameter)
    override fun clickToMarketIndex(parameter: Parameter) = clickToMarketIndex.onNext(parameter)
    override fun clickToMarketCommentary(parameter: Parameter) = clickToMarketCommentary.onNext(parameter)
    override fun clickToInventory(parameter: Parameter) = clickToInventory.onNext(parameter)
    override fun clickToYourBuyOffers(parameter: Parameter) = clickToYourBuyOffers.onNext(parameter)
    override fun clickToYourSellOffers(parameter: Parameter) = clickToYourSellOffers.onNext(parameter)
    override fun clickToBookingDashboard(parameter: Parameter) = clickToBookingDashboard.onNext(parameter)
    override fun clickToCargoTracking(parameter: Parameter) = clickToCargoTracking.onNext(parameter)
    override fun clickToPayCollectPlan(parameter: Parameter) = clickToPayCollectPlan.onNext(parameter)
    override fun clickToTransactionStatement(parameter: Parameter) = clickToTransactionStatement.onNext(parameter)
    override fun clickToInvoice(parameter: Parameter) = clickToInvoice.onNext(parameter)
    override fun clickToLogout(parameter: Parameter) = clickToLogout.onNext(parameter)
    override fun getMessages(parameter: Parameter) = getMessages.onNext(parameter)
    override fun insertMessages(messages: List<Message>) = insertMessages.onNext(messages)
    override fun deleteMessage(msgSeq: Long) = deleteMessage.onNext(msgSeq)
    override fun deleteAllMessages(parameter: Parameter) = deleteAllMessages.onNext(parameter)

    override fun onSuccessClearAll(): Observable<Parameter> = onSuccessClearAll
    override fun onCloseMenu(): Observable<Parameter> = onCloseMenu
    override fun gotoMessage(): Observable<Parameter> = gotoMessage
    override fun gotoHome(): Observable<Parameter> = gotoHome
    override fun gotoTradeMarket(): Observable<Parameter> = gotoTradeMarket
    override fun gotoSellOffer(): Observable<Parameter> = gotoSellOffer
    override fun gotoBuyOffer(): Observable<Parameter> = gotoBuyOffer
    override fun gotoInventory(): Observable<Parameter> = gotoInventory
    override fun gotoMarketWatch(): Observable<Parameter> = gotoMarketWatch
    override fun gotoMarketIndex(): Observable<Parameter> = gotoMarketIndex
    override fun gotoMarketCommentary(): Observable<Parameter> = gotoMarketCommentary
    override fun gotoYourBuyOffers(): Observable<Parameter> = gotoYourBuyOffers
    override fun gotoYourSellOffers(): Observable<Parameter> = gotoYourSellOffers
    override fun gotoBookingDashboard(): Observable<Parameter> = gotoBookingDashboard
    override fun gotoCargoTracking(): Observable<Parameter> = gotoCargoTracking
    override fun gotoPayCollectPlan(): Observable<Parameter> = gotoPayCollectPlan
    override fun gotoTransactionStatement(): Observable<Parameter> = gotoTransactionStatement
    override fun gotoInvoice(): Observable<Parameter> = gotoInvoice
    override fun goLogin(): Observable<Parameter> = goLogin

    override fun onSuccessRefresh(): Observable<User> = onSuccessRefresh
    override fun onSuccessGetMessages(): Observable<List<Message>> = onSuccessGetMessages
    override fun onSuccessInsertMessages(): Observable<Boolean> = onSuccessInsertMessages
    override fun onSuccessDeleteMessage(): Observable<Int> = onSuccessDeleteMessage
    override fun onSuccessDeleteAllMessages(): Observable<Boolean> = onSuccessDeleteAllMessages
    override fun menuItem(): Observable<String> = menuItem
}


interface MenuViewModelInputs {
    fun clickToMessage(parameter: Parameter)
    fun clickClearAll(parameter: Parameter)
    fun clickToCloseMenu(parameter: Parameter)
    fun clickToHome(parameter: Parameter)
    fun clickToMarket(parameter: Parameter)
    fun clickToSellOffer(parameter: Parameter)
    fun clickToBuyOffer(parameter: Parameter)
    fun clickToMarketWatch(parameter: Parameter)
    fun clickToMarketIndex(parameter: Parameter)
    fun clickToMarketCommentary(parameter: Parameter)
    fun clickToInventory(parameter: Parameter)
    fun clickToYourBuyOffers(parameter: Parameter)
    fun clickToYourSellOffers(parameter: Parameter)
    fun clickToBookingDashboard(parameter: Parameter)
    fun clickToCargoTracking(parameter: Parameter)
    fun clickToPayCollectPlan(parameter: Parameter)
    fun clickToTransactionStatement(parameter: Parameter)
    fun clickToInvoice(parameter: Parameter)
    fun clickToLogout(parameter: Parameter)

    fun getMessages(parameter: Parameter)
    fun insertMessages(messages: List<Message>)
    fun deleteMessage(msgSeq: Long)
    fun deleteAllMessages(parameter: Parameter)
}

interface MenuViewModelOutPuts {
    fun gotoMessage(): Observable<Parameter>
    fun onSuccessClearAll(): Observable<Parameter>
    fun onCloseMenu(): Observable<Parameter>
    fun gotoHome(): Observable<Parameter>
    fun gotoTradeMarket(): Observable<Parameter>
    fun gotoSellOffer(): Observable<Parameter>
    fun gotoBuyOffer(): Observable<Parameter>
    fun gotoMarketWatch(): Observable<Parameter>
    fun gotoMarketIndex(): Observable<Parameter>
    fun gotoMarketCommentary(): Observable<Parameter>
    fun gotoInventory(): Observable<Parameter>
    fun gotoYourBuyOffers(): Observable<Parameter>
    fun gotoYourSellOffers(): Observable<Parameter>
    fun gotoBookingDashboard(): Observable<Parameter>
    fun gotoCargoTracking(): Observable<Parameter>
    fun gotoPayCollectPlan(): Observable<Parameter>
    fun gotoTransactionStatement(): Observable<Parameter>
    fun gotoInvoice(): Observable<Parameter>
    fun goLogin(): Observable<Parameter>

    fun onSuccessRefresh(): Observable<User>
    fun onSuccessGetMessages(): Observable<List<Message>>
    fun onSuccessInsertMessages(): Observable<Boolean>
    fun onSuccessDeleteMessage(): Observable<Int>
    fun onSuccessDeleteAllMessages(): Observable<Boolean>
    fun menuItem(): Observable<String>
}