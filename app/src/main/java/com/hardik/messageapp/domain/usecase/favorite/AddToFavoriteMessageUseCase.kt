package com.hardik.messageapp.domain.usecase.favorite

import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import com.hardik.messageapp.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddToFavoriteMessageUseCase @Inject constructor(private  val favoriteRepository: FavoriteRepository) {
    suspend operator fun invoke(messages: List<FavoriteMessageEntity>) = favoriteRepository.addToFavoriteMessage(favoriteMessages = messages)
}