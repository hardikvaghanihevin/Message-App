package com.hardik.messageapp.domain.usecase.conversation.recyclebin

import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RemoveFromRecyclebinConversationThreadUseCase @Inject constructor(
    private val recyclebinRepository: RecyclebinRepository,
    private val messageRepository: MessageRepository,

    private val contactRepository: ContactRepository,
    private val phoneNumberUtil: PhoneNumberUtil,
) {
    private val TAG = BASE_TAG + RemoveFromRecyclebinConversationThreadUseCase::class.java.simpleName
    suspend operator fun invoke(senders: List<String>): Flow<Boolean> = flow {
        //todo: before restore(delete from table) insert all message in cursor (note: base on sender insert in same threadId's or if not exist then create and insert in)
        val isRestored: Boolean = recyclebinRepository.restoreConversationThreads(senders = senders)// delete all threadId & conversation from this table
        emit(isRestored) // Emits the result
    }.flowOn(Dispatchers.IO)
}