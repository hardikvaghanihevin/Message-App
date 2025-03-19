package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleBinThreadDao {
    @Query("SELECT threadId FROM recyclebin_threads")
    fun getRecycleBinThreadIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun moveToRecycleBinThread(recycleBin: List<RecycleBinThreadEntity>): List<Long>

    @Query("DELETE FROM recyclebin_threads WHERE threadId IN (:threadIds)")
    suspend fun restoreFromRecycleBinThread(threadIds: List<Long>): Int
}



