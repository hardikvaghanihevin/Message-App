package com.hardik.messageapp.domain.model

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson

data class Message(
    val id: Long,
    val threadId: Long,
    val sender: String,
    val messageBody: String,
    val timestamp: Long,
    val dateSent: Long = 0L,
    val creator: String? = null,
    val errorCode: Int = 0,
    val locked: Int = 0,
    val person: String? = null,
    val protocol: String? = null,
    val read: Boolean = false,
    val replyPath: Boolean = false,
    val seen: Boolean = false,
    val serviceCenter: String? = null,
    val status: Int = 0,
    val subject: String? = null,
    val subscriptionId: Int = 0,
    val type: Int = 0, //Telephony.Sms.MESSAGE_TYPE_OUTBOX todo:checked value here

    val normalizeNumber: String = "",
    val displayName: String = "",
    val photoUri: String = "",
    val isArchived: Boolean = false,
    val unSeenCount: Int = 0,
    val matchFoundCount: Int = 0, // user for searching queries
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
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
                        oldItem.normalizeNumber == newItem.normalizeNumber &&
                        oldItem.displayName == newItem.displayName &&
                        oldItem.photoUri == newItem.photoUri &&
                        oldItem.isArchived == newItem.isArchived &&
                        oldItem.unSeenCount == newItem.unSeenCount &&
                        oldItem.matchFoundCount == newItem.matchFoundCount
            }
        }

        fun List<Message>.toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }
    }
}
