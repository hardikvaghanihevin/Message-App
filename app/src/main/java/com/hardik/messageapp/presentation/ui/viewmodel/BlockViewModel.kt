package com.hardik.messageapp.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.conversation.block.DeleteBlockConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.block.GetBlockedConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.block.UnblockConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.util.CollapsingToolbarStateManager
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
class BlockViewModel @Inject constructor(
    private val getBlockedConversationThreadsUseCase: GetBlockedConversationThreadsUseCase,
    private val deleteBlockConversationThreadsUseCase: DeleteBlockConversationThreadsUseCase,
    private val unblockConversationThreadsUseCase: UnblockConversationThreadsUseCase,

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
) : ViewModel() {
    private val TAG = BASE_TAG + BlockViewModel::class.java.simpleName

    init { fetchBlockedConversationThread() }

    //region Fetch BlockConversationThread list
    private val _blockedConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val blockedConversations = _blockedConversations.asStateFlow()// todo: use for message(block) list

    private val _blockedNumbers = MutableStateFlow<List<String>>(emptyList())
    val blockedNumbers = _blockedNumbers.asStateFlow()// todo: use for number(block) list

    fun fetchBlockedConversationThread() {
        Log.i(TAG, "fetchBlockedConversationThread: ", )
        viewModelScope.launch {
            getBlockedConversationThreadsUseCase().collect { blockThread ->
                _blockedConversations.value = blockThread.first // todo: here message block
                _blockedNumbers.value = blockThread.second // todo: here block number like [+919586682667, AD-AIRTEL] all type
            }
        }
    }
    //endregion Fetch BlockConversationThread list

    //region Block and Unblock ConversationThread
    private val _isUnblockNumber = MutableStateFlow<Boolean>(false)
    val isUnblockNumber: StateFlow<Boolean> = _isUnblockNumber.asStateFlow()

    fun unblockNumbers(numbers: List<String>) {
        viewModelScope.launch {
            unblockConversationThreadsUseCase.unblockNumbers(numbers)
                .collectLatest { isUnblock ->
                    _isUnblockNumber.value = isUnblock

                    if (isUnblock) fetchBlockedConversationThread() // unblock number
                }
        }
    }

    private val _isUnblockConversationThread = MutableStateFlow<Boolean>(false)
    val isUnblockConversationThread: StateFlow<Boolean> = _isUnblockConversationThread.asStateFlow()

    fun unblockConversations(blockThreads: List<BlockThreadEntity>) {
        viewModelScope.launch {
            unblockConversationThreadsUseCase(blockThreads)
                .collectLatest {isUnblock ->
                    _isUnblockConversationThread.value = isUnblock // ✅ Updates state safely

                    if (isUnblock) fetchBlockedConversationThread() // refresh data list, ✅ Executes only after deletion is confirmed
                }
        }
    }
    //endregion

    //region Delete ConversationThread
    private val _isDeleteBlockConversationThread = MutableStateFlow<Boolean>(false)
    val isDeleteBlockConversationThread: StateFlow<Boolean> = _isDeleteBlockConversationThread.asStateFlow()
    fun deleteBlockConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            deleteConversationThreadUseCase(threadIds)
                .collect { isDeleted ->
                    _isDeleteBlockConversationThread.value = isDeleted // ✅ Updates state safely

                    if (isDeleted) { fetchBlockedConversationThread() }// ✅ Executes only after deletion is confirmed
                }
        }
    }

    fun deleteBlockConversationBySender(senders: List<String>) {
        viewModelScope.launch {
            deleteBlockConversationThreadsUseCase(senders)
                .collect { isDeleted ->
                    _isDeleteBlockConversationThread.value = isDeleted // ✅ Updates state safely

                    if (isDeleted) { fetchBlockedConversationThread() }// ✅ Executes only after deletion is confirmed
                }
        }
    }
    //endregion Delete ConversationThread

    /** Tab bar state: Block number & Block message */
    private val _tabState = MutableStateFlow<Int>(0)
    private val tabState: StateFlow<Int> = _tabState.asStateFlow()
    fun onTabStateChanged(tabPosition: Int) { _tabState.value = tabPosition }

    //region Count for selected conversationThreads
    /**
     * Used in [BlockActivity.kt]
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

    // region Combined state
    val blockAndToolbarCombinedState: StateFlow<Triple<Int, Int, Pair<List<ConversationThread>, List<ConversationThread>>>> by lazy {
        combine(
            tabState,
            toolbarCollapsedState,
            blockedConversations,
            countSelectedConversationThreads
        ) { tabPosition, collapseState, blockedList, selectedList ->
            Triple(tabPosition, collapseState, Pair(blockedList, selectedList))
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Triple(
                0,
                CollapsingToolbarStateManager.STATE_EXPANDED,
                Pair(emptyList(), emptyList())
            )
        )
    }


    //endregion Count for selected conversationThreads
}
