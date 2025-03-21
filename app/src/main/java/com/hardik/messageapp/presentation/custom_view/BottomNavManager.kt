package com.hardik.messageapp.presentation.custom_view

import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.NavViewBottomBinding

object BottomNavManager {

    fun setup(
        binding: NavViewBottomBinding,
        onMessageClick: (() -> Unit)? = null,
        onSettingsClick: (() -> Unit)? = null
    ) {
        // Default selection
        setSelected(binding.navViewBottomLlMessage, binding)

        // Click listeners
        binding.navViewBottomLlMessage.setOnClickListener {
            setSelected(binding.navViewBottomLlMessage, binding)
            onMessageClick?.invoke() // Call function if provided
        }

        binding.navViewBottomLlSettings.setOnClickListener {
            setSelected(binding.navViewBottomLlSettings, binding)
            onSettingsClick?.invoke() // Call function if provided
        }
    }

    private fun setSelected(selectedView: LinearLayout, binding: NavViewBottomBinding) {
        resetSelection(binding)

        selectedView.setBackgroundResource(R.drawable.bottom_nav_background_1)
        val textView = selectedView.getChildAt(0) as TextView
        val selectedColor = ContextCompat.getColor(selectedView.context, R.color.white)

        textView.setTextColor(selectedColor)
        setDrawableColor(textView, selectedColor)
    }

    private fun resetSelection(binding: NavViewBottomBinding) {
        val views = listOf(binding.navViewBottomLlMessage, binding.navViewBottomLlSettings)
        for (view in views) {
            view.setBackgroundResource(R.drawable.bottom_nav_background_0)
            val textView = view.getChildAt(0) as TextView
            val defaultColor = ContextCompat.getColor(view.context, R.color.black)

            textView.setTextColor(defaultColor)
            setDrawableColor(textView, defaultColor)
        }
    }

    private fun setDrawableColor(textView: TextView, color: Int) {
        textView.compoundDrawablesRelative.forEach { drawable ->
            drawable?.setTint(color)
        }
    }
}


/* todo: it also use fun with implement interface for click
object BottomNavManager {

    private var callback: WeakReference<BottomNavCallback>? = null

    fun setup(
        binding: NavViewBottomBinding,
        callback: BottomNavCallback
    ) {
        this.callback = WeakReference(callback)

        // Default selection
        setSelected(binding.navViewBottomLlMessage, binding)

        // Click listeners
        binding.navViewBottomLlMessage.setOnClickListener {
            setSelected(binding.navViewBottomLlMessage, binding)
            this.callback?.get()?.onMessageSelected()
        }

        binding.navViewBottomLlSettings.setOnClickListener {
            setSelected(binding.navViewBottomLlSettings, binding)
            this.callback?.get()?.onSettingsSelected()
        }
    }

    private fun setSelected(selectedView: LinearLayout, binding: NavViewBottomBinding) {
        // Reset all selections
        resetSelection(binding)

        // Apply selected state
        selectedView.setBackgroundResource(R.drawable.bottom_nav_background_1)
        val textView = selectedView.getChildAt(0) as TextView
        val selectedColor = ContextCompat.getColor(selectedView.context, R.color.white)

        textView.setTextColor(selectedColor)
        setDrawableColor(textView, selectedColor)
    }

    private fun resetSelection(binding: NavViewBottomBinding) {
        val views = listOf(binding.navViewBottomLlMessage, binding.navViewBottomLlSettings)
        for (view in views) {
            view.setBackgroundResource(R.drawable.bottom_nav_background_0)
            val textView = view.getChildAt(0) as TextView
            val defaultColor = ContextCompat.getColor(view.context, R.color.black)

            textView.setTextColor(defaultColor)
            setDrawableColor(textView, defaultColor)
        }
    }

    private fun setDrawableColor(textView: TextView, color: Int) {
        textView.compoundDrawablesRelative.forEach { drawable ->
            drawable?.setTint(color)
        }
    }

    interface BottomNavCallback {
        fun onMessageSelected()
        fun onSettingsSelected()
    }
}*/

