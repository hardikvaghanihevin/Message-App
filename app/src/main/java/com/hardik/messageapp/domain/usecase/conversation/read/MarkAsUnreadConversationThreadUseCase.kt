package com.hardik.messageapp.domain.usecase.conversation.read

import com.hardik.messageapp.domain.repository.ReadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MarkAsUnreadConversationThreadUseCase @Inject constructor(private val readRepository: ReadRepository) {

    operator fun invoke(threadIds: List<Long>) : Flow<Boolean> = flow {
        val isUnread: Boolean = readRepository.markAsUnreadConversationThreads(threadIds)
        emit(isUnread) // Emits the result
    }.flowOn(Dispatchers.IO) // Run on background thread
}