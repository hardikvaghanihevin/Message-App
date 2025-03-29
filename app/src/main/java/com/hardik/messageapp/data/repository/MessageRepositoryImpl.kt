package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MessageRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val context: Context,
) : MessageRepository {
    private val TAG = BASE_TAG + MessageRepositoryImpl::class.java.simpleName
    val projection = arrayOf(
        Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
        Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
        Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
        Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
        Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
    )

    private fun queryMessage(
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): List<Message> {
        val uri = Telephony.Sms.CONTENT_URI


        val messageList = mutableListOf<Message>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val sms = Message(

                    threadId = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                        ?: 0L,
                    id = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
                        ?: 0L,
                    sender = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        ?: " ",
                    messageBody = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        ?: "",
                    creator = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.CREATOR))
                        ?: "",
                    timestamp = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        ?: 0L,
                    dateSent = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))
                        ?: 0L,
                    errorCode = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE))
                        ?: 0,
                    locked = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.LOCKED))
                        ?: 0,
                    person = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.PERSON))
                        ?: "",
                    protocol = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL))
                        ?: "",
                    read = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                    replyPath = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                    seen = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                    serviceCenter = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER))
                        ?: "",
                    status = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS))
                        ?: 0,
                    subject = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBJECT))
                        ?: "",
                    subscriptionId = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID))
                        ?: 0,
                    type = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                        ?: 0,
                    isArchived = false // Default value

                )
                messageList.add(sms)
            }
        }
        Log.e(TAG, "queryMessage: ${messageList.size}", )
        return messageList
    }

    private fun queryMessageByThreadId(threadId: Long): List<Message> {
        val messages = mutableListOf<Message>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} ASC" // Sort (ASC,DESC) messages by date in descending order
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val threadIdIndex = it.getColumnIndex(Telephony.Sms.THREAD_ID)
            val senderIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val timestampIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val dateSentIndex = it.getColumnIndex(Telephony.Sms.DATE_SENT)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            val readIndex = it.getColumnIndex(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val message = Message(
                    threadId = it.getLong(threadIdIndex),
                    id = it.getLong(idIndex),
                    sender = it.getString(senderIndex) ?: "Unknown",
                    messageBody = it.getString(bodyIndex) ?: "",
                    creator = null, // Not available in SMS provider
                    timestamp = it.getLong(timestampIndex),
                    dateSent = it.getLong(dateSentIndex),
                    errorCode = 0, // Not available in SMS provider
                    locked = 0, // Not available in SMS provider
                    person = null,
                    protocol = null,
                    read = it.getInt(readIndex) == 1,
                    replyPath = false,
                    seen = false,
                    serviceCenter = null,
                    status = 0,
                    subject = null,
                    subscriptionId = 0,
                    type = it.getInt(typeIndex),
                    isArchived = false,
                    unSeenCount = 0
                )
                messages.add(message)
            }
        }

        return messages
    }


    override fun getMessagesByThreadId(threadId: Long) = callbackFlow {
        val messages = queryMessageByThreadId(threadId)
        trySend(messages).isSuccess // Emit the initial message list for the given threadId

        // Observer for real-time updates
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(queryMessageByThreadId(threadId)).isSuccess // Emit updated messages
            }
        }

        context.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer)

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }

    }.flowOn(Dispatchers.IO)


    //region Fetch Message list
    override fun getMessages(): Flow<List<Message>> = callbackFlow {
        val messages = queryMessage()
        trySend(messages).isSuccess // Emit the initial message list

        // Optionally, observe changes if you want real-time updates
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(queryMessage()).isSuccess // Emit updated messages on change
            }
        }
        context.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer)

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }

    }.flowOn(Dispatchers.IO)

    override fun getMessages(messageId: Long): Flow<Message?> = callbackFlow {
        val selection = "${Telephony.Sms._ID} = ?"
        val selectionArgs = arrayOf(messageId.toString())

        val messages = queryMessage(selection, selectionArgs)
        trySend(messages.firstOrNull()).isSuccess // Emit the single message (or null if not found)

        awaitClose {} // No need to observe changes here

    }.flowOn(Dispatchers.IO)

    override fun getMessages(messageIds: List<Long>): Flow<List<Message>> = callbackFlow {
        val selection = "${Telephony.Sms._ID} IN (${messageIds.joinToString(",")})"

        val messages = queryMessage(selection)
        trySend(messages).isSuccess // Emit filtered messages

        awaitClose {} // No need to observe changes here

    }.flowOn(Dispatchers.IO)
    //endregion

    //region Delete Messages
    override suspend fun deleteMessage(smsIds: List<Long>): Boolean {
        if (smsIds.isEmpty()) return false // Return false if no IDs are provided

        return try {
            val selection = "${Telephony.Sms._ID} IN (${smsIds.joinToString(",")})"
            val uri = Telephony.Sms.CONTENT_URI
            val rowsDeleted = context.contentResolver.delete(uri, selection, null)

            rowsDeleted > 0 // Return true if at least one message was deleted
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        /* return try {// for single delete
             val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, smsId.toString())
             val rowsDeleted = context.contentResolver.delete(uri, null, null)
             rowsDeleted > 0
         } catch (e: Exception) {
             e.printStackTrace()
             false
         }*/
    }

    //Todo : favorite, copy, ...
    //endregion

    //region Insert Messages
    override suspend fun insertMessage(message: Message) {
        /*try {
            val threadId = getThreadId(message.sender) ?: 0L  // ✅ Fix: Use message.sender

            val values = ContentValues().apply {
                put("thread_id", threadId) // ✅ Retrieve from database
                put("address", message.sender) // ✅ Sender Number
                put("body", message.messageBody) // ✅ Message Content
                put("date", message.timestamp) // ✅ Message Timestamp
                put("date_sent", message.dateSent) // ❌ Not available, keeping default
                put("read", if (message.read) 1 else 0) // ✅ 0 - Unread, 1 - Read
                put("seen", if (message.seen) 1 else 0) // ✅ 0 - Unseen, 1 - Seen
                put("type", message.type) // ✅ 1 for received, 2 for sent
                put("service_center", message.serviceCenter) // ✅ Service Center (if available)
                put("reply_path_present", if (message.replyPath) 1 else 0) // ✅ Reply Path
                put("protocol", message.protocol?.toIntOrNull()) // ✅ Sometimes available
                put("status", message.status) // ❌ Not available, keeping default 0
                put("locked", message.locked) // ❌ Not available, default 0
                put("error_code", message.errorCode) // ❌ Not available, default 0
                put("subscription_id", message.subscriptionId) // ❌ Retrieve later if needed
                put("subject", message.subject) // ❌ Not available, keeping default null
                put("creator", message.creator) // ❌ Not available, keeping default null

                //person,id,isActive
            }

            val uri = context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)

            Log.d(TAG, "SmsRepository -- SMS inserted into inbox with threadId $threadId: $uri")

        } catch (e: Exception) {
            Log.e(TAG, "SmsRepository -- Failed to insert SMS", e)
        }*/
        try {
            val threadId = getThreadId(message.sender) ?: 0L
            val values = ContentValues().apply {
                put("thread_id", threadId)
                put("address", message.sender)
                put("body", message.messageBody)
                put("date", message.timestamp)
                put("read", if (message.read) 1 else 0)
                put("seen", if (message.seen) 1 else 0)
                put("type", message.type)
                put("service_center", message.serviceCenter)
                put("reply_path_present", if (message.replyPath) 1 else 0)
            }

            val uri = context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)

            if (uri == null) {
                Log.e(TAG, "SMS insertion failed! Make sure the app is the default SMS app.")
            } else {
                Log.d(TAG, "SMS inserted into inbox: $uri")
            }

        } catch (e: Exception) {
            Log.e(TAG, "SmsRepository -- Failed to insert SMS", e)
        }
    }


    private fun getThreadId(sender: String): Long {
        val uri = Uri.parse("content://sms/")
        val projection = arrayOf("thread_id")
        val selection = "address = ?"
        val selectionArgs = arrayOf(sender)

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
            }
        }
        return 0
    }

    private fun updateThreadId(messageId: Long, threadId: Long) {
        val values = ContentValues().apply {
            put("thread_id", threadId)
        }

        context.contentResolver.update(
            Uri.parse("content://sms/"),
            values,
            "id = ?",
            arrayOf(messageId.toString())
        )
    }
    //endregion
}
