package com.cyberlogitec.freight9.ui.routefilter.pod

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.lib.model.Port
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_routefilter_pod.*
import kotlinx.android.synthetic.main.body_bof_pol.*
import kotlinx.android.synthetic.main.item_bof_content_pod.view.*
import timber.log.Timber
import java.util.*


@RequiresActivityViewModel(value = RouteFilterPodVm::class)
class RouteFilterPodAct : BaseActivity<RouteFilterPodVm>(), SearchView.OnQueryTextListener {

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

        setContentView(R.layout.act_routefilter_pod)

        (application as App).component.inject(this)

        // set status bar
        getWindow().statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_routefilter_pod, menuType = MenuType.POPUP, title = "", isEnableNavi=false)


        toolbar_pod.setSafeOnClickListener {
            Timber.d("f9: toolbar_pod click")
            viewModel.inPuts.clickToTitlebar( Parameter.CLICK )
        }

        // init recycler views
        recyclerViewInit()

        // init search views
        searchViewInit()

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onSuccessRefresh")

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
                        intent.putExtra("iPodCd", it.portCode)
                        intent.putExtra("iPodNm", it.portName)
                        setResult(Activity.RESULT_OK, intent)

                        finish()
                    }
                }

        viewModel.outPuts.onClickTitlebar()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onClickNext")
                    val intent = Intent()
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

    override fun onQueryTextSubmit(query: String): Boolean {
        Timber.d("f9: onQueryTextSubmit( ${query} )")

        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        Timber.d("f9: onQueryTextChange( ${newText} )")

        adapter.searchNrefresh(newText)
        return false
    }

    private fun recyclerViewInit() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@RouteFilterPodAct)
            adapter = this@RouteFilterPodAct.adapter
        }
    }

    private fun searchViewInit() {
        // SearchView init
        val id: Int = tx_search_pod.getContext().getResources().getIdentifier("android:id/search_src_text", null, null)
        val textView = tx_search_pod.findViewById(id) as TextView
        textView.setPadding(0, 2, 0, 2)
        textView.setTextColor(Color.BLACK)
        textView.setHintTextColor( ContextCompat.getColor(this, R.color.greyish_brown) )
        textView.setHint("To")

        tx_search_pod.setOnQueryTextListener( this )
    }

    private class RecyclerAdapter() : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        var onItemChecked: (Port) -> Unit = {}

        private var datum = mutableListOf<Port>()
        private var datum4search = mutableListOf<Port>()

        override fun getItemCount(): Int {
            return datum4search.size
        }

        fun setDatum(datum: List<Port>) {
            this.datum.clear()
            this.datum.addAll(datum)

            datum4search.clear()
            datum4search.addAll(datum)
        }

        // Filter Class
        fun searchNrefresh(inputs: String) {
            Timber.d("f9: searchNrefresh(inputs: ${inputs}) ++ ")

            val charText = inputs.toUpperCase(Locale.getDefault())
            datum4search.clear()
            if (charText.length == 0) {
                datum4search.addAll(datum)
            } else {
                for (wp in datum) {
                    if (wp.portCode.toUpperCase(Locale.getDefault()).startsWith(charText) /* || wp.portName.toUpperCase(Locale.getDefault()).startsWith(charText) */ ) {
                        datum4search.add(wp)
                    }
                }
            }
            Timber.d("f9: searchNrefresh(datum4search.size: ${datum4search.size}) --")

            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bof_content_pod, parent, false))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Timber.d("onBindViewHolder --> position ${position} ")

            with(holder.itemView) {
                val data = datum4search[position]

                tv_content_podCd.text = data.portCode
                tv_content_podNm.text = data.portName

                lo_bof_content_pod.setSafeOnClickListener {
                    Timber.d("lo_bof_content_pol --> click ${data.portCode} ")

                    onItemChecked(data)
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }


}