package com.hardik.messageapp.domain.usecase.conversation.fetch

import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import javax.inject.Inject

class GetConversationThreadsUseCase @Inject constructor(private val conversationThreadRepository: ConversationThreadRepository) {
    suspend operator fun invoke() = conversationThreadRepository.getConversationThreads()
    suspend operator fun invoke(threadId: Long) = conversationThreadRepository.getConversationThreads(threadId = threadId)
    suspend operator fun invoke(threadIds: List<Long>) = conversationThreadRepository.getConversationThreads(threadIds = threadIds)
}