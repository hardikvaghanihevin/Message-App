package com.hardik.messageapp.domain.usecase.block

import com.hardik.messageapp.domain.repository.BlockRepository
import javax.inject.Inject

class UnblockNumbersUseCase @Inject constructor(private val repository: BlockRepository) {
    suspend operator fun invoke(numbers: List<String>): Boolean = repository.unblockNumbers(numbers)
}
