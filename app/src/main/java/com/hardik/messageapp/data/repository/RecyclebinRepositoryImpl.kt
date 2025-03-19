package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.Telephony
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RecyclebinRepositoryImpl @Inject constructor(
    private val context: Context,
    private val recycleBinThreadDao: RecycleBinThreadDao,

    private val conversationThreadRepository: ConversationThreadRepository,
) : RecyclebinRepository {

    //region Fetch deleted ConversationThread list
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRecycleBinConversationThreads(): Flow<List<ConversationThread>> {
        return recycleBinThreadDao.getRecycleBinThreadIds().flatMapLatest { threadIds ->
            if (threadIds.isEmpty()) {
                flowOf(emptyList()) // Return an empty list if no thread IDs exist
            } else {
                conversationThreadRepository.getConversationThreads(threadIds)
            }
        }
    }
    //endregion

    //region Add and Remove RecycleBin ConversationThread
    override suspend fun moveToRecycleBinConversationThread(recycleBinThreads: List<RecycleBinThreadEntity>): Boolean {
        val count = recycleBinThreadDao.moveToRecycleBinThread(recycleBinThreads)
        return count.size == recycleBinThreads.size
    }

    override suspend fun restoreConversationThreads(threadIds: List<Long>): Boolean {
        val count = recycleBinThreadDao.restoreFromRecycleBinThread(threadIds)
        return count > 0 // Return true if restoration was successful
    }
    //endregion

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
    override suspend fun deletePermanently(threadIds: List<Long>): Boolean {
        if (threadIds.isEmpty()) return false // Return false if the list is empty

        val uri = Telephony.Sms.CONTENT_URI
        val selection = "${Telephony.Sms.THREAD_ID} IN (${threadIds.joinToString(",")})"

        val deletedRows = context.contentResolver.delete(uri, selection, null)

        recycleBinThreadDao.restoreFromRecycleBinThread(threadIds)// todo : also remove from recyclebin

        return deletedRows > 0 // Return true if deletion was successful
    }
    //endregion

}