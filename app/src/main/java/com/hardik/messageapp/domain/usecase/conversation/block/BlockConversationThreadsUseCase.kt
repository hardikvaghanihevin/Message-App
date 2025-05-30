package com.hardik.messageapp.domain.usecase.conversation.block

import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BlockConversationThreadsUseCase @Inject constructor(private val repository: BlockRepository) {
    suspend operator fun invoke(blockThreads: List<BlockThreadEntity>): Flow<Boolean> = flow {
        val isBlocked = repository.blockConversations(blockThreads) //do block
        emit(isBlocked) // Emits the result
    }.flowOn(Dispatchers.IO)
}
