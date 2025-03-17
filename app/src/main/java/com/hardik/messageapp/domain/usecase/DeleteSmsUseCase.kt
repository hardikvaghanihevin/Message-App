package com.hardik.messageapp.domain.usecase

import com.hardik.messageapp.domain.repository.SmsRepository
import javax.inject.Inject

class DeleteSmsUseCase @Inject constructor(private val smsRepository: SmsRepository) {
    suspend operator fun invoke(smsId: Long): Boolean { return smsRepository.deleteSms(smsId) }
}
