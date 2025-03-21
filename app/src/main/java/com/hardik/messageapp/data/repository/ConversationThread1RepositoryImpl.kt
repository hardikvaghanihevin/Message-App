package com.hardik.messageapp.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConversationThread1RepositoryImpl @Inject constructor(
    private val context: Context,
) : ConversationThreadRepository {
    private val TAG = BASE_TAG + ConversationThread1RepositoryImpl::class.java.simpleName

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

            if (threadIdIndex == -1 || addressIndex == -1 || bodyIndex == -1 || dateIndex == -1) {
                Log.e("querySms", "Column index not found!")
                return emptyList()
            }

            while (cursor.moveToNext()) {
                val threadId = cursor.getLongOrNull(threadIdIndex) ?: continue
                val sender = cursor.getStringOrNull(addressIndex) ?: "Unknown"
                val lastMessage = cursor.getStringOrNull(bodyIndex) ?: ""
                val timestamp = cursor.getLongOrNull(dateIndex) ?: 0L

                // Store only the latest message per thread
                if (!threadMap.containsKey(threadId)) {
                    //threadMap[threadId] = ConversationThread(threadId, sender, lastMessage, timestamp)
                }
            }
        }

        return threadMap.values.toList()
    }

    // Extension function for safe cursor access
    private fun Cursor.getStringOrNull(columnIndex: Int): String? =
        if (columnIndex != -1) getString(columnIndex) else null

    private fun Cursor.getLongOrNull(columnIndex: Int): Long? =
        if (columnIndex != -1) getLong(columnIndex) else null
    //endregion

}