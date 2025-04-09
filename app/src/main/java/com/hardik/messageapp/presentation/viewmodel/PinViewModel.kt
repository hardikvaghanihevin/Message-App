package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.usecase.pin.GetPinnedConversationsUseCase
import com.hardik.messageapp.domain.usecase.pin.PinConversationsUseCase
import com.hardik.messageapp.domain.usecase.pin.UnpinConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val getPinnedConversationsUseCase: GetPinnedConversationsUseCase,
    private val pinConversationsUseCase: PinConversationsUseCase,
    private val unpinConversationsUseCase: UnpinConversationsUseCase
) : ViewModel() {


    //region Fetch PinnedConversationThread list

    private val _pinnedConversations = MutableStateFlow<List<Long>>(emptyList())
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

//    fun pinConversations(threadIds: List<Long>) {
//        viewModelScope.launch {
//            val success = pinConversationsUseCase(threadIds)
//            if (success) fetchPinnedConversations()
//        }
//    }
//
//    fun unpinConversations(threadIds: List<Long>) {
//        viewModelScope.launch {
//            val success = unpinConversationsUseCase(threadIds)
//            if (success) fetchPinnedConversations()
//        }
//    }
    //endregion
}
