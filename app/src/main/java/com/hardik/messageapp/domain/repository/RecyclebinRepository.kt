package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import kotlinx.coroutines.flow.Flow

interface RecyclebinRepository {

    //region Fetch deleted ConversationThread list
    fun getRecycleBinConversationThreads(): Flow<List<ConversationThread>>
    //endregion

    //region Add and Remove RecycleBin ConversationThread
    suspend fun moveToRecycleBinConversationThread(recycleBinThreads: List<RecycleBinThreadEntity>): Boolean

    suspend fun restoreConversationThreads(threadIds: List<Long>): Boolean
    //endregion


    //region Block deleted ConversationThread
    suspend fun blockNumbers(numbers: List<String>): Boolean
    //endregion

    //region Delete ConversationThread Permanently
    suspend fun deletePermanently(threadIds: List<Long>): Boolean
    //endregion
}