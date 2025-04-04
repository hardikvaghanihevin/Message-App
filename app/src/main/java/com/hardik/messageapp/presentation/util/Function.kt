package com.hardik.messageapp.presentation.util

import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.hardik.messageapp.helper.SmsDefaultAppHelper

//region mark as read & unread
fun Activity.markSmsAsRead(messageId: Long) {
    if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
        Log.e("SmsRepository", "Cannot update SMS: App is not default SMS app!")
        return
    }

    val values = ContentValues().apply {
        put(Telephony.Sms.READ, 1)
        put(Telephony.Sms.SEEN, 1)
    }

    val updateUri = Uri.parse("content://sms/inbox/$messageId")
    val rowsUpdated = contentResolver.update(updateUri, values, null, null)

    if (rowsUpdated > 0) {
        Log.d("SmsRepository", "SMS marked as read: $messageId")
    } else {
        Log.e("SmsRepository", "Failed to mark SMS as read: $messageId")
    }
}

fun Activity.markThreadAsRead(threadId: Long) {
    if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
        Log.e("SmsRepository", "Cannot update SMS: App is not default SMS app!")
        return
    }

    val values = ContentValues().apply {
        put(Telephony.Sms.READ, 1)
        put(Telephony.Sms.SEEN, 1)
    }

    val updateUri = Uri.parse("content://sms/")
    val rowsUpdated = contentResolver.update(updateUri, values, "thread_id = ?", arrayOf(threadId.toString()))

    if (rowsUpdated > 0) {
        Log.d("SmsRepository", "All messages in thread $threadId marked as read.")
    } else {
        Log.e("SmsRepository", "Failed to mark messages in thread $threadId as read.")
    }
}

fun Activity.markSmsAsUnread(messageId: Long) {
    if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
        Log.e("SmsRepository", "Cannot update SMS: App is not default SMS app!")
        return
    }

    val values = ContentValues().apply {
        put(Telephony.Sms.READ, 0)  // Set unread
        put(Telephony.Sms.SEEN, 0)  // Set unseen
    }

    val updateUri = Uri.parse("content://sms/$messageId")
    val rowsUpdated = contentResolver.update(updateUri, values, null, null)

    if (rowsUpdated > 0) {
        Log.d("SmsRepository", "SMS marked as unread: $messageId")
    } else {
        Log.e("SmsRepository", "Failed to mark SMS as unread: $messageId")
    }
}

fun Activity.markLastMessageAsUnread(threadId: Long) {
    if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
        Log.e("SmsRepository", "Cannot update SMS: App is not default SMS app!")
        return
    }

    // Query the last message in the thread
    val cursor = contentResolver.query(
        Uri.parse("content://sms/"),
        arrayOf("_id"),
        "thread_id = ?",
        arrayOf(threadId.toString()),
        "date DESC LIMIT 1" // Get the most recent message
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val lastMessageId = it.getLong(it.getColumnIndexOrThrow("_id"))

            // Update only the last message
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 0)  // Set unread
                put(Telephony.Sms.SEEN, 0)  // Set unseen
            }

            val updateUri = Uri.parse("content://sms/$lastMessageId")
            val rowsUpdated = contentResolver.update(updateUri, values, null, null)

            if (rowsUpdated > 0) {
                Log.d("SmsRepository", "Last message ($lastMessageId) in thread $threadId marked as unread.")
            } else {
                Log.e("SmsRepository", "Failed to mark last message in thread $threadId as unread.")
            }
        } else {
            Log.e("SmsRepository", "No messages found in thread $threadId.")
        }
    }
}



fun Activity.markThreadAsUnread(threadId: Long) {
    if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
        Log.e("SmsRepository", "Cannot update SMS: App is not default SMS app!")
        return
    }

    val values = ContentValues().apply {
        put(Telephony.Sms.READ, 0)  // Set unread
        put(Telephony.Sms.SEEN, 0)  // Set unseen
    }

    val updateUri = Uri.parse("content://sms/")
    val rowsUpdated = contentResolver.update(updateUri, values, "thread_id = ?", arrayOf(threadId.toString()))

    if (rowsUpdated > 0) {
        Log.d("SmsRepository", "All messages in thread $threadId marked as unread.")
    } else {
        Log.e("SmsRepository", "Failed to mark messages in thread $threadId as unread.")
    }
}

//endregion


