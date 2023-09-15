package com.fazziclay.opentoday.gui.item

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(context: Context) : RecyclerView.ViewHolder(FrameLayout(context)) {
    companion object {
        private const val PADDING_LEFT = 0
        private const val PADDING_TOP = 5
        private const val PADDING_RIGHT = 0
        private const val PADDING_BOTTOM = 5
    }


    @JvmField
    val layout: FrameLayout = itemView as FrameLayout

    init {
        layout.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM)
        layout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    fun bind(view: View?) {
        layout.removeAllViews()
        if (view == null) {
            layout.visibility = View.GONE
            layout.setPadding(0, 0, 0, 0)
        } else {
            layout.visibility = View.VISIBLE
            layout.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM)
            layout.addView(view)
        }
    }

    fun bindOnClick(onClickListener: OnClickListener) {
        layout.setOnClickListener(onClickListener)
    }
}