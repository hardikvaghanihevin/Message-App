package com.hardik.messageapp.presentation.ui.activity

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.BottomNavMenuManager
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity() : AppCompatActivity()
{
    private val TAG = BASE_TAG + BaseActivity::class.java.simpleName
    val conversationViewModel: ConversationThreadViewModel by viewModels()

    val messageViewModel: MessageViewModel by viewModels()

    private val bottomNavMenuManager = BottomNavMenuManager()


    // Function to detect if keyboard is open
    fun isKeyboardVisible(rootView: View): Boolean { //todo: call like ->isKeyboardVisible(requireActivity().window.decorView)
        val screenHeight = rootView.height
        val visibleHeight = Rect().apply { rootView.getWindowVisibleDisplayFrame(this) }.height()
        return screenHeight - visibleHeight > screenHeight * 0.15 // If more than 15% of screen height is occupied, keyboard is open
    }



    fun hideKeyboard(view: View? = null) {
        val targetView = view ?: currentFocus ?: window.decorView.rootView
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(targetView.windowToken, 0)
        targetView.clearFocus() // This helps prevent the keyboard from popping back up
    }

    fun showKeyboard(view: View? = null) {
        val targetView = view ?: currentFocus ?: window.decorView.rootView
        targetView.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT)
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
        handleBackPress()
    }


    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.e(TAG, "handleOnBackPressed: ", )
                if (!handleOnSoftBackPress()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed() // ✅ Correct way
                }
            }
        })
    }

    /**
     * Override this method in child Activities/Fragments to handle back press softly.
     * If it returns `true`, the default back action is prevented.
     */
    open fun handleOnSoftBackPress(): Boolean { return false }
    //protected abstract fun handleOnSoftBackPress(): Boolean
}