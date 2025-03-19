package com.hardik.messageapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.BlockedNumberContract
import com.hardik.messageapp.domain.model.BlockedNumber
import com.hardik.messageapp.domain.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BlockRepositoryImpl @Inject constructor(
    private val context: Context
) : BlockRepository {

    //region Fetch BlockConversationThread list
    override fun getBlockedNumbers(): Flow<List<BlockedNumber>> = flow {
        val blockedNumbers = mutableListOf<BlockedNumber>()
        val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI

        context.contentResolver.query(uri, arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER), null, null, null)?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                blockedNumbers.add(BlockedNumber(number))
            }
        }

        emit(blockedNumbers) // Emit list of blocked numbers
    }.flowOn(Dispatchers.IO)
    //endregion

    //region Block and Unblock ConversationThread

    override suspend fun blockNumbers(numbers: List<String>): Boolean {
        return try {
            for (number in numbers) {
                val values = ContentValues().apply {
                    put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                }
                context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun unblockNumbers(numbers: List<String>): Boolean {
        return try {
            val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
            for (number in numbers) {
                val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
                val selectionArgs = arrayOf(number)

                context.contentResolver.delete(uri, selection, selectionArgs)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //endregion
}
