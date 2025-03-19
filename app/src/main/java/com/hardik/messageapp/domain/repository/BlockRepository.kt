package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockRepository {

    //region Fetch BlockConversationThread list
    fun getBlockedNumbers(): Flow<List<BlockedNumber>>
    //endregion

    //region Block and Unblock ConversationThread
    suspend fun blockNumbers(numbers: List<String>): Boolean

    suspend fun unblockNumbers(numbers: List<String>): Boolean
    //endregion
}
