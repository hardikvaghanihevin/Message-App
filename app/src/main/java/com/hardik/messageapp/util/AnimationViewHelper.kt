package com.hardik.messageapp.util

import android.view.View

object AnimationViewHelper {

    // Function to show a view with animation
    // Function to show a view with animation
    fun showViewWithAnimation(
        view: View, duration: Long = 300,
        startAlpha: Float = 0.5f, endAlpha: Float = 1f,
        startScale: Float = 0.8f, endScale: Float = 1f
    ) {
        view.animate()
            .alpha(endAlpha)
            .scaleX(endScale)
            .scaleY(endScale)
            .setDuration(duration)
            .withStartAction {
                view.visibility = View.VISIBLE
                view.alpha = startAlpha
                view.scaleX = startScale
                view.scaleY = startScale
            }
            .withEndAction {
                view.alpha = endAlpha
                view.scaleX = endScale
                view.scaleY = endScale
            }
            .start()
    }

    // Function to hide a view with animation
    fun hideViewWithAnimation(
        view: View, duration: Long = 300,
        startAlpha: Float = 1f, endAlpha: Float = 0.5f,
        startScale: Float = 1f, endScale: Float = 0.8f
    ) {
        view.animate()
            .alpha(endAlpha)
            .scaleX(endScale)
            .scaleY(endScale)
            .setDuration(duration)
            .withStartAction {
                view.alpha = startAlpha
                view.scaleX = startScale
                view.scaleY = startScale
            }
            .withEndAction {
                view.visibility = View.GONE
                view.alpha = endAlpha
                view.scaleX = endScale
                view.scaleY = endScale
            }
            .start()
    }

    // Function to toggle view visibility with animation
    fun toggleViewVisibilityWithAnimation(
        view: View, isVisible: Boolean, duration: Long = 300,
        startAlpha: Float = 0.5f, endAlpha: Float = 1f,
        startScale: Float = 0.8f, endScale: Float = 1f
    ) {
        if (isVisible) {
            showViewWithAnimation(view, duration, startAlpha, endAlpha, startScale, endScale)
        } else {
            hideViewWithAnimation(
                view, duration, startAlpha = endAlpha, endAlpha = startAlpha,
                startScale = endScale, endScale = startScale
            )
        }
    }

    // Function to toggle view visibility with animation
    fun toggleViewVisibilityWithAnimation(view: View, isVisible: Boolean, duration: Long = 300) {
        if (isVisible) {
            showViewWithAnimation(view, duration)
        } else {
            hideViewWithAnimation(view, duration)
        }
    }

    fun toggleViewVisibilityWithAnimation(
        view: View, isVisible: Int, duration: Long = 300,
        startAlpha: Float = 0.5f, endAlpha: Float = 1f,
        startScale: Float = 0.8f, endScale: Float = 1f
    ) {
        if (isVisible == View.VISIBLE) {
            showViewWithAnimation(view, duration, startAlpha, endAlpha, startScale, endScale)
        } else {
            hideViewWithAnimation(
                view, duration, startAlpha = endAlpha, endAlpha = startAlpha,
                startScale = endScale, endScale = startScale
            )
        }
    }
    fun View.toggleIfNeeded(targetVisibility: Int, duration: Long) {
        if (visibility != targetVisibility) {
            toggleViewVisibilityWithAnimation(this, targetVisibility, duration)
        }
    }
}
