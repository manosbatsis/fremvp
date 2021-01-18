package com.cyberlogitec.freight9.lib.ui.textpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R

class TextPicker(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0): RecyclerView(context, attrs, defStyle) {
    interface OnValueChangeListener {
        fun onValueChange(textPicker: TextPicker, value: String, index: Int)
    }

    var divider: Drawable? = context.getDrawable(R.drawable.textpicker_divider)

    internal var _value: String? = null
    val value: String?
        get() = _value

    internal var _index: Int = -1
    var index: Int
        get() = _index
        set(position) {
            _index = position
            _value = (adapter as TextAdapter).items[index]._value

            scrollToPosition(position +  0)
        }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        initAttributes(attrs)
        layoutManager = LinearLayoutManager(context)
        adapter = TextAdapter()
        setHasFixedSize(true)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)
    }

    private fun initAttributes(attributeSet: AttributeSet?) {
        if (attributeSet == null) {
            return
        }
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TextPicker)
        divider = context.getDrawable(R.drawable.textpicker_divider)
        this.overScrollMode = View.OVER_SCROLL_NEVER
        typedArray.recycle()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        if (SCROLL_STATE_IDLE == state) {
            with (layoutManager as LinearLayoutManager) {
                val textAdapter = adapter as TextAdapter
                _index = this.findFirstCompletelyVisibleItemPosition()
                _value = textAdapter.items[_index]._value
                textAdapter.listeners.forEach {

                    // init text color
                    println("jk: TextPicker: onScrollStateChanged: _index: ${_index}, _value: ${_value}")
                    it.onValueChange(this@TextPicker, textAdapter.items[_index]._value, _index)
                }
            }
        }
    }

    fun setSelected(index: Int) {
        println("setSelected: " + index)

        val textAdapter = adapter as TextAdapter
        for (i in 0 .. textAdapter.items.size - 1) {
            textAdapter.items[i]._isSelected = false
        }
        textAdapter.items[index]._isSelected = true

        textAdapter.notifyDataSetChanged()
    }

    fun setItems(items: List<TextItem>) {
        val textAdapter = adapter as TextAdapter
        textAdapter.items = items
        textAdapter.notifyDataSetChanged()

        scrollToPosition(0)

        if (items.isEmpty()) {
            _index = -1
            _value = null
        } else {
            _index = 0
            _value = items[0]._value
        }
    }

    fun getItems(): List<TextItem> {
        return (adapter as TextAdapter).items
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val y = (height / 5)

        divider?.let {
            it.setBounds(0, 2 * y, width, 2 * y + it.intrinsicHeight)
            it.draw(canvas)
            it.setBounds(0, 3 * y, width, 3 * y + (it.intrinsicHeight))
            it.draw(canvas)
        }
    }

    fun addOnValueChangeListener(listener: OnValueChangeListener): Boolean {
        return (adapter as TextAdapter).listeners.add(listener)
    }

    fun removeOnValueChangeListener(listener: OnValueChangeListener): Boolean {
        return (adapter as TextAdapter).listeners.remove(listener)
    }

    override fun setAdapter(newAdapter: Adapter<*>?) {
        if (newAdapter is TextAdapter) {
            setTextAdapter(newAdapter)
        } else {
            throw Exception("Adapter must be an instance of TextAdapter")
        }
    }

    fun setTextAdapter(newAdapter: TextAdapter) {
        super.setAdapter(newAdapter)
    }
}
