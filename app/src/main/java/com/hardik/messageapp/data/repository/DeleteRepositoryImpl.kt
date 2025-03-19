package com.hardik.messageapp.data.repository

import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.repository.DeleteRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import javax.inject.Inject

class DeleteRepositoryImpl @Inject constructor(
    private val recyclebinRepository: RecyclebinRepository,
) : DeleteRepository {
    //region Delete ConversationThread
    override suspend fun deleteConversationThreads(threadIds: List<Long>): Boolean {
        val recycleBinThreads = threadIds.map { RecycleBinThreadEntity(it) }
        return recyclebinRepository.moveToRecycleBinConversationThread(recycleBinThreads)
    }

    //endregion
}