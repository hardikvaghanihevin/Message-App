package com.hardik.messageapp.domain.usecase

import android.util.Log
import com.google.gson.Gson
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Conversation
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MyDataRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.analyzeSender
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlin.properties.Delegates

class GetMyDataUseCase @Inject constructor(val myDataRepository: MyDataRepository) {
    private val TAG = BASE_TAG + GetMyDataUseCase::class.java.simpleName
    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time

    suspend operator fun invoke(): Flow<List<ConversationThread>> {
        return coroutineScope {
            val threadJob: Deferred<Flow<List<Conversation>>> = async(Dispatchers.IO) { myDataRepository.fetchConversations() }
            val smsJob: Deferred<Flow<Map<Long, Message>>> = async(Dispatchers.IO) { myDataRepository.fetchMessages() }
            val contactsJob: Deferred<Flow<Map<String, Contact>>> = async(Dispatchers.IO) { myDataRepository.fetchContacts() }

            val threadList: Flow<List<Conversation>> = threadJob.await()
            val smsMap: Flow<Map<Long, Message>> = smsJob.await()
            val contactsMap: Flow<Map<String, Contact>> = contactsJob.await()



            combine(threadList, smsMap, contactsMap) { threads, messages, contacts ->
                //contacts.forEach{Log.i(BASE_TAG, "fetchContacts: key:${it.key} - P:${it.value.phoneNumbers} - D:${it.value.displayName} - F:${it.value.firstName} - L:${it.value.lastName}", ) }
                
                contacts.keys.forEach { key -> Log.d(TAG, "Contact Key: $key, Name: ${contacts[key]?.displayName}") }
                messages.mapNotNull { sms -> }

                threads.mapNotNull { thread ->
                    val message = messages[thread.threadId] // Get the latest message for the thread
                    val sender = message?.sender?.trim()

                    // Skip if sender is null, empty, or blank
                    if (sender.isNullOrEmpty()) return@mapNotNull null

                    // Determine sender type (1 = Number, 2 = Name)
                    val senderType = analyzeSender(sender)

                    val displayName = if (senderType == 1) { contacts.get(sender)?.displayName } else sender
                    Log.e(TAG, "invoke: $displayName $sender", )

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
                        phoneNumber = if (senderType == 1) sender else "", // Use number if valid
                        contactName = "contactName", // Use contact name or sender name
                        displayName = displayName ?: sender // Keep it consistent with contactName
                    )
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart {
                    startTime = System.currentTimeMillis()
                }
                .onCompletion {
                    endTime = System.currentTimeMillis()
                    Log.i(TAG, "Total execution time: ${endTime - startTime}ms")
                }
        }
    }


    fun List<ConversationThread>.toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

