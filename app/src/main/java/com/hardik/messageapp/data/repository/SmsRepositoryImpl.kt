package com.hardik.messageapp.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.hardik.messageapp.domain.model.SmsMessage
import com.hardik.messageapp.domain.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SmsRepositoryImpl @Inject constructor(
    private val context: Context
) : SmsRepository {

    private val smsList = mutableListOf<SmsMessage>()

    override fun getSmsMessages(): Flow<List<SmsMessage>> = callbackFlow {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        fun fetchSms() {
            smsList.clear()
            val cursor = context.contentResolver.query(uri, projection, null, null, "${Telephony.Sms.DATE} DESC")
            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val sms = SmsMessage(
                        id = it.getLong(idIndex),
                        sender = it.getString(addressIndex),
                        message = it.getString(bodyIndex),
                        timestamp = it.getLong(dateIndex)
                    )
                    smsList.add(sms)
                }
            }
            trySend(smsList) // Send updated list
        }

        // 1️⃣ Listen for database updates (deletion, modification)
        val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                fetchSms() // Fetch new messages
            }
        }
        context.contentResolver.registerContentObserver(uri, true, smsObserver)

        // 2️⃣ Register SMS BroadcastReceiver for new messages
        val smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fetchSms() // Manually refresh the list when a new SMS arrives
            }
        }
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        context.registerReceiver(smsReceiver, filter)

        // Initial fetch
        fetchSms()

        // 3️⃣ Cleanup when flow is canceled
        awaitClose {
            context.contentResolver.unregisterContentObserver(smsObserver)
            context.unregisterReceiver(smsReceiver)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteSms(smsId: Long): Boolean {
        return try {
            val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, smsId.toString())
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
