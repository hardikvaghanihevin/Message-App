package com.hardik.messageapp.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.message.InsertMessageUseCase
import com.hardik.messageapp.helper.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    private val TAG = BASE_TAG + SmsReceiver::class.java

    @Inject
    lateinit var insertMessageUseCase: InsertMessageUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>?
                if (pdus != null) {
                    for (pdu in pdus) {

                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val senderNumber = smsMessage.displayOriginatingAddress
                        val messageBody = smsMessage.messageBody
                        val timestamp = smsMessage.timestampMillis
                        val serviceCenter = smsMessage.serviceCenterAddress
                        val replyPath = smsMessage.isReplyPathPresent
                        val protocol = smsMessage.protocolIdentifier

                        Log.d(TAG, "SMSReceiver --> From: $senderNumber, Message: $messageBody")

                        // ✅ Create Message Object (Fill missing fields with defaults)
                        val message = Message(
                            threadId = 0,  // ❌ Retrieve later from content://sms/
                            id = 0,  // ❌ Retrieve later from content://sms/
                            sender = senderNumber,
                            messageBody = messageBody,
                            creator = null, // ❌ Not available
                            timestamp = timestamp,
                            dateSent = 0, // ❌ Not available
                            errorCode = 0, // ❌ Not available
                            locked = 0, // ❌ Not available
                            person = null, // ❌ Not available
                            protocol = protocol.toString(), // ✅ Sometimes available
                            read = false, // ❌ Not available (default `false`)
                            replyPath = replyPath, // ✅ Available
                            seen = false, // ❌ Not available (default `false`)
                            serviceCenter = serviceCenter, // ✅ Available
                            status = 0, // ❌ Not available
                            subject = null, // ❌ Not available
                            subscriptionId = 0, // ❌ Retrieve later if needed
                            type = 1, // ✅ Set `1` for received SMS
                            isArchived = false // ❌ Not available (default `false`)
                        )

                        // Insert the SMS with the correct threadId
                        CoroutineScope(Dispatchers.IO).launch {
                            insertMessageUseCase(message)

                            // ✅ Post an EventBus event
                            EventBus.getDefault().post(message)
                        }
                    }
                }
            }
            // Force refresh by notifying the repository
            // Send a broadcast to notify the activity
            val localIntent = Intent("com.example.NEW_SMS_RECEIVED")
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }
}