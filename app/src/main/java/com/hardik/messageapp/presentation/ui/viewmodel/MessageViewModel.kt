package com.hardik.messageapp.presentation.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationUseCase
import com.hardik.messageapp.domain.usecase.message.InsertMessageUseCase
import com.hardik.messageapp.domain.usecase.message.delete.DeleteMessageUseCase
import com.hardik.messageapp.domain.usecase.message.fetch.GetMessagesUseCase
import com.hardik.messageapp.util.AppDataSingleton
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getConversationUseCase: GetConversationUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,

    private val deleteMessageUseCase: DeleteMessageUseCase,

    private val insertMessageUseCase: InsertMessageUseCase,
) : ViewModel() {
    private val TAG = BASE_TAG + MessageViewModel::class.java.simpleName

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init { fetchSmsMessages() }

    //region Fetch Message list
    fun fetchSmsMessages(needToUpdate: Boolean = false) {
        //viewModelScope.launch { getMessagesUseCase().collectLatest { messages -> _messages.value = messages } }
        if (needToUpdate) { viewModelScope.launch { getMessagesUseCase() } }

        viewModelScope.launch {
            AppDataSingleton.messages.collect {
                //Log.e(TAG, "$TAG - fetchSmsMessages: ${it.size}", )
                if (it.isEmpty()) { getMessagesUseCase() }
                else { _messages.emit(it) }// Update the state with the fetched messages
            }
        }
    }

    private val _messagesOfThread = MutableStateFlow<List<Message>>(emptyList())
    val messagesOfThread: StateFlow<List<Message>> = _messagesOfThread.asStateFlow()

    fun getMessagesByThreadId(threadId: Long) {
        viewModelScope.launch {
            getMessagesUseCase.getMessagesByThreadId(threadId = threadId).collectLatest { messages ->
                _messagesOfThread.value = messages
            }
        }
    }
    //endregion

    //region Delete Messages
    fun deleteSms(smsIds: List<Long>) {
        viewModelScope.launch {
            val isDeleted = deleteMessageUseCase(smsIds)
            if (isDeleted) {
                // Note :- Refresh SMS list after deletion
                refreshData()
            }
        }
    }
    //endregion

    //region Send/Insert Messages

    private val _smsSent = MutableSharedFlow<Unit>(
        replay = 1, // Ensures latest value is replayed
        extraBufferCapacity = 1 // Avoids dropping events if no collector is active
    )
    val smsSent = _smsSent.asSharedFlow()

    fun insertSms(message: Message, subscriptionId: Int ,context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MessageViewModel", "SMS permission not granted.")
            return // Stop execution if permission is not granted
        }
        viewModelScope.launch(Dispatchers.IO) {
//            insertMessageUseCase(message)
//            _smsSent.emit(Unit)  // Notify UI about new SMS
            try {
                val smsManager: SmsManager = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        context.getSystemService(SmsManager::class.java)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                    }
                    else -> {
                        SmsManager.getDefault()
                    }
                }


                smsManager.sendTextMessage(message.sender, null, message.messageBody, null, null)

                // ✅ Insert sent message into the database
                insertMessageUseCase(message.copy(type = 2)) // Type 2: Sent Message

                _smsSent.emit(Unit)  // Notify UI that the message was sent
                //Log.d(TAG,"$TAG - SMS sent successfully to ${message.sender}")
            } catch (e: Exception) {
                //Log.e(TAG, "$TAG - Failed to send SMS", e)
            }
        }
    }

    fun insertOrUpdateMessages(messages: List<Message>) {
        //Log.e(TAG, "insertOrUpdateMessages: count:${messages.size}", )
        viewModelScope.launch {
            val j = launch (Dispatchers.IO) {
                //Log.i(TAG, "$TAG - insertOrUpdateMessages: ")
                insertMessageUseCase.insertOrUpdateMessages(messages)
            }
            j.join()
            // Once the database operation is complete, trigger the next action
            refreshData() // ✅ Example: Fetch updated messages
        }
    }
    //endregion



    private fun refreshData() {
        Log.e(TAG, "refreshData: ", )
        viewModelScope.launch {
            getConversationUseCase(TAG)
        }
    }

}
