package com.hardik.messageapp.domain.usecase.conversation.delete

import com.hardik.messageapp.domain.repository.DeleteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DeleteConversationThreadUseCase @Inject constructor(private val deleteRepository: DeleteRepository) {
    //suspend operator fun invoke(threadIds: List<Long>): Boolean = deleteRepository.deleteConversationThreads(threadIds)
    operator fun invoke(threadIds: List<Long>) : Flow<Boolean> = flow {
        val isDeleted: Boolean = deleteRepository.deleteConversationThreads(threadIds)
        emit(isDeleted) // Emits the result
    }.flowOn(Dispatchers.IO) // Run on background thread
}

