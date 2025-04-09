package com.hardik.messageapp.domain.usecase.conversation.fetch

import android.util.Log
import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.BlockThreadDao
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

class GetConversationUseCase @Inject constructor(
    private val recycleBinThreadDao: RecycleBinThreadDao,
    private val archivedThreadDao: ArchivedThreadDao,
    private val blockThreadDao: BlockThreadDao,
    private val conversationRepository: ConversationRepository,
    private val phoneNumberUtil: PhoneNumberUtil)
{

    private val TAG = BASE_TAG + GetConversationUseCase::class.java.simpleName
    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time

    suspend operator fun invoke(callFromWhere: String = "") {
        Log.e(TAG, "invoke: call from :$callFromWhere")

        coroutineScope {
            //val threadJob: Deferred<List<Conversation>> = async(Dispatchers.IO) { conversationRepository.fetchConversations().first() }
            //val smsJob: Deferred<Map<Long, Message>> = async(Dispatchers.IO) { conversationRepository.fetchMessages().first() }
            //val contactsJob: Deferred<Map<String, Contact>> = async(Dispatchers.IO) { conversationRepository.fetchContacts().first() }

            val threadJob = async(Dispatchers.IO) {
                val allThreads = mutableListOf<Conversation>()
                conversationRepository.fetchConversations().collect { chunk ->
                    allThreads += chunk
                }
                allThreads
            }

            val smsJob = async(Dispatchers.IO) {
                val allSms = mutableMapOf<Long, Message>()
                conversationRepository.fetchMessages().collect { chunk ->
                    allSms.putAll(chunk)
                }
                allSms
            }

            val contactsJob = async(Dispatchers.IO) {
                val allContact = mutableMapOf<String, Contact>()
                conversationRepository.fetchContacts().collect { chunk ->
                    allContact.putAll(chunk)
                }
                allContact
            }

            val recycleBinThreadIdsJob = async(Dispatchers.IO) { recycleBinThreadDao.getRecycleBinThreadIds().first() }
            val archiveThreadIdsJob = async(Dispatchers.IO) { archivedThreadDao.getArchivedThreadIds().first() }
            val blockThreadIdsJob = async(Dispatchers.IO) { blockThreadDao.getBlockThreadIds().first() }

            val threadList = threadJob.await()
            val smsMap = smsJob.await()
            val contactsMap = contactsJob.await()
            val recycleBinThreadIds = recycleBinThreadIdsJob.await()
            val archiveThreadIds = archiveThreadIdsJob.await()
            val blockThreadIds = blockThreadIdsJob.await()

            val conversationList = threadList.mapNotNull { thread ->
                val message = smsMap[thread.threadId]
                val sender = message?.sender?.trim()

                if (sender.isNullOrEmpty()) return@mapNotNull null

                val senderType = analyzeSender(sender)

                val contact = if (senderType == 1) {
                    val phoneNumberKey = sender.removeCountryCode(phoneNumberUtil)
                    val foundContact = contactsMap[phoneNumberKey]
                    if (foundContact != null) {
                        foundContact.copy(
                            contactId = foundContact.contactId,
                            normalizeNumber = foundContact.normalizeNumber,
                            photoUri = foundContact.photoUri ?: "",
                            displayName = foundContact.displayName ?: sender
                        )
                    } else {
                        Contact(
                            contactId = -1,
                            displayName = phoneNumberKey,
                            phoneNumbers = mutableListOf(phoneNumberKey),
                            photoUri = null,
                            normalizeNumber = phoneNumberKey
                        )
                    }
                } else {
                    Contact(
                        contactId = -1,
                        displayName = sender,
                        phoneNumbers = mutableListOf(""),
                        photoUri = null,
                        normalizeNumber = ""
                    )
                }

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
                    displayName = contact.displayName,
                    unSeenCount = message.unSeenCount
                )
            }

            val filteredList = conversationList.filterNot {
                it.threadId in recycleBinThreadIds || it.threadId in archiveThreadIds || it.threadId in blockThreadIds
            }

            val startTime = System.currentTimeMillis()

            launch(Dispatchers.IO) {
                AppDataSingleton.updateConversationThreads(conversationList)
            }

            launch(Dispatchers.IO) {
                val privateList = conversationList.filter { it.normalizeNumber.isNotEmpty() }
                AppDataSingleton.updateConversationThreadsPrivate(privateList)
            }

            launch(Dispatchers.IO) {
                AppDataSingleton.filterConversationThreads(filteredList)
            }

            val endTime = System.currentTimeMillis()
            Log.i(TAG, "$TAG - Total execution time Combine: ${endTime - startTime}ms")
        }
    }

    //region last fastest data getting
 /*
   suspend operator fun invoke(callFromWhere: String = "") {
        Log.e(TAG, "invoke: call from :$callFromWhere", )
        // Todo: store in AppDataSingleton object
        coroutineScope {
            val threadJob: Deferred<List<Conversation>> = async(Dispatchers.IO) { conversationRepository.fetchConversations().first() }
            val smsJob: Deferred<Map<Long, Message>> = async(Dispatchers.IO) { conversationRepository.fetchMessages().first() }
            val contactsJob: Deferred<Map<String, Contact>> = async(Dispatchers.IO) { conversationRepository.fetchContacts().first() }

            val recycleBinThreadIdsJob: Deferred<List<Long>> = async(Dispatchers.IO) { recycleBinThreadDao.getRecycleBinThreadIds().first() }
            val archiveThreadIdsJob: Deferred<List<Long>> = async(Dispatchers.IO) { archivedThreadDao.getArchivedThreadIds().first() }
            val blockThreadIdsJob: Deferred<List<Long>> = async(Dispatchers.IO) { blockThreadDao.getBlockThreadIds().first() }

            val threadList = threadJob.await()
            val smsMap = smsJob.await()
            val contactsMap = contactsJob.await()
            val recycleBinThreadIds = recycleBinThreadIdsJob.await()
            val archiveThreadIds = archiveThreadIdsJob.await()
            val blockThreadIds = blockThreadIdsJob.await()

            *//*combine(
                flowOf(threadList),
                flowOf(smsMap),
                flowOf(contactsMap),
                flowOf(recycleBinThreadIds),
                flowOf(archiveThreadIds),
                flowOf(blockThreadIds)
            ) { threads, messages, contacts, binThreadIds, archivedThreadIds, blockedThreadIds -> }*//*
            combine(
                arrayOf(
                    flowOf(threadList),
                    flowOf(smsMap),
                    flowOf(contactsMap),
                    flowOf(recycleBinThreadIds),
                    flowOf(archiveThreadIds),
                    flowOf(blockThreadIds)
                ).asList()
            ) { valuesArray ->
                val threads = valuesArray[0] as List<Conversation>
                val messages = valuesArray[1] as Map<Long, Message>
                val contacts = valuesArray[2] as Map<String, Contact>
                val binThreadIds = valuesArray[3] as List<Long>
                val archivedThreadIds = valuesArray[4] as List<Long>
                val blockedThreadIds = valuesArray[5] as List<Long>
                //Log.v(TAG, "$TAG - invoke: ${threads.size}, ${messages.size}, ${contacts.size}, ${binThreadIds.size}, ${archivedThreadIds.size}, ${blockedThreadIds.size}")

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
                val filteredList = conversationList.filterNot { it.threadId in binThreadIds || it.threadId in archivedThreadIds  || it.threadId in blockedThreadIds }


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
    }*/

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

    //endregion last fastest data getting

}

