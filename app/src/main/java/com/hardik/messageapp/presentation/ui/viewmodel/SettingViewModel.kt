package com.hardik.messageapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor() : ViewModel() {

    //region Collapsed state
    /** Toolbar collapse state: EXPANDED, COLLAPSED, or INTERMEDIATE. */
    private val _toolbarCollapsedState = MutableStateFlow<Int>(CollapsingToolbarStateManager.STATE_EXPANDED)
    val toolbarCollapsedState: StateFlow<Int> = _toolbarCollapsedState.asStateFlow()
    fun onToolbarStateChanged(collapsedState: Int) { _toolbarCollapsedState.value = collapsedState }
    //endregion Collapsed state
}