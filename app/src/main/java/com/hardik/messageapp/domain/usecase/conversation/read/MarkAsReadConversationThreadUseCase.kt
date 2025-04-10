package com.hardik.messageapp.domain.usecase.conversation.read

import com.hardik.messageapp.domain.repository.ReadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MarkAsReadConversationThreadUseCase @Inject constructor(private val readRepository: ReadRepository) {

    operator fun invoke(threadIds: List<Long>) : Flow<Boolean> = flow {
        val isRead: Boolean = readRepository.markAsReadConversationThreads(threadIds)
        emit(isRead) // Emits the result
    }.flowOn(Dispatchers.IO) // Run on background thread
}
