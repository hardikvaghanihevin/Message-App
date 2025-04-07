package com.hardik.messageapp.presentation.custom_view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.hardik.messageapp.R



enum class PopupMenu(val items: List<Pair<Int, Int>>) {
    HOME(
        listOf(
            R.string.delete_all to R.drawable.real_ic_delete,
            R.string.block_conversation to R.drawable.real_ic_block,
            R.string.mark_as_read to R.drawable.real_ic_mark_as_read,
            R.string.archived to R.drawable.real_ic_archive,
            R.string.scheduled to R.drawable.real_ic_scheduled,
            R.string.starred_message to R.drawable.real_ic_starred,
            R.string.recycle_bin to R.drawable.real_ic_recyclebin,
            R.string.settings to R.drawable.real_ic_settings
        )
    ),
    VIEW_UNREAD_MESSAGE(
        listOf(
            R.string.mark_as_read to R.drawable.real_ic_mark_as_read
        )
    ),
    ARCHIVE(
        listOf(
            R.string.unarchive_all to R.drawable.real_ic_unarchive,
            R.string.delete_all to R.drawable.real_ic_delete
        )
    ),
    BLOCK(
        listOf(
            R.string.unblock_all to R.drawable.real_ic_unblock,
            R.string.delete_all to R.drawable.real_ic_delete
        )
    ),
    RECYCLE_BIN(
        listOf(
            R.string.restore_all to R.drawable.real_ic_restore,
            R.string.block_all to R.drawable.real_ic_block,
            R.string.delete_all to R.drawable.real_ic_delete
        )
    ),
    CHAT(
        listOf(
            R.string.share to R.drawable.real_ic_share,
            R.string.forward to R.drawable.real_ic_forward,
            R.string.view_details to R.drawable.real_ic_info
        )
    )
    ;



    fun getMenuItems(context: Context): List<Pair<String, Int>> {
        return items.map { (stringRes, iconRes) ->
            context.getString(stringRes) to iconRes
        }
    }
}
class CustomPopupMenu(
private val context: Context,
private val anchorView: View,
private val menuItems: List<Pair<String, Int?>>,
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
        menuItems.forEachIndexed { index, (title, iconResId) ->
            val menuItemView = LayoutInflater.from(context)
                .inflate(R.layout.item_popup_menu, container, false)

            val titleTextView = menuItemView.findViewById<TextView>(R.id.popup_tv)
            val iconImageView = menuItemView.findViewById<ImageView>(R.id.popup_img)

            titleTextView.text = title

            if (iconResId != null) {
                iconImageView.setImageResource(iconResId)
                iconImageView.visibility = View.VISIBLE
            } else {
                iconImageView.visibility = View.GONE
            }

            menuItemView.setOnClickListener {
                onItemClick(title)
                popupWindow.dismiss()
            }

            container.addView(menuItemView)

            // Animation
            menuItemView.alpha = 0f
            menuItemView.translationY = 20f
            menuItemView.postDelayed({
                menuItemView.animate().alpha(1f).translationY(0f).setDuration(200).start()
            }, (index * 80).toLong())

            // Separator
            if (showUnderLine && index < menuItems.size - 1) {
                val separatorView = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1
                    ).apply {
                        setMargins(24, 8, 24, 8)
                    }
                    setBackgroundColor(Color.LTGRAY)
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

    fun show(showAbove: Boolean = false, alignStart: Boolean = true) {
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        // Convert 25dp to pixels
        val marginDp = 24
        val marginPx = (marginDp * context.resources.displayMetrics.density).toInt()

        // Measure the popup dimensions
        popupWindow.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = popupWindow.contentView.measuredWidth
        val popupHeight = popupWindow.contentView.measuredHeight

        // X offset based on alignment (start or end), with margin
        val xOffset = if (alignStart) {
            anchorX + marginPx
        } else {
            anchorX + anchorView.width - popupWidth - marginPx
        }

        // Y offset based on position (above or below)
        val yOffset = if (showAbove) {
            anchorY - popupHeight
        } else {
            anchorY + anchorView.height
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)
    }


}
