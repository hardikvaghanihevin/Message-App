package com.hardik.messageapp.util

import android.content.Context
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.flow.Flow

/*
override fun getPhoneNumberByRecipientId(recipientId: String): Flow<String?> = flow {
    val uri = Uri.parse("content://mms-sms/canonical-addresses")
    val projection = arrayOf("_id", "address")
    val selection = "_id = ?"
    val selectionArgs = arrayOf(recipientId)

    val phoneNumber = context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("address"))
        } else null
    }
    Log.i(Constants.BASE_TAG, "getPhoneNumberByRecipientId: $phoneNumber")
    emit(phoneNumber) // Emit either a phone number or null
}.flowOn(Dispatchers.IO)

override fun getContactNameByPhoneNumber(phoneNumber: String):  Flow<String?> = flow {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        emit(null) // Return null if permission is not granted
        return@flow
    }

    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
    val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
    val selectionArgs = arrayOf("%$phoneNumber%") // Using LIKE to handle different formats

    val contactName = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
        } else null
    }

    Log.v(Constants.BASE_TAG, "getContactNameByPhoneNumber: $contactName", )
    emit(contactName)
}.flowOn(Dispatchers.IO)

@OptIn(ExperimentalCoroutinesApi::class)
override fun getPhoneNumberAndContactNameByRecipientId(context: Context, recipientId: String): Flow<Pair<String?, String?>> =
    flow {
        if (recipientId.isBlank()) {
            emit(Pair(null, null))
            return@flow
        }
        emitAll(getPhoneNumberByRecipientId(recipientId)
            .flatMapLatest { phoneNumber ->
                if (phoneNumber != null) {
                    getContactNameByPhoneNumber(phoneNumber)
                        .map { contactName -> Pair(contactName, phoneNumber) }
                } else {
                    flowOf(Pair(null, null))
                }
            })
    }.flowOn(Dispatchers.IO)

private fun getPhoneNumberFromRecipientId(recipientIds: String): String {
    val uri = Uri.parse("content://mms-sms/canonical-addresses")
    val projection = arrayOf("_id", "address")
    val formattedIds = recipientIds.split(" ").joinToString(",") { it.trim() }
    val cursor: Cursor? = context.contentResolver.query(
        uri, projection, "_id IN ($formattedIds)", null, null
    )
    val phoneNumbers = mutableListOf<String>()
    cursor?.use {
        while (it.moveToNext()) {
            phoneNumbers.add(it.getString(it.getColumnIndexOrThrow("address")))
        }
    }
    return phoneNumbers.joinToString(", ")
}*/
fun convertPxToDpSp(context: Context, px: Float): Pair<Float, Float> {
    val displayMetrics = context.resources.displayMetrics

    // Convert px to dp
    val dp = px / (displayMetrics.densityDpi / 160f)

    // Convert px to sp (considering scaledDensity for user font preferences)
    val sp = px / displayMetrics.scaledDensity

    return Pair((dp/1.3911f), (sp/1.6155F))
}

fun analyzeSender(sender: String?): Int = when {
    sender.isNullOrBlank() -> -1
    sender.any { it.isLetter() } -> 2
    sender.any { it.isDigit() } -> 1
    else -> -1
}

fun String.removeCountryCode(phoneInstance: PhoneNumberUtil): String {
    return try {
        val phoneNumber = phoneInstance.parse(this, null)
        var nationalNumber =
            phoneInstance.getNationalSignificantNumber(phoneNumber).replace(" ", "")
                .replace("-", "")
                .replace("(", "").replace(")", "")
        if (nationalNumber.startsWith("0")) {
            nationalNumber = nationalNumber.substring(1)
        }
        nationalNumber
    } catch (e: Exception) {
        var cleanedNumber = this.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        if (cleanedNumber.startsWith("0")) {
            cleanedNumber = cleanedNumber.substring(1)
        }
        cleanedNumber
    }
}


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
        size > 0 -> size          // For very small sets, use the actual size,  no need to chunk
        else -> 1                 // When size is 0 or negative, fallback to minimum safe chunk size
    }
}

suspend fun <T> Flow<List<T>>.flattenToList(): List<T> {
    val result = mutableListOf<T>()
    collect { chunk -> result += chunk }
    return result
}

suspend fun <K, V> Flow<Map<K, V>>.flattenToMap(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    collect { chunk -> result.putAll(chunk) }
    return result
}


fun String.firstUppercase(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}


fun getBestMatchedNumber(phoneNumbers: List<String>?, query: String): String? {
    if (phoneNumbers.isNullOrEmpty() || query.isEmpty()) return null

    return phoneNumbers
        .filter { it.contains(query, ignoreCase = true) }
        .maxByOrNull { number ->
            val matchIndex = number.indexOf(query, ignoreCase = true)
            if (matchIndex >= 0) query.length else 0
        }
}


fun extractNumber(contact: Contact?, query: String, phoneNumberUtil: PhoneNumberUtil): String {
    return (getBestMatchedNumber(contact?.phoneNumbers, query)?.removeCountryCode(phoneNumberUtil)) ?: contact?.normalizeNumber.takeUnless { it.isNullOrEmpty() } ?: query
}



