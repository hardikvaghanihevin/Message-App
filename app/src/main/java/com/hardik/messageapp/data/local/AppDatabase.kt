package com.hardik.messageapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hardik.messageapp.data.local.dao.ArchivedThreadDao
import com.hardik.messageapp.data.local.dao.FavoriteMessageDao
import com.hardik.messageapp.data.local.dao.PinThreadDao
import com.hardik.messageapp.data.local.dao.RecycleBinThreadDao
import com.hardik.messageapp.data.local.entity.ArchivedThreadEntity
import com.hardik.messageapp.data.local.entity.FavoriteMessageEntity
import com.hardik.messageapp.data.local.entity.PinThreadEntity
import com.hardik.messageapp.data.local.entity.RecycleBinThreadEntity

@Database(entities = [ArchivedThreadEntity::class, RecycleBinThreadEntity::class, PinThreadEntity::class, FavoriteMessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    //abstract fun archivedMessageDao(): ArchivedMessageDao
    abstract fun archivedThreadDao(): ArchivedThreadDao
    abstract fun recycleBinThreadDao(): RecycleBinThreadDao
    abstract fun pinThreadDao(): PinThreadDao
    abstract fun favoriteMessageDao(): FavoriteMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "message_db"
                )
                    .fallbackToDestructiveMigration() // Handle DB schema changes safely
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
