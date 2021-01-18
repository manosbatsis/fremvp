package com.cyberlogitec.freight9.ui.member

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.trademarket.MarketActivity
import com.cyberlogitec.freight9.viewmodels.LoginViewModel
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_login.*
import timber.log.Timber

@RequiresActivityViewModel(value = LoginViewModel::class)
class LoginActivity : BaseActivity<LoginViewModel>() {
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("f9: onCreate")
        setContentView(R.layout.act_login)
        (application as App).component.inject(this)


        keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
                onShowKeyboard = {_, _ ->
//                    iv_sign_in.visibility = View.GONE
//                    tv_signin_title.visibility = View.GONE
                    tv_button_signin.visibility = View.VISIBLE
                    scrollview_login.smoothScrollTo(0, scrollview_login.bottom)
                    Timber.v("diver:/keyboard up")
                },
                onHideKeyboard = {
//                    iv_sign_in.visibility = View.VISIBLE
//                    tv_signin_title.visibility = View.VISIBLE
//                    tv_button_signin.visibility = View.INVISIBLE
                    Timber.v("diver:/keyboard down")
                }
        )


        editText_id.addTextWatcher({viewModel.inPuts.id(it) })
        editText_password.addTextWatcher({ viewModel.inPuts.password(it) })
        editText_id.onFocusChangeListener = SignInFocusListener(editText_id.hint.toString())
        editText_password.onFocusChangeListener = SignInFocusListener(editText_password.hint.toString())
        tv_button_signin.setOnClickListener {it?.let { viewModel.inPuts.loginClick(Parameter.CLICK) } }

        checkbox_remember_me.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.inPuts.toggleRememberMe(isChecked)
            when(isChecked){
                true-> {
                    buttonView.setTextColor(getColor(R.color.white))
                    buttonView.setTypeface(buttonView.typeface, Typeface.BOLD)
                }
                false-> {
                    buttonView.setTextColor(getColor(R.color.blue_violet))
                    buttonView.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        viewModel.getRememberMe()?.let {
            checkbox_remember_me.isChecked = it
        }

        viewModel.outPuts.loginSuccess()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivityWithFinish(MarketActivity)")
                    intent.setClass(this, MarketActivity::class.java)
                    intent.putExtra(Intents.GOTO_EVENT_SERVICE_RUN, true)
                    startActivityWithFinish(intent)
                }

        viewModel.outPuts.setLoginButtonIsEnabled()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: button_login enabled = ${it}")
                    tv_button_signin.isEnabled = it
                }

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this, getString(R.string.loading), true)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    showToast(it.toString())
                }
    }

    inner class SignInFocusListener(hintText: String) : View.OnFocusChangeListener{
        private var hintText = hintText
        override fun onFocusChange(v: View?, hasFocus: Boolean) {
            val editText : EditText = v as EditText
            when(hasFocus){
                true->{
                    editText.setTextColor(ContextCompat.getColor(baseContext, R.color.blue_violet))
                    editText.hint=""
                }
                false->{
                    editText.setTextColor(Color.WHITE)
                    editText.hint=hintText
                }
            }
        }
    }
}