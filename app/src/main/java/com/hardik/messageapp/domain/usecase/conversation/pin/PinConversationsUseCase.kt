package com.hardik.messageapp.domain.usecase.conversation.pin

import com.hardik.messageapp.domain.repository.PinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PinConversationsUseCase @Inject constructor(private val repository: PinRepository ) {
    suspend operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        val isPinned = repository.pinConversations(threadIds)
        emit(isPinned)
    }.flowOn(Dispatchers.IO)
}
