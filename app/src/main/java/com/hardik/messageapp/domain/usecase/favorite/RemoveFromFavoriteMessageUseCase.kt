package com.hardik.messageapp.domain.usecase.favorite

import com.hardik.messageapp.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFromFavoriteMessageUseCase @Inject constructor(private val favoriteRepository: FavoriteRepository) {
    suspend operator fun invoke(messageIds: List<Long>) = favoriteRepository.removeFromFavoriteMessage(messageIds = messageIds)
}