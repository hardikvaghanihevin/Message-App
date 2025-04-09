package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.Telephony
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.domain.repository.RecyclebinRepository
import com.hardik.messageapp.presentation.util.AppDataSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RecyclebinRepositoryImpl @Inject constructor(
    private val context: Context,
    private val recycleBinThreadDao: RecycleBinThreadDao,// for soft deletes list
    private val blockThreadDao: BlockThreadDao,

    private val conversationRepository: ConversationRepository,
) : RecyclebinRepository {

    //region Fetch deleted ConversationThread list
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRecycleBinConversationThreads(): Flow<List<ConversationThread>> = flow {
        //val systemSmsFlow: Flow<List<ConversationThread>> = conversationThreadRepository.getConversationThreads() // Get all SMS messages
        val systemSmsFlow: Flow<List<ConversationThread>> = AppDataSingleton.conversationThreads // Get all SMS messages
        val recyclebinIdsFlow: Flow<List<Long>> = recycleBinThreadDao.getRecycleBinThreadIds() // Get recycle bin IDs
        //val blockIdsFlow: Flow<List<Long>> = blockThreadDao.getBlockThreadIds() // Get recycle bin IDs

        combine(systemSmsFlow, recyclebinIdsFlow) { smsList, recyclebinIds ->
            //smsList.filter { it.threadId in recyclebinIds && it.threadId in blockIds} // Filter only recyclebin/block messages
            smsList.filter { it.threadId in recyclebinIds} // Filter only recyclebin/block messages
        }.collect { emit(it) }
    }.flowOn(Dispatchers.IO)
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
    override suspend fun deletePermanently(threadIds: List<Long>): Boolean = coroutineScope {
        if (threadIds.isEmpty()) return@coroutineScope false

        try {
            val deleteSystemJob = async(Dispatchers.IO) {
                val uri = Telephony.Sms.CONTENT_URI
                val selection = "${Telephony.Sms.THREAD_ID} IN (${threadIds.joinToString(",")})"
                val deletedRows = context.contentResolver.delete(uri, selection, null)
                deletedRows > 0
            }

            val deleteFromDbJob = async(Dispatchers.IO) {
                recycleBinThreadDao.deleteFromRecycleBinThread(threadIds) // You should have a delete method
                true // Assume successful if no exception
            }

            val systemResult = deleteSystemJob.await()
            val dbResult = deleteFromDbJob.await()

            systemResult && dbResult// Return true if deletion was successful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //endregion

}