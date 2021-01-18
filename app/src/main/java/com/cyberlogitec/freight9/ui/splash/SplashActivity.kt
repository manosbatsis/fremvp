package com.cyberlogitec.freight9.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.CurrentUser
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.view.SpinView
import com.cyberlogitec.freight9.lib.util.createNotificationPushChannel
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.startActivityWithFinish
import com.cyberlogitec.freight9.ui.member.LoginActivity
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_splash.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresActivityViewModel(value = SplashViewModel::class)
class SplashActivity : BaseActivity<SplashViewModel>() {

    @Inject
    lateinit var currentUser: CurrentUser
    private lateinit var containerFrame: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_splash)

        // set full screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        (applicationContext as? App)?.component?.inject(this)

        containerFrame = findViewById(R.id.container_splash)
        ll_loading_splash.visibility = View.GONE

        // FCM init
        fcmInit()

// Asis:
//        Completable.complete()
//                .delay(3000, TimeUnit.MILLISECONDS)
//                .subscribe({ startActivityWithFinish(if (currentUser.user != null) HomeActivity::class.java else LoginActivity::class.java) })

// Tobe: Login Always


//        Completable.complete()
//                .doOnComplete {
//                    viewModel.inPuts.initializeData(Parameter.NULL)
//                }
//                .delay(3000, TimeUnit.MILLISECONDS)
//                .subscribe {
//                    Timber.v("diver:/ start activity")
//                    //startActivityWithFinish(LoginActivity::class.java)
//                }

        val welcomFadeOut = AnimationUtils.loadAnimation(this, R.anim.splash_fadeout)

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .doOnNext {
                    if (isIntentMainAction()) {
                        runOnUiThread {
                            iv_splash_welcome.animation = welcomFadeOut
                            iv_splash_welcome.visibility = View.INVISIBLE
                        }
                    }
                }
                .delay(if (isIntentMainAction()) 500L else 100L, TimeUnit.MILLISECONDS)
                .doOnNext {
                    if (isIntentMainAction()) {
                        runOnUiThread {
                            lottie_app_start.visibility = View.VISIBLE
                            lottie_app_start.playAnimation()
                        }
                    }
                }
                .doOnComplete {
                    viewModel.inPuts.initializeData(Parameter.NULL)
                }
                .delay(if (isIntentMainAction()) 3000L else 0L, TimeUnit.MILLISECONDS)
                .subscribe {
                    Timber.v("diver:/ start activity")
                    if (isIntentMainAction()) {
                        if (viewModel.loginFlag) {
                            Timber.v("diver:/Market activity")
                            startActivityWithFinish(MarketActivity::class.java)
                        } else {
                            Timber.v("diver:/Login activity")
                            startActivityWithFinish(LoginActivity::class.java)
                        }
                    } else {
                        // intent : GOTO_MESSAGE_BOX extra 가 있는 경우 menu로 이동
                        if (viewModel.loginFlag) {
                            // MarketActivity
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            intent.setClass(this, MarketActivity::class.java)
                        } else {
                            // LoginActivity
                            intent.setClass(this, LoginActivity::class.java)
                        }
                        startActivityWithFinish(intent)
                    }
                }

        viewModel.error
                .bindToLifecycle(this)
                .doOnNext {
                    Timber.v("diver:/ error on viewmodel")
                }
                .subscribe {
                    showToast(it.toString())
                    Timber.v("diver:/ start activity")
                    startActivityWithFinish(LoginActivity::class.java)
                }

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    it?.let {
                        val pair = it as Pair<*, *>
                        when(pair.second) {
                            SplashViewModel.LoadingParameter.LOADING_PORT -> {Timber.d("diver:/ showloading port")}
                            SplashViewModel.LoadingParameter.LOADING_SCHEDULE -> {Timber.d("diver:/ showloading schedule")}
                        }
                        ll_loading_splash.visibility = View.VISIBLE
                        tv_loading_splash.text = if (pair.second == SplashViewModel.LoadingParameter.LOADING_PORT) {
                            getString(R.string.loading_port_mdm).toString()
                        } else {
                            getString(R.string.loading_schedule).toString()
                        }
                        val wrapParam = ViewGroup.LayoutParams.WRAP_CONTENT
                        val containerParams = ViewGroup.LayoutParams(wrapParam, wrapParam)
                        if (containerFrame.childCount == 0) {
                            containerFrame.addView(SpinView(this, R.drawable.ic_loading_l), containerParams)
                        }
                    }
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() : $it")
                    Timber.d("diver:/ hideLoadingDialog")
                    ll_loading_splash.visibility = View.GONE
                    containerFrame.removeAllViews()
                }
    }

    private fun isIntentMainAction() = intent.action == Intent.ACTION_MAIN

    private fun fcmInit() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.w("getInstanceId failed : " + task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    Timber.d("f9: Push Token ==> " + task.result?.token)
                })

        // PUSH channel 생성 (need for O, P, Q)
        createNotificationPushChannel()
    }
}