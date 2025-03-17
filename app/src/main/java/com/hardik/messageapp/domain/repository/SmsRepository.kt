package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.SmsMessage
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    fun getSmsMessages(): Flow<List<SmsMessage>>

    suspend fun deleteSms(smsId: Long): Boolean
}