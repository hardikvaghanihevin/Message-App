package com.hardik.messageapp.domain.usecase.recyclebin

import com.hardik.messageapp.domain.repository.RecyclebinRepository
import javax.inject.Inject

class GetRecyclebinConversationThreadUseCase @Inject constructor(private val recyclebinRepository: RecyclebinRepository) {
    operator fun invoke() = recyclebinRepository.getRecycleBinConversationThreads()
}