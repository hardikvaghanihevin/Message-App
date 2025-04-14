package com.hardik.messageapp.domain.usecase.conversation.recyclebin

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecyclebinConversationThreadUseCase @Inject constructor(private val recyclebinRepository: RecyclebinRepository) {
    operator fun invoke(): Flow<List<ConversationThread>> = recyclebinRepository.getRecycleBinConversationThreads()
}