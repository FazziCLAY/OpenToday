package com.fazziclay.opentoday.gui.item

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.fazziclay.opentoday.app.items.item.Item

class ItemViewHolder(context: Context) : RecyclerView.ViewHolder(FrameLayout(context)) {
    companion object {
        private const val PADDING_LEFT = 1
        private const val PADDING_TOP = 8
        private const val PADDING_RIGHT = 1
        private const val PADDING_BOTTOM = 8
    }


    @JvmField
    val layout: FrameLayout = itemView as FrameLayout
    @JvmField
    var item: Item? = null
    @JvmField
    var destroyer: Destroyer = Destroyer()

    init {
        layout.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM)
        layout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    fun bind(item: Item?, view: View?) {
        layout.removeAllViews()
        this.item = item
        if (view == null) {
            layout.visibility = View.GONE
            layout.setPadding(0, 0, 0, 0)
        } else {
            layout.visibility = View.VISIBLE
            layout.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM)
            layout.addView(view)
        }
    }

    fun recycle() {
        destroyer.destroy()
        destroyer.recycle()
        layout.removeAllViews()
        this.item = null
    }

    override fun toString(): String {
        return "ItemViewHolder{layout=${layout}, destroyer=${destroyer}, item=${item}}"
    }
}