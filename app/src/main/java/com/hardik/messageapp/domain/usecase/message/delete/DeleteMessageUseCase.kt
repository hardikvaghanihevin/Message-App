package com.hardik.messageapp.domain.usecase.message.delete

import com.hardik.messageapp.domain.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(smsIds: List<Long>): Boolean { return messageRepository.deleteMessage(smsIds) }
}
