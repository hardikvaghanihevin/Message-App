package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.util.Log
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.model.Message.Companion.listFromJson
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.analyzeSender
import com.hardik.messageapp.util.getOptimalChunkSize
import com.hardik.messageapp.util.removeCountryCode
import com.hardik.messageapp.util.resolveThreadId
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecyclebinRepositoryImpl @Inject constructor(
    private val context: Context,
    private val recycleBinThreadDao: RecycleBinThreadDao,// for soft deletes list
    private val blockThreadDao: BlockThreadDao,

    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val phoneNumberUtil: PhoneNumberUtil,
) : RecyclebinRepository {
    private val TAG = BASE_TAG + RecyclebinRepositoryImpl::class.java.simpleName

    //region Fetch deleted ConversationThread list
    /*override fun getRecycleBinConversationThreads(): Flow<List<ConversationThread>> = flow {
        //val systemSmsFlow: Flow<List<ConversationThread>> = conversationThreadRepository.getConversationThreads() // Get all SMS messages
        val systemSmsFlow: Flow<List<ConversationThread>> = AppDataSingleton.conversationThreads // Get all SMS messages
        val recyclebinIdsFlow: Flow<List<Long>> = recycleBinThreadDao.getRecycleBinThreadIds() // Get recycle bin IDs
        //val blockIdsFlow: Flow<List<Long>> = blockThreadDao.getBlockThreadIds() // Get recycle bin IDs

        combine(systemSmsFlow, recyclebinIdsFlow) { smsList, recyclebinIds ->
            //smsList.filter { it.threadId in recyclebinIds && it.threadId in blockIds} // Filter only recyclebin/block messages
            smsList.filter { it.threadId in recyclebinIds} // Filter only recyclebin/block messages
        }.collect { emit(it) }
    }.flowOn(Dispatchers.IO)*/
    override fun getRecycleBinConversationThreads(): Flow<List<ConversationThread>> = channelFlow {
        recycleBinThreadDao.getRecycleBinData().collectLatest { recycleBinThreadEntity ->

//            val duplicateSenders = recycleBinThreadEntity
//                .groupingBy { it.sender }
//                .eachCount()
//                .filter { it.value > 1 }
//                .keys
//
//            val threadIdsSenders: Map<Long, String> = recycleBinThreadEntity
//                .filter { it.sender in duplicateSenders }
//                .associate { resolveThreadId(context, it.sender) to it.sender }
//            Log.i(TAG, "getRecycleBinConversationThreads: $threadIdsSenders")

            val conversationList: MutableList<ConversationThread> = mutableListOf()

            recycleBinThreadEntity.forEachIndexed { index, item ->
                val sender = item.sender
                //val threadId = threadIdsSenders.entries.find { it.value == sender }?.key ?: 0L

                val message: Message = listFromJson(item.messageJson).last()

                val senderType = analyzeSender(sender)
                val contact: Contact = if (senderType == 1) {
                    withContext(Dispatchers.IO) {
                        contactRepository.getContactByNumber(sender) ?:
                        Contact(contactId = -1, displayName = sender, phoneNumbers = mutableListOf(""), photoUri = null, normalizeNumber = "")
                    }
                } else {
                    Contact(contactId = -1, displayName = sender, phoneNumbers = mutableListOf(""), photoUri = null, normalizeNumber = "")
                }

                val thread = ConversationThread(
                    threadId = 0,
                    id = message.id,
                    sender = sender,
                    messageBody = message.messageBody,
                    creator = message.creator,
                    timestamp = message.timestamp,
                    dateSent = message.dateSent,
                    errorCode = message.errorCode,
                    locked = message.locked,
                    person = message.person,
                    protocol = message.protocol,
                    read = message.read,
                    replyPath = message.replyPath,
                    seen = message.seen,
                    serviceCenter = message.serviceCenter,
                    status = message.status,
                    subject = message.subject,
                    subscriptionId = message.subscriptionId,
                    type = message.type,
                    isArchived = message.isArchived,
                    snippet = message.messageBody,
                    date = message.timestamp,
                    recipientIds = "",
                    contactId = contact.contactId,
                    normalizeNumber = sender.removeCountryCode(phoneNumberUtil),
                    photoUri = contact.photoUri.orEmpty(),
                    displayName = contact.displayName,
                    unSeenCount = 0L,
                    isPin = false
                )

                conversationList.add(thread)
            }

            // ✅ Emit safely in channelFlow
            send(conversationList)
        }
    }.flowOn(Dispatchers.IO)



    //endregion Fetch deleted ConversationThread list

    //region Add and Remove RecycleBin ConversationThread
    override suspend fun moveToRecycleBinConversationThread(recycleBinThreadEntities: List<RecycleBinThreadEntity>): Boolean {
        val count = recycleBinThreadDao.moveToRecycleBinThread(recycleBinThreadEntities)
        return count.size == recycleBinThreadEntities.size
    }

    /*override suspend fun restoreConversationThreads(senders: List<String>): Boolean {
        //todo: before restore(delete from table) insert all message in cursor (note: base on sender insert in same threadId's or if not exist then create and insert in)
        recycleBinThreadDao.getRecycleBinDataBySenders(senders).collectLatest { recycleBinThreadEntity ->
//            recycleBinThreadEntity.forEachIndexed { index, item ->
//                val sender = item.sender
//                val message = listFromJson(item.messageJson)
//                Log.e(TAG, "restoreConversationThreads: $sender | $message", )
//            }

            val groupedMessages: Map<String, List<Message>> = recycleBinThreadEntity
                .groupBy { it.sender } // group by sender
                .mapValues { entry ->
                    entry.value.flatMap { listFromJson(it.messageJson) } // flatten messages
                }
            //val totalMessages = groupedMessages.values.sumOf { it.size }
            //Log.i(TAG, "restoreConversationThreads: ${groupedMessages.keys.size} - ${groupedMessages.values.size} - $totalMessages", )
            groupedMessages.forEach{ (sender, messages) ->
                val threadId = resolveThreadId(context, sender)
                val restoreMessageList: List<Message> = messages.map { it.copy(threadId = threadId, id = 0L) }
                messageRepository.insertOrUpdateMessages(restoreMessageList)
            }
        }
        val count = recycleBinThreadDao.restoreFromRecycleBinThread(emptyList())
        return count > 0 // Return true if restoration was successful
    }*/

    override suspend fun restoreConversationThreads(senders: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch once
            val recycleBinData = recycleBinThreadDao.getRecycleBinDataBySenders(senders)
            if (recycleBinData.isEmpty()) return@withContext false

            // 2. Group & flatten messages
            val groupedMessages: Map<String, List<Message>> = recycleBinData
                .groupBy { it.sender }
                .mapValues { entry -> entry.value.flatMap { listFromJson(it.messageJson) } }

            val allMessagesToInsert = mutableListOf<Message>()
            groupedMessages.forEach { (sender, messages) ->
                val threadId = resolveThreadId(context, sender)
                messages.mapTo(allMessagesToInsert) {
                    it.copy(threadId = threadId, id = 0L)
                }
            }

            // 3. Chunked batch insert
            val chunkSize = getOptimalChunkSize(allMessagesToInsert.size)
            allMessagesToInsert.chunked(chunkSize).forEach { chunk ->
                messageRepository.insertOrUpdateMessages(chunk) // Should be using Room DAO @Insert
            }

            // 4. Delete only after successful insert
            val count = recycleBinThreadDao.restoreFromRecycleBinThread(senders)

            return@withContext count > 0
        } catch (e: Exception) {
            Log.e(TAG, "restoreConversationThreads error: ${e.localizedMessage}", e)
            return@withContext false
        }
    }

    //endregion Add and Remove RecycleBin ConversationThread

    //region Block deleted ConversationThread
    override suspend fun blockNumbers(numbers: List<String>): Boolean {
        return try {
            for (number in numbers) {
                val values = ContentValues().apply {
                    put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                }
                context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //endregion

    //region Delete ConversationThread Permanently
    override suspend fun deletePermanently(threadIds: List<Long>): Boolean = coroutineScope {
        if (threadIds.isEmpty()) return@coroutineScope false

        try {
            val deleteSystemJob = async(Dispatchers.IO) {
                val uri = Telephony.Sms.CONTENT_URI
                val selection = "${Telephony.Sms.THREAD_ID} IN (${threadIds.joinToString(",")})"
                val deletedRows = context.contentResolver.delete(uri, selection, null)
                deletedRows > 0
            }

            //val deleteFromDbJob = async(Dispatchers.IO) {
                //recycleBinThreadDao.deleteFromRecycleBinThread(threadIds) // You should have a delete method
                //true // Assume successful if no exception
            //}

            val systemResult = deleteSystemJob.await()
            //val dbResult = deleteFromDbJob.await()

            systemResult// && dbResult// Return true if deletion was successful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //endregion Delete ConversationThread Permanently

}