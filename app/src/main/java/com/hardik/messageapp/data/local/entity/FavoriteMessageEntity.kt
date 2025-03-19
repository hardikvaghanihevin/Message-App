package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_message")
data class FavoriteMessageEntity (
    @PrimaryKey val id: Long, // Unique Message ID
    val threadId: Long, // Thread ID
)