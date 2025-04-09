package com.hardik.messageapp.domain.repository

interface ReadRepository {
    //region Mark as read/unRead ConversationThread
    suspend fun markAsReadConversationThreads(threadIds: List<Long>): Boolean
    suspend fun markAsUnreadConversationThreads(threadIds: List<Long>): Boolean
    //endregion
}