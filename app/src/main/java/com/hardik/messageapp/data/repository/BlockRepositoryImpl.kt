package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.util.Log
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.BlockRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.util.AppDataSingleton
import kotlinx.coroutines.Deferred
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

class BlockRepositoryImpl @Inject constructor(
    private val context: Context,
    private val blockThreadDao: BlockThreadDao,
    private val recycleBinThreadDao: RecycleBinThreadDao,

) : BlockRepository {
    private val TAG = BASE_TAG + BlockRepositoryImpl::class.simpleName

    fun getSystemBlockedNumbers(context: Context): Flow<List<String>> = flow {
        val systemBlocked = mutableListOf<String>()
        val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
        context.contentResolver.query(uri, arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER), null, null, null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                systemBlocked.add(number.trim().lowercase())
            }
        }
        emit(systemBlocked)
    }

    //region Fetch BlockConversationThread list
    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time
    override fun getBlockedConversations(): Flow<List<ConversationThread>> = flow {
        coroutineScope {
            val systemBlockedList: Deferred<List<String>> = async { getSystemBlockedNumbers(context).first() }
            val dbBlockList: Deferred<List<BlockThreadEntity>> = async(Dispatchers.IO) { blockThreadDao.getBlockThreadsData().first() }
            val conversationList: Deferred<List<ConversationThread>> = async (Dispatchers.IO) { AppDataSingleton.conversationThreads.first() }
            val recyclebinIds: Deferred<List<Long>> = async (Dispatchers.IO) { recycleBinThreadDao.getRecycleBinThreadIds().first() }// Get bin Ids

            val systemBlocked = systemBlockedList.await()
            val dbBlockedThreads = dbBlockList.await()
            val recyclebinThreadIds = recyclebinIds.await()
            val conversationThreads = conversationList.await()
            Log.i(TAG, "getBlockedConversations: A")

            combine(
                flowOf(systemBlocked),
                flowOf(dbBlockedThreads),
                flowOf(recyclebinThreadIds),
                flowOf(conversationThreads)
            ) { blockedList, dbBlocked, recyclebin, threads ->
                Log.e(TAG, "getBlockedConversations: B", )
                // blocked [1,2,3,4,5] ->date class is list<String>
                // bdBlocked [2,5,6] ->data class is List<BlockThreadEntity>
                // threads [1,2,3,4,5,6,7,8,9..] ->data class is List<ConversationThread>
                //work here to match and mismatch item and do act on there result
                // now Ans [1,2,3,4,5,6] take from threads it buy threadIds or Sender, number

                // ans insert to both which in not on there table like:
                // blocked [1,2,3,4,5,6] -> "6" is inserted
                // bdBlocked [1,2,3,4,5,6] -> "1,3,4" is inserted

                val normalizedBlocked: List<String> = blockedList.map { it.trim().lowercase() }

                val matchedThreads: List<ConversationThread> = threads.filter { thread ->
                    normalizedBlocked.any { blocked ->
                        if (blocked.any { it.isDigit() }) {
                            thread.normalizeNumber.trim().lowercase() == blocked ||
                                    thread.sender.trim().lowercase() == blocked
                        } else {
                            thread.sender.trim().lowercase() == blocked ||
                                    thread.displayName.trim().lowercase() == blocked
                        }
                    }
                }

                // Now matchedThreads contains threads matched from systemBlocked
                val matchedThreadIds: Set<Long> = matchedThreads.map { it.threadId }.toSet()
                val dbBlockedIds: Set<Long> = dbBlocked.map { it.threadId }.toSet()

                val mergedThreadIds: Set<Long> = matchedThreadIds + dbBlockedIds



                // 👉 Identify missing in dbBlocked (need to insert in DB)
                //val toInsertInDb: List<ConversationThread> = matchedThreads.filterNot { dbBlockedIds.contains(it.threadId) }
                val toInsertInDbThreadId: List<Long> = mergedThreadIds.filterNot { dbBlockedIds.contains(it) }

                val toInsertInDb: List<ConversationThread> = threads.filter { it.threadId in toInsertInDbThreadId }
                Log.e(TAG, "getBlockedConversations: normalizedBlocked:$normalizedBlocked", )

                // 🔁 Insert to DB
                val blockList = toInsertInDb.map { thread -> BlockThreadEntity(threadId = thread.threadId, number = thread.normalizeNumber, sender = thread.sender) }
                blockConversations(blockList) // suspend fun

                val filteredThreads: List<ConversationThread> = threads.filter { it.threadId in mergedThreadIds && it.threadId !in recyclebin }
                filteredThreads// final result
                
/*

                // 👉 Identify missing in systemBlocked (optional: log or notify)
                val systemBlockedThreadIds: Set<Long> = threads.filter { thread ->
                    normalizedBlocked.any { blocked ->
                        if (blocked.any { it.isDigit() }) {
                            thread.normalizeNumber.trim().lowercase() == blocked ||
                                    thread.sender.trim().lowercase() == blocked
                        } else {
                            thread.sender.trim().lowercase() == blocked ||
                                    thread.displayName.trim().lowercase() == blocked
                        }
                    }
                }.map { it.threadId }.toSet()

                val toInsertInSystemBlocked = dbBlocked.filterNot {
                    systemBlockedThreadIds.contains(it.threadId)
                }

                // 🔁 Insert to DB
                val blockList = toInsertInDb.map { thread -> BlockThreadEntity(threadId = thread.threadId, number = thread.normalizeNumber, sender = thread.sender) }
                blockThreadDao.blockThread(blockList) // suspend fun

                // 🔁 Optional: Log for systemBlocked insert suggestions
                toInsertInSystemBlocked.forEach {
                    Log.d("SystemBlock", "ThreadId not in systemBlocked: ${it.threadId}, number: ${it.number}")
                }*/


            }
                .flowOn(Dispatchers.IO)
                .onStart { startTime = System.currentTimeMillis() }
                .onCompletion {
                    endTime = System.currentTimeMillis()
                    //Log.i(TAG, "$TAG - Total execution time Combine: ${endTime - startTime}ms")
                }
                .collect {
                    //Log.e(TAG, "popupMenuBlockConversation: \n${it.joinToString("\n") { thread -> "threadId=${thread.threadId}, sender=${thread.sender}, normalizeNumber=${thread.normalizeNumber}" }}")
                    //LogUtil.d(TAG, "getBlockedConversations: ${it.toJson()}")
                    emit(it)
                }

        }
    }

    /*override fun getBlockedNumbers(): Flow<List<BlockThreadEntity>> = flow {
       coroutineScope {
           // 🔹 System blocked numbers fetch
           val systemJob = async(Dispatchers.IO) {
               val systemBlocked = mutableListOf<String>()
               val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
               context.contentResolver.query(uri, arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER), null, null, null)?.use { cursor ->
                   val numberIndex = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                   while (cursor.moveToNext()) {
                       val number = cursor.getString(numberIndex)
                       systemBlocked.add(number)
                   }
               }
               systemBlocked
           }

           // 🔸 DB blocked thread fetch
           val dbJob = async(Dispatchers.IO) {
               blockThreadDao.getBlockThreadsData().first() // Fetch list from Room
           }

           val systemBlocked = systemJob.await()
           val dbBlockedThreads: List<BlockThreadEntity> = dbJob.await()

           // 🔁 (optional) Log or check system blocked vs db blocked if needed
           Log.e(TAG, "getBlockedNumbers: System blocked: $systemBlocked")
           Log.e(TAG, "getBlockedNumbers1: DB blocked: $dbBlockedThreads")

           // 🔍 Match system blocked items with DB's number/sender
           val matchingThreadIds = dbBlockedThreads
               .filter { entity ->
                   systemBlocked.any { blocked ->
                       val normalizedBlocked = blocked.trim().lowercase()
                       entity.number.trim().lowercase() == normalizedBlocked ||
                               entity.sender.trim().lowercase() == normalizedBlocked
                   }
               }
               .map { it.threadId }

           // ✅ Emit only DB result
           emit(dbBlockedThreads)
       }
   }*/

    //endregion

    //region Block and Unblock ConversationThread

    override suspend fun blockConversations(blockThreads: List<BlockThreadEntity>): Boolean = coroutineScope {
        try {
            Log.i(TAG, "blockNumbers: blockThreads:$blockThreads", )
            // Launch both operations in parallel using async
            val systemBlockJob = async {
//                val numbers = blockThreads.map { it.number }
                val numbers = blockThreads.mapNotNull { it.number.takeIf { num -> num.isNotBlank() } }
                for (number in numbers) {
                    val values = ContentValues().apply {
                        put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                    }
                    context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
                }
                true // return success
            }

            val dbBlockJob = async {
                val success = blockThreadDao.blockThread(blockThreads)
                success.size == blockThreads.size
            }

            // Await both results and return combined success
            systemBlockJob.await() && dbBlockJob.await()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    override suspend fun unblockConversations(blockThreads: List<BlockThreadEntity>): Boolean = coroutineScope {
        try {
            val threadIds = blockThreads.map { it.threadId }
            val numbers = blockThreads.map { it.number }

            // Parallel job to unblock system numbers
            val systemUnblockJob = async {
                val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
                for (number in numbers) {
                    val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
                    val selectionArgs = arrayOf(number)
                    context.contentResolver.delete(uri, selection, selectionArgs)
                }
                true
            }

            // Parallel job to delete from DB
            val dbUnblockJob = async {
                val deletedCount = blockThreadDao.unblockThread(threadIds)
                deletedCount == threadIds.size
            }

            // Await both and return combined result
            systemUnblockJob.await() && dbUnblockJob.await()

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //endregion
}
