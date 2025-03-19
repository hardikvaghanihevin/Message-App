package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.usecase.recyclebin.DeleteFromRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.GetRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.MoveToRecyclebinConversationThreadUseCase
import com.hardik.messageapp.domain.usecase.recyclebin.RemoveFromRecyclebinConversationThreadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecyclebinViewModel @Inject constructor(
    private val getRecyclebinConversationThreadUseCase: GetRecyclebinConversationThreadUseCase,
    private val moveToRecyclebinConversationThreadUseCase: MoveToRecyclebinConversationThreadUseCase,
    private val removeFromRecyclebinConversationThreadUseCase: RemoveFromRecyclebinConversationThreadUseCase,
    private val deleteFromRecyclebinConversationThreadUseCase: DeleteFromRecyclebinConversationThreadUseCase,
) : ViewModel() {

    //region Fetch deleted ConversationThread list
    private val _recyclebinConversations = MutableStateFlow<List<ConversationThread>>(emptyList())
    val recyclebinConversations = _recyclebinConversations.asStateFlow()
    fun fetchRecycleBinConversationThreads() {
        viewModelScope.launch{
            getRecyclebinConversationThreadUseCase().collect { binList ->
                _recyclebinConversations.value = binList
            }
        }
        getRecyclebinConversationThreadUseCase()
    }
    //endregion

    //region Add and Remove RecycleBin ConversationThread
    fun moveToRecyclebin(recycleBinThreads: List<RecycleBinThreadEntity>) {
        viewModelScope.launch {
            val success = moveToRecyclebinConversationThreadUseCase(recycleBinThreads = recycleBinThreads)
        }
    }

    fun removeFromRecyclebin(threadIds: List<Long>) {
        viewModelScope.launch {
            val success = removeFromRecyclebinConversationThreadUseCase(threadIds = threadIds)
        }
    }
    //endregion

    //region Delete ConversationThread Permanently
    fun deletePermanently(threadIds: List<Long>) {
        viewModelScope.launch {
            val success = deleteFromRecyclebinConversationThreadUseCase(threadIds = threadIds)
        }
    }
    //endregion

}