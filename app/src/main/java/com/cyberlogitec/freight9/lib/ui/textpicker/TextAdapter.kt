package com.cyberlogitec.freight9.lib.ui.textpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import kotlinx.android.synthetic.main.textpicker_item_text.view.*


class TextAdapter(val layoutId: Int = R.layout.textpicker_item_text,
                  val viewId: Int = R.id.ll_textpicker_item_text): RecyclerView.Adapter<TextAdapter.ViewHolder>() {

    internal var items: List<TextItem> = emptyList()
    internal val listeners = mutableSetOf<TextPicker.OnValueChangeListener>()
    lateinit var layoutManager: LinearLayoutManager
    lateinit var parent: TextPicker

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llView: LinearLayout = view.findViewById(R.id.ll_textpicker_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        val height = parent.measuredHeight / 5
        itemView.minimumHeight = height
        itemView.layoutParams.height = height

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size + 4   // front:2 + rear:2 ==> add 4 empty items
    }

    override fun onBindViewHolder(holder: TextAdapter.ViewHolder, position: Int) {
        println("jk: TextAdapter: onBindViewHolder( ${position} )")

        if (items.isEmpty()) return

        // Container type 처럼 구성되는 경우
        var isAddedPrefix = false
        // POL Code, POL Name 으로 구성되는 경우
        if (items[0]._prefix.isNotEmpty()) isAddedPrefix = true

        holder.llView.textpicker_item_prefix_text.visibility = if (isAddedPrefix) View.VISIBLE else View.GONE

        // Prefix display
        if (isAddedPrefix) {
            holder.llView.textpicker_item_prefix_text.setTextAppearance(R.style.txt_opensans_eb_16_verylightpink)
            if (position <= 1 || position > items.size + 1) {
                holder.llView.textpicker_item_prefix_text.text = ""
                holder.itemView.setOnClickListener(null)
            } else {
                holder.llView.textpicker_item_prefix_text.text = items[position - 2]._prefix
                if (items[position - 2]._isSelected) {
                    holder.llView.textpicker_item_prefix_text.setTextAppearance(
                            if (isAddedPrefix) R.style.txt_opensans_eb_16_greyishbrown else R.style.txt_opensans_eb_16_greyishbrown)
                }
            }
        }

        // Value display
        holder.llView.textpicker_item_text.setTextAppearance(R.style.txt_opensans_r_16_bfbfbf)
        if (position <= 1 || position > items.size + 1) {
            holder.llView.textpicker_item_text.text = ""
            holder.itemView.setOnClickListener(null)
        } else {
            holder.llView.textpicker_item_text.text = items[position - 2]._value
            if (items[position - 2]._isSelected) {
                holder.llView.textpicker_item_text
                        .setTextAppearance(if (isAddedPrefix) R.style.txt_opensans_r_16_737373 else R.style.txt_opensans_r_16_333333)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        parent = recyclerView as TextPicker
        layoutManager = parent.layoutManager as LinearLayoutManager
    }
}