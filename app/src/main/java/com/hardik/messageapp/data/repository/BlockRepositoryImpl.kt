package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.util.Log
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.domain.repository.BlockRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BlockRepositoryImpl @Inject constructor(
    private val context: Context,
    private val blockThreadDao: BlockThreadDao,
) : BlockRepository {
    private val TAG = BASE_TAG + BlockRepositoryImpl::class.simpleName

    //region Fetch BlockConversationThread list
     override fun getBlockedNumbers(): Flow<List<BlockThreadEntity>> = flow {
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

            val systemBlockedNumbers = systemJob.await()
            val dbBlockedThreads = dbJob.await()

            // 🔁 (optional) Log or check system blocked vs db blocked if needed
            Log.e(TAG, "getBlockedNumbers: System blocked: $systemBlockedNumbers")
            Log.e(TAG, "getBlockedNumbers1: DB blocked: $dbBlockedThreads")

            // ✅ Emit only DB result
            emit(dbBlockedThreads)
        }
    }

    //endregion

    //region Block and Unblock ConversationThread

    override suspend fun blockNumbers(blockThreads: List<BlockThreadEntity>): Boolean = coroutineScope {
        try {
            Log.e(TAG, "blockNumbers: blockThreads:$blockThreads", )
            // Launch both operations in parallel using async
            val systemBlockJob = async {
                //val numbers = blockThreads.map { it.number }
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
    override suspend fun unblockNumbers(blockThreads: List<BlockThreadEntity>): Boolean = coroutineScope {
        try {
            val threadIds = blockThreads.map { it.threadId }
            val numbers = blockThreads.map { it.number.toString() }

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
