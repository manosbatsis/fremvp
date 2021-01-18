package com.cyberlogitec.freight9.lib.ui.fragmentstepper

import androidx.fragment.app.FragmentManager
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import com.trello.rxlifecycle3.components.support.RxFragment


abstract class SmartFragmentStatePagerAdapter(fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var registeredFragments = SparseArray<RxFragment>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var fragment = super.instantiateItem(container, position) as RxFragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, item)
    }

    fun getRegisteredFragment(postion: Int): RxFragment {
        return registeredFragments.get(postion)
    }
}