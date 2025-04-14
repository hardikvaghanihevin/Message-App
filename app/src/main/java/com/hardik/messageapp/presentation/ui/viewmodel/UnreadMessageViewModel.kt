package com.hardik.messageapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationUseCase
import com.hardik.messageapp.domain.usecase.conversation.read.GetUnreadConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.read.MarkAsReadConversationThreadUseCase
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnreadMessageViewModel @Inject constructor(
    private val getUnreadConversationThreadUseCase: GetUnreadConversationThreadsUseCase,
    private val markAsReadConversationThreadUseCase: MarkAsReadConversationThreadUseCase,
    private val getConversationUseCase: GetConversationUseCase
) : ViewModel() {
    private val TAG = BASE_TAG + UnreadMessageViewModel::class.java.simpleName

    init { fetchUnreadConversationThread() }

    // region Fetch Unread ConversationThread list
    private val _unreadConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val unreadConversations = _unreadConversations.asStateFlow()
    private fun fetchUnreadConversationThread(){
        viewModelScope.launch {
            getUnreadConversationThreadUseCase().collect{ unreadList ->
                //Log.e(TAG, "fetchUnreadConversationThread: size:${unreadList.size} - $unreadList", )
                _unreadConversations.value = unreadList
            }
        }
    }
    // endregion

    // region Mark as unread Conversation Thread
    /** Mark as read Conversation Thread */
    private val _isMarkAsReadConversationThread = MutableStateFlow<Boolean>(false)
    val isMarkAsReadConversationThread: StateFlow<Boolean> = _isMarkAsReadConversationThread.asStateFlow()
    fun markAsReadConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            markAsReadConversationThreadUseCase(threadIds = threadIds)
                .collectLatest { isRead -> _isMarkAsReadConversationThread.value = isRead

                    if (isRead) getConversationUseCase()
                }
        }
    }
    // endregion Mark as unread Conversation Thread

    //region Count for selected conversationThreads
    /**
     * Used in [UnreaMessageActivity.kt]
     */
    private val _countSelectedConversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val countSelectedConversationThreads: StateFlow<List<ConversationThread>> = _countSelectedConversationThreads.asStateFlow()
    fun onSelectedChanged(selectedConversations: List<ConversationThread>){ _countSelectedConversationThreads.value = selectedConversations }
    //endregion

    //region Toolbar state
    private val _toolbarState = MutableStateFlow<Boolean>(false)// Track Show search todo: managing between 'search' & 'toolbar menus'.
    val toolbarState: StateFlow<Boolean> = _toolbarState.asStateFlow()
    fun onToolbarStateChanged(collapsedState: Boolean) { _toolbarState.value = collapsedState }
    //endregion

    // region Combined state
    val unreadAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Boolean, List<ConversationThread>>> by lazy {
        combine(unreadConversations, toolbarState, countSelectedConversationThreads) { unreadList, collapseState, selectedConversations ->
            Triple(unreadList, collapseState, selectedConversations)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(emptyList(),false, emptyList() ))
    }
    //endregion
}