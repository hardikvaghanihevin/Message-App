package com.hardik.messageapp.data.repository

import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.BlockThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.ArchivedThreadEntity
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ArchiveRepository
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.util.AppDataSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val archivedThreadDao: ArchivedThreadDao,// for archive list
    private val recycleBinThreadDao: RecycleBinThreadDao,// for soft deletes list
    private val blockThreadDao: BlockThreadDao,// for block list

    private val conversationRepository: ConversationRepository,
) : ArchiveRepository {

    //region Fetch Archive ConversationThread list
    override fun getArchivedConversationThreads(): Flow<List<ConversationThread>> = flow {
        //val systemSmsFlow = conversationThreadRepository.getConversationThreads() // Get all SMS messages
        val systemSmsFlow: Flow<List<ConversationThread>> = AppDataSingleton.conversationThreads // Get all SMS messages
        val archivedIdsFlow: Flow<List<Long>> = archivedThreadDao.getArchivedThreadIds() // Get archived IDs
        val recyclebinIdsFlow: Flow<List<Long>> = recycleBinThreadDao.getRecycleBinThreadIds() // Get bin IDs
        val blockIdsFlow: Flow<List<Long>> = blockThreadDao.getBlockThreadIds() // Get archived IDs

        combine(systemSmsFlow, archivedIdsFlow, recyclebinIdsFlow, blockIdsFlow) { smsList, archivedIds, recyclebinIds, blockIds ->
            //smsList.filter { it.threadId in archivedIds } // Filter only archived messages
            smsList.filter { it.threadId in archivedIds && it.threadId !in recyclebinIds && it.threadId !in blockIds} // Keep only those which are archived but NOT in recycle bin
        }.collect { emit(it) }
    }.flowOn(Dispatchers.IO)
    //endregion

    //region Add and Remove Archive ConversationThread
    override suspend fun archiveConversationThread(threadIds: List<Long>): Boolean {
        val archivedThreads = threadIds.map { ArchivedThreadEntity(it) }
        val success = archivedThreadDao.archiveThread(archivedThreads)
        return success.size == threadIds.size
    }

    override suspend fun unarchiveConversationThread(threadIds: List<Long>): Boolean {
        val success = archivedThreadDao.unarchiveThread(threadIds)
        return success > 0
    }
    //endregion
}