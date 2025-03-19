package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recyclebin_threads")
data class RecycleBinThreadEntity ( @PrimaryKey val threadId: Long, // Unique thread ID
)


