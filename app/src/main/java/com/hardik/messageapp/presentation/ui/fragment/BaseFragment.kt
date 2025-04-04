package com.hardik.messageapp.presentation.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment {

    constructor() : super() // Default constructor for fragments without a layout ID

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) // Constructor for fragments with a layout ID

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPress()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (handleSoftBackPress()) {
                    return // Consume back press
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed() // Default back press
                }
            }
        })
    }

    protected abstract fun handleSoftBackPress(): Boolean
}
