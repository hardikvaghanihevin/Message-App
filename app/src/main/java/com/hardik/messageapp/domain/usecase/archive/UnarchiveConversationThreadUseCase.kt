package com.hardik.messageapp.domain.usecase.archive

import com.hardik.messageapp.domain.repository.ArchiveRepository
import javax.inject.Inject

class UnarchiveConversationThreadUseCase @Inject constructor(private val archiveRepository: ArchiveRepository) {
    suspend operator fun invoke(threadIds: List<Long>) = archiveRepository.unarchiveConversationThread(threadIds)
}