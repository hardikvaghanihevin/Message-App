package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.archive.ArchiveConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.archive.GetArchivedConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.archive.UnarchiveConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
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
class ArchiveViewModel @Inject constructor(
    private val getArchivedConversationThreadsUseCase: GetArchivedConversationThreadsUseCase,
    private val archiveConversationThreadUseCase: ArchiveConversationThreadUseCase,
    private val unarchiveConversationThreadUseCase: UnarchiveConversationThreadUseCase,

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
) : ViewModel() {
    init { fetchArchiveConversationThread() }

    // region Fetch Archive ConversationThread list
    private val _archivedConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val archivedConversations = _archivedConversations.asStateFlow()
    fun fetchArchiveConversationThread(){
        viewModelScope.launch {
            getArchivedConversationThreadsUseCase().collect{ archivedList ->
                _archivedConversations.value = archivedList
            }
        }
    }
    // endregion

    //region Add and Remove Archive ConversationThread
    private val _isArchivedConversationThread = MutableStateFlow<Boolean>(false)
    val isArchivedConversationThread: StateFlow<Boolean> = _isArchivedConversationThread.asStateFlow()
    fun archiveConversationThread(threadIds: List<Long>) {
        viewModelScope.launch {
            archiveConversationThreadUseCase(threadIds = threadIds)
                .collectLatest{ isArchived -> _isArchivedConversationThread.value = isArchived

                    if (isArchived) fetchArchiveConversationThread()
                }
        }
    }

    private val _isUnarchivedConversationThread = MutableStateFlow<Boolean>(false)
    val isUnarchivedConversationThread: StateFlow<Boolean> = _isUnarchivedConversationThread.asStateFlow()
    fun unarchiveConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            unarchiveConversationThreadUseCase(threadIds = threadIds)
                .collectLatest { isUnarchived -> _isUnarchivedConversationThread.value = isUnarchived

                    if (isUnarchived) fetchArchiveConversationThread() // refresh data list
                }
        }
    }
    //endregion

    // region Delete Conversation Thread
    private val _isDeleteArchiveConversationThread = MutableStateFlow<Boolean>(false)
    val isDeleteArchiveConversationThread: StateFlow<Boolean> = _isDeleteArchiveConversationThread.asStateFlow()
    fun deleteArchiveConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            deleteConversationThreadUseCase(threadIds)
                .collect { isDeleted ->
                    _isDeleteArchiveConversationThread.value = isDeleted // ✅ Updates state safely

                    if (isDeleted) { fetchArchiveConversationThread() }// ✅ Executes only after deletion is confirmed
                }
        }
    }
    // endregion Delete Conversation Thread


    //region Count for selected conversationThreads
    /**
     * Used in [ArchiveActivity.kt]
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
    val archiveAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Boolean, List<ConversationThread>>> by lazy {
        combine(archivedConversations, toolbarState, countSelectedConversationThreads) { archivedList, collapseState, selectedConversations ->
            Triple(archivedList, collapseState, selectedConversations)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(emptyList(),false, emptyList() ))
    }
    //endregion
}