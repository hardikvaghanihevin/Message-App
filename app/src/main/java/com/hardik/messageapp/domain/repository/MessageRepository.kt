package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    //region Fetch Message list
    fun getMessagesByThreadId(threadId: Long): Flow<List<Message>>
    fun getMessagesByThreadIds(threadIds: List<Long>): Flow<Map<String, List<Message>>> //todo: Map<number/threadId, List<message>>
    fun getMessages(): Flow<List<Message>>
    fun getMessages(messageId: Long): Flow<Message?>
    fun getMessages(messageIds: List<Long>): Flow<List<Message>>
    //endregion

    //region Insert Messages
    suspend fun insertMessage(message: Message)
    suspend fun insertOrUpdateMessages(messages: List<Message>)
    //endregion

    //region Delete Messages
    suspend fun deleteMessage(smsIds: List<Long>): Boolean
    //endregion
}