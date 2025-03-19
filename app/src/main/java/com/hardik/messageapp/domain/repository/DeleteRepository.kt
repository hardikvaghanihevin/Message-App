package com.hardik.messageapp.domain.repository

interface DeleteRepository {
    //region Delete ConversationThread
    suspend fun deleteConversationThreads(threadIds: List<Long>): Boolean
    //endregion
}