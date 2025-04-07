package com.hardik.messageapp.domain.usecase.block

import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BlockNumbersUseCase @Inject constructor(private val repository: BlockRepository) {
    suspend operator fun invoke(blockThreads: List<BlockThreadEntity>): Flow<Boolean> = flow {
        //repository.blockNumbers(numbers)
        val isBlocked = repository.blockNumbers(blockThreads)
        emit(isBlocked) // Emits the result
    }.flowOn(Dispatchers.IO)
}
