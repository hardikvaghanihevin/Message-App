package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import kotlinx.coroutines.flow.Flow

interface BlockRepository {

    //region Fetch BlockConversationThread list
    fun getBlockedConversations(): Flow<List<ConversationThread>>
    //endregion

    //region Block and Unblock ConversationThread
    suspend fun blockConversations(blockThreads: List<BlockThreadEntity>): Boolean

    suspend fun unblockConversations(blockThreads: List<BlockThreadEntity>): Boolean
    //endregion
}
