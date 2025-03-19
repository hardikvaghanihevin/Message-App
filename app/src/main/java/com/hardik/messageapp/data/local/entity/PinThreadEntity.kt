package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_threads")
data class PinThreadEntity (@PrimaryKey val threadId: Long, // Unique thread ID
)
