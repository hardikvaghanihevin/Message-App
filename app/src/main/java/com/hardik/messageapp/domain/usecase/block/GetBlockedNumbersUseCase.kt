package com.hardik.messageapp.domain.usecase.block

import com.hardik.messageapp.domain.model.BlockedNumber
import com.hardik.messageapp.domain.repository.BlockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockedNumbersUseCase @Inject constructor(private val repository: BlockRepository) {
    operator fun invoke(): Flow<List<BlockedNumber>> = repository.getBlockedNumbers()
}