package com.hardik.messageapp.presentation.custom_view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.hardik.messageapp.R

class CustomPopupMenu(
private val context: Context,
private val anchorView: View,
private val menuItems: List<String>,
private val showUnderLine: Boolean = false,
private val onItemClick: (String) -> Unit
) {
    private val popupWindow: PopupWindow

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_custom_popup_menu, null)
        val container = layout.findViewById<LinearLayout>(R.id.menuContainer)

        // Initialize PopupWindow before using it
        popupWindow = PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            contentView = layout
            isFocusable = true // Allows keyboard input inside the popup
            isOutsideTouchable = true // Allows outside click dismissal
            elevation = 10f // Adds shadow effect
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Ensures outside click dismissal

        }


        // Add menu items dynamically
        menuItems.forEachIndexed { index, item ->
            val menuItemView = LayoutInflater.from(context)
                .inflate(R.layout.item_popup_menu, container, false) as TextView
            menuItemView.text = item
            menuItemView.setOnClickListener {
                onItemClick(item)
                popupWindow.dismiss()
            }
            container.addView(menuItemView)

            // Apply staggered animation
            menuItemView.alpha = 0f
            menuItemView.translationY = 20f
            menuItemView.postDelayed({
                menuItemView.animate().alpha(1f).translationY(0f).setDuration(200).start()
            }, (index * 80).toLong())

            // Add separator only if showUnderLine is true and it's not the last item
            if (showUnderLine && index < menuItems.size - 1) {
                val separatorView = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1 // Separator height
                    ).apply {
                        setMargins(16, 8, 16, 8) // Padding for separator
                    }
                    setBackgroundColor(Color.LTGRAY) // Separator color
                }
                container.addView(separatorView)
            }
        }

    }

    fun show() {
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val xOffset = location[0] + anchorView.width - popupWindow.contentView.measuredWidth
        val yOffset = location[1] + anchorView.height

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
}
