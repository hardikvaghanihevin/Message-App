package com.hardik.messageapp.domain.usecase.message.fetch

import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    private val TAG = BASE_TAG + GetMessagesUseCase::class.java.simpleName
    operator fun invoke() = messageRepository.getMessages()
    operator fun invoke(messageId: Long) = messageRepository.getMessages(messageId = messageId)
    operator fun invoke(messageIds: List<Long>) = messageRepository.getMessages(messageIds = messageIds)
    fun getMessagesByThreadId(threadId: Long) = messageRepository.getMessagesByThreadId(threadId = threadId)
}
