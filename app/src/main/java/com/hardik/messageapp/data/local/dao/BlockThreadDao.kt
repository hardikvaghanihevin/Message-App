package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockThreadDao {
    @Query("SELECT threadId FROM block_threads")
    fun getBlockThreadIds(): Flow<List<Long>>

    @Query("SELECT * FROM block_threads")
    fun getBlockThreadsData(): Flow<List<BlockThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockThread(archives: List<BlockThreadEntity>): List<Long>

    @Query("DELETE FROM block_threads WHERE threadId IN (:threadIds)") // for single item [threadId = :threadId]
    suspend fun unblockThread(threadIds: List<Long>): Int
}