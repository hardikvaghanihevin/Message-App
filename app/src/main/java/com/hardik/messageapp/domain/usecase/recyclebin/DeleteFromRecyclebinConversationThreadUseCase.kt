package com.hardik.messageapp.domain.usecase.recyclebin

import com.hardik.messageapp.domain.repository.RecyclebinRepository
import javax.inject.Inject

class DeleteFromRecyclebinConversationThreadUseCase @Inject constructor(private val recyclebinRepository: RecyclebinRepository) {
    suspend operator fun invoke(threadIds: List<Long>) = recyclebinRepository.deletePermanently(threadIds = threadIds)
}