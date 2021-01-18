package com.cyberlogitec.freight9.lib.ui.swipe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.*
import android.widget.ListView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/*
 * Copyright 2013 Google Inc.
 * Copyright 2015 Bruno Romeu Nunes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A [View.OnTouchListener] that makes the list items in a [androidx.recyclerview.widget.RecyclerView]
 * dismissable by swiping.
 *
 *
 *
 * Example usage:
 *
 *
 * <pre>
 * SwipeDismissRecyclerViewTouchListener touchListener =
 * new SwipeDismissRecyclerViewTouchListener(
 * listView,
 * new SwipeDismissRecyclerViewTouchListener.OnDismissCallback() {
 * public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 * for (int position : reverseSortedPositions) {
 * adapter.remove(adapter.getItem(position));
 * }
 * adapter.notifyDataSetChanged();
 * }
 * });
 * listView.setOnTouchListener(touchListener);
 * listView.setOnScrollListener(touchListener.makeScrollListener());
</pre> *
 *
 *
 *
 * This class Requires API level 11 or later due to use of [ ].
 */

class SwipeableRecyclerViewTouchListener
/**
 * Constructs a new swipe touch listener for the given [androidx.recyclerview.widget.RecyclerView]
 *
 * @param recyclerView The recycler view whose items should be dismissable by swiping.
 * @param listener     The listener for the swipe events.
 */
(// Fixed properties
        private val mRecyclerView: RecyclerView, private val mSwipeListener: SwipeListener) : RecyclerView.OnItemTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private val mSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mAnimationTime: Long
    private var mViewWidth = 1 // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private val mPendingDismisses = ArrayList<PendingDismissData>()
    private var mDismissAnimationRefCount = 0
    private var mAlpha: Float = 0.toFloat()
    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()
    private var mSwiping: Boolean = false
    private var mSwipingSlop: Int = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var mDownPosition: Int = 0
    private var mAnimatingPosition = ListView.INVALID_POSITION
    private var mDownView: View? = null
    private var mPaused: Boolean = false
    private var mFinalDelta: Float = 0.toFloat()

    private var mSwipingLeft: Boolean = false
    private var mSwipingRight: Boolean = false

    init {
        val vc = ViewConfiguration.get(mRecyclerView.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = mRecyclerView.context.resources.getInteger(
                android.R.integer.config_shortAnimTime).toLong()


        /**
         * This will ensure that this SwipeableRecyclerViewTouchListener is paused during list view scrolling.
         * If a scroll listener is already assigned, the caller should still pass scroll changes through
         * to this listener.
         */
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        })
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, motionEvent: MotionEvent): Boolean {
        return handleTouchEvent(motionEvent)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // Do nothing.
    }

    override fun onTouchEvent(rv: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }

    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.width
        }

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (mPaused) {
                    return false
                }

                // Find the child view that was touched (perform a hit test)
                val rect = Rect()
                val childCount = mRecyclerView.childCount
                val listViewCoords = IntArray(2)
                mRecyclerView.getLocationOnScreen(listViewCoords)
                val x = motionEvent.rawX.toInt() - listViewCoords[0]
                val y = motionEvent.rawY.toInt() - listViewCoords[1]
                var child: View
                for (i in 0 until childCount) {
                    child = mRecyclerView.getChildAt(i)
                    child.getHitRect(rect)
                    if (rect.contains(x, y)) {
                        mDownView = child
                        break
                    }
                }

                if (mDownView != null && mAnimatingPosition != mRecyclerView.getChildLayoutPosition(mDownView!!)) {
                    mAlpha = mDownView!!.alpha
                    mDownX = motionEvent.rawX
                    mDownY = motionEvent.rawY
                    mDownPosition = mRecyclerView.getChildLayoutPosition(mDownView!!)
                    mSwipingLeft = mSwipeListener.canSwipeLeft(mDownPosition)
                    mSwipingRight = mSwipeListener.canSwipeRight(mDownPosition)
                    if (mSwipingLeft || mSwipingRight) {
                        mVelocityTracker = VelocityTracker.obtain()
                        mVelocityTracker!!.addMovement(motionEvent)
                    } else {
                        mDownView = null
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mVelocityTracker == null) {
                    return false
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    ViewCompat.animate(mDownView!!)
                            .translationX(0f)
                            .alpha(mAlpha)
                            .setDuration(mAnimationTime)
                            .setListener(null)
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
                mDownX = 0f
                mDownY = 0f
                mDownView = null
                mDownPosition = ListView.INVALID_POSITION
                mSwiping = false
            }

            MotionEvent.ACTION_UP -> {
                if (mVelocityTracker == null) {
                    return false
                }

                mFinalDelta = motionEvent.rawX - mDownX
                mVelocityTracker!!.addMovement(motionEvent)
                mVelocityTracker!!.computeCurrentVelocity(1000)
                val velocityX = mVelocityTracker!!.xVelocity
                val absVelocityX = Math.abs(velocityX)
                val absVelocityY = Math.abs(mVelocityTracker!!.yVelocity)
                var dismiss = false
                var dismissRight = false
                if (Math.abs(mFinalDelta) > mViewWidth / 2 && mSwiping) {
                    dismiss = true
                    dismissRight = mFinalDelta > 0
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = velocityX < 0 == mFinalDelta < 0
                    dismissRight = mVelocityTracker!!.xVelocity > 0
                }
                if (dismiss && mDownPosition != mAnimatingPosition && mDownPosition != ListView.INVALID_POSITION) {
                    ViewCompat.animate(mDownView!!)
                            .translationX(0f)
                            .alpha(mAlpha)
                            .setDuration(mAnimationTime)
                            .setListener(null)


                    val dismissPositions = IntArray(1)
                    dismissPositions[0] = mDownPosition
                    if (mFinalDelta < 0) {
                        mSwipeListener.onDismissedBySwipeLeft(mRecyclerView, dismissPositions)
                    } else {
                        mSwipeListener.onDismissedBySwipeRight(mRecyclerView, dismissPositions)
                    }


                    /*// dismiss
                    final View downView = mDownView; // mDownView gets null'd before animation ends
                    final int downPosition = mDownPosition;
                    ++mDismissAnimationRefCount;
                    mAnimatingPosition = mDownPosition;
                    ViewCompat.animate(mDownView)
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new ViewPropertyAnimatorListener() {
                                @Override
                                public void onAnimationStart(View view) {
                                    // Do nothing.
                                }

                                @Override
                                public void onAnimationEnd(View view) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        performDismiss(downView, downPosition);
                                        // cancel
                                        ViewCompat.animate(mDownView)
                                                .translationX(0)
                                                .alpha(mAlpha)
                                                .setDuration(mAnimationTime)
                                                .setListener(null);
                                    }
                                }

                                @Override
                                public void onAnimationCancel(View view) {
                                    // Do nothing.
                                }
                            });*/

                } else {
                    // cancel
                    ViewCompat.animate(mDownView!!)
                            .translationX(0f)
                            .alpha(mAlpha)
                            .setDuration(mAnimationTime)
                            .setListener(null)
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
                mDownX = 0f
                mDownY = 0f
                mDownView = null
                mDownPosition = ListView.INVALID_POSITION
                mSwiping = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker == null || mPaused) {
                    return false
                }

                mVelocityTracker!!.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - mDownX
                val deltaY = motionEvent.rawY - mDownY
                if (!mSwiping && Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true
                    mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                }

                if (deltaX < 0 && !mSwipingLeft)
                    mSwiping = false
                if (deltaX > 0 && !mSwipingRight)
                    mSwiping = false

                if (mSwiping) {
                    ViewCompat.setTranslationX(mDownView!!, deltaX - mSwipingSlop)
                    ViewCompat.setAlpha(mDownView!!, Math.max(0f, Math.min(mAlpha,
                            mAlpha * (1f - Math.abs(deltaX) / mViewWidth))))
                    return true
                }
            }
        }

        return false
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun performDismiss(dismissView: View, dismissPosition: Int) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        val lp = dismissView.layoutParams
        val originalLayoutParamsHeight = lp.height
        val originalHeight = dismissView.height

        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                --mDismissAnimationRefCount
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses)

                    val dismissPositions = IntArray(mPendingDismisses.size)
                    for (i in mPendingDismisses.indices.reversed()) {
                        dismissPositions[i] = mPendingDismisses[i].position
                    }

                    if (mFinalDelta < 0) {
                        mSwipeListener.onDismissedBySwipeLeft(mRecyclerView, dismissPositions)
                    } else {
                        mSwipeListener.onDismissedBySwipeRight(mRecyclerView, dismissPositions)
                    }

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    mDownPosition = ListView.INVALID_POSITION

                    val lp: ViewGroup.LayoutParams
                    /*for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view.setAlpha(mAlpha);
                        pendingDismiss.view.setTranslationX(0);

                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = originalLayoutParamsHeight;

                        pendingDismiss.view.setLayoutParams(lp);
                    }*/

                    // Send a cancel event
                    val time = SystemClock.uptimeMillis()
                    val cancelEvent = MotionEvent.obtain(time, time,
                            MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
                    mRecyclerView.dispatchTouchEvent(cancelEvent)

                    mPendingDismisses.clear()
                    mAnimatingPosition = ListView.INVALID_POSITION
                }
            }
        })

        animator.addUpdateListener { valueAnimator ->
            lp.height = valueAnimator.animatedValue as Int
            dismissView.layoutParams = lp
        }

        mPendingDismisses.add(PendingDismissData(dismissPosition, dismissView))
        animator.start()
    }

    /**
     * The callback interface used by [SwipeableRecyclerViewTouchListener] to inform its client
     * about a swipe of one or more list item positions.
     */
    interface SwipeListener {
        /**
         * Called to determine whether the given position can be swiped to the left.
         */
        fun canSwipeLeft(position: Int): Boolean

        /**
         * Called to determine whether the given position can be swiped to the right.
         */
        fun canSwipeRight(position: Int): Boolean

        /**
         * Called when the item has been dismissed by swiping to the left.
         *
         * @param recyclerView           The originating [androidx.recyclerview.widget.RecyclerView].
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         * order for convenience.
         */
        fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray)

        /**
         * Called when the item has been dismissed by swiping to the right.
         *
         * @param recyclerView           The originating [androidx.recyclerview.widget.RecyclerView].
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         * order for convenience.
         */
        fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray)
    }

    internal inner class PendingDismissData(var position: Int, var view: View) : Comparable<PendingDismissData> {

        override fun compareTo(other: PendingDismissData): Int {
            // Sort by descending position
            return other.position - position
        }
    }
}
