package com.cyberlogitec.freight9.ui.routeselect.featured

import android.os.Bundle
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import kotlinx.android.synthetic.main.act_preferred_route_edit.*
import timber.log.Timber

@RequiresActivityViewModel(value = PreferredRouteEditViewModel::class)
class PreferredRouteEditActivity : BaseActivity<PreferredRouteEditViewModel>(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.act_preferred_route_edit)
        (application as App).component.inject(this)
        window.statusBarColor = getColor(R.color.black)

        iv_preferred_route_edit_done.setOnClickListener {
            onBackPressed()
        }

        val updateFragment = PreferredRouteEditFragment(viewModel)
        supportFragmentManager.beginTransaction().add(R.id.container_preferred_route, updateFragment).commit()
    }
}