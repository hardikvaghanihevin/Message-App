package com.hardik.messageapp.domain.usecase.archive

import com.hardik.messageapp.domain.repository.ArchiveRepository
import javax.inject.Inject

class ArchiveConversationThreadUseCase @Inject constructor(private val archiveRepository: ArchiveRepository) {
    suspend operator fun invoke(threadIds: List<Long>) = archiveRepository.archiveConversationThread(threadIds)
}