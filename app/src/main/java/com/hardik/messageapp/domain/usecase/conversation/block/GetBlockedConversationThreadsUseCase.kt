package com.hardik.messageapp.domain.usecase.conversation.block

import com.hardik.messageapp.domain.repository.BlockRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import javax.inject.Inject

class GetBlockedConversationThreadsUseCase @Inject constructor(private val repository: BlockRepository) {
    private val TAG = BASE_TAG + GetBlockedConversationThreadsUseCase::class.java.simpleName
    operator fun invoke() = repository.getBlockedConversations()

}