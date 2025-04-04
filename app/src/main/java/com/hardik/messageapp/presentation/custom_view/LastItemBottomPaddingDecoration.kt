package com.hardik.messageapp.presentation.custom_view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LastItemBottomPaddingDecoration(private val bottomPadding: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == state.itemCount - 1) { // Only for last item
            outRect.bottom = bottomPadding
        }
    }
}