package com.cyberlogitec.freight9.lib.util

import android.text.InputFilter
import android.text.Spanned

class MinMaxFilter(intMin: Int, intMax: Int) : InputFilter {

    private var intMin: Int = 0
    private var intMax: Int = 0

    init {
        this.intMin = intMin
        this.intMax = intMax
    }

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (isInRange(intMin, intMax, input))
                return null
        }
        catch (e: NumberFormatException) {
            //e.printStackTrace()
        }
        return ""
    }

    private fun isInRange(min: Int, max: Int, input: Int): Boolean {
        return if (max > min) input in min..max else input in max..min
    }
}