package com.hardik.messageapp.domain.usecase.pin

import com.hardik.messageapp.domain.repository.PinRepository
import javax.inject.Inject

class UnpinConversationUseCase @Inject constructor( private val repository: PinRepository ) {
    suspend operator fun invoke(threadIds: List<Long>): Boolean = repository.unpinConversations(threadIds)
}
