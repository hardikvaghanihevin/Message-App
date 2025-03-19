package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.message.delete.DeleteMessageUseCase
import com.hardik.messageapp.domain.usecase.message.fetch.GetMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,

    private val deleteMessageUseCase: DeleteMessageUseCase,
) : ViewModel() {

    //region Fetch Message list

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun fetchSmsMessages() {
        viewModelScope.launch {
            getMessagesUseCase().collectLatest { messages ->
                _messages.value = messages
            }
        }
    }
    //endregion

    //region Delete Messages
    fun deleteSms(smsIds: List<Long>) {
        viewModelScope.launch {
            val isDeleted = deleteMessageUseCase(smsIds)
            if (isDeleted) {
                // Note :- Refresh SMS list after deletion
            }
        }
    }
    //endregion

}
