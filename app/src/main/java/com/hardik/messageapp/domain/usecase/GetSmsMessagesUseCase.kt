package com.hardik.messageapp.domain.usecase

import com.hardik.messageapp.domain.model.SmsMessage
import com.hardik.messageapp.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSmsMessagesUseCase @Inject constructor(private val smsRepository: SmsRepository) {
    operator fun invoke(): Flow<List<SmsMessage>> { return smsRepository.getSmsMessages() }
}
