package com.hardik.messageapp.presentation.ui.activity

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.BottomNavMenuManager
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity() : AppCompatActivity()
{
    val conversationViewModel: ConversationThreadViewModel by viewModels()

    val messageViewModel: MessageViewModel by viewModels()

    private val bottomNavMenuManager = BottomNavMenuManager()


    // Function to detect if keyboard is open
    fun isKeyboardVisible(rootView: View): Boolean {
        val screenHeight = rootView.height
        val visibleHeight = Rect().apply { rootView.getWindowVisibleDisplayFrame(this) }.height()
        return screenHeight - visibleHeight > screenHeight * 0.15 // If more than 15% of screen height is occupied, keyboard is open
    }

    fun showBottomMenu(menuId: BottomMenu) {
        bottomNavMenuManager.showBottomMenu(this, menuId)
    }

    fun hideBottomMenu(menuId: BottomMenu? = null) {
        if (menuId == null) { bottomNavMenuManager.hideAllMenus(this) }
        else { bottomNavMenuManager.hideBottomMenu(this, menuId) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!onSoftBackPressed()) {
                    isEnabled = false
                    onBackPressed()
                }
            }
        })
    }

    /**
     * Override this method in child Activities/Fragments to handle back press softly.
     * If it returns `true`, the default back action is prevented.
     */
    open fun onSoftBackPressed(): Boolean {
        return false
    }
}