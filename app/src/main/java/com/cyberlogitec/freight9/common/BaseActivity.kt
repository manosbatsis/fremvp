package com.cyberlogitec.freight9.common

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.RxBusEvent
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.rx.RxBus
import com.cyberlogitec.freight9.lib.ui.dialog.ProgressDialog
import com.cyberlogitec.freight9.ui.member.LoginActivity
import com.cyberlogitec.freight9.ui.splash.SplashActivity
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber

open class BaseActivity<out T> : RxAppCompatActivity() where T : BaseViewModel {
    //MenuItem was replaced by custom menu
    //protected val clickOptionItems: PublishSubject<MenuItem> by lazy { PublishSubject.create<MenuItem>() }

    protected val backPress: PublishSubject<Parameter> by lazy { PublishSubject.create<Parameter>() }
    protected val loadingDialog by lazy {
        var style: ProgressDialog.Style
        when(this) {
            is LoginActivity -> {
                style = ProgressDialog.Style.SPIN_LIGHT_CENTER_TITLE
            }
            // Pending
            is SplashActivity -> {
                style = ProgressDialog.Style.SPIN_LIGHT_BOTTOM_TITLE
            }
            else -> {
                style = ProgressDialog.Style.SPIN_DARK
            }
        }
        ProgressDialog(this).setStyle(style)
    }
    private var originalViewModel: T? = null

    @Suppress("UNCHECKED_CAST")
    private fun createViewModel(): T = javaClass.getAnnotation(RequiresActivityViewModel::class.java)?.let {
        val className = it.value.java
        val constructor = className.getConstructor(Context::class.java)
        constructor.newInstance(this) as? T ?: throw RuntimeException()
    } ?: throw RuntimeException()

    protected val viewModel: T
        get() = originalViewModel ?: createViewModel().apply { this@BaseActivity.originalViewModel = this }

    protected fun clickViewParameterClick(parameterClick: ParameterClick) =
            viewModel.clickViewParameterClick(parameterClick)

    protected fun clickViewParameterAny(pair: Pair<ParameterAny, Any>) =
            viewModel.clickViewParameterAny(pair)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenRxBusEvent()
        viewModel.onCreate(this, savedInstanceState)
        viewModel.intent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
        originalViewModel = null
    }

    protected fun actionbarInit(toolbar: Toolbar, titleColor: Int = Color.WHITE, isEnableNavi: Boolean = true, naviColor: Int? = null, title: String = "") {
        toolbar.title = title
        toolbar.setTitleTextColor(titleColor)

        setSupportActionBar(toolbar)
        if (isEnableNavi)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (naviColor != null)
            supportActionBar?.setHomeAsUpIndicator(
                    ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)?.mutate()
                            .apply { this?.setColorFilter(ContextCompat.getColor(this@BaseActivity, naviColor), PorterDuff.Mode.SRC_ATOP) }
            )
    }

    protected fun defaultbarInit(toolbar: View,
                                 titleColor: Int = Color.WHITE,
                                 isEnableNavi: Boolean = true,
                                 naviColor: Int? = null,
                                 title: String = "",
                                 menuType: MenuType? = null) {
        toolbar.toolbar_title_text.text = title
        toolbar.toolbar_title_text.setTextColor(titleColor)

        if (isEnableNavi) {
            toolbar.toolbar_left_btn.visibility = View.VISIBLE
        } else {
            toolbar.toolbar_left_btn.visibility = View.INVISIBLE
        }

        when(menuType) {
            MenuType.DEFAULT -> {
                toolbar_done_btn.visibility = View.GONE
                toolbar.toolbar_right_btn.visibility = View.VISIBLE
                toolbar.toolbar_right_btn.setImageResource(R.drawable.ic_toolbar_menu)
            }
            MenuType.CROSS -> {
                toolbar_done_btn.visibility = View.GONE
                toolbar.toolbar_right_btn.visibility = View.VISIBLE
                toolbar.toolbar_right_btn.setImageResource(R.drawable.ic_toolbar_cross)
            }
            MenuType.DONE -> {
                toolbar_done_btn.visibility = View.VISIBLE
                toolbar.toolbar_right_btn.visibility =  View.GONE
            }
            else -> {
                toolbar_done_btn.visibility = View.INVISIBLE
                toolbar.toolbar_right_btn.visibility = View.GONE
            }
        }

        if (naviColor != null) {
            toolbar.setBackgroundColor(naviColor)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun listenRxBusEvent() {
        RxBus.listen(RxBusEvent::class.java)
                .compose(bindToLifecycle())
                .subscribe {
                    it?.let {
                        Timber.d("f9: this = $this, listenRxBusEvent = $it")
                        viewModel.rxBusEvent(it)
                    }
                }
    }

    enum class MenuType {
        DEFAULT,
        CROSS,
        DONE,
        POPUP,
    }
}
