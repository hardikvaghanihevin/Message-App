package com.hardik.messageapp.domain.usecase

import android.util.Log
import com.google.gson.Gson
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.MyDataRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.analyzeSender
import com.hardik.messageapp.helper.removeCountryCode
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlin.properties.Delegates

class GetMyDataUseCase @Inject constructor(private val myDataRepository: MyDataRepository, private val phoneNumberUtil: PhoneNumberUtil) {
    private val TAG = BASE_TAG + GetMyDataUseCase::class.java.simpleName
    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time

    suspend operator fun invoke(): Flow<List<ConversationThread>> = flow {
        coroutineScope {
            val threadJob = async(Dispatchers.IO) { myDataRepository.fetchConversations().first() }
            val smsJob = async(Dispatchers.IO) { myDataRepository.fetchMessages().first() }
            val contactsJob = async(Dispatchers.IO) { myDataRepository.fetchContacts().first() }

            val threadList = threadJob.await()
            val smsMap = smsJob.await()
            val contactsMap = contactsJob.await()

            combine(
                flowOf(threadList),
                flowOf(smsMap),
                flowOf(contactsMap)
            ) { threads, messages, contacts ->
                Log.e(TAG, "invoke: ${threads.size}, ${messages.size}")

                threads.mapNotNull { thread ->
                    val message = messages[thread.threadId] // Get the latest message for the thread
                    val sender = message?.sender?.trim()

                    // Skip if sender is null, empty, or blank
                    if (sender.isNullOrEmpty()) return@mapNotNull null

                    val senderType = analyzeSender(sender)
                    val displayName = if (senderType == 1)
                    {
                        val phoneNumberKey = sender.removeCountryCode(phoneNumberUtil)
                        contacts[phoneNumberKey]?.displayName
                    } else sender

                    //Log.e(TAG, "invoke: $displayName $sender")

                    ConversationThread(
                        threadId = thread.threadId,
                        id = message.id ?: 0L,
                        sender = sender,
                        messageBody = message.messageBody.orEmpty(),
                        creator = message.creator,
                        timestamp = message.timestamp ?: 0L,
                        dateSent = message.dateSent ?: 0L,
                        errorCode = message.errorCode ?: 0,
                        locked = message.locked ?: 0,
                        person = message.person,
                        protocol = message.protocol,
                        read = thread.read,
                        replyPath = message.replyPath ?: false,
                        seen = message.seen ?: false,
                        serviceCenter = message.serviceCenter,
                        status = message.status ?: 0,
                        subject = message.subject,
                        subscriptionId = message.subscriptionId ?: 0,
                        type = message.type ?: 0,
                        isArchived = message.isArchived ?: false,
                        snippet = thread.snippet,
                        date = thread.date,
                        recipientIds = thread.recipientIds,
                        phoneNumber = if (senderType == 1) sender else "",
                        contactName = displayName ?: sender,
                        displayName = displayName ?: sender
                    )
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { startTime = System.currentTimeMillis() }
                .onCompletion {
                    endTime = System.currentTimeMillis()
                    Log.i(TAG, "Total execution time: ${endTime - startTime}ms")
                }
                .collect { emit(it) } // Emit the final result inside flow
        }
    }



    fun List<ConversationThread>.toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

