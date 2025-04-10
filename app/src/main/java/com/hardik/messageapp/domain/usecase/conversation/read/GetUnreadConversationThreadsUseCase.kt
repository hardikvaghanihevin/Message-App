package com.hardik.messageapp.domain.usecase.conversation.read

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ReadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadConversationThreadsUseCase @Inject constructor(private val readRepository: ReadRepository) {
    operator fun invoke(isGeneral:Boolean = true): Flow<List<ConversationThread>> = readRepository.getUnreadConversationThreads(isGeneral = isGeneral)
}