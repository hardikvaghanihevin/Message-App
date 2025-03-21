package com.hardik.messageapp.domain.model

import androidx.recyclerview.widget.DiffUtil

/*data class ConversationThread1(
    val threadId: Long,       // Unique ID for the conversation thread
    val sender: String,       // Contact name or phone number
    val lastMessage: String,  // Last message in the thread
    val timestamp: Long,      // Timestamp of the last message
    val isRead: Boolean = false // Default value set to `false`
) {
    companion object {
        val DIFF_CALLBACK1 = object : DiffUtil.ItemCallback<ConversationThread1>() {
            override fun areItemsTheSame(
                oldItem: ConversationThread1,
                newItem: ConversationThread1
            ): Boolean =
                oldItem.threadId == newItem.threadId  // Compare unique thread IDs

            override fun areContentsTheSame(oldItem: ConversationThread1, newItem: ConversationThread1): Boolean {
                return oldItem.sender == newItem.sender &&
                        oldItem.lastMessage == newItem.lastMessage &&
                        oldItem.timestamp == newItem.timestamp
            }
        }
    }
}*/


/**
 * Data class representing a conversation thread.
 *
 * @property threadId Unique identifier for the conversation thread.
 * @property snippet The last message in the thread.
 * @property date Timestamp of the last message (in milliseconds).
 * @property read Indicates whether the last message in the thread has been read.
 * @property recipientIds The recipient(s) associated with this thread (may contain multiple IDs).
 * @property phoneNumber The phone number extracted from the recipient ID.
 * @property contactName The contact name associated with the phone number (if available).
 */
data class ConversationThread(
    val threadId: Long,         // Unique ID for the conversation thread
    val snippet: String,        // Last message in the thread
    val date: Long,             // Timestamp of the last message (epoch time in milliseconds)
    val read: Boolean = false,  // Indicates if the last message was read (default: false)
    val recipientIds: String,   // Recipient ID(s) linked to this thread
    val phoneNumber: String,    // Phone number associated with the recipient ID
    val contactName: String,    // Contact name retrieved from the phone number (if available)
    val displayName: String,    // Display name set byu Contact name retrieved from the phone number (if available)
) {
    companion object {
        /**
         * Callback for calculating the differences between two `ConversationThread` objects
         * in a `ListAdapter` (for RecyclerView).
         */
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ConversationThread>() {

            /**
             * Checks if two conversation threads represent the same item by comparing their unique `threadId`.
             */
            override fun areItemsTheSame(
                oldItem: ConversationThread,
                newItem: ConversationThread
            ): Boolean = oldItem.threadId == newItem.threadId

            /**
             * Checks if the contents of two conversation threads are the same.
             * Compares all relevant fields to detect any updates.
             */
            override fun areContentsTheSame(oldItem: ConversationThread, newItem: ConversationThread): Boolean {
                return oldItem.snippet == newItem.snippet &&
                        oldItem.date == newItem.date &&
                        oldItem.read == newItem.read &&
                        oldItem.recipientIds == newItem.recipientIds &&
                        oldItem.phoneNumber == newItem.phoneNumber &&
                        oldItem.contactName == newItem.contactName
            }
        }
    }
}