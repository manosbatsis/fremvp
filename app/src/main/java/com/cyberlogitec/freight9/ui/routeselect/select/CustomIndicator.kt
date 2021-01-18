package com.cyberlogitec.freight9.ui.routeselect.select

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout

class CustomIndicator : LinearLayout {
    private var mContext: Context
    private var mDefaultCircle: Int = 0
    private var mSelectCircle: Int = 0
    private var imageDot: MutableList<ImageView> = mutableListOf()

    private val temp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)

    constructor(context: Context) : super(context){
        mContext = context
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
    }

    fun createDotPanel(count: Int, defaultCircle: Int, selectCircle: Int, position: Int){
        this.removeAllViews()
        mDefaultCircle = defaultCircle
        mSelectCircle = selectCircle

        for (i in 0 until count){
            imageDot.add(ImageView(mContext).apply { setPadding(temp.toInt(), 0, temp.toInt(), 0) })
            this.addView(imageDot[i])
        }
        selectDot(position)
    }

    fun selectDot(position: Int){
        for (i in imageDot.indices){
//            Timber.v("diver:/ select position: "+position)
            if(i==position){
                imageDot[i].setImageResource(mSelectCircle)
            } else {
                imageDot[i].setImageResource(mDefaultCircle)
            }
        }
    }
}