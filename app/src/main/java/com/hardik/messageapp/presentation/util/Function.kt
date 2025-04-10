package com.hardik.messageapp.presentation.util

import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import kotlinx.coroutines.flow.Flow

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



