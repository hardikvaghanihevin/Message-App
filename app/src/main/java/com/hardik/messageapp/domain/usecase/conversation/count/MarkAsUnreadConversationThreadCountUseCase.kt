package com.hardik.messageapp.domain.usecase.conversation.count

import com.hardik.messageapp.domain.repository.ReadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MarkAsUnreadConversationThreadCountUseCase @Inject constructor(
    private val readRepository: ReadRepository
) {
    operator fun invoke(): Flow<Map<Long, Long>> = flow {
        val result = mutableMapOf<Long, Long>()
        readRepository.markAsUnreadConversationCountThreads().collect { chunk ->
            result.putAll(chunk)
        }
        emit(result.toMap()) // emit final result
    }.flowOn(Dispatchers.IO)
}
