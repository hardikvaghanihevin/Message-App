package com.hardik.messageapp.util

import com.google.android.material.appbar.AppBarLayout

class CollapsingToolbarStateManager(private val appBarLayout: AppBarLayout) {

    companion object {
        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_INTERMEDIATE = 2
    }

    private var currentState = STATE_EXPANDED
    private var listeners = mutableListOf<OnStateChangeListener>()

    init {
        // Initialize the offset changed listener
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
            val totalScrollRange = appBar.totalScrollRange

            // Calculate collapse percentage
            val collapsePercentage = Math.abs(verticalOffset).toFloat() / totalScrollRange.toFloat()

            // Determine the state based on collapse percentage
            val newState = when {
                collapsePercentage == 0f -> STATE_EXPANDED
                collapsePercentage == 1f -> STATE_COLLAPSED
                else -> STATE_INTERMEDIATE
            }

            // Notify listeners if state has changed
            if (newState != currentState) {
                currentState = newState
                notifyStateChange()
            }
        })
    }

    /**
     * Add a listener to be notified of state changes
     */
    fun addOnStateChangeListener(listener: OnStateChangeListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Remove a previously added listener
     */
    fun removeOnStateChangeListener(listener: OnStateChangeListener) {
        listeners.remove(listener)
    }

    /**
     * Get the current state of the toolbar
     */
    fun getCurrentState(): Int {
        return currentState
    }

    /**
     * Check if the toolbar is currently collapsed
     */
    fun isCollapsed(): Boolean {
        return currentState == STATE_COLLAPSED
    }

    /**
     * Check if the toolbar is currently expanded
     */
    fun isExpanded(): Boolean {
        return currentState == STATE_EXPANDED
    }

    /**
     * Notify all registered listeners about the state change
     */
    private fun notifyStateChange() {
        for (listener in listeners) {
            listener.onStateChanged(currentState)
        }
    }

    /**
     * Interface for state change callbacks
     */
    interface OnStateChangeListener {
        fun onStateChanged(newState: Int)
    }
}