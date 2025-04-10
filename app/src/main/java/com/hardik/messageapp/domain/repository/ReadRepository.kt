package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.ConversationThread
import kotlinx.coroutines.flow.Flow

interface ReadRepository {
    //region Mark as read/unRead ConversationThread
    fun getUnreadConversationThreads(isGeneral:Boolean): Flow<List<ConversationThread>>
    suspend fun markAsReadConversationThreads(threadIds: List<Long>): Boolean
    suspend fun markAsUnreadConversationThreads(threadIds: List<Long>): Boolean
    suspend fun markAsUnreadConversationCountThreads(): Flow<Map<Long, Long>>
    //endregion
}