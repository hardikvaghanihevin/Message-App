package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.BlockedNumber
import com.hardik.messageapp.domain.usecase.block.BlockNumbersUseCase
import com.hardik.messageapp.domain.usecase.block.GetBlockedNumbersUseCase
import com.hardik.messageapp.domain.usecase.block.UnblockNumbersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    private val getBlockedNumbersUseCase: GetBlockedNumbersUseCase,
    private val blockNumbersUseCase: BlockNumbersUseCase,
    private val unblockNumbersUseCase: UnblockNumbersUseCase
) : ViewModel() {


    //region Fetch BlockConversationThread list

    private val _blockedNumbers = MutableStateFlow<List<BlockedNumber>>(emptyList())
    val blockedNumbers = _blockedNumbers.asStateFlow()
    fun fetchBlockedNumbers() {
        viewModelScope.launch {
            getBlockedNumbersUseCase().collect { numbers ->
                _blockedNumbers.value = numbers
            }
        }
    }
    //endregion

    //region Block and Unblock ConversationThread

    fun blockNumbers(numbers: List<String>) {// blockViewModel.blockNumbers(listOf("+1234567890"))
        viewModelScope.launch {
            val success = blockNumbersUseCase(numbers)
            if (success) fetchBlockedNumbers()
        }
    }

    fun unblockNumbers(numbers: List<String>) {
        viewModelScope.launch {
            val success = unblockNumbersUseCase(numbers)
            if (success) fetchBlockedNumbers()
        }
    }
    //endregion
}
