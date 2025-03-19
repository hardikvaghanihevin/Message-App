package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.ConversationThread
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    //region Fetch Archive ConversationThread list
    fun getArchivedConversationThreads(): Flow<List<ConversationThread>>
    //endregion

    //region Add and Remove Archive ConversationThread
    suspend fun archiveConversationThread(threadIds: List<Long>): Boolean
    suspend fun unarchiveConversationThread(threadIds: List<Long>): Boolean
    //endregion
}