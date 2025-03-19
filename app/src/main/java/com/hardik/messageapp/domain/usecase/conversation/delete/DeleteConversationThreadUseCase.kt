package com.hardik.messageapp.domain.usecase.conversation.delete

import com.hardik.messageapp.domain.repository.DeleteRepository
import javax.inject.Inject

class DeleteConversationThreadUseCase @Inject constructor(private val deleteRepository: DeleteRepository) {
    suspend operator fun invoke(threadIds: List<Long>): Boolean = deleteRepository.deleteConversationThreads(threadIds)
}