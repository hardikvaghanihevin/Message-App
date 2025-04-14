package com.hardik.messageapp.domain.usecase.conversation.pin

import com.hardik.messageapp.domain.repository.PinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UnpinConversationsUseCase @Inject constructor(private val repository: PinRepository ) {
    suspend operator fun invoke(threadIds: List<Long>): Flow<Boolean> = flow {
        val isUnpin = repository.unpinConversations(threadIds)
        emit(isUnpin)
    }.flowOn(Dispatchers.IO)
}
