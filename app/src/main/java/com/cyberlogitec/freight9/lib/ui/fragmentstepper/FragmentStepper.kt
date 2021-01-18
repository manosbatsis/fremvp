package com.cyberlogitec.freight9.lib.ui.fragmentstepper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity


class FragmentStepper: ViewPager {

    lateinit var pagerAdapter: StepperFragmentPagerAdapter
    lateinit var stepsChangeListener: StepsChangeListener
    private lateinit var activity: RxAppCompatActivity
    private var totalStep: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    override fun onInterceptHoverEvent(event: MotionEvent?): Boolean {
        return false
    }

    // swipe disable
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    // swipe disable
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    private fun init() {
        setCustomScroller()
        this.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(position: Int) { }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                stepsChangeListener.onStepChanged(position)
            }
        })
    }

    fun goToOtherStep(step: Int) {
        currentItem = step
    }

    fun goToNexStep() {
        currentItem += 1
    }

    fun goToPreviousStep() {
        currentItem -= 1
    }

    fun getCurrentStep() = currentItem

    fun isFirstStep(): Boolean = currentItem == 0

    fun isLastStep(): Boolean = currentItem == totalStep -1

    fun setParentActivity(activity: RxAppCompatActivity) {
        if (activity is StepsManager) {
            this.activity = activity
            pagerAdapter = StepperFragmentPagerAdapter(activity.supportFragmentManager, activity)
            adapter = pagerAdapter
        }
        else throw ActivityNotStepsManagerException()
    }

    fun setTotalStep(totalStep: Int) {
        this.totalStep = totalStep
    }

    fun getCurrentFragment() = pagerAdapter.getItem(currentItem)

    fun getCurrentFragment(position: Int) = pagerAdapter.getItem(position)

    fun getRegisteredFragment(position: Int) = pagerAdapter.getRegisteredFragment(position)

    private fun setCustomScroller() {
        try {
            val viewPager= ViewPager::class.java
            val scroller = viewPager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, CustomScroller(context))
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class CustomScroller(context: Context?) : Scroller(context, DecelerateInterpolator()) {
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 0)
        }
    }

    interface StepsChangeListener{
        fun onStepChanged(stepNumber: Int)
    }
}
