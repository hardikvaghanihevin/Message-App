package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleBinThreadDao {
    @Query("SELECT threadId FROM recyclebin_threads")
    fun getRecycleBinThreadIds(): Flow<List<Long>>

    //@Query("SELECT * FROM recyclebin_threads")
    @Query("SELECT * FROM recyclebin_threads WHERE sender IN (:senders)")
    //fun getRecycleBinDataBySenders(senders: List<String>): Flow<List<RecycleBinThreadEntity>>
    suspend fun getRecycleBinDataBySenders(senders: List<String>): List<RecycleBinThreadEntity>

    @Query("SELECT * FROM recyclebin_threads WHERE timestamp IN (SELECT MAX(timestamp) FROM recyclebin_threads GROUP BY sender)")
    fun getRecycleBinData(): Flow<List<RecycleBinThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun moveToRecycleBinThread(recycleBin: List<RecycleBinThreadEntity>): List<Long>

    @Transaction
    @Query("DELETE FROM recyclebin_threads WHERE sender IN (:senders)")
    suspend fun restoreFromRecycleBinThread(senders: List<String>): Int

    @Query("DELETE FROM recyclebin_threads WHERE threadId IN (:threadIds)")
    suspend fun deleteFromRecycleBinThread(threadIds: List<Long>): Int
}



