package com.cyberlogitec.freight9.ui.routefilter.both

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.db.PortDao
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_routefilter_both.*
import kotlinx.android.synthetic.main.body_bof_pol.*
import kotlinx.android.synthetic.main.item_bof_content_both.view.*
import kotlinx.android.synthetic.main.item_bof_content_pol.view.lo_bof_content_pol
import timber.log.Timber


@RequiresActivityViewModel(value = RouteFilterBothVm::class)
class RouteFilterBothAct : BaseActivity<RouteFilterBothVm>() {

    // list view
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    onItemChecked = { viewModel.inPuts.clickToItemChecked(it) }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_routefilter_both)

        (application as App).component.inject(this)

        // set status bar
        getWindow().statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_routefilter_both, menuType = MenuType.POPUP, title = "", isEnableNavi=false)

        toolbar_both.setSafeOnClickListener {
            Timber.d("f9: toolbar_common click")
            viewModel.inPuts.clickToTitlebar( Parameter.CLICK )
        }

        // init recycler views
        recyclerViewInit()

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh")

                    Timber.d("f9: ${it} ")

                    it?.let {
                        adapter.setDatum(it)
                        adapter.notifyDataSetChanged()
                    }
                }

        viewModel.outPuts.onClickItemChecked()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickItemChecked --> ${it}")
                    it?.let {
                        val intent = Intent()
                        intent.putExtra("iPolCd", it.iPolCd)
                        intent.putExtra("iPolNm", it.iPolNm)
                        intent.putExtra("iPodCd", it.iPodCd)
                        intent.putExtra("iPodNm", it.iPodNm)
                        setResult(Activity.RESULT_OK, intent)

                        finish()
                    }
                }

        viewModel.outPuts.onClickTitlebar()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickTitlebar")
                    val intent = Intent()
                    intent.putExtra("iPolCd", "")
                    intent.putExtra("iPolNm", "")
                    intent.putExtra("iPodCd", "")
                    intent.putExtra("iPodNm", "")
                    setResult(Activity.RESULT_OK, intent)

                    finish()
                }

        ////////////////////////////////////////////////////////////////////////////////////////////

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog : ${it}")
                    loadingDialog.dismiss()
                }

        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    showToast(it.toString())
                }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@RouteFilterBothAct)
            adapter = this@RouteFilterBothAct.adapter
        }
    }

    private class RecyclerAdapter() : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        var onItemChecked: (PortDao.RouteMinimal) -> Unit = {}

        private var datum = mutableListOf<PortDao.RouteMinimal>()
        private var datum4search = mutableListOf<PortDao.RouteMinimal>()

        override fun getItemCount(): Int {
            return datum4search.size
        }

        fun setDatum(datum: List<PortDao.RouteMinimal>) {
            this.datum.clear()
            this.datum.addAll(datum)

            datum4search.clear()
            datum4search.addAll(datum)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_content_both, parent, false))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Timber.d("onBindViewHolder --> position ${position} ")

            with(holder.itemView) {
                val data = datum4search[position]

                tv_pol_cd.text = data.iPolCd
                tv_pol_name.text = data.iPolNm
                tv_pod_cd.text = data.iPodCd
                tv_pod_name.text = data.iPodNm

                lo_bof_content_pol.setSafeOnClickListener {
                    Timber.d("lo_bof_content_pol --> click ${data.iPolCd}, ${data.iPodCd} ")

                    onItemChecked(data)
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }


}