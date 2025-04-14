package com.hardik.messageapp.domain.usecase.conversation.archive

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivedConversationThreadsUseCase @Inject constructor(private val archiveRepository: ArchiveRepository) {
    operator fun invoke(): Flow<List<ConversationThread>> = archiveRepository.getArchivedConversationThreads()
}
