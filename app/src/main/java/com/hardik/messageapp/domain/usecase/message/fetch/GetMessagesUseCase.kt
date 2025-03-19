package com.hardik.messageapp.domain.usecase.message.fetch

import com.hardik.messageapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    operator fun invoke() = messageRepository.getMessages()
    operator fun invoke(messageId: Long) = messageRepository.getMessages(messageId = messageId)
    operator fun invoke(messageIds: List<Long>) = messageRepository.getMessages(messageIds = messageIds)
}
