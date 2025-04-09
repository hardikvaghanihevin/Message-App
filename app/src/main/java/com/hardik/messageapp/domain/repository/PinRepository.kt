package com.hardik.messageapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface PinRepository {

    //region Fetch PinnedConversationThread list
    fun getPinnedConversations(): Flow<List<Long>>
    //endregion

    //region Pin and Unpin ConversationThread
    suspend fun pinConversations(threadIds: List<Long>): Boolean

    suspend fun unpinConversations(threadIds: List<Long>): Boolean
    //endregion
}


