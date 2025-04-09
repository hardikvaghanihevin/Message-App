package com.hardik.messageapp.domain.usecase.block

import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UnblockConversationThreadsUseCase @Inject constructor(private val repository: BlockRepository) {
    suspend operator fun invoke(blockThreads: List<BlockThreadEntity>): Flow<Boolean> = flow {
        val isUnblock = repository.unblockConversations(blockThreads)
        emit(isUnblock) // Emits the result
    }.flowOn(Dispatchers.IO)
}
