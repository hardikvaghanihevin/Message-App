package com.hardik.messageapp.domain.model

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson

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
    val threadId: Long,         //(From Thread or Sms) Unique ID for the conversation thread, common for Sms messages
    val id: Long,               //(From Sms) Unique ID for the conversation
    val sender: String,         //(From Sms)
    val messageBody: String,    //(From Sms)
    val creator: String?,       //(From Sms)
    val timestamp: Long,        //(From Sms)
    val dateSent: Long,         //(From Sms)
    val errorCode: Int,         //(From Sms)
    val locked: Int,            //(From Sms)
    val person: String?,        //(From Sms)
    val protocol: String?,      //(From Sms)
    val read: Boolean = false,  //(From Thread) Indicates if the last message was read (default: false)
    val replyPath: Boolean,     //(From Sms)
    val seen: Boolean,          //(From Sms)
    val serviceCenter: String?, //(From Sms)
    val status: Int,            //(From Sms)
    val subject: String?,       // (From Sms)
    val subscriptionId: Int,    // (From Sms)
    val type: Int,              // (From Sms)
    val isArchived: Boolean = false, // (From Sms)
    val snippet: String,        // (From Thread) Last message in the thread
    val date: Long,             // (From Thread) Timestamp of the last message (epoch time in milliseconds)
    val recipientIds: String,   // (From Thread) Recipient ID(s) linked to this thread
    val contactId: Int,
    val normalizeNumber: String,    // (From canonical-addresses) Phone number associated with the recipient ID
    val photoUri: String,       // (From CommonDataKinds.Photo) Contact photo(URI) retrieved (if available)
    val displayName: String,    // (From CommonDataKinds.Phone) Display name set byu Contact name retrieved from the phone number (if available)
    val unSeenCount: Long,       // (From CommonDataKinds.)
    val isPin: Boolean,
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
                return oldItem.threadId == newItem.threadId &&
                        oldItem.id == newItem.id &&
                        oldItem.sender == newItem.sender &&
                        oldItem.messageBody == newItem.messageBody &&
                        oldItem.creator == newItem.creator &&
                        oldItem.timestamp == newItem.timestamp &&
                        oldItem.dateSent == newItem.dateSent &&
                        oldItem.errorCode == newItem.errorCode &&
                        oldItem.locked == newItem.locked &&
                        oldItem.person == newItem.person &&
                        oldItem.protocol == newItem.protocol &&
                        oldItem.read == newItem.read &&
                        oldItem.replyPath == newItem.replyPath &&
                        oldItem.seen == newItem.seen &&
                        oldItem.serviceCenter == newItem.serviceCenter &&
                        oldItem.status == newItem.status &&
                        oldItem.subject == newItem.subject &&
                        oldItem.subscriptionId == newItem.subscriptionId &&
                        oldItem.type == newItem.type &&
                        oldItem.isArchived == newItem.isArchived &&
                        oldItem.snippet == newItem.snippet &&
                        oldItem.date == newItem.date &&
                        oldItem.recipientIds == newItem.recipientIds &&
                        oldItem.normalizeNumber == newItem.normalizeNumber &&
                        oldItem.photoUri == newItem.photoUri &&
                        oldItem.displayName == newItem.displayName &&
                        oldItem.unSeenCount == newItem.unSeenCount &&
                        oldItem.isPin == newItem.isPin
            }
        }

        fun List<ConversationThread>.toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }
    }

}