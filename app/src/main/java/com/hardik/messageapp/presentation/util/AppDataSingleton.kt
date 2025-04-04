package com.hardik.messageapp.presentation.util

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppDataSingleton {
    private val TAG = BASE_TAG + AppDataSingleton::class.java.simpleName

    //region ConversationThreads all
    private val _conversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreads: StateFlow<List<ConversationThread>> = _conversationThreads.asStateFlow()

    fun updateConversationThreads(newList: List<ConversationThread>) {
        //Log.e(TAG, "updateConversationThreads: ", )
        _conversationThreads.value = newList }
    //endregion

    //region ConversationThreads Private all
    private val _conversationThreadsPrivate = MutableStateFlow<List<ConversationThread>>(emptyList())
    val conversationThreadsPrivate: StateFlow<List<ConversationThread>> = _conversationThreadsPrivate.asStateFlow()

    fun updateConversationThreadsPrivate(newList: List<ConversationThread>) {
        //Log.e(TAG, "updateConversationThreadsPrivate: ", )
        _conversationThreadsPrivate.value = newList }
    //endregion

    // Filtered conversation threads that are **not** in the recycle bin, the archived
    private val _filteredConversationThreads = MutableStateFlow<List<ConversationThread>>(emptyList())
    val filteredConversationThreads: StateFlow<List<ConversationThread>> = _filteredConversationThreads.asStateFlow()
    fun filterConversationThreads(conversationThreads: List<ConversationThread>) {
        //Log.e(TAG, "filterConversationThreads: ", )
        _filteredConversationThreads.value = conversationThreads
    }


    //region Messages (note: not used any where
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun updateMessages(newList: List<Message>) {
        //Log.e(TAG, "updateMessages: ", )
        _messages.value = newList }
    //endregion
}