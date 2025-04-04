package com.hardik.messageapp.domain.usecase.conversation.fetch

import android.util.Log
import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Conversation
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.analyzeSender
import com.hardik.messageapp.helper.removeCountryCode
import com.hardik.messageapp.presentation.util.AppDataSingleton
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

class GetConversationUseCase @Inject constructor(
    private val recycleBinThreadDao: RecycleBinThreadDao,
    private val archivedThreadDao: ArchivedThreadDao,
    private val conversationRepository: ConversationRepository,
    private val phoneNumberUtil: PhoneNumberUtil)
{

    private val TAG = BASE_TAG + GetConversationUseCase::class.java.simpleName
    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time

    suspend operator fun invoke() {
        // Todo: store in AppDataSingleton object
        coroutineScope {
            val threadJob: Deferred<List<Conversation>> = async(Dispatchers.IO) { conversationRepository.fetchConversations().first() }
            val smsJob: Deferred<Map<Long, Message>> = async(Dispatchers.IO) { conversationRepository.fetchMessages().first() }
            val contactsJob: Deferred<Map<String, Contact>> = async(Dispatchers.IO) { conversationRepository.fetchContacts().first() }

            val recycleBinThreadIdsJob: Deferred<List<Long>> = async(Dispatchers.IO) { recycleBinThreadDao.getRecycleBinThreadIds().first() }
            val archiveThreadIdsJob: Deferred<List<Long>> = async(Dispatchers.IO) { archivedThreadDao.getArchivedThreadIds().first() }

            val threadList = threadJob.await()
            val smsMap = smsJob.await()
            val contactsMap = contactsJob.await()
            val recycleBinThreadIds = recycleBinThreadIdsJob.await()
            val archiveThreadIds = archiveThreadIdsJob.await()


            combine(
                flowOf(threadList),
                flowOf(smsMap),
                flowOf(contactsMap),
                flowOf(recycleBinThreadIds),
                flowOf(archiveThreadIds)
            ) { threads, messages, contacts, binThreadIds, archivedThreadIds ->
                Log.e(TAG, "$TAG - invoke: ${threads.size}, ${messages.size}")

                // ✅ Generate conversation list
                val conversationList = threads.mapNotNull { thread ->
                    val message = messages[thread.threadId] // Get the latest message for the thread
                    val sender = message?.sender?.trim()

                    // Skip if sender is null, empty, or blank
                    if (sender.isNullOrEmpty()) return@mapNotNull null

                    val senderType = analyzeSender(sender)

                    val contact: Contact = if (senderType == 1) { // numbers
                        val phoneNumberKey: String = sender.removeCountryCode(phoneNumberUtil)

                        val foundContact: Contact? = contacts[phoneNumberKey]

                        if (foundContact != null) {
                            val contactId: Int = foundContact.contactId
                            val normalizeNumber: String = foundContact.normalizeNumber
                            val photoUri: String = foundContact.photoUri ?: ""
                            val displayName: String = foundContact.displayName ?: sender

                            foundContact.copy(contactId = contactId, normalizeNumber = normalizeNumber, photoUri = photoUri, displayName = displayName)
                        } else {
                            // Provide a default Contact object when not found
                            Contact(
                                contactId = -1, // Or generate a unique ID
                                displayName = phoneNumberKey,
                                phoneNumbers = mutableListOf(phoneNumberKey), // Or an empty list
                                photoUri = null,
                                normalizeNumber = phoneNumberKey
                            )
                        }
                    } else {
                        // Provide a default Contact object when senderType != 1
                        Contact(
                            contactId = -1, // Or generate a unique ID
                            displayName = sender,
                            phoneNumbers = mutableListOf(""), // Or an empty list
                            photoUri = null,
                            normalizeNumber = ""
                        )
                    }
                    

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
                        contactId = contact.contactId,
                        normalizeNumber = contact.normalizeNumber,
                        photoUri = contact.photoUri ?: "",
                        displayName = contact.displayName ,
                        unSeenCount = message.unSeenCount,
                    )
                }

                // ✅ Filter conversations (Remove archived & recycle bin threads)
                val filteredList = conversationList.filterNot { it.threadId in binThreadIds || it.threadId in archivedThreadIds }


                Pair(conversationList , filteredList)
            }
                .flowOn(Dispatchers.IO)
                .onStart { startTime = System.currentTimeMillis() }
                .onCompletion {
                    endTime = System.currentTimeMillis()
                    Log.i(TAG, "$TAG - Total execution time Combine: ${endTime - startTime}ms")
                }
                    //LogUtil.d(TAG, "PrivateList: ${privateList.toJson()}")
                .collect { (conversationList, filteredList) ->
                    launch (Dispatchers.IO) { // Launch in IO dispatcher for background work
                        AppDataSingleton.updateConversationThreads(conversationList) }

                    launch (Dispatchers.IO) { // Launch in IO dispatcher for background work
                        val privateList: List<ConversationThread> = conversationList.filter{ conversationThread -> conversationThread.normalizeNumber.isNotEmpty() }
                        AppDataSingleton.updateConversationThreadsPrivate(privateList) }

                    launch (Dispatchers.IO) { // Launch in IO dispatcher for background work
                        AppDataSingleton.filterConversationThreads(filteredList) }


                } // Emit the final result inside flow

        }
    }

    /*suspend operator fun invoke(): Flow<List<ConversationThread>> = flow {
        // Todo: store in ViewModel
        coroutineScope {
            val threadJob = async(Dispatchers.IO) { conversationRepository.fetchConversations().first() }
            val smsJob = async(Dispatchers.IO) { conversationRepository.fetchMessages().first() }
            val contactsJob = async(Dispatchers.IO) { conversationRepository.fetchContacts().first() }

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

                    val displayName = if (senderType == 1) {
                        val phoneNumberKey = sender.removeCountryCode(phoneNumberUtil)
                        contacts[phoneNumberKey]?.displayName } else sender

                    val photoUri = if (senderType == 1) {
                        val phoneNumberKey = sender.removeCountryCode(phoneNumberUtil)
                        contacts[phoneNumberKey]?.photoUri } else ""

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
                        photoUri = photoUri ?: "",
                        displayName = displayName ?: sender,
                        unSeenCount = message.usSeenCount,
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
    }*/
}

