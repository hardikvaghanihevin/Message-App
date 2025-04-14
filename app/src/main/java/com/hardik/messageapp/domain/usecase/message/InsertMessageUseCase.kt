package com.hardik.messageapp.domain.usecase.message

import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import javax.inject.Inject

class InsertMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    private val TAG = BASE_TAG + InsertMessageUseCase::class.java.simpleName
    suspend operator fun invoke(message: Message) {
        // ✅ Insert SMS into the database
        messageRepository.insertMessage(message)

//        // ✅ Query the actual threadId after insertion
//        val threadId = messageRepository.getThreadId(message.sender)
//
//        // ✅ Update the message with correct threadId
//        messageRepository.updateThreadId(message.id, threadId)
    }

    suspend fun insertOrUpdateMessages(messages: List<Message>){
        //Log.i(TAG, "$TAG - insertOrUpdateMessages: ")
        messageRepository.insertOrUpdateMessages(messages)
    }
}


