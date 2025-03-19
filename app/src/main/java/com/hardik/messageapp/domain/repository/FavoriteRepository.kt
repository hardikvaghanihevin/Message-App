package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import com.hardik.messageapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    //region Fetch FavoriteMessage list
    fun getFavoriteMessages(): Flow<List<Message>>
    //endregion

    //region Add and Remove ConversationThread
    suspend fun addToFavoriteMessage(favoriteMessages: List<FavoriteMessageEntity>): Boolean

    suspend fun removeFromFavoriteMessage(messageIds: List<Long>): Boolean
    //endregion
}