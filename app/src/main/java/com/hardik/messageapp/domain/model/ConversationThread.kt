package com.hardik.messageapp.domain.model

data class ConversationThread(
    val threadId: Long,       // Unique ID for the conversation thread
    val sender: String,       // Contact name or phone number
    val lastMessage: String,  // Last message in the thread
    val timestamp: Long       // Timestamp of the last message
)
