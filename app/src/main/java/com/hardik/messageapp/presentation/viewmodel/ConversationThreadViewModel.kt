package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.GetMyDataUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationThreadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationThreadViewModel @Inject constructor(
    private val getConversationThreadUseCase: GetConversationThreadsUseCase,
    private val myDataUseCase: GetMyDataUseCase,

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            myDataUseCase().collectLatest { conversationThread ->
                _conversationThreads.value = conversationThread
            }
        }
    }

    //region Fetch ConversationThread (Message list)

    private val _conversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreads: StateFlow<List<ConversationThread>> =
        _conversationThreads.asStateFlow()

    fun fetchConversationThreads() {
        viewModelScope.launch {
            getConversationThreadUseCase().collectLatest { conversationThreads ->
                _conversationThreads.value = conversationThreads
            }
        }
    }

    private val _conversationThread = MutableStateFlow<ConversationThread?>(null)
    val conversationThread: StateFlow<ConversationThread?> = _conversationThread.asStateFlow()
    fun fetchConversationThread(threadId: Long) {
        viewModelScope.launch {
            getConversationThreadUseCase(threadId = threadId).collectLatest { conversationThread ->
                _conversationThread.value = conversationThread
            }
        }
    }
    //endregion

    //region Delete ConversationThread
    private val _isDeleteConversationThread = MutableStateFlow<Boolean>(false)
    val isDeleteConversationThread: StateFlow<Boolean> = _isDeleteConversationThread.asStateFlow()
    fun deleteSmsThreads(threadIds: List<Long>) {
        viewModelScope.launch {
            val isDeleted = deleteConversationThreadUseCase(threadIds)
            _isDeleteConversationThread.value = isDeleted
        }
    }
    //endregion




}
