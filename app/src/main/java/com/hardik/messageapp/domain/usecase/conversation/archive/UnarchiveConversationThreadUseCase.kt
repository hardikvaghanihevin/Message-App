package com.hardik.messageapp.domain.usecase.conversation.archive

import com.hardik.messageapp.domain.repository.ArchiveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UnarchiveConversationThreadUseCase @Inject constructor(private val archiveRepository: ArchiveRepository) {
    //suspend operator fun invoke(threadIds: List<Long>) = archiveRepository.unarchiveConversationThread(threadIds)
    suspend operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        val isArchived: Boolean = archiveRepository.unarchiveConversationThread(threadIds)
        emit(isArchived) // Emits the result
    }.flowOn(Dispatchers.IO)
}