package com.hardik.messageapp.data.repository

import com.hardik.messageapp.data.local.dao.FavoriteMessageDao
import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.FavoriteRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteMessageDao: FavoriteMessageDao,

    private val messageRepository: MessageRepository,
) : FavoriteRepository
{
    //region Fetch FavoriteMessage list
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavoriteMessages(): Flow<List<Message>> {
        return favoriteMessageDao.getFavoriteMessages().flatMapLatest { favorite ->
            if (favorite.isEmpty()){
                flowOf(emptyList())// Return an empty list if no favorite exist
            } else {
                val messageIds = favorite.map { it.id }
                messageRepository.getMessages(messageIds)
            }
        }
    }
    //endregion

    //region Add and Remove ConversationThread
    override suspend fun addToFavoriteMessage(favoriteMessages: List<FavoriteMessageEntity>): Boolean {
        val success = favoriteMessageDao.upsertFavoriteMessages(recycleBin = favoriteMessages)
        return success.size == favoriteMessages.size
    }

    override suspend fun removeFromFavoriteMessage(messageIds: List<Long>): Boolean {
        val success = favoriteMessageDao.unfavoriteMessages(messageIds)
        return success > 0
    }
    //endregion
}