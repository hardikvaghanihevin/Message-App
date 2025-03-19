package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hardik.messageapp.data.local.entity.PinThreadEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface PinThreadDao {
    @Query("SELECT threadId FROM pin_threads")
    fun getPinnedConversations(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinConversations(recycleBin: List<PinThreadEntity>): List<Long>

    @Query("DELETE FROM pin_threads WHERE threadId IN (:threadIds)")
    suspend fun unpinConversations(threadIds: List<Long>): Int
}


