package com.hardik.messageapp.domain.usecase.conversation.recyclebin

import com.hardik.messageapp.domain.repository.RecyclebinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DeleteFromRecyclebinConversationThreadUseCase @Inject constructor(private val recyclebinRepository: RecyclebinRepository) {
    //suspend operator fun invoke(threadIds: List<Long>) = recyclebinRepository.deletePermanently(threadIds = threadIds)
    suspend operator fun invoke(senders: List<String>) : Flow<Boolean> = flow {
        val isPermanentDelete = recyclebinRepository.deleteFromRecycleBinBySender(senders = senders)
        emit(isPermanentDelete) // Emits the result
    }.flowOn(Dispatchers.IO)
}