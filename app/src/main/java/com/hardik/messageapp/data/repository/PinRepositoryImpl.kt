package com.hardik.messageapp.data.repository

import com.hardik.messageapp.data.local.dao.PinThreadDao
import com.hardik.messageapp.data.local.entity.PinThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.domain.repository.PinRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PinRepositoryImpl @Inject constructor(
    private val pinThreadDao: PinThreadDao,

    private val conversationThreadRepository: ConversationThreadRepository
) : PinRepository {



    //region Fetch PinnedConversationThread list

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPinnedConversations(): Flow<List<ConversationThread>> {
        return pinThreadDao.getPinnedConversations().flatMapLatest { threadIds ->
            if (threadIds.isEmpty()) {
                flowOf(emptyList()) // Return an empty list if no thread IDs exist
            } else {
                conversationThreadRepository.getConversationThreads(threadIds)
            }
        }
    }
    //endregion

    //region Pin and Unpin ConversationThread
    override suspend fun pinConversations(threadIds: List<Long>): Boolean {
        val pinConversations = threadIds.map { PinThreadEntity(it) }
        val success = pinThreadDao.pinConversations(pinConversations)
        return success.size == threadIds.size
    }

    override suspend fun unpinConversations(threadIds: List<Long>): Boolean {
        val success = pinThreadDao.unpinConversations(threadIds)
        return success > 0
    }
    //endregion

}
