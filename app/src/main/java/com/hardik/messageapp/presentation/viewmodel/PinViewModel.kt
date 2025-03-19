package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.pin.GetPinnedConversationsUseCase
import com.hardik.messageapp.domain.usecase.pin.PinConversationUseCase
import com.hardik.messageapp.domain.usecase.pin.UnpinConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val getPinnedConversationsUseCase: GetPinnedConversationsUseCase,
    private val pinConversationUseCase: PinConversationUseCase,
    private val unpinConversationUseCase: UnpinConversationUseCase
) : ViewModel() {


    //region Fetch PinnedConversationThread list

    private val _pinnedConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val pinnedConversations = _pinnedConversations.asStateFlow()
    fun fetchPinnedConversations() {
        viewModelScope.launch {
            getPinnedConversationsUseCase().collect { pinnedList ->
                _pinnedConversations.value = pinnedList
            }
        }
    }
    //endregion

    //region Pin and Unpin ConversationThread

    fun pinConversations(threadIds: List<Long>) {
        viewModelScope.launch {
            val success = pinConversationUseCase(threadIds)
            if (success) fetchPinnedConversations()
        }
    }

    fun unpinConversations(threadIds: List<Long>) {
        viewModelScope.launch {
            val success = unpinConversationUseCase(threadIds)
            if (success) fetchPinnedConversations()
        }
    }
    //endregion
}
