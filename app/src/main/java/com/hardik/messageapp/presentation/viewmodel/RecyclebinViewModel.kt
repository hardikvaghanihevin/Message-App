package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.block.BlockConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.DeleteFromRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.GetRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.MoveToRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.RemoveFromRecyclebinConversationThreadUseCase
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
class RecyclebinViewModel @Inject constructor(
    private val getRecyclebinConversationThreadUseCase: GetRecyclebinConversationThreadUseCase, // get list
    private val moveToRecyclebinConversationThreadUseCase: MoveToRecyclebinConversationThreadUseCase,
    private val removeFromRecyclebinConversationThreadUseCase: RemoveFromRecyclebinConversationThreadUseCase, // restore from recyclebin
    private val blockConversationThreadsUseCase: BlockConversationThreadsUseCase, // do block numbers from recyclebin
    private val deleteFromRecyclebinConversationThreadUseCase: DeleteFromRecyclebinConversationThreadUseCase, // delete permanently from recyclebin

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
) : ViewModel() {

    init { fetchRecycleBinConversationThreads() }
    //region Fetch deleted ConversationThread list
    private val _recyclebinConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val recyclebinConversations = _recyclebinConversations.asStateFlow()
    fun fetchRecycleBinConversationThreads() {
        viewModelScope.launch{
            getRecyclebinConversationThreadUseCase().collect { binList ->
                _recyclebinConversations.value = binList
            }
        }
        //getRecyclebinConversationThreadUseCase()
    }
    //endregion

    //region Add and Remove RecycleBin ConversationThread
/*    fun moveToRecyclebin(recycleBinThreads: List<RecycleBinThreadEntity>) {
        viewModelScope.launch {
            val success = moveToRecyclebinConversationThreadUseCase(recycleBinThreads = recycleBinThreads)
        }
    }*/

    private val _isRestoreConversationThread = MutableStateFlow<Boolean>(false)
    val isRestoreConversationThread: StateFlow<Boolean> = _isRestoreConversationThread.asStateFlow()

    fun restoreConversations(threadIds: List<Long>) {
        viewModelScope.launch {
            removeFromRecyclebinConversationThreadUseCase(threadIds = threadIds)
                .collectLatest { isUnblocked -> _isRestoreConversationThread.value = isUnblocked

                    if (isUnblocked) fetchRecycleBinConversationThreads() // refresh data list
                }
        }
    }
    //endregion Add and Remove RecycleBin ConversationThread

    //region Block Conversation Thread
    private val _isBlockRecyclebinConversationThread = MutableStateFlow<Boolean>(false)
    val isBlockRecyclebinConversationThread: StateFlow<Boolean> = _isBlockRecyclebinConversationThread.asStateFlow()
    fun blockRecyclebinConversationByThreadIds(blockThreads: List<BlockThreadEntity>) {
        viewModelScope.launch {
            blockConversationThreadsUseCase(blockThreads = blockThreads)
               .collectLatest { isBlocked -> _isBlockRecyclebinConversationThread.value = isBlocked

                    if (isBlocked) fetchRecycleBinConversationThreads() // refresh data list
                }
        }
    }
    //endregion Block Conversation Thread

    //region Delete ConversationThread Permanently
    private val _isDeleteRecyclebinConversationThread = MutableStateFlow<Boolean>(false)
    val isDeleteRecyclebinConversationThread: StateFlow<Boolean> = _isDeleteRecyclebinConversationThread.asStateFlow()
    fun deleteRecyclebinConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            deleteFromRecyclebinConversationThreadUseCase(threadIds)
                .collect { isDeleted ->
                    _isDeleteRecyclebinConversationThread.value = isDeleted // ✅ Updates state safely

                    if (isDeleted) { fetchRecycleBinConversationThreads() }// ✅ Executes only after deletion is confirmed
                }
        }
    }
    // endregion  Delete ConversationThread Permanently


    //region Count for selected conversationThreads
    /**
     * Used in [RecyclebinActivity.kt]
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
    val recyclebinAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Boolean, List<ConversationThread>>> by lazy {
        combine(recyclebinConversations, toolbarState, countSelectedConversationThreads) { binList, collapseState, selectedConversations ->
            Triple(binList, collapseState, selectedConversations)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(emptyList(),false, emptyList() ))
    }
    //endregion
}