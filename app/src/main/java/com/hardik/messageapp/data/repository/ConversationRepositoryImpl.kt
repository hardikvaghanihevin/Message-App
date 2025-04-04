package com.hardik.messageapp.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Conversation
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.ConversationRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.removeCountryCode
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlin.properties.Delegates

class ConversationRepositoryImpl@Inject constructor(
    private val context: Context,
    private val phoneNumberUtil: PhoneNumberUtil,
): ConversationRepository {
    private val TAG = BASE_TAG + ConversationRepositoryImpl::class.java.simpleName

    private var startTime by Delegates.notNull<Long>() // Start time
    private var endTime by Delegates.notNull<Long>() // End time
    override fun fetchConversations(): Flow<List<Conversation>> = flow {
        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS,
        )

        val cursor = context.contentResolver.query(
            Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true"),
            projection, null, null, "${Telephony.Threads.DATE} DESC"
        )

        val threads = mutableListOf<Conversation>()

        cursor?.use {
            while (it.moveToNext()) {
                val threadId =
                    it.getLongOrNull(it.getColumnIndex(Telephony.Threads._ID)) ?: continue
                val snippet = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.SNIPPET)) ?: ""
                val date = it.getLongOrNull(it.getColumnIndex(Telephony.Threads.DATE)) ?: 0L
                val recipientIds =
                    it.getStringOrNull(it.getColumnIndex(Telephony.Threads.RECIPIENT_IDS))
                        ?: "Unknown"
                val read = it.getIntOrNull(it.getColumnIndex(Telephony.Threads.READ)) == 1

                threads.add(
                    Conversation(
                        threadId = threadId,
                        snippet = snippet,
                        date = date,
                        recipientIds = recipientIds,
                        read = read
                    )
                )
            }
        }

        emit(threads)
    }.flowOn(Dispatchers.IO) // Ensures execution happens on the IO thread
        .onStart { startTime = System.currentTimeMillis() }
        .onCompletion {cause ->
            endTime = System.currentTimeMillis()
            //Log.i(TAG, "$TAG - Total execution time Conversation: ${endTime - startTime}ms")
            //if (cause is CancellationException) { Log.w(TAG, "$TAG - Flow was cancelled: ${cause.message}") }
        }

    private var startTimeFM by Delegates.notNull<Long>() // Start time
    private var endTimeFM by Delegates.notNull<Long>() // End time
    override fun fetchMessages(): Flow<Map<Long, Message>> = flow {
        val uri = Telephony.Sms.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.THREAD_ID,
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.CREATOR,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.ERROR_CODE,
            Telephony.Sms.LOCKED,
            Telephony.Sms.PERSON,
            Telephony.Sms.PROTOCOL,
            Telephony.Sms.READ,
            Telephony.Sms.REPLY_PATH_PRESENT,
            Telephony.Sms.SEEN,
            Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.STATUS,
            Telephony.Sms.SUBJECT,
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms.TYPE
        )

        val cursor = context.contentResolver.query(
            uri, projection, null, null, "${Telephony.Sms.DATE} DESC"
        )

        val smsMap = mutableMapOf<Long, Message>()

        cursor?.use {
            while (it.moveToNext()) {
                val threadId = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.THREAD_ID)) ?: 0L

                if (!smsMap.containsKey(threadId)) {
                    smsMap[threadId] = Message(
                        id = it.getLongOrNull(it.getColumnIndex(Telephony.Sms._ID)) ?: 0L,
                        threadId = threadId,
                        sender = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.ADDRESS)) ?: "",
                        messageBody = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.BODY))
                            ?: "",
                        timestamp = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE)) ?: 0L,
                        dateSent = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE_SENT))
                            ?: 0L,
                        creator = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.CREATOR)),
                        errorCode = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.ERROR_CODE))
                            ?: 0,
                        locked = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.LOCKED)) ?: 0,
                        person = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PERSON)),
                        protocol = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PROTOCOL)),
                        read = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.READ)) == 1,
                        replyPath = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                        seen = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SEEN)) == 1,
                        serviceCenter = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SERVICE_CENTER)),
                        status = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.STATUS)) ?: 0,
                        subject = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SUBJECT)),
                        subscriptionId = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
                            ?: 0,
                        type = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.TYPE)) ?: 0,
                        isArchived = false,

                        )
                }
            }
        }

        //Log.e(BASE_TAG, "fetchMessages: ${smsMap.size}", )
        emit(smsMap) // Emit the map directly
    }.flowOn(Dispatchers.IO)
        .onStart { startTimeFM = System.currentTimeMillis() }
        .onCompletion {cause ->
            endTimeFM = System.currentTimeMillis()
            //Log.i(TAG, "$TAG - Total execution time Message: ${endTimeFM - startTimeFM}ms")
            //if (cause is CancellationException) { Log.w(TAG, "$TAG - Flow was cancelled: ${cause.message}") }

        }

    private var startTimeFC by Delegates.notNull<Long>() // Start time
    private var endTimeFC by Delegates.notNull<Long>() // End time
    override fun fetchContacts(): Flow<Map<String, Contact>> = flow {
        val contactMap = mutableMapOf<String, Contact>()

        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(
            uri, projection, null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
//                val phoneNumber = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: continue
//                val phoneNumberKey = phoneNumber.removeCountryCode(phoneNumberUtil)
//                val contact = contactMap.getOrPut(phoneNumberKey) {
//                    Contact(contactId = contactId, phoneNumbers = mutableListOf(phoneNumber))
//                }
//                if (!contact.phoneNumbers.contains(phoneNumber)) { contact.phoneNumbers.add(phoneNumber) }

        cursor?.use {
            val idIndex: Int = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val imageIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getInt(idIndex)
                val displayName = it.getString(nameIndex) ?: "Unknown"
                val imageUri = it.getString(imageIndex)
                val number = it.getString(numberIndex).replace(" ", "").replace("-", "")

                val phoneNumberKey = number.removeCountryCode(phoneNumberUtil)

                // Check if the number exists in the map, otherwise create a new Contact
                val contact = contactMap.getOrPut(phoneNumberKey) {
                    Contact(
                        contactId = contactId,
                        displayName = displayName,
                        phoneNumbers = mutableListOf(),
                        photoUri = imageUri,
                        normalizeNumber = phoneNumberKey
                    )
                }

                // Add the phone number if not already present
                if (!contact.phoneNumbers.contains(number)) {
                    contact.phoneNumbers.add(number)
                }
            }
        }

        emit(contactMap) // ✅ Emits Map<String, Contact>
    }.flowOn(Dispatchers.IO)
        .onStart { startTimeFC = System.currentTimeMillis() }
        .onCompletion {cause ->
            endTimeFC = System.currentTimeMillis()
            //Log.i(TAG, "$TAG - Total execution time Contact: ${endTimeFC - startTimeFC}ms")
            //if (cause is CancellationException) { Log.w(TAG, "$TAG - Flow was cancelled: ${cause.message}") }

        }
}