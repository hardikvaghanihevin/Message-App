package com.hardik.messageapp.domain.usecase.message

import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MessageRepository
import javax.inject.Inject

class InsertMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(message: Message) {
        // ✅ Insert SMS into the database
        messageRepository.insertMessage(message)

//        // ✅ Query the actual threadId after insertion
//        val threadId = messageRepository.getThreadId(message.sender)
//
//        // ✅ Update the message with correct threadId
//        messageRepository.updateThreadId(message.id, threadId)
    }
}


