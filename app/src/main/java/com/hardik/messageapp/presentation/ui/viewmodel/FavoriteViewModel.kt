package com.hardik.messageapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.favorite.AddToFavoriteMessageUseCase
import com.hardik.messageapp.domain.usecase.favorite.GetFavoriteMessageUseCase
import com.hardik.messageapp.domain.usecase.favorite.RemoveFromFavoriteMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteMessageUseCase: GetFavoriteMessageUseCase,
    private val addToFavoriteMessageUseCase: AddToFavoriteMessageUseCase,
    private val removeFromFavoriteMessageUseCase: RemoveFromFavoriteMessageUseCase,
) : ViewModel() {

    //region Fetch FavoriteMessage list
    private val _favoriteMessages = MutableStateFlow<List<Message>>(emptyList())
    val favoriteMessages = _favoriteMessages.asStateFlow()
    fun fetchFavoriteMessages() {
        viewModelScope.launch {
            getFavoriteMessageUseCase().collect { favoriteMessages ->
                _favoriteMessages.value = favoriteMessages
            }
        }
    }
    //endregion

    //region Add and Remove ConversationThread
    fun addToFavorite(messages: List<FavoriteMessageEntity>) {
        viewModelScope.launch {
            addToFavoriteMessageUseCase(messages = messages)
        }
    }
    fun removeFromFavorite(messageIds: List<Long>) {
        viewModelScope.launch {
            removeFromFavoriteMessageUseCase(messageIds = messageIds)
        }
    }
    //endregion
}