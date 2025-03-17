package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.SmsMessage
import com.hardik.messageapp.domain.usecase.DeleteSmsUseCase
import com.hardik.messageapp.domain.usecase.GetSmsMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val getSmsMessagesUseCase: GetSmsMessagesUseCase,
    private val deleteSmsUseCase: DeleteSmsUseCase,
) : ViewModel() {

    private val _smsMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val smsMessages: StateFlow<List<SmsMessage>> = _smsMessages.asStateFlow()

    fun fetchSmsMessages() {
        viewModelScope.launch {
            getSmsMessagesUseCase().collectLatest { messages ->
                _smsMessages.value = messages
            }
        }
    }

    fun deleteSms(smsId: Long) {
        viewModelScope.launch {
            val isDeleted = deleteSmsUseCase(smsId)
            if (isDeleted) {
                fetchSmsMessages() // Refresh SMS list after deletion
            }
        }
    }
}
