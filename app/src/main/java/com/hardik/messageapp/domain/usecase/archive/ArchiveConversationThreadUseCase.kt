package com.hardik.messageapp.domain.usecase.archive

import com.hardik.messageapp.domain.repository.ArchiveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ArchiveConversationThreadUseCase @Inject constructor(private val archiveRepository: ArchiveRepository) {
    //    suspend operator fun invoke(threadIds: List<Long>) = archiveRepository.archiveConversationThread(threadIds)
    suspend operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        val isArchived: Boolean = archiveRepository.archiveConversationThread(threadIds)
        emit(isArchived) // Emits the result
    }.flowOn(Dispatchers.IO)
}