package com.hardik.messageapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMessageDao {
    @Query("SELECT * FROM favorite_message")
    fun getFavoriteMessages(): Flow<List<FavoriteMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoriteMessages(recycleBin: List<FavoriteMessageEntity>): List<Long>

    @Query("DELETE FROM favorite_message WHERE id IN (:ids)")
    suspend fun unfavoriteMessages(ids: List<Long>): Int
}