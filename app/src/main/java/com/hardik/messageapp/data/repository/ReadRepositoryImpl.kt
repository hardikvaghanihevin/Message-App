package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ReadRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.util.AppDataSingleton
import com.hardik.messageapp.presentation.util.getOptimalChunkSize
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    //private val messageRepository: MessageRepository,
) : ReadRepository {
    private val TAG = BASE_TAG + ReadRepositoryImpl::class.java.simpleName
    override fun getUnreadConversationThreads(isGeneral: Boolean): Flow<List<ConversationThread>> {
        //Log.i(TAG, "getUnreadConversationThreads: ")
        val originalList: Flow<List<ConversationThread>> = AppDataSingleton.conversationThreads
        val unreadCountMapFlow: Flow<Map<Long, Long>> = if (isGeneral) AppDataSingleton.unreadMessageCountGeneral else AppDataSingleton.unreadMessageCountPrivate

        return combine(originalList, unreadCountMapFlow) { threads, unreadMap ->
            val unreadIds = unreadMap.keys.toSet()
            val chunkSize = getOptimalChunkSize(unreadIds.size)
            // ✅ Chunking for huge lists
            threads.asSequence()
                .chunked(chunkSize) // Adjust chunk size as needed
                .flatMap { chunk ->
                    chunk.filter { it.threadId in unreadIds }
                }.toList()

        }.flowOn(Dispatchers.Default) // ⚡ Offload to background thread
            .distinctUntilChanged()     // Prevent redundant UI updates
    }

    //region Mark as read/unRead ConversationThread
    override suspend fun markAsReadConversationThreads(threadIds: List<Long>): Boolean {
        if (threadIds.isEmpty()) {
            Log.w(TAG, "No thread IDs provided.")
            return false
        }

        return withContext(Dispatchers.IO) {
            val chunkSize = getOptimalChunkSize(threadIds.size)
            var anyUpdated = false

            threadIds.chunked(chunkSize).forEachIndexed { index, chunk ->
                try {
                    val values = ContentValues().apply {
                        put(Telephony.Sms.READ, 1)
                        put(Telephony.Sms.SEEN, 1)
                    }

                    val placeholders = chunk.joinToString(",") { "?" }
                    val selection = "thread_id IN ($placeholders)"
                    val selectionArgs = chunk.map { it.toString() }.toTypedArray()

                    val rowsUpdated = context.contentResolver.update(
                        Telephony.Sms.CONTENT_URI,
                        values,
                        selection,
                        selectionArgs
                    )

                    if (rowsUpdated > 0) {
                        Log.d(TAG, "Chunk #$index: Marked $rowsUpdated messages as read for threads: $chunk")
                        anyUpdated = true
                    } else {
                        Log.w(TAG, "Chunk #$index: No messages updated for threads: $chunk")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Chunk #$index: Error marking as read: ${e.localizedMessage}")
                }
            }

            anyUpdated
        }
    }


    override suspend fun markAsUnreadConversationThreads(threadIds: List<Long>): Boolean {
        if (threadIds.isEmpty()) {
            Log.w(TAG, "No thread IDs provided.")
            return false
        }

        return try {
            var updatedAny = false
            val chunkSize = getOptimalChunkSize(threadIds.size)

            withContext(Dispatchers.IO) {
                coroutineScope {
                    threadIds.chunked(chunkSize).forEach { chunk ->
                        launch {
                            chunk.forEach { threadId ->
                                val cursor = context.contentResolver.query(
                                    Telephony.Sms.CONTENT_URI,
                                    arrayOf(Telephony.Sms._ID),
                                    "thread_id = ?",
                                    arrayOf(threadId.toString()),
                                    "date DESC LIMIT 1"
                                )

                                cursor?.use {
                                    if (it.moveToFirst()) {
                                        val lastMessageId = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))

                                        val values = ContentValues().apply {
                                            put(Telephony.Sms.READ, 0)
                                            put(Telephony.Sms.SEEN, 0)
                                        }

                                        val updateUri = Uri.withAppendedPath(
                                            Telephony.Sms.CONTENT_URI,
                                            lastMessageId.toString()
                                        )
                                        val rowsUpdated = context.contentResolver.update(updateUri, values, null, null)

                                        if (rowsUpdated > 0) {
                                            Log.d(TAG, "Last message ($lastMessageId) in thread $threadId marked as unread.")
                                            updatedAny = true
                                        } else {
                                            Log.e(TAG, "Failed to mark last message in thread $threadId as unread.")
                                        }
                                    } else {
                                        Log.w(TAG, "No messages found in thread $threadId.")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            updatedAny
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as unread: ${e.localizedMessage}")
            false
        }
    }

    override suspend fun markAsUnreadConversationCountThreads(): Flow<Map<Long, Long>> = flow {
        val result = mutableMapOf<Long, Long>()
        val emitThreshold = 1000
        var counter = 0
        val startTime = System.currentTimeMillis()

        val cursor = withContext(Dispatchers.IO) {
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms.THREAD_ID),
                "${Telephony.Sms.READ} = 0",
                null,
                null
            )
        }

        cursor?.use {
            while (it.moveToNext()) {
                val threadId = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                result[threadId] = result.getOrDefault(threadId, 0L) + 1
                counter++

                if (counter % emitThreshold == 0) {
                    emit(result.toMap()) // ✅ emit now in correct context
                }
            }

            if (counter % emitThreshold != 0) {
                emit(result.toMap())
            }
        }

        val endTime = System.currentTimeMillis()
        Log.i(TAG, "$TAG - Execution time: ${endTime - startTime}ms | Threads: ${result.size}")
    }.flowOn(Dispatchers.IO) // ✅ run entire flow on IO


//endregion
}
