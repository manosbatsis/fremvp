package com.cyberlogitec.freight9.lib.ui.fragmentstepper

import androidx.fragment.app.FragmentManager
import com.trello.rxlifecycle3.components.support.RxFragment

class StepperFragmentPagerAdapter(fm: FragmentManager, private var stepsManager: StepsManager): SmartFragmentStatePagerAdapter(fm)
{
    override fun getItem(position: Int): RxFragment {
        return stepsManager.getStep(position)
    }

    override fun getCount(): Int {
        return stepsManager.getCount()
    }
}