package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.usecase.block.BlockNumbersUseCase
import com.hardik.messageapp.domain.usecase.block.GetBlockedNumbersUseCase
import com.hardik.messageapp.domain.usecase.block.UnblockNumbersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    private val getBlockedNumbersUseCase: GetBlockedNumbersUseCase,
    private val blockNumbersUseCase: BlockNumbersUseCase,
    private val unblockNumbersUseCase: UnblockNumbersUseCase
) : ViewModel() {


    //region Fetch BlockConversationThread list

    private val _blockedNumbers = MutableStateFlow<List<BlockThreadEntity>>(emptyList())
    val blockedNumbers = _blockedNumbers.asStateFlow()
    fun fetchBlockedNumbers() {
        viewModelScope.launch {
            getBlockedNumbersUseCase().collect { blockThread ->
                _blockedNumbers.value = blockThread
            }
        }
    }
    //endregion

    //region Block and Unblock ConversationThread

    fun blockNumbers(blockThreads: List<BlockThreadEntity>) {// blockViewModel.blockNumbers(listOf("+1234567890"))
        viewModelScope.launch {
            blockNumbersUseCase(blockThreads)
                .collectLatest { isBlock ->
                    if (isBlock) fetchBlockedNumbers()
                }
        }
    }

    fun unblockNumbers(blockThreads: List<BlockThreadEntity>) {
        viewModelScope.launch {
            unblockNumbersUseCase(blockThreads)
                .collectLatest {isUnblock ->
                    if (isUnblock) fetchBlockedNumbers() // refresh data list
                }
        }
    }
    //endregion
}
