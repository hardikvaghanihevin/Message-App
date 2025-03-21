package com.hardik.messageapp.data.repository

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val context: Context
) : MessageRepository {

    private fun queryMessage(selection: String? = null, selectionArgs: Array<String>? = null): List<Message> {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE, Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ, Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER, Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE)

        val messageList = mutableListOf<Message>()

        context.contentResolver.query(uri, projection, selection, selectionArgs, "${Telephony.Sms.DATE} DESC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val sms = Message(
                    /*threadId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)),
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)),
                    sender = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: " ",
                    messageBody = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)),
                    creator = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.CREATOR)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)),
                    dateSent = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT)),
                    errorCode = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE)),
                    locked = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.LOCKED)),
                    person = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.PERSON)),
                    protocol = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)),
                    read = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                    replyPath = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                    seen = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                    serviceCenter = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)),
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS)),
                    subject = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)),
                    subscriptionId = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID)),
                    type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)),
                    isArchived = false // Default value
                    */

                    //-------

                    /*threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID).takeIf { it >= 0 } ?: 0),
                    id = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID).takeIf { it >= 0 } ?: 0),
                    sender = cursor.getColumnIndex(Telephony.Sms.ADDRESS).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "Unknown",
                    messageBody = cursor.getColumnIndex(Telephony.Sms.BODY).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "",
                    creator = cursor.getColumnIndex(Telephony.Sms.CREATOR).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "Unknown",
                    timestamp = cursor.getLong((cursor.getColumnIndex(Telephony.Sms.DATE).takeIf { it >= 0 } ?: 0L) as Int),
                    dateSent = cursor.getLong((cursor.getColumnIndex(Telephony.Sms.DATE_SENT).takeIf { it >= 0 } ?: 0L) as Int),
                    errorCode = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.ERROR_CODE).takeIf { it >= 0 } ?: 0),
                    locked = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.LOCKED).takeIf { it >= 0 } ?: 0),
                    person = cursor.getColumnIndex(Telephony.Sms.PERSON).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "",
                    protocol = cursor.getColumnIndex(Telephony.Sms.PROTOCOL).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "",
                    read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ).takeIf { it >= 0 } ?: 0) == 1,
                    replyPath = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT).takeIf { it >= 0 } ?: 0) == 1,
                    seen = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.SEEN).takeIf { it >= 0 } ?: 0) == 1,
                    serviceCenter = cursor.getColumnIndex(Telephony.Sms.SERVICE_CENTER).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "",
                    status = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.STATUS).takeIf { it >= 0 } ?: 0),
                    subject = cursor.getColumnIndex(Telephony.Sms.SUBJECT).takeIf { it >= 0 }?.let { cursor.getString(it) } ?: "",
                    subscriptionId = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID).takeIf { it >= 0 } ?: -1),
                    type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE).takeIf { it >= 0 } ?: 0),
                    isArchived = false // Default value*/

                    //-------

                    threadId = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)) ?: 0L,
                    id = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)) ?: 0L,
                    sender = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: " ",
                    messageBody = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: "",
                    creator = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.CREATOR)) ?: "",
                    timestamp = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)) ?: 0L,
                    dateSent = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT)) ?: 0L,
                    errorCode = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE)) ?: 0,
                    locked = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.LOCKED)) ?: 0,
                    person = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.PERSON)) ?: "",
                    protocol = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)) ?: "",
                    read = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                    replyPath = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                    seen = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                    serviceCenter = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)) ?: "",
                    status = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.STATUS)) ?: 0,
                    subject = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)) ?: "",
                    subscriptionId = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID)) ?: 0,
                    type = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)) ?: 0,
                    isArchived = false // Default value

                )
                messageList.add(sms)
            }
        }
        return messageList
    }

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
}
