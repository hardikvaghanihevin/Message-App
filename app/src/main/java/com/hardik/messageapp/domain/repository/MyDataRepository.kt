package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Conversation
import com.hardik.messageapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MyDataRepository {
    fun fetchConversations(): Flow<List<Conversation>>
    fun fetchMessages(): Flow<Map<Long, Message>>
    fun fetchContacts(): Flow<Map<String, Contact>>

}