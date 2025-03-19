package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.archive.ArchiveConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.archive.GetArchivedConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.archive.UnarchiveConversationThreadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getArchivedConversationThreadsUseCase: GetArchivedConversationThreadsUseCase,
    private val archiveConversationThreadUseCase: ArchiveConversationThreadUseCase,
    private val unarchiveConversationThreadUseCase: UnarchiveConversationThreadUseCase,
) : ViewModel() {

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
    fun archiveConversationThread(threadIds: List<Long>) {
        viewModelScope.launch {
            archiveConversationThreadUseCase(threadIds = threadIds)
            // note :- refresh data list
        }
    }

    fun unarchiveConversationThread(threadIds: List<Long>) {
        viewModelScope.launch {
            unarchiveConversationThreadUseCase(threadIds = threadIds)
            // note :- refresh data list
        }
    }
    //endregion
}