package com.hardik.messageapp.domain.model

data class SmsMessage(
    val id: Long,
    val sender: String,
    val message: String,
    val timestamp: Long
)
