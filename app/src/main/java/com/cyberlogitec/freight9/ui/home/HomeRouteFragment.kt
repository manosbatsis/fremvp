package com.cyberlogitec.freight9.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cyberlogitec.freight9.R
import com.trello.rxlifecycle3.components.support.RxFragment
import timber.log.Timber

class HomeRouteFragment constructor(val viewModel: HomeViewModel): RxFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_sof_route_selector, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.v("f9: onViewCreated")

        // hide action bar
        // (activity as AppCompatActivity).supportActionBar?.hide()

        // on click event
        //tv_home.setOnClickListener{
        //    Timber.d("f9: clickToHome")
        //    viewModel.inPuts.clickToHome(Parameter.CLICK)
        //}


        //
        // belows are deprecated --> later, consider injeting ViewModel with DI
        // activity?.let {
        // val viewModel = ViewModelProviders.of(this).get(MenuViewModel::class.java)
        // val viewModel = ViewModelProvider.NewInstanceFactory().create(MenuViewModel::class.java)
        // }
        //
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Timber.v("f9: onActivityCreated")
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: HomeViewModel) : HomeRouteFragment {
            val fragment = HomeRouteFragment(viewModel)
            return fragment
        }
    }
}