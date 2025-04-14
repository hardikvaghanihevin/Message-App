package com.hardik.messageapp.presentation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.conversation.archive.ArchiveConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.block.BlockConversationThreadsUseCase
import com.hardik.messageapp.domain.usecase.conversation.delete.DeleteConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationUseCase
import com.hardik.messageapp.domain.usecase.conversation.read.MarkAsReadConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.read.MarkAsUnreadConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.conversation.pin.GetPinnedConversationsUseCase
import com.hardik.messageapp.domain.usecase.conversation.pin.PinConversationsUseCase
import com.hardik.messageapp.domain.usecase.conversation.pin.UnpinConversationsUseCase
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.AppDataSingleton
import com.hardik.messageapp.util.CollapsingToolbarStateManager
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

    private val deleteConversationThreadUseCase: DeleteConversationThreadUseCase,
    private val archiveConversationThreadUseCase: ArchiveConversationThreadUseCase,
    private val blockConversationThreadsUseCase: BlockConversationThreadsUseCase,

    private val pinnedConversationsUseCase: GetPinnedConversationsUseCase,
    private val pinConversationsUseCase: PinConversationsUseCase,
    private val unPinConversationsUseCase: UnpinConversationsUseCase,

    private val markAsReadConversationThreadUseCase: MarkAsReadConversationThreadUseCase,
    private val markAsUnreadConversationThreadUseCase: MarkAsUnreadConversationThreadUseCase,

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

    val unreadMessageCountGeneral: StateFlow<Map<Long, Long>> = AppDataSingleton.unreadMessageCountGeneral
    val unreadMessageCountPrivate: StateFlow<Map<Long, Long>> = AppDataSingleton.unreadMessageCountPrivate

    init { fetchConversationThreads() }

    //region Fetch ConversationThread (Message list)
    fun fetchConversationThreads(needToUpdate: Boolean = false) {
        if (needToUpdate){ viewModelScope.launch { getConversationUseCase(TAG) } }

        viewModelScope.launch {

            launch {
                AppDataSingleton.conversationThreads.collectLatest {
                    Log.v(TAG, "fetchConversationThreads: ${it.size}", )
                    if (it.isEmpty()){ getConversationUseCase(TAG) }
                    else {
                        _conversationThreads.emit(it)
                    } // Update the state with the fetched conversation threads
                }
            }
            launch {
                    //_filteredConversationThreads.emit(it) //todo: set pin thread first
                combine(
                    AppDataSingleton.conversationThreadsGeneral,
                    pinnedConversationsUseCase()
                ) { threadList: List<ConversationThread>, pinnedThreadIds: List<Long> ->
                    /* region spastic pin set list
                    val pinnedThreadIdSet = pinnedThreadIds.toSet()

                    val pinned = threadList
                        .filter { it.threadId in pinnedThreadIdSet }
                        .map { it.copy(isPin = true) }

                    val others = threadList
                        .filterNot { it.threadId in pinnedThreadIdSet }
                        .map { it.copy(isPin = false) }

                    pinned + others*/
                    threadList
                }.collectLatest { updatedList ->
                    _filteredConversationThreads.emit(updatedList)
                }
            }
        }
        fetchConversationThreadsPrivate() // when data is update "fetchConversationThreads(needToUpdate: Boolean = false)"
    }
    //endregion

    //region Fetch ConversationThread (Message list) Private
    private fun fetchConversationThreadsPrivate(needToUpdate: Boolean = false) {

        if (needToUpdate){ viewModelScope.launch { getConversationUseCase(TAG) } }

        viewModelScope.launch {
            AppDataSingleton.conversationThreadsPrivate.collectLatest {
                if (it.isEmpty()){ getConversationUseCase(TAG) }
                _conversationThreadsPrivate.value = it  // Update the state with the fetched conversation threads
            }
        }
    }
    //endregion

    //region Add to Archive ConversationThread
    private val _isArchivedConversationThread = MutableStateFlow<Boolean>(false)
    val isArchivedConversationThread: StateFlow<Boolean> = _isArchivedConversationThread.asStateFlow()
    fun archiveConversationThread(threadIds: List<Long>) {
        viewModelScope.launch {
            archiveConversationThreadUseCase(threadIds = threadIds)
                .collectLatest{ isArchived -> _isArchivedConversationThread.value = isArchived

                    if (isArchived) fetchConversationThreadsPrivate(needToUpdate = true)
                }
        }
    }
    //endregion Add to Archive ConversationThread

    //region Delete ConversationThreads
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
    //endregion Delete ConversationThreads

    // region Block Conversation Thread
    private val _isBlockConversationThread = MutableStateFlow<Boolean>(false)
    val isBlockConversationThread: StateFlow<Boolean> = _isBlockConversationThread.asStateFlow()

    fun blockConversationByThreads(blockThreads: List<BlockThreadEntity>) {
        viewModelScope.launch {
            blockConversationThreadsUseCase(blockThreads = blockThreads)
                .collectLatest { isBlocked -> _isBlockConversationThread.value = isBlocked

                    if (isBlocked) fetchConversationThreads(needToUpdate = true) // refresh data list
                }
        }
    }
    // endregion Block Conversation Thread


    // region Pin and Unpin ConversationThread
    /** Pin Conversation Thread */
    fun pinConversationsByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            pinConversationsUseCase(threadIds = threadIds)
                .collectLatest { isPinned ->
                    if (isPinned) fetchConversationThreads(needToUpdate = true)
                }
        }
    }

    /** Unpin Conversation Thread */
    fun unpinConversationsByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            unPinConversationsUseCase(threadIds = threadIds)
                .collectLatest { isUnpinned ->
                    if (isUnpinned) fetchConversationThreads(needToUpdate = true)
                }
        }
    }
    // endregion Pin and Unpin ConversationThread

    // region Mark as read/unread Conversation Thread
    /** Mark as read Conversation Thread */
    private val _isMarkAsReadConversationThread = MutableStateFlow<Boolean>(false)
    val isMarkAsReadConversationThread: StateFlow<Boolean> = _isMarkAsReadConversationThread.asStateFlow()
    fun markAsReadConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            markAsReadConversationThreadUseCase(threadIds = threadIds)
                .collectLatest { isRead -> _isMarkAsReadConversationThread.value = isRead

                    if (isRead) fetchConversationThreads(needToUpdate = true)
                }
        }
    }

    /** Mark as unread Conversation Thread */
    private val _isMarkAsUnreadConversationThread = MutableStateFlow<Boolean>(false)
    val isMarkAsUnreadConversationThread: StateFlow<Boolean> = _isMarkAsUnreadConversationThread.asStateFlow()
    fun markAsUnreadConversationByThreadIds(threadIds: List<Long>) {
        viewModelScope.launch {
            markAsUnreadConversationThreadUseCase(threadIds = threadIds)
                .collectLatest { isUnread -> _isMarkAsUnreadConversationThread.value = isUnread

                    if (isUnread) fetchConversationThreads(needToUpdate = true)
                }
        }
    }
    // endregion Mark as read/unread Conversation Thread




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
    // unreadMessageCountGeneral,unreadConversationThreadsCountGeneral
    // unreadMessageCountPrivate,unreadConversationThreadsCountPrivate

    //region Combined State
    /** Combines selected conversations and toolbar collapse state. */
    val cvThreadAndToolbarCombinedState: StateFlow<Triple<List<ConversationThread>, Int, Pair<Pair<Map<Long, Long>, Int>, Pair<Map<Long, Long>, Int>>>> by lazy {
        combine(
            countSelectedConversationThreads,
            toolbarCollapsedState,
            combine(
                unreadMessageCountGeneral,
                unreadConversationThreadsCountGeneral,
                unreadMessageCountPrivate,
                unreadConversationThreadsCountPrivate
            ) { unreadGenMap, unreadGenCount, unreadPrivMap, unreadPrivCount ->
                Pair(Pair(unreadGenMap, unreadGenCount), Pair(unreadPrivMap, unreadPrivCount))
            }
        ) { conversations, collapseState, unreadCounts ->
            Triple(conversations, collapseState, unreadCounts)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Triple(
                emptyList(),
                CollapsingToolbarStateManager.STATE_EXPANDED,
                Pair(Pair(emptyMap(), 0), Pair(emptyMap(), 0))
            )
        )
    }
    //endregion


    //region BottomNavManager Unread count
    /** Unread conversation threads (General) */
    private val unreadConversationThreadsCountGeneral: StateFlow<Int> = unreadMessageCountGeneral.map { it.size }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    private val countUnreadConversationThreads: StateFlow<List<ConversationThread>> =
        conversationThreads.map { conversations -> conversations.filter { !it.read } }  // Filter unread conversations
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    /** Unread conversation threads (Private) */
    private val unreadConversationThreadsCountPrivate: StateFlow<Int> = unreadMessageCountPrivate.map { it.size }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    private val countUnreadPrivateConversationThreads: StateFlow<List<ConversationThread>> =
        conversationThreadsPrivate.map { conversations -> conversations.filter { !it.read } }  // Filter unread conversations
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



    // Combined State
    /** Combines unread conversations count (list) 'General' and 'Private' state. */
    val unreadGeneralAndPrivateConversationThreadsCount: StateFlow<Pair<Int, Int>> by lazy {
        combine(unreadConversationThreadsCountGeneral, unreadConversationThreadsCountPrivate) { unreadGeneral, unreadPrivate ->
            Pair(unreadGeneral, unreadPrivate)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(0,0))
    }

    val countUnreadGeneralAndPrivateConversationThreads: StateFlow<Pair<List<ConversationThread>, List<ConversationThread>>> by lazy {
        combine(countUnreadConversationThreads, countUnreadPrivateConversationThreads) { unreadGeneral, unreadPrivate ->
            Pair(unreadGeneral, unreadPrivate)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), emptyList()))
    }
    //endregion BottomNavManager Unread count

}
