package com.cyberlogitec.freight9.ui.inventory.tooltipview

import android.content.Context
import android.graphics.Typeface
import android.util.LruCache
import timber.log.Timber

object Typefaces {
    private val FONT_CACHE = LruCache<String, Typeface>(4)

    operator fun get(c: Context, assetPath: String): Typeface? {
        synchronized(FONT_CACHE) {
            var typeface = FONT_CACHE.get(assetPath)
            if (typeface == null) {
                try {
                    typeface = Typeface.createFromAsset(c.assets, assetPath)
                    FONT_CACHE.put(assetPath, typeface)
                } catch (e: Exception) {
                    Timber.e("f9: Could not get typeface '$assetPath' because ${e.message}")
                    return null
                }
            }
            return typeface
        }
    }
}