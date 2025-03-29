package com.hardik.messageapp.presentation.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hardik.messageapp.R
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.usecase.message.InsertMessageUseCase
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.ui.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    private val TAG = BASE_TAG + SmsReceiver::class.java
    private val CHANNEL_ID = "sms_notifications"
    private val NOTIFICATION_ID = 1001  // Unique ID for notification

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

                        // ✅ Create Message Object
                        val message = Message(
                            threadId = 0,
                            id = 0,
                            sender = senderNumber,
                            messageBody = messageBody,
                            creator = null,
                            timestamp = timestamp,
                            dateSent = 0,
                            errorCode = 0,
                            locked = 0,
                            person = null,
                            protocol = protocol.toString(),
                            read = false,
                            replyPath = replyPath,
                            seen = false,
                            serviceCenter = serviceCenter,
                            status = 0,
                            subject = null,
                            subscriptionId = 0,
                            type = 1,
                            isArchived = false
                        )

                        // ✅ Insert the SMS and Trigger Notification
                        CoroutineScope(Dispatchers.IO).launch {
                            insertMessageUseCase(message)
                            EventBus.getDefault().post(message)
                        }

                        // ✅ Show Notification
                        showNotification(context, senderNumber, messageBody)
                    }
                }
            }
            // Force refresh by notifying the repository
            // Send a broadcast to notify the activity
            val localIntent = Intent("com.example.NEW_SMS_RECEIVED")
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }

    // ✅ Show Notification when an SMS is received
    private fun showNotification(context: Context, sender: String, message: String) {
        createNotificationChannel(context) // Ensure the notification channel is created

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.dummy_ic_sms_notify) // ✅ Ensure you have `ic_sms.xml` in `res/drawable`
            .setContentTitle("New SMS from $sender")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // ✅ Check Notification Permission Before Sending
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            } else {
                Log.e(TAG, "Notification permission NOT granted, skipping notification.")
            }
        } else {
            // ✅ For Android 12 and below, no need for permission
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }


    // ✅ Create Notification Channel (For Android 8.0+)
    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SMS Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for received SMS"
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
