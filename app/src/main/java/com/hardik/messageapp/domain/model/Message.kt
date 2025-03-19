package com.hardik.messageapp.domain.model

data class Message(
    val threadId: Long,
    val id: Long,
    val sender: String,
    val messageBody: String,
    val creator: String?,
    val timestamp: Long,
    val dateSent: Long,
    val errorCode: Int,
    val locked: Int,
    val person: String?,
    val protocol: String?,
    val read: Boolean,
    val replyPath: Boolean,
    val seen: Boolean,
    val serviceCenter: String?,
    val status: Int,
    val subject: String?,
    val subscriptionId: Int,
    val type: Int,
    val isArchived: Boolean = false
)
