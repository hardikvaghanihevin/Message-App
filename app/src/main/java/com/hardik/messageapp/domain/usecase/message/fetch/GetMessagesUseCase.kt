package com.hardik.messageapp.domain.usecase.message.fetch

import android.util.Log
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.util.AppDataSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    private val TAG = BASE_TAG + GetMessagesUseCase::class.java.simpleName
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) { // Use withContext if you just have one sequential task
            try {
                messageRepository.getMessages().collect { messageList ->
                    AppDataSingleton.updateMessages(messageList)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching messages", e)
            }
        }
    }
    //operator fun invoke() = messageRepository.getMessages()
    //operator fun invoke(messageId: Long) = messageRepository.getMessages(messageId = messageId)
    //operator fun invoke(messageIds: List<Long>) = messageRepository.getMessages(messageIds = messageIds)
    fun getMessagesByThreadId(threadId: Long) =
        messageRepository.getMessagesByThreadId(threadId = threadId)
}
