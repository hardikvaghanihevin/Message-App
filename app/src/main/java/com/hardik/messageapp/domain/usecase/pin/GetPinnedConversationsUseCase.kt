package com.hardik.messageapp.domain.usecase.pin

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.PinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPinnedConversationsUseCase @Inject constructor(private val repository: PinRepository) {
    operator fun invoke(): Flow<List<ConversationThread>> = repository.getPinnedConversations()
}
