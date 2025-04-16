package com.hardik.messageapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recyclebin_threads")
data class RecycleBinThreadEntity (
    @PrimaryKey (autoGenerate = true) val id: Long = 0, // Unique ID (auto-incrementing)
    val threadId: Long, // Unique thread ID
    val sender: String, // +91704385****,AD-AIRTEL
    val messageJson: String, // All message list as json (note: each time when threadId is same or different)
    val timestamp: Long,
){
    companion object {
//        val gson = Gson()
//        fun List<RecycleBinThreadEntity>.toJson(): String {
//            return gson.toJson(this)
//        }
//        fun RecycleBinThreadEntity.toJson(): String {
//            return gson.toJson(this)
//        }
//
//        fun fromJson(json: String): RecycleBinThreadEntity {
//            return gson.fromJson(json, RecycleBinThreadEntity::class.java)
//        }
//
//        fun listFromJson(json: String): List<RecycleBinThreadEntity> {
//            val type = object : TypeToken<List<RecycleBinThreadEntity>>() {}.type
//            return gson.fromJson(json, type)
//        }
    }
}



