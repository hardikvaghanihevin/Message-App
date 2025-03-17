package com.hardik.messageapp.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hardik.messageapp.helper.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    private val TAG = BASE_TAG + SmsReceiver::class.java

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>?
                if (pdus != null) {
                    for (pdu in pdus) {
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val messageBody = smsMessage.messageBody
                        val senderNumber = smsMessage.displayOriginatingAddress

                        //Log.d("SMSReceiver", "From: $senderNumber, Message: $messageBody")
                        //Process the received SMS message.

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