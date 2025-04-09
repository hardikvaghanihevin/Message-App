package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_threads")
data class BlockThreadEntity(
    @PrimaryKey val threadId: Long, // Unique thread ID
    val number: String,
    val sender: String,
)
