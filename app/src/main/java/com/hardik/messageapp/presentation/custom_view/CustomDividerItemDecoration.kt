package com.hardik.messageapp.presentation.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R

class CustomDividerItemDecoration(
    context: Context,
    @DimenRes marginStartRes: Int? = null,
    @DimenRes marginEndRes: Int? = null,
    @DimenRes marginTopRes: Int? = null,
    @DimenRes marginBottomRes: Int? = null,
    private val marginStart: Int = 0,
    private val marginEnd: Int = 0,
    private val marginTop: Int = 0,
    private val marginBottom: Int = 0
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable? = ContextCompat.getDrawable(context, R.drawable.custom_divider)

    private val startPx = marginStartRes?.let { context.resources.getDimensionPixelSize(it) } ?: marginStart
    private val endPx = marginEndRes?.let { context.resources.getDimensionPixelSize(it) } ?: marginEnd
    private val topPx = marginTopRes?.let { context.resources.getDimensionPixelSize(it) } ?: marginTop
    private val bottomPx = marginBottomRes?.let { context.resources.getDimensionPixelSize(it) } ?: marginBottom

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        divider ?: return
        val left = parent.paddingLeft + startPx
        val right = parent.width - parent.paddingRight - endPx

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin + topPx
            val bottom = top + divider.intrinsicHeight + bottomPx

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}

