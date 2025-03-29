package com.hardik.messageapp.domain.model

import com.google.gson.Gson

class Conversation(
    val threadId: Long,         // Unique ID for the conversation thread
    val snippet: String,        // Last message in the thread
    val date: Long,             // Timestamp of the last message (epoch time in milliseconds)
    val read: Boolean = false,  // Indicates if the last message was read (default: false)
    val recipientIds: String,   // Recipient ID(s) linked to this thread
) {
    companion object {
        fun List<Conversation>.toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }
    }
}