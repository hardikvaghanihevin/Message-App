package com.hardik.messageapp.presentation.util

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

object AppDataSingleton {
    private val TAG = BASE_TAG + AppDataSingleton::class.java.simpleName

    private val singletonScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //region ConversationThreads all
    private val _conversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreads: StateFlow<List<ConversationThread>> = _conversationThreads.asStateFlow()

    fun updateConversationThreads(newList: List<ConversationThread>) {
        //Log.e(TAG, "updateConversationThreads: ", )
        _conversationThreads.value = newList }

    private val _unreadConversationMap = MutableStateFlow<Map<Long, Long>>(emptyMap())
    val unreadConversationMap: StateFlow<Map<Long, Long>> = _unreadConversationMap.asStateFlow()

    fun markAsUnreadConversationCountThreads(unreadMap: Map<Long, Long>) {
        _unreadConversationMap.value = unreadMap
    }
    //endregion ConversationThreads all

    //region For General & Private
    /** ConversationThreads Private all */
    private val _conversationThreadsPrivate = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreadsPrivate: StateFlow<List<ConversationThread>> = _conversationThreadsPrivate.asStateFlow()

    fun updateConversationThreadsPrivate(newList: List<ConversationThread>) {
        //Log.e(TAG, "updateConversationThreadsPrivate: ", )
        _conversationThreadsPrivate.value = newList }

    val unreadMessageCountPrivate: StateFlow<Map<Long, Long>> =
        combine(unreadConversationMap, conversationThreadsPrivate) { unreadMap, privateThreads ->
            val privateThreadIds = privateThreads.map { it.threadId }.toSet()
            unreadMap.filterKeys { it in privateThreadIds }
        }.stateIn(
            scope = singletonScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    /** Filtered conversation threads that are **not** in the recycle bin, the archived */
    private val _conversationThreadsGeneral = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreadsGeneral: StateFlow<List<ConversationThread>> = _conversationThreadsGeneral.asStateFlow()
    fun updateConversationThreadsGeneral(conversationThreads: List<ConversationThread>) {
        //Log.e(TAG, "filterConversationThreads: ", )
        _conversationThreadsGeneral.value = conversationThreads
    }

    val unreadMessageCountGeneral: StateFlow<Map<Long, Long>> =
        combine(unreadConversationMap, conversationThreadsGeneral) { unreadMap, generalThreads ->
            val generalThreadIds = generalThreads.map { it.threadId }.toSet()
            unreadMap.filterKeys { it in generalThreadIds }
        }.stateIn(
            scope = singletonScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )
    //endregion For General & Private


    //region Messages (note: not used any where
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun updateMessages(newList: List<Message>) {
        //Log.e(TAG, "updateMessages: ", )
        _messages.value = newList }
    //endregion
}