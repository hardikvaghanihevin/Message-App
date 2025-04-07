package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import kotlinx.coroutines.flow.Flow

interface BlockRepository {

    //region Fetch BlockConversationThread list
    fun getBlockedNumbers(): Flow<List<BlockThreadEntity>>
    //endregion

    //region Block and Unblock ConversationThread
    suspend fun blockNumbers(blockThreads: List<BlockThreadEntity>): Boolean

    suspend fun unblockNumbers(blockThreads: List<BlockThreadEntity>): Boolean
    //endregion
}
