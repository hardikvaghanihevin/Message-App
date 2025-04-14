package com.hardik.messageapp.presentation.custom_view

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.NavViewTopBinding

object TopNavManager {
    enum class CountType { BLOCK_NUMBER, BLOCK_MESSAGE }

    fun setup(
        binding: NavViewTopBinding,
        onBlockNumberClick: (() -> Unit)? = null,
        onBlockMessageClick: (() -> Unit)? = null
    ) {
        // Default selection
        setSelected(binding, binding.navViewTopLlNumber, isLeftSelected = true)

        // Click listeners
        binding.navViewTopLlNumber.setOnClickListener {
            setSelected(binding, binding.navViewTopLlNumber, isLeftSelected = true)
            onBlockNumberClick?.invoke() // Call function if provided
        }

        binding.navViewTopLlMessage.setOnClickListener {
            setSelected(binding, binding.navViewTopLlMessage, isLeftSelected = false)
            onBlockMessageClick?.invoke() // Call function if provided
        }
    }

    private fun setSelected(binding: NavViewTopBinding, selectedView: LinearLayout, isLeftSelected: Boolean) {
        resetSelection(binding)

        // Apply selected styles
        selectedView.setBackgroundResource(R.drawable.bottom_nav_background_1)
        val textView = selectedView.getChildAt(0) as TextView
        val textViewCount = selectedView.getChildAt(1) as TextView

        val selectedColor = ContextCompat.getColor(selectedView.context, R.color.color_nav_item_select)

        textView.setTextColor(selectedColor)
        setDrawableColor(textView, selectedColor)

        // Update underline
        binding.navViewBottomUnderline.setBackgroundResource(
            if (isLeftSelected) R.drawable.bg_bottom_nav_underline_left else R.drawable.bg_bottom_nav_underline_right
        )
    }

    private fun resetSelection(binding: NavViewTopBinding) {
        val views = listOf(binding.navViewTopLlNumber, binding.navViewTopLlMessage)
        for (view in views) {
            view.setBackgroundResource(R.drawable.bottom_nav_background_0)
            val textView = view.getChildAt(0) as TextView
            val textViewCount = view.getChildAt(1) as TextView

            val defaultColor = ContextCompat.getColor(view.context, R.color.color_nav_item_unselect)

            textView.setTextColor(defaultColor)
            setDrawableColor(textView, defaultColor)
        }
    }

    private fun setDrawableColor(textView: TextView, color: Int) {
        textView.compoundDrawablesRelative.forEach { drawable ->
            drawable?.setTint(color)
        }
    }

    fun updateCount(binding: NavViewTopBinding, countType: CountType, count: Int) {
        val countTextView: TextView = when (countType) {
            CountType.BLOCK_NUMBER -> binding.numberCount.apply {
                visibility = View.VISIBLE.takeIf { count > 0 } ?: View.GONE
                text = count.toString()
            }
            CountType.BLOCK_MESSAGE -> binding.messageCount.apply {
                visibility = View.VISIBLE.takeIf { count > 0 } ?: View.GONE
                text = count.toString()
            }
        }

//        countTextView.let {
//            // Update the background
//            it.setBackgroundResource(R.drawable.badge_background)
//
//            // Update the text
//            it.text = count
//
//            // Update layout parameters if needed
//            val layoutParams = it.layoutParams
//            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
//            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            it.layoutParams = layoutParams
//
//            it.requestLayout() // Force recalculation
//        }
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

