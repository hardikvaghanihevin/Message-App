package com.hardik.messageapp.presentation.custom_view

import android.app.Activity
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.hardik.messageapp.R
import com.hardik.messageapp.helper.Constants.BASE_TAG

enum class BottomMenu(val menuId: Int) {
    BOTTOM_MENU_1_ARCHIVE_DELETE_MORE(R.id.included_navViewBottomMenu1), // Main Activity
    BOTTOM_MENU_2_UNARCHIVE_BLOCK_DELETE(R.id.included_navViewBottomMenu2), // Main Activity
    BOTTOM_MENU_3_RESTORE_BLOCK_DELETE(R.id.included_navViewBottomMenu3), // Search Activity
    BOTTOM_MENU_4_DELETE_UNBLOCK(R.id.included_navViewBottomMenu4), // Contact Activity
    BOTTOM_MENU_5_FAVORITE_COPY_DELETE_MORE(R.id.included_navViewBottomMenu5), // Settings Activity
    BOTTOM_MENU_7_BLOCK_ARCHIVE_EDIT_DELETE(R.id.included_navViewBottomMenu7), // Settings Activity
    BOTTOM_MENU_8_UNBLOCK_UNARCHIVE_ADD_DELETE(R.id.included_navViewBottomMenu8), // General Activity (Global);;
}

class BottomNavMenuManager {
    private val TAG = BASE_TAG + BottomNavMenuManager::class.java.simpleName
    private val bottomMenus = BottomMenu.values().map { it.menuId }

    /**
     * Hide all bottom menus (only if they exist in the given activity)
     */
    fun hideAllMenus(activity: Activity) {
        bottomMenus.forEach { menuId ->
            val menuView = activity.findViewById<View>(menuId)
            if (menuView?.visibility == View.VISIBLE) {
                menuView.startAnimation(getHideAnimation(menuView))
                menuView.visibility = View.GONE
            }
        }
    }

    /**
     * Show only the selected bottom menu (only if it exists in the given activity)
     */
    fun showBottomMenu(activity: Activity, menu: BottomMenu) {
        //Log.e(TAG, "showBottomMenu: Showing ${menu.name}")

        // Hide other menus except the one to be shown
        bottomMenus.forEach { menuId ->
            if (menuId != menu.menuId) {
                val menuView = activity.findViewById<View>(menuId)
                if (menuView?.visibility == View.VISIBLE) {
                    menuView.startAnimation(getHideAnimation(menuView))
                    menuView.visibility = View.GONE
                }
            }
        }

        // Show selected menu with animation
        val menuView = activity.findViewById<View>(menu.menuId)
        if (menuView?.visibility != View.VISIBLE) {
            menuView.visibility = View.VISIBLE
            menuView.startAnimation(getShowAnimation(menuView))
        }
    }

    /**
     * Hide only the specified bottom menu (if it exists in the given activity)
     */
    fun hideBottomMenu(activity: Activity, menu: BottomMenu) {
        val menuView = activity.findViewById<View>(menu.menuId)
        if (menuView?.visibility == View.VISIBLE) {
            menuView.startAnimation(getHideAnimation(menuView))
            menuView.visibility = View.GONE
        }
    }

    /**
     * Returns the animation for showing the menu (slide up from bottom)
     */
    private fun getShowAnimation(view: View): Animation {
        return TranslateAnimation(0f, 0f, view.height.toFloat(), 0f).apply {
            duration = 300
            fillAfter = true
        }
    }

    /**
     * Returns the animation for hiding the menu (slide down to bottom)
     */
    private fun getHideAnimation(view: View): Animation {
        return TranslateAnimation(0f, 0f, 0f, view.height.toFloat()).apply {
            duration = 300
            fillAfter = false
        }
    }
}
