package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archived_threads")
data class ArchivedThreadEntity( @PrimaryKey val threadId: Long, // Unique thread ID
)

