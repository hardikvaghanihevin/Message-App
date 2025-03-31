package com.hardik.messageapp.presentation.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R

class CustomDividerItemDecoration(
    context: Context,
    private val marginStart: Int,  // Left padding
    private val marginEnd: Int,    // Right padding
    private val marginTop: Int,    // Top padding
    private val marginBottom: Int  // Bottom padding
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.custom_divider)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        divider ?: return
        val left = parent.paddingLeft + marginStart
        val right = parent.width - parent.paddingRight - marginEnd

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin + marginTop
            val bottom = top + divider.intrinsicHeight + marginBottom

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}
