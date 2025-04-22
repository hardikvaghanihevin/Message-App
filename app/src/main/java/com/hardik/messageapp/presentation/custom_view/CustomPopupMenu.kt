package com.hardik.messageapp.presentation.custom_view

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hardik.messageapp.R



enum class PopupMenu(val items: List<Pair<Int, Int>>) {
    HOME_ALL(
        listOf(
            R.string.delete_all to R.drawable.real_ic_delete,
            R.string.block_conversation to R.drawable.real_ic_block,
            R.string.mark_all_as_read to R.drawable.real_ic_mark_as_read,
            R.string.archived to R.drawable.real_ic_archive,
            R.string.scheduled to R.drawable.real_ic_scheduled,
            R.string.starred_message to R.drawable.real_ic_starred,
            R.string.recycle_bin to R.drawable.real_ic_recyclebin,
            R.string.settings to R.drawable.real_ic_settings
        )
    ),
    HOME_UNLESS_READ(
        listOf(
            R.string.delete_all to R.drawable.real_ic_delete,
            R.string.block_conversation to R.drawable.real_ic_block,
            R.string.archived to R.drawable.real_ic_archive,
            R.string.scheduled to R.drawable.real_ic_scheduled,
            R.string.starred_message to R.drawable.real_ic_starred,
            R.string.recycle_bin to R.drawable.real_ic_recyclebin,
            R.string.settings to R.drawable.real_ic_settings
        )
    ),
    HOME_READ_PIN_BLOCK(
        listOf(
            R.string.mark_as_read to R.drawable.real_ic_mark_as_read,
            R.string.pin_conversation to R.drawable.real_ic_pin,
            R.string.block_conversation to R.drawable.real_ic_block,
        )
    ),
    HOME_UNREAD_UNPIN_BLOCK(
        listOf(
            R.string.mark_as_unread to R.drawable.real_ic_mark_as_unread,
            R.string.unpin_conversation to R.drawable.real_ic_unpin,
            R.string.block_conversation to R.drawable.real_ic_block,
        )
    ),
    HOME_UNREAD_PIN_BLOCK(
        listOf(
            R.string.mark_as_unread to R.drawable.real_ic_mark_as_unread,
            R.string.pin_conversation to R.drawable.real_ic_pin,
            R.string.block_conversation to R.drawable.real_ic_block,
        )
    ),
    HOME_READ_UNPIN_BLOCK(
        listOf(
            R.string.mark_as_read to R.drawable.real_ic_mark_as_read,
            R.string.unpin_conversation to R.drawable.real_ic_unpin,
            R.string.block_conversation to R.drawable.real_ic_block,
        )
    ),
    VIEW_UNREAD_MESSAGE(
        listOf(
            R.string.mark_all_as_read to R.drawable.real_ic_mark_as_read
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

            val titleTextView = menuItemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.popup_item)
            /*val titleTextView = menuItemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.popup_tv)
            val iconImageView = menuItemView.findViewById<ImageView>(R.id.popup_img)
            if (iconResId != null) {
                iconImageView.setImageResource(iconResId)
                iconImageView.visibility = View.VISIBLE
            } else {
                iconImageView.visibility = View.GONE
            }*/

            titleTextView.text = title

            if (iconResId != null) {
                // Set the drawable at the start of the TextView
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
            } else {
                // Remove any existing compound drawables if iconResId is null
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
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
                    setBackgroundColor(ContextCompat.getColor(context, R.color.app_color_03))
                }
                container.addView(separatorView)
            }
        }

    }

    fun showNearAnchorWithMargin(
        showAbove: Boolean,
        alignStart: Boolean,
        marginTopDp: Int = 16,
        marginBottomDp: Int = 16,
        marginStartDp: Int = 16,
        marginEndDp: Int = 16
    ) {
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val anchorX = location[0]
        val anchorY = location[1]
        val anchorWidth = anchorView.width
        val anchorHeight = anchorView.height

        val density = context.resources.displayMetrics.density
        val marginTop = (marginTopDp * density).toInt()
        val marginBottom = (marginBottomDp * density).toInt()
        val marginStart = (marginStartDp * density).toInt()
        val marginEnd = (marginEndDp * density).toInt()

        popupWindow.contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupWindow.contentView.measuredWidth
        val popupHeight = popupWindow.contentView.measuredHeight

        val x = if (alignStart) {
            anchorX + marginStart
        } else {
            anchorX + anchorWidth - popupWidth - marginEnd
        }

        val y = if (showAbove) {
            anchorY - popupHeight - marginBottom
        } else {
            anchorY + anchorHeight + marginTop
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
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

    fun showWithScreenMargin() {
        val screenSize = Point()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getSize(screenSize)
        val screenWidth = screenSize.x
        val screenHeight = screenSize.y

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        // Convert 16dp to px
        val marginPx = (16 * context.resources.displayMetrics.density).toInt()

        // Measure popup content
        popupWindow.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = popupWindow.contentView.measuredWidth
        val popupHeight = popupWindow.contentView.measuredHeight

        // Determine if there's more space below or above the anchor view
        val spaceBelow = screenHeight - (anchorY + anchorView.height)
        val spaceAbove = anchorY

        val showBelow = spaceBelow >= popupHeight + marginPx || spaceBelow >= spaceAbove

        // X offset: try to align to start (left), but keep within screen + margins
        val xOffset = when {
            anchorX + popupWidth + marginPx > screenWidth -> screenWidth - popupWidth - marginPx
            anchorX < marginPx -> marginPx
            else -> anchorX
        }

        // Y offset: either show below or above the anchor, with margin applied
        val yOffset = if (showBelow) {
            (anchorY + anchorView.height).coerceAtMost(screenHeight - popupHeight - marginPx)
        } else {
            (anchorY - popupHeight).coerceAtLeast(marginPx)
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)
    }




}
