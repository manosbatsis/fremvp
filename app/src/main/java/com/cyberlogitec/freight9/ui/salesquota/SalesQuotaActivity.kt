package com.cyberlogitec.freight9.ui.salesquota

import android.os.Bundle
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.startActivity
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber


@RequiresActivityViewModel(value = SalesQuotaViewModel::class)
class SalesQuotaActivity : BaseActivity<SalesQuotaViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sales_quota)
        (application as App).component.inject(this)

        // set status bar
        getWindow().statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common, menuType = MenuType.DEFAULT, title = "SALES QUOTA")

        // wait click event (toolbar left button)
        toolbar_common.toolbar_left_btn.setOnClickListener{
           it?.let {
               Timber.d("f9: toolbar_left_btn clcick")
               onBackPressed()
           }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_common.toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")

            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }


        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startActivity(MenuActivity::class.java)
                }
    }

}