package com.cyberlogitec.freight9.lib.ui.datepicker

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.datepicker.date.MonthPicker
import com.cyberlogitec.freight9.lib.ui.datepicker.util.LinearGradient
import com.cyberlogitec.freight9.lib.util.getEngShortMonth
import com.cyberlogitec.freight9.lib.util.toDp
import java.text.Format


/**
 * 滚动选择器
 * Created by ycuwq on 2017/12/12.
 */
open class WheelPicker<T> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    /**
     * 数据集合
     */
    private lateinit var dataList: List<T>

    private var mDataFormat: Format? = null

    private var mTextAlign: Paint.Align = Paint.Align.CENTER

    private var mTypeFace: Typeface? = null

    private var mAreaAlign: Int = 1

    /**
     * Item的Text的颜色
     */
    private var mTextColor = 0
    private var mTextSize = 0
    private lateinit var textPaint: Paint

    /**
     * 字体渐变，开启后越靠近边缘，字体越模糊
     */
    private var mIsTextGradual = false

    /**
     * 选中的Item的Text颜色
     */
    private var mSelectedItemTextColor = 0

    /**
     * 选中的Item的Text大小
     */
    private var mSelectedItemTextSize = 0
    private lateinit var selectedItemPaint: Paint

    /**
     * 指示器文字
     * 会在中心文字后边多绘制一个文字。
     */
    private var mIndicatorText: String? = null

    /**
     * 指示器文字颜色
     */
    private var mIndicatorTextColor = 0

    /**
     * 指示器文字大小
     */
    private var mIndicatorTextSize = 0
    private lateinit var indicatorPaint: Paint

    private lateinit var paint: Paint

    /**
     * 最大的一个Item的文本的宽高
     */
    private var mTextMaxWidth = 0
    private var mTextMaxHeight = 0

    /**
     * 输入的一段文字，可以用来测量 mTextMaxWidth
     */
    private var mItemMaximumWidthText: String? = null

    /**
     * 显示的Item一半的数量（中心Item上下两边分别的数量）
     * 总显示的数量为 mHalfVisibleItemCount * 2 + 1
     */
    private var mHalfVisibleItemCount = 0

    /**
     * 两个Item之间的高度间隔
     */
    private var mItemHeightSpace = 0
    private var mItemWidthSpace = 0
    private var mItemHeight = 0

    /**
     * 当前的Item的位置
     */
    private var mCurrentPosition = 0

    /**
     * 是否将中间的Item放大
     */
    private var mIsZoomInSelectedItem = false

    /**
     * 是否显示幕布，中央Item会遮盖一个颜色颜色
     */
    private var mIsShowCurtain = false

    /**
     * 幕布颜色
     */
    private var mCurtainColor = 0

    /**
     * 是否显示幕布的边框
     */
    private var mIsShowCurtainBorder = false

    /**
     * 幕布边框的颜色
     */
    private var mCurtainBorderColor = 0

    /**
     * 整个控件的可绘制面积
     */
    private val mDrawnRect: Rect

    /**
     * 中心被选中的Item的坐标矩形
     */
    private val mSelectedItemRect: Rect

    /**
     * 第一个Item的绘制Text的坐标
     */
    private var mFirstItemDrawX = 0
    private var mFirstItemDrawY = 0

    /**
     * 中心的Item绘制text的Y轴坐标
     */
    private var mCenterItemDrawnY = 0
    private lateinit var mScroller: Scroller
    private val mTouchSlop: Int

    /**
     * 该标记的作用是，令mTouchSlop仅在一个滑动过程中生效一次。
     */
    private var mTouchSlopFlag = false
    private var mTracker: VelocityTracker? = null
    private var mTouchDownY = 0

    /**
     * Y轴Scroll滚动的位移
     */
    private var mScrollOffsetY = 0

    /**
     * 最后手指Down事件的Y轴坐标，用于计算拖动距离
     */
    private var mLastDownY = 0

    /**
     * 是否循环读取
     */
    private var mIsCyclic = true

    /**
     * 最大可以Fling的距离
     */
    private var mMaxFlingY = 0
    private var mMinFlingY = 0

    /**
     * 设置最小滚动速度,如果实际速度小于此速度，将不会触发滚动。
     * @param minimumVelocity 最小速度
     */
    /**
     * 滚轮滑动时的最小/最大速度
     */
    private var minimumVelocity = 50

    /**
     * 设置最大滚动的速度,实际滚动速度的上限
     * @param maximumVelocity 最大滚动速度
     */
    private var maximumVelocity = 12000

    /**
     * 是否是手动停止滚动
     */
    private var mIsAbortScroller = false
    private val mLinearGradient: LinearGradient
    private val mHandler = Handler()
    private var mOnWheelChangeListener: OnWheelChangeListener<T>? = null
    private val mScrollerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mScroller.computeScrollOffset()) {
                mScrollOffsetY = mScroller.currY
                postInvalidate()
                mHandler.postDelayed(this, 16)
            }
            if (mScroller.isFinished || (mScroller.finalY == mScroller.currY
                            && mScroller.finalX == mScroller.currX)) {
                if (mItemHeight == 0) {
                    return
                }
                var position = -mScrollOffsetY / mItemHeight
                position = fixItemPosition(position)
                if (mCurrentPosition != position) {
                    mCurrentPosition = position
                    mOnWheelChangeListener?.onWheelSelected(dataList[position], position)
                }
            }
        }
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker)
        mTextSize = a.getDimensionPixelSize(R.styleable.WheelPicker_itemTextSize, resources.getDimensionPixelSize(R.dimen.WheelItemTextSize))
        mTextColor = a.getColor(R.styleable.WheelPicker_itemTextColor, Color.parseColor("#9d9d9d"))
        mIsTextGradual = a.getBoolean(R.styleable.WheelPicker_textGradual, false)
        mIsCyclic = a.getBoolean(R.styleable.WheelPicker_wheelCyclic, false)
        mHalfVisibleItemCount = a.getInteger(R.styleable.WheelPicker_halfVisibleItemCount, 2)
        mItemMaximumWidthText = a.getString(R.styleable.WheelPicker_itemMaximumWidthText)
        mSelectedItemTextColor = a.getColor(R.styleable.WheelPicker_selectedTextColor, Color.parseColor("#292929"))
        mSelectedItemTextSize = a.getDimensionPixelSize(R.styleable.WheelPicker_selectedTextSize, resources.getDimensionPixelSize(R.dimen.WheelSelectedItemTextSize))
        mCurrentPosition = a.getInteger(R.styleable.WheelPicker_currentItemPosition, 0)
        mItemWidthSpace = a.getDimensionPixelSize(R.styleable.WheelPicker_itemWidthSpace, resources.getDimensionPixelOffset(R.dimen.WheelItemWidthSpace))
        mItemHeightSpace = a.getDimensionPixelSize(R.styleable.WheelPicker_itemHeightSpace, resources.getDimensionPixelOffset(R.dimen.WheelItemHeightSpace))
        mIsZoomInSelectedItem = a.getBoolean(R.styleable.WheelPicker_zoomInSelectedItem, false)
        mIsShowCurtain = a.getBoolean(R.styleable.WheelPicker_wheelCurtain, false)
        mCurtainColor = a.getColor(R.styleable.WheelPicker_wheelCurtainColor, Color.parseColor("#303d3d3d"))
        mIsShowCurtainBorder = a.getBoolean(R.styleable.WheelPicker_wheelCurtainBorder, true)
        mCurtainBorderColor = a.getColor(R.styleable.WheelPicker_wheelCurtainBorderColor, Color.parseColor("#f2f2f2"))
        mIndicatorText = a.getString(R.styleable.WheelPicker_indicatorText)
        mIndicatorTextColor = a.getColor(R.styleable.WheelPicker_indicatorTextColor, mSelectedItemTextColor)
        mIndicatorTextSize = a.getDimensionPixelSize(R.styleable.WheelPicker_indicatorTextSize, mTextSize)
        mAreaAlign = a.getInteger(R.styleable.WheelPicker_areaAlign, 1);
        a.recycle()
    }

    private fun computeTextSize() {
        mTextMaxHeight = 0
        mTextMaxWidth = mTextMaxHeight
        if (dataList.isNotEmpty()) {
            paint.textSize = if (mSelectedItemTextSize > mTextSize) mSelectedItemTextSize.toFloat() else mTextSize.toFloat()
            mTextMaxWidth = if (!TextUtils.isEmpty(mItemMaximumWidthText)) {
                paint.measureText(mItemMaximumWidthText).toInt()
            } else {
                paint.measureText(dataList[0].toString()).toInt()
            }
            val metrics = paint.fontMetrics
            mTextMaxHeight = (metrics.bottom - metrics.top).toInt()
        }
    }

    private fun initPaint() {

        paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = mTextColor
        textPaint.textSize = mTextSize.toFloat()
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)

        selectedItemPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
        selectedItemPaint.style = Paint.Style.FILL
        selectedItemPaint.textAlign = Paint.Align.CENTER
        selectedItemPaint.color = mSelectedItemTextColor
        selectedItemPaint.textSize = mSelectedItemTextSize.toFloat()
        selectedItemPaint.typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)

        indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
        indicatorPaint.style = Paint.Style.FILL
        indicatorPaint.textAlign = Paint.Align.LEFT
        indicatorPaint.color = mIndicatorTextColor
        indicatorPaint.textSize = mIndicatorTextSize.toFloat()
    }

    /**
     * 计算实际的大小
     * @param specMode 测量模式
     * @param specSize 测量的大小
     * @param size     需要的大小
     * @return 返回的数值
     */
    private fun measureSize(specMode: Int, specSize: Int, size: Int): Int {
        return if (specMode == MeasureSpec.EXACTLY) {
            specSize
        } else {
            Math.min(specSize, size)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val specWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val specHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = mTextMaxWidth + mItemWidthSpace
        var height = (mTextMaxHeight + mItemHeightSpace) * getVisibleItemCount()
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        setMeasuredDimension(measureSize(specWidthMode, specWidthSize, width),
                measureSize(specHeightMode, specHeightSize, height))
    }

    /**
     * 计算Fling极限
     * 如果为Cyclic模式则为Integer的极限值，如果正常模式，则为一整个数据集的上下限。
     */
    private fun computeFlingLimitY() {
        mMinFlingY = if (mIsCyclic) Int.MIN_VALUE else -mItemHeight * (dataList.size - 1)
        mMaxFlingY = if (mIsCyclic) Int.MAX_VALUE else 0
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mDrawnRect[paddingLeft, paddingTop, width - paddingRight] = height - paddingBottom
        mItemHeight = mDrawnRect.height() / getVisibleItemCount()
        mFirstItemDrawX = mDrawnRect.centerX()
        mFirstItemDrawY = ((mItemHeight - (selectedItemPaint.ascent() + selectedItemPaint.descent())) / 2).toInt()
        //中间的Item边框
        mSelectedItemRect[paddingLeft, mItemHeight * mHalfVisibleItemCount, width - paddingRight] = mItemHeight + mItemHeight * mHalfVisibleItemCount
        computeFlingLimitY()
        mCenterItemDrawnY = mFirstItemDrawY + mItemHeight * mHalfVisibleItemCount
        mScrollOffsetY = -mItemHeight * mCurrentPosition
    }

    /**
     * 修正坐标值，让其回到dateList的范围内
     * @param position 修正前的值
     * @return  修正后的值
     */
    private fun fixItemPosition(position: Int): Int {
        var calcPosition = position
        if (position < 0) {
            //将数据集限定在0 ~ dataList.size()-1之间
            calcPosition = dataList.size + position % dataList.size
        }
        if (position >= dataList.size) {
            //将数据集限定在0 ~ dataList.size()-1之间
            calcPosition = position % dataList.size
        }
        return calcPosition
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.textAlign = Paint.Align.CENTER
        if (mIsShowCurtain) {
            paint.style = Paint.Style.FILL
            paint.color = mCurtainColor
            canvas.drawRect(mSelectedItemRect, paint)
        }
        if (mIsShowCurtainBorder) {
            paint.style = Paint.Style.FILL
            paint.color = mCurtainBorderColor
            //canvas.drawRect(mSelectedItemRect, paint)
            //canvas.drawRect(mDrawnRect, paint)
            with(mSelectedItemRect) {
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), top.toFloat() + 2.toDp(), paint)
                canvas.drawRect(left.toFloat(), bottom.toFloat(), right.toFloat(), bottom.toFloat() + 2.toDp(), paint)
            }
        }
        val drawnSelectedPos = -mScrollOffsetY / mItemHeight
        paint.style = Paint.Style.FILL
        //首尾各多绘制一个用于缓冲
        for (drawDataPos in drawnSelectedPos - mHalfVisibleItemCount - 1..drawnSelectedPos + mHalfVisibleItemCount + 1) {
            var position = drawDataPos
            if (mIsCyclic) {
                position = fixItemPosition(position)
            } else {
                if (position < 0 || position > dataList.size - 1) {
                    continue
                }
            }

            val data = dataList[position]
            val itemDrawY = mFirstItemDrawY + (drawDataPos + mHalfVisibleItemCount) * mItemHeight + mScrollOffsetY
            //距离中心的Y轴距离
            val distanceY = Math.abs(mCenterItemDrawnY - itemDrawY)
            if (mIsTextGradual) {
                //文字颜色渐变要在设置透明度上边，否则会被覆盖
                //计算文字颜色渐变
                if (distanceY < mItemHeight) {  //距离中心的高度小于一个ItemHeight才会开启渐变
                    val colorRatio = 1 - distanceY / mItemHeight.toFloat()
                    selectedItemPaint.color = mLinearGradient.getColor(colorRatio)
                    textPaint.color = mLinearGradient.getColor(colorRatio)
                } else {
                    selectedItemPaint.color = mSelectedItemTextColor
                    textPaint.color = mTextColor
                }
                //计算透明度渐变
                var alphaRatio: Float
                alphaRatio = if (itemDrawY > mCenterItemDrawnY) {
                    (mDrawnRect.height() - itemDrawY) /
                            (mDrawnRect.height() - mCenterItemDrawnY).toFloat()
                } else {
                    itemDrawY / mCenterItemDrawnY.toFloat()
                }
                alphaRatio = if (alphaRatio < 0.0F) 0.0F else 0.5F
                selectedItemPaint.alpha = (alphaRatio * 255).toInt()
                textPaint.alpha = (alphaRatio * 255).toInt()
            }

            //开启此选项,会将越靠近中心的Item字体放大
            if (mIsZoomInSelectedItem) {
                if (distanceY < mItemHeight) {
                    val addedSize = (mItemHeight - distanceY) / mItemHeight.toFloat() * (mSelectedItemTextSize - mTextSize)
                    selectedItemPaint.textSize = mTextSize + addedSize
                    textPaint.textSize = mTextSize + addedSize
                } else {
                    selectedItemPaint.textSize = mTextSize.toFloat()
                    textPaint.textSize = mTextSize.toFloat()
                }
            } else {
                selectedItemPaint.textSize = mTextSize.toFloat()
                textPaint.textSize = mTextSize.toFloat()
            }

            var drawText = mDataFormat?.format(data) ?: data.toString()
            if (this is MonthPicker) {
                drawText = getEngShortMonth(data.toString().toInt())
            }

            selectedItemPaint.textAlign = mTextAlign
            textPaint.textAlign = mTextAlign

            //在中间位置的Item作为被选中的。
            if (distanceY < mItemHeight / 2) {
                canvas.drawText(drawText, computeDrawX(drawText, selectedItemPaint), itemDrawY.toFloat(), selectedItemPaint)
            } else {
                canvas.drawText(drawText, computeDrawX(drawText, textPaint), itemDrawY.toFloat(), textPaint)
            }
        }

        mIndicatorText?.let { indicatorText ->
            canvas.drawText(indicatorText, mFirstItemDrawX + mTextMaxWidth / 2.toFloat(), mCenterItemDrawnY.toFloat(), indicatorPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain()
        }
        mTracker?.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsAbortScroller = if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                    true
                } else {
                    false
                }
                mTracker?.clear()
                run {
                    mLastDownY = event.y.toInt()
                    mTouchDownY = mLastDownY
                }
                mTouchSlopFlag = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mTouchSlopFlag && Math.abs(mTouchDownY - event.y) < mTouchSlop) {
                    return false
                }
                mTouchSlopFlag = false
                val move = event.y - mLastDownY
                mScrollOffsetY += move.toInt()
                mLastDownY = event.y.toInt()
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (!mIsAbortScroller && mTouchDownY == mLastDownY) {
                    performClick()
                    if (event.y > mSelectedItemRect.bottom) {
                        val scrollItem = (event.y - mSelectedItemRect.bottom).toInt() / mItemHeight + 1
                        mScroller.startScroll(0, mScrollOffsetY, 0,
                                -scrollItem * mItemHeight)
                    } else if (event.y < mSelectedItemRect.top) {
                        val scrollItem = (mSelectedItemRect.top - event.y).toInt() / mItemHeight + 1
                        mScroller.startScroll(0, mScrollOffsetY, 0,
                                scrollItem * mItemHeight)
                    }
                } else {
                    mTracker?.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                    val velocity = mTracker?.yVelocity?.toInt() ?: 0
                    if (Math.abs(velocity) > minimumVelocity) {
                        mScroller.fling(0, mScrollOffsetY, 0, velocity,
                                0, 0, mMinFlingY, mMaxFlingY)
                        mScroller.finalY = mScroller.finalY +
                                computeDistanceToEndPoint(mScroller.finalY % mItemHeight)
                    } else {
                        mScroller.startScroll(0, mScrollOffsetY, 0,
                                computeDistanceToEndPoint(mScrollOffsetY % mItemHeight))
                    }
                }
                if (!mIsCyclic) {
                    if (mScroller.finalY > mMaxFlingY) {
                        mScroller.finalY = mMaxFlingY
                    } else if (mScroller.finalY < mMinFlingY) {
                        mScroller.finalY = mMinFlingY
                    }
                }
                mHandler.post(mScrollerRunnable)
                mTracker?.recycle()
                mTracker = null
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun computeDistanceToEndPoint(remainder: Int): Int {
        return if (Math.abs(remainder) > mItemHeight / 2) {
            if (mScrollOffsetY < 0) {
                -mItemHeight - remainder
            } else {
                mItemHeight - remainder
            }
        } else {
            -remainder
        }
    }

    private fun computeDrawX(text: String, paint: Paint): Float {
        val drawX: Float
        if (mAreaAlign == Paint.Align.LEFT.ordinal) {
            // Left
            drawX = mFirstItemDrawX.toFloat() - (mDrawnRect.width() - paint.measureText(text)) / 2
        } else if (mAreaAlign == Paint.Align.CENTER.ordinal) {
            // Center
            drawX = mFirstItemDrawX.toFloat()
        } else {
            // Right
            drawX = mFirstItemDrawX.toFloat() + (mDrawnRect.width() - paint.measureText(text)) / 2
        }
        return drawX
    }

    fun setOnWheelChangeListener(onWheelChangeListener: OnWheelChangeListener<T>?) {
        mOnWheelChangeListener = onWheelChangeListener
    }

    fun setDataList(dataList: List<T>) {
        this.dataList = dataList
        if (dataList.isNotEmpty()) {
            computeTextSize()
            computeFlingLimitY()
            requestLayout()
            postInvalidate()
        }
    }

    open fun getTextColor(): Int {
        return mTextColor
    }

    /**
     * 一般列表的文本颜色
     * @param textColor 文本颜色
     */
    open fun setTextColor(textColor: Int) {
        if (mTextColor == textColor) {
            return
        }
        textPaint.setColor(textColor)
        mTextColor = textColor
        mLinearGradient.setStartColor(textColor)
        postInvalidate()
    }

    open fun getTextSize(): Int {
        return mTextSize
    }

    /**
     * 一般列表的文本大小
     * @param textSize 文字大小
     */
    open fun setTextSize(textSize: Int) {
        if (mTextSize == textSize) {
            return
        }
        mTextSize = textSize
        textPaint.setTextSize(textSize.toFloat())
        computeTextSize()
        postInvalidate()
    }

    open fun getSelectedItemTextColor(): Int {
        return mSelectedItemTextColor
    }

    open fun setAreaAlign(areaAlign: Int) {
        if (mAreaAlign == areaAlign) {
            return
        }
        mAreaAlign = areaAlign
        postInvalidate()
    }

    /**
     * 设置被选中时候的文本颜色
     * @param selectedItemTextColor 文本颜色
     */
    open fun setSelectedItemTextColor(selectedItemTextColor: Int) {
        if (mSelectedItemTextColor == selectedItemTextColor) {
            return
        }
        selectedItemPaint.setColor(selectedItemTextColor)
        mSelectedItemTextColor = selectedItemTextColor
        mLinearGradient.setEndColor(selectedItemTextColor)
        postInvalidate()
    }

    open fun getSelectedItemTextSize(): Int {
        return mSelectedItemTextSize
    }

    /**
     * 设置被选中时候的文本大小
     * @param selectedItemTextSize 文字大小
     */
    open fun setSelectedItemTextSize(selectedItemTextSize: Int) {
        if (mSelectedItemTextSize == selectedItemTextSize) {
            return
        }
        selectedItemPaint.setTextSize(selectedItemTextSize.toFloat())
        mSelectedItemTextSize = selectedItemTextSize
        computeTextSize()
        postInvalidate()
    }


    open fun getItemMaximumWidthText(): String? {
        return mItemMaximumWidthText
    }

    /**
     * 设置输入的一段文字，用来测量 mTextMaxWidth
     * @param itemMaximumWidthText 文本内容
     */
    open fun setItemMaximumWidthText(itemMaximumWidthText: String) {
        mItemMaximumWidthText = itemMaximumWidthText
        requestLayout()
        postInvalidate()
    }


    open fun getHalfVisibleItemCount(): Int {
        return mHalfVisibleItemCount
    }

    /**
     * 显示的个数等于上下两边Item的个数+ 中间的Item
     * @return 总显示的数量
     */
    open fun getVisibleItemCount(): Int {
        return mHalfVisibleItemCount * 2 + 1
    }

    /**
     * 设置显示数据量的个数的一半。
     * 为保证总显示个数为奇数,这里将总数拆分，总数为 mHalfVisibleItemCount * 2 + 1
     * @param halfVisibleItemCount 总数量的一半
     */
    open fun setHalfVisibleItemCount(halfVisibleItemCount: Int) {
        if (mHalfVisibleItemCount == halfVisibleItemCount) {
            return
        }
        mHalfVisibleItemCount = halfVisibleItemCount
        requestLayout()
    }

    open fun getItemWidthSpace(): Int {
        return mItemWidthSpace
    }

    open fun setItemWidthSpace(itemWidthSpace: Int) {
        if (mItemWidthSpace == itemWidthSpace) {
            return
        }
        mItemWidthSpace = itemWidthSpace
        requestLayout()
    }

    open fun getItemHeightSpace(): Int {
        return mItemHeightSpace
    }

    /**
     * 设置两个Item之间的间隔
     * @param itemHeightSpace 间隔值
     */
    open fun setItemHeightSpace(itemHeightSpace: Int) {
        if (mItemHeightSpace == itemHeightSpace) {
            return
        }
        mItemHeightSpace = itemHeightSpace
        requestLayout()
    }

    open fun getCurrentPosition(): Int {
        return mCurrentPosition
    }

    /**
     * 设置当前选中的列表项,将滚动到所选位置
     * @param currentPosition 设置的当前位置
     */
    open fun setCurrentPosition(currentPosition: Int) {
        setCurrentPosition(currentPosition, true)
    }

    /**
     * 设置当前选中的列表位置
     * @param currentPosition 设置的当前位置
     * @param smoothScroll 是否平滑滚动
     */
    @Synchronized
    fun setCurrentPosition(currentPosition: Int, smoothScroll: Boolean) {
        var currentPosition = currentPosition
        if (currentPosition > dataList.size - 1) {
            currentPosition = dataList.size - 1
        }
        if (currentPosition < 0) {
            currentPosition = 0
        }
        if (mCurrentPosition == currentPosition) {
            return
        }
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
        }

        //如果mItemHeight=0代表还没有绘制完成，这时平滑滚动没有意义
        if (smoothScroll && mItemHeight > 0) {
            mScroller.startScroll(0, mScrollOffsetY, 0, (mCurrentPosition - currentPosition) * mItemHeight)
            //            mScroller.setFinalY(mScroller.getFinalY() +
//                    computeDistanceToEndPoint(mScroller.getFinalY() % mItemHeight));
            val finalY = -currentPosition * mItemHeight
            mScroller.finalY = finalY
            mHandler.post(mScrollerRunnable)
        } else {
            mCurrentPosition = currentPosition
            mScrollOffsetY = -mItemHeight * mCurrentPosition
            postInvalidate()
            mOnWheelChangeListener?.onWheelSelected(dataList[currentPosition], currentPosition)
        }
    }

    open fun isZoomInSelectedItem(): Boolean {
        return mIsZoomInSelectedItem
    }

    open fun setZoomInSelectedItem(zoomInSelectedItem: Boolean) {
        if (mIsZoomInSelectedItem == zoomInSelectedItem) {
            return
        }
        mIsZoomInSelectedItem = zoomInSelectedItem
        postInvalidate()
    }

    open fun isCyclic(): Boolean {
        return mIsCyclic
    }

    /**
     * 设置是否循环滚动。
     * @param cyclic 上下边界是否相邻
     */
    open fun setCyclic(cyclic: Boolean) {
        if (mIsCyclic == cyclic) {
            return
        }
        mIsCyclic = cyclic
        computeFlingLimitY()
        requestLayout()
    }

    open fun getMinimumVelocity(): Int {
        return minimumVelocity
    }

    /**
     * 设置最小滚动速度,如果实际速度小于此速度，将不会触发滚动。
     * @param minimumVelocity 最小速度
     */
    open fun setMinimumVelocity(minimumVelocity: Int) {
        this.minimumVelocity = minimumVelocity
    }

    open fun getMaximumVelocity(): Int {
        return maximumVelocity
    }

    /**
     * 设置最大滚动的速度,实际滚动速度的上限
     * @param maximumVelocity 最大滚动速度
     */
    open fun setMaximumVelocity(maximumVelocity: Int) {
        this.maximumVelocity = maximumVelocity
    }

    open fun isTextGradual(): Boolean {
        return mIsTextGradual
    }

    /**
     * 设置文字渐变，离中心越远越淡。
     * @param textGradual 是否渐变
     */
    open fun setTextGradual(textGradual: Boolean) {
        if (mIsTextGradual == textGradual) {
            return
        }
        mIsTextGradual = textGradual
        postInvalidate()
    }

    open fun isShowCurtain(): Boolean {
        return mIsShowCurtain
    }

    /**
     * 设置中心Item是否有幕布遮盖
     * @param showCurtain 是否有幕布
     */
    open fun setShowCurtain(showCurtain: Boolean) {
        if (mIsShowCurtain == showCurtain) {
            return
        }
        mIsShowCurtain = showCurtain
        postInvalidate()
    }

    open fun getCurtainColor(): Int {
        return mCurtainColor
    }

    /**
     * 设置幕布颜色
     * @param curtainColor 幕布颜色
     */
    open fun setCurtainColor(curtainColor: Int) {
        if (mCurtainColor == curtainColor) {
            return
        }
        mCurtainColor = curtainColor
        postInvalidate()
    }

    open fun isShowCurtainBorder(): Boolean {
        return mIsShowCurtainBorder
    }

    /**
     * 设置幕布是否显示边框
     * @param showCurtainBorder 是否有幕布边框
     */
    open fun setShowCurtainBorder(showCurtainBorder: Boolean) {
        if (mIsShowCurtainBorder == showCurtainBorder) {
            return
        }
        mIsShowCurtainBorder = showCurtainBorder
        postInvalidate()
    }

    open fun getCurtainBorderColor(): Int {
        return mCurtainBorderColor
    }

    /**
     * 幕布边框的颜色
     * @param curtainBorderColor 幕布边框颜色
     */
    open fun setCurtainBorderColor(curtainBorderColor: Int) {
        if (mCurtainBorderColor == curtainBorderColor) {
            return
        }
        mCurtainBorderColor = curtainBorderColor
        postInvalidate()
    }

    fun setIndicatorText(indicatorText: String?) {
        mIndicatorText = indicatorText
        postInvalidate()
    }

    fun setIndicatorTextColor(indicatorTextColor: Int) {
        mIndicatorTextColor = indicatorTextColor
        indicatorPaint.color = mIndicatorTextColor
        postInvalidate()
    }

    fun setIndicatorTextSize(indicatorTextSize: Int) {
        mIndicatorTextSize = indicatorTextSize
        indicatorPaint.textSize = mIndicatorTextSize.toFloat()
        postInvalidate()
    }

    /**
     * 设置数据集格式
     * @param dataFormat 格式
     */
    fun setDataFormat(dataFormat: Format): Unit {
        mDataFormat = dataFormat
        postInvalidate()
    }

    open fun getDataFormat(): Format? {
        return mDataFormat
    }

    open fun setSelectedItemTextAlign(textAlign: Paint.Align) {
        mTextAlign = textAlign
        postInvalidate()
    }

    interface OnWheelChangeListener<T> {
        fun onWheelSelected(item: T, position: Int)
    }

    init {
        initAttrs(context, attrs)
        initPaint()
        mLinearGradient = LinearGradient(mTextColor, mSelectedItemTextColor)
        mDrawnRect = Rect()
        mSelectedItemRect = Rect()
        mScroller = Scroller(context)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
    }
}
