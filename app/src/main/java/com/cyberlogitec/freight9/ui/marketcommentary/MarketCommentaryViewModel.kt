package com.cyberlogitec.freight9.ui.marketcommentary

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketCommentaryViewModel(context: Context) : BaseViewModel(context), MarketCommentaryInputs, MarketCommentaryOutputs  {
    val inPuts: MarketCommentaryInputs = this

    val outPuts: MarketCommentaryOutputs = this

    private val gotoMenu = PublishSubject.create<Parameter>()


    init {


    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    override fun gotoMenu() : Observable<Parameter> = gotoMenu

}

interface MarketCommentaryInputs {
}

interface MarketCommentaryOutputs {
    fun gotoMenu() : Observable<Parameter>
}