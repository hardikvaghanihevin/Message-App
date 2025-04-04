package com.hardik.messageapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationUseCase
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.util.AppDataSingleton
import com.hardik.messageapp.presentation.util.CollapsingToolbarStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationThreadViewModel  @Inject constructor(
    private val getConversationUseCase: GetConversationUseCase,

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase
) : ViewModel() {
    private val TAG = BASE_TAG + ConversationThreadViewModel::class.java.simpleName

    /**
     * used in @MessageFragment.kt
     * */
    private val _conversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreads: StateFlow<List<ConversationThread>> = _conversationThreads.asStateFlow()


    /**
     * used in [PrivateFragment]
     * */
    private val _conversationThreadsPrivate = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreadsPrivate: StateFlow<List<ConversationThread>> = _conversationThreadsPrivate.asStateFlow()

    /** Filtered conversation threads that are **not** in the recycle bin, the archived (Ensure first emission waits for all flows before UI update) */
    private val _filteredConversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val filteredConversationThreads: StateFlow<List<ConversationThread>> = _filteredConversationThreads.asStateFlow()


    init { fetchConversationThreads() }

    //region Fetch ConversationThread (Message list)
    fun fetchConversationThreads(needToUpdate: Boolean = false) {
        if (needToUpdate){ viewModelScope.launch { getConversationUseCase() } }

        viewModelScope.launch {

            launch {
                AppDataSingleton.conversationThreads.collectLatest {
                    Log.e(TAG, "fetchConversationThreads: ${it.size}", )
                    if (it.isEmpty()){ getConversationUseCase() }
                    else {
                        _conversationThreads.emit(it)
                    } // Update the state with the fetched conversation threads
                }
            }
            launch {//todo: still pending
                AppDataSingleton.filteredConversationThreads.collectLatest {
                    _filteredConversationThreads.emit(it)
                }
            }
        }
        fetchConversationThreadsPrivate()
    }
    //endregion

    //region Fetch ConversationThread (Message list) Private
    private fun fetchConversationThreadsPrivate(needToUpdate: Boolean = false) {

        if (needToUpdate){ viewModelScope.launch { getConversationUseCase() } }

        viewModelScope.launch {
            AppDataSingleton.conversationThreadsPrivate.collectLatest {
                if (it.isEmpty()){ getConversationUseCase() }
                _conversationThreadsPrivate.value = it  // Update the state with the fetched conversation threads
            }
        }
    }
    //endregion

    //region Delete ConversationThread
    private val _isDeleteConversationThread = MutableStateFlow<Boolean>(false)
    val isDeleteConversationThread: StateFlow<Boolean> = _isDeleteConversationThread.asStateFlow()
    fun deleteConversationByThreads(threadIds: List<Long>) {
        viewModelScope.launch {
            deleteConversationThreadUseCase(threadIds)
                .collect { isDeleted ->
                    _isDeleteConversationThread.value = isDeleted // ✅ Updates state safely

                    if (isDeleted) { fetchConversationThreads(needToUpdate = true) }// ✅ Executes only after deletion is confirmed
                }
        }
    }
    //endregion



    //region Count for selected conversationThreads
    /**
     * Used in [MessageFragment.kt]
     */
    private val _countSelectedConversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val countSelectedConversationThreads: StateFlow<List<ConversationThread>> = _countSelectedConversationThreads.asStateFlow()
    fun onSelectedChanged(selectedConversations: List<ConversationThread>){ _countSelectedConversationThreads.value = selectedConversations }
    //endregion

    //region Collapsed state
    /** Toolbar collapse state: EXPANDED, COLLAPSED, or INTERMEDIATE. */
    private val _toolbarCollapsedState = MutableStateFlow<Int>(CollapsingToolbarStateManager.STATE_EXPANDED)
    private val toolbarCollapsedState: StateFlow<Int> = _toolbarCollapsedState.asStateFlow()
    fun onToolbarStateChanged(collapsedState: Int) { _toolbarCollapsedState.value = collapsedState }
    //endregion

    //region Combined State
    /** Combines selected conversations and toolbar collapse state. */
    val cvThreadAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Int,Pair<List<ConversationThread>,List<ConversationThread>>>> by lazy {
        combine(countSelectedConversationThreads, toolbarCollapsedState, countUnreadGeneralAndPrivateConversationThreads) { conversations, collapseState, undreadGeneralPrivateConverThread ->
            Triple(conversations, collapseState, undreadGeneralPrivateConverThread)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(emptyList(), CollapsingToolbarStateManager.STATE_EXPANDED, Pair(emptyList(), emptyList() )))
    }
    //endregion


    //region Unread conversation threads (Message)
    private val countUnreadConversationThreads: StateFlow<List<ConversationThread>> =
        conversationThreads.map { conversations -> conversations.filter { !it.read } }  // Filter unread conversations
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // Unread conversation threads (Private)
    private val countUnreadPrivateConversationThreads: StateFlow<List<ConversationThread>> =
        conversationThreadsPrivate.map { conversations -> conversations.filter { !it.read } }  // Filter unread conversations
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



    // Combined State
    /** Combines unread conversations count (list) 'General' and 'Private' state. */
    val countUnreadGeneralAndPrivateConversationThreads: StateFlow<Pair<List<ConversationThread>, List<ConversationThread>>> by lazy {
        combine(countUnreadConversationThreads, countUnreadPrivateConversationThreads) { unreadGeneral, unreadPrivate ->
            Pair(unreadGeneral, unreadPrivate)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), emptyList()))
    }
    //endregion

}
