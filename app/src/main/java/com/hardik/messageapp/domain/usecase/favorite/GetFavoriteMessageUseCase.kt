package com.hardik.messageapp.domain.usecase.favorite

import com.hardik.messageapp.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoriteMessageUseCase @Inject constructor(private val favoriteRepository: FavoriteRepository) {
    operator fun invoke() = favoriteRepository.getFavoriteMessages()
}