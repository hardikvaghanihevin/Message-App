package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hardik.messageapp.data.local.entity.ArchivedThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivedThreadDao {
    @Query("SELECT threadId FROM archived_threads")
    fun getArchivedThreadIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun archiveThread(archives: List<ArchivedThreadEntity>): List<Long>

    @Query("DELETE FROM archived_threads WHERE threadId IN (:threadIds)") // for single item [threadId = :threadId]
    suspend fun unarchiveThread(threadIds: List<Long>): Int
}
