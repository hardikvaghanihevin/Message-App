package com.hardik.messageapp.presentation.util

import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.helper.SmsDefaultAppHelper
import com.hardik.messageapp.presentation.custom_view.PopupMenu

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


/**
 * This function for home bottom menu show option*/
fun evaluateSelectionGetHomeBottomMenu(list: List<ConversationThread>): PopupMenu {
    val allRead = list.all { it.read }
    val hasUnread = list.any { !it.read }
    //val allUnread = list.all { !it.read }

    val allPin = list.all { it.isPin }
    val hasUnpin = list.any { !it.isPin }
    //val allUnpin = list.all { !it.isPin }

    return when {
        allRead && allPin -> PopupMenu.HOME_UNREAD_UNPIN_BLOCK
        allRead && hasUnpin -> PopupMenu.HOME_UNREAD_PIN_BLOCK
        hasUnread && allPin -> PopupMenu.HOME_READ_UNPIN_BLOCK
        hasUnread && hasUnpin -> PopupMenu.HOME_READ_PIN_BLOCK
        else -> PopupMenu.HOME_READ_PIN_BLOCK
    }
}

fun evaluateSelectionGetHomeToolbarMenu(list: List<ConversationThread>): PopupMenu {
    val allRead = list.all { it.read }
    val hasUnread = list.any { !it.read }

    return when {
        allRead -> PopupMenu.HOME_UNLESS_READ
        hasUnread -> PopupMenu.HOME_ALL
        else -> PopupMenu.HOME_ALL
    }
}

/**
 * Calculates the optimal chunk size for batch operations (e.g., ContentResolver updates)
 * based on the total number of thread IDs to be processed.
 *
 * Why it matters:
 * - Performing operations on very large datasets all at once (e.g., 1 lakh threads) can
 *   cause performance bottlenecks, ANRs, or even binder transaction failures due to
 *   system-level limitations (IPC, SQLite limits).
 * - Smaller datasets benefit from less batching, avoiding the overhead of unnecessary loops.
 *
 * @param size Total number of thread IDs to process.
 * @return The ideal chunk size for batching updates.
 */
fun getOptimalChunkSize(size: Int): Int {
    return when {
        size >= 100_000 -> 1000   // For very large datasets, larger chunks reduce IPC overhead
        size >= 50_000 -> 500     // Balanced chunk size for mid-large datasets
        size >= 10_000 -> 300     // Safe and efficient for mid-sized datasets
        size >= 1000 -> 100       // Works well for moderately sized thread sets
        size >= 500 -> 50         // Safer for smaller batches with minimal delay
        else -> size              // For very small sets, no need to chunk
    }
}




