package com.hardik.messageapp.data.repository

import android.content.Context
import android.provider.Telephony
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConversationThreadRepositoryImpl @Inject constructor(
    private val context: Context,
) : ConversationThreadRepository {

    //region Fetch ConversationThread (Message list)

    // Fetch all conversation threads
    override fun getConversationThreads(): Flow<List<ConversationThread>> = flow {
        emit(querySms()) // Uses default null parameters to fetch all threads
    }.flowOn(Dispatchers.IO)

    // Fetch a specific conversation thread by ID
    override fun getConversationThreads(threadId: Long): Flow<ConversationThread?> = flow {
        val threads = querySms("${Telephony.Sms.THREAD_ID} = ?", arrayOf(threadId.toString()))
        emit(threads.firstOrNull()) // Emit the first (or null if not found)
    }.flowOn(Dispatchers.IO)

    override fun getConversationThreads(threadIds: List<Long>): Flow<List<ConversationThread>> = flow {
        if (threadIds.isEmpty()) {
            emit(emptyList()) // Return an empty list if no IDs are provided
            return@flow
        }

        val selection = "${Telephony.Sms.THREAD_ID} IN (${threadIds.joinToString(",") { "?" }})"
        val selectionArgs = threadIds.map { it.toString() }.toTypedArray()

        emit(querySms(selection, selectionArgs)) // Use existing querySms function
    }.flowOn(Dispatchers.IO)

    private fun querySms(selection: String? = null, selectionArgs: Array<String>? = null): List<ConversationThread> {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)

        val threadMap = mutableMapOf<Long, ConversationThread>()

        context.contentResolver.query(uri, projection, selection, selectionArgs, "${Telephony.Sms.DATE} DESC")?.use { cursor ->
            val threadIdIndex = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
            val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(threadIdIndex)
                val sender = cursor.getString(addressIndex)
                val lastMessage = cursor.getString(bodyIndex)
                val timestamp = cursor.getLong(dateIndex)

                // Store only the latest message per thread
                if (!threadMap.containsKey(threadId)) {
                    threadMap[threadId] = ConversationThread(threadId, sender, lastMessage, timestamp)
                }
            }
        }

        return threadMap.values.toList()
    }

    //endregion

}