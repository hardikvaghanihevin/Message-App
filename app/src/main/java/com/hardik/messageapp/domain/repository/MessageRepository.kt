package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    //region Fetch Message list
    fun getMessages(): Flow<List<Message>>
    fun getMessages(messageId: Long): Flow<Message?>
    fun getMessages(messageIds: List<Long>): Flow<List<Message>>
    //endregion

    //region Insert Messages
    suspend fun insertMessage(message: Message)
    //endregion

    //region Delete Messages
    suspend fun deleteMessage(smsIds: List<Long>): Boolean
    //endregion
}