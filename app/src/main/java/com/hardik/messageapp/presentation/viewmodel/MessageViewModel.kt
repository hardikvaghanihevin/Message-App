package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.message.InsertMessageUseCase
import com.hardik.messageapp.domain.usecase.message.delete.DeleteMessageUseCase
import com.hardik.messageapp.domain.usecase.message.fetch.GetMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,

    private val deleteMessageUseCase: DeleteMessageUseCase,

    private val insertMessageUseCase: InsertMessageUseCase,
) : ViewModel() {

    //region Fetch Message list

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun fetchSmsMessages() {
        viewModelScope.launch { getMessagesUseCase().collectLatest { messages -> _messages.value = messages } }
    }

    private val _messagesOfThread = MutableStateFlow<List<Message>>(emptyList())
    val messagesOfThread: StateFlow<List<Message>> = _messagesOfThread.asStateFlow()

    fun getMessagesByThreadId(threadId: Long) {
        viewModelScope.launch {
            getMessagesUseCase.getMessagesByThreadId(threadId = threadId).collectLatest { messages ->
                _messagesOfThread.value = messages
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

    //region Insert Messages
    private val _smsReceived = MutableSharedFlow<Unit>(
        replay = 1, // Ensures latest value is replayed
        extraBufferCapacity = 1 // Avoids dropping events if no collector is active
    )
    val smsReceived = _smsReceived.asSharedFlow()

    fun insertSms(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            insertMessageUseCase(message)
            _smsReceived.emit(Unit)  // Notify UI about new SMS

            // OR ✅ Notify ConversationThreadViewModel AFTER inserting the message
            //withContext(Dispatchers.Main) { conversationThreadViewModel.fetchConversationThreads() }
        }
    }
    //endregion



}
