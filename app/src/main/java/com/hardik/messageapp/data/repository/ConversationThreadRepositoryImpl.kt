package com.hardik.messageapp.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConversationThreadRepositoryImpl @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
) : ConversationThreadRepository {
    private val TAG = BASE_TAG + ConversationThreadRepositoryImpl::class.java.simpleName

    override fun getConversationThreads(): Flow<List<ConversationThread>> = flow {
        emit(queryThreads())
    }.flowOn(Dispatchers.IO)

    override fun getConversationThreads(threadId: Long): Flow<ConversationThread?> = flow {
        val threads = queryThreads("${Telephony.Threads._ID} = ?", arrayOf(threadId.toString()))
        emit(threads.firstOrNull())
    }.flowOn(Dispatchers.IO)

    override fun getConversationThreads(threadIds: List<Long>): Flow<List<ConversationThread>> = flow {
        if (threadIds.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val selection = "${Telephony.Threads._ID} IN (${threadIds.joinToString(",") { "?" }})"
        val selectionArgs = threadIds.map { it.toString() }.toTypedArray()

        emit(queryThreads(selection, selectionArgs))
    }.flowOn(Dispatchers.IO)

    private suspend fun queryThreads(selection: String? = null, selectionArgs: Array<String>? = null): List<ConversationThread> {
        val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true") // Use simple query for performance
        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS,
        )

        val threadList = mutableListOf<ConversationThread>()

        context.contentResolver.query(uri, projection, selection, selectionArgs, "${Telephony.Threads.DATE} DESC")?.use { cursor ->
            val threadIdIndex = cursor.getColumnIndex(Telephony.Threads._ID)
            val snippetIndex = cursor.getColumnIndex(Telephony.Threads.SNIPPET)
            val dateIndex = cursor.getColumnIndex(Telephony.Threads.DATE)
            val readIndex = cursor.getColumnIndex(Telephony.Threads.READ)
            val recipientIdsIndex = cursor.getColumnIndex(Telephony.Threads.RECIPIENT_IDS)

            if (threadIdIndex == -1 || snippetIndex == -1 || dateIndex == -1 || readIndex == -1 || recipientIdsIndex == -1) {
                Log.e(TAG, "Column index not found!")
                return emptyList()
            }

            while (cursor.moveToNext()) {
                val threadId = cursor.getLongOrNull(threadIdIndex) ?: continue // If null, skip this iteration
                val lastMessage = cursor.getStringOrNull(snippetIndex) ?: "" // Default to an empty string if null
                val timestamp = cursor.getLongOrNull(dateIndex) ?: 0L // Default to 0 if null
                val recipientIds = cursor.getStringOrNull(recipientIdsIndex) ?: "Unknown" // Default to "Unknown" if null
                val read = cursor.getIntOrNull(readIndex) == 1 // Convert to Boolean safely

                // Blocking the coroutine to fetch contact details before adding to list
                //val (contactName, phoneNumber) = runBlocking { contactRepository.getPhoneNumberAndContactNameByRecipientId(context, recipientIds).first() }
                //threadList.add(ConversationThread(threadId = threadId, recipientIds = recipientIds, snippet = lastMessage, date = timestamp, read = read, phoneNumber = phoneNumber ?: "Unknown", contactName = contactName ?: "Unknown",))

                val contactFlow = contactRepository.getPhoneNumberAndContactNameByRecipientId(context, recipientIds)
                contactFlow.collect { (contactName, phoneNumber) ->
                    threadList.add(
                        ConversationThread(
                            threadId = threadId,
                            recipientIds = recipientIds,
                            snippet = lastMessage,
                            date = timestamp,
                            read = read,
                            phoneNumber = phoneNumber ?: "Unknown",
                            contactName = contactName ?: "Unknown",
                            displayName = contactName ?: phoneNumber ?: "Unknown"
                        )
                    )
                }
            }
        }

        return threadList
    }

    private fun getPhoneNumberFromRecipientId(recipientIds: String): String {
        val uri = Uri.parse("content://mms-sms/canonical-addresses")
        val projection = arrayOf("_id", "address")
        val formattedIds = recipientIds.split(" ").joinToString(",") { it.trim() }
        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, "_id IN ($formattedIds)", null, null
        )
        val phoneNumbers = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                phoneNumbers.add(it.getString(it.getColumnIndexOrThrow("address")))
            }
        }
        return phoneNumbers.joinToString(", ")
    }
}