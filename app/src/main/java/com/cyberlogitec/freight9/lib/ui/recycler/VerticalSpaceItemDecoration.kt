package com.cyberlogitec.freight9.lib.ui.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration : RecyclerView.ItemDecoration() {

    var verticalSpaceHeight:Int? = null

    fun VerticalSpaceItemDecoration(spaceHeight:Int) {
        verticalSpaceHeight = spaceHeight
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if(parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount -1) {
            outRect.bottom = verticalSpaceHeight!!
        }
    }
}


