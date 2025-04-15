package com.hardik.messageapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.conversation.block.BlockConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.recyclebin.DeleteFromRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.recyclebin.GetRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.recyclebin.MoveToRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.recyclebin.RemoveFromRecyclebinConversationThreadUseCase
import com.hardik.messageapp.util.CollapsingToolbarStateManager
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
    private val toolbarState: StateFlow<Boolean> = _toolbarState.asStateFlow()
    fun onToolbarStateChanged(toolbarState: Boolean) { _toolbarState.value = toolbarState }
    //endregion

    //region Collapsed state
    /** Toolbar collapse state: EXPANDED, COLLAPSED, or INTERMEDIATE. */
    private val _toolbarCollapsedState = MutableStateFlow<Int>(CollapsingToolbarStateManager.STATE_EXPANDED)
    private val toolbarCollapsedState: StateFlow<Int> = _toolbarCollapsedState.asStateFlow()
    fun onToolbarStateChanged(collapsedState: Int) { _toolbarCollapsedState.value = collapsedState }
    //endregion

    // region Combined state
    val recyclebinAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Pair<Boolean, Int>, List<ConversationThread>>> by lazy {
        combine(
            recyclebinConversations,
            toolbarState,
            toolbarCollapsedState,
            countSelectedConversationThreads
        ) { binList, isToolbarVisible, collapsedState, selectedConversations ->
            Triple(
                binList,
                Pair(isToolbarVisible, collapsedState),
                selectedConversations
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Triple(
                emptyList(),
                Pair(false, CollapsingToolbarStateManager.STATE_EXPANDED),
                emptyList()
            )
        )
    }
    //endregion
}