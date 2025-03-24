package com.hardik.messageapp.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Conversation
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.MyDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MyDataRepositoryImpl@Inject constructor(
    private val context: Context
): MyDataRepository {
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
                val threadId = it.getLongOrNull(it.getColumnIndex(Telephony.Threads._ID)) ?: continue
                val snippet = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.SNIPPET)) ?: ""
                val date = it.getLongOrNull(it.getColumnIndex(Telephony.Threads.DATE)) ?: 0L
                val recipientIds = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.RECIPIENT_IDS)) ?: "Unknown"
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

    override fun fetchMessages(): Flow<Map<Long, Message>> = flow {
        val uri = Telephony.Sms.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
            Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
            Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
            Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
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
                        messageBody = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.BODY)) ?: "",
                        timestamp = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE)) ?: 0L,
                        dateSent = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE_SENT)) ?: 0L,
                        creator = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.CREATOR)),
                        errorCode = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.ERROR_CODE)) ?: 0,
                        locked = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.LOCKED)) ?: 0,
                        person = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PERSON)),
                        protocol = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PROTOCOL)),
                        read = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.READ)) == 1,
                        replyPath = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                        seen = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SEEN)) == 1,
                        serviceCenter = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SERVICE_CENTER)),
                        status = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.STATUS)) ?: 0,
                        subject = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SUBJECT)),
                        subscriptionId = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)) ?: 0,
                        type = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.TYPE)) ?: 0,
                        isArchived = false
                    )
                }
            }
        }

        emit(smsMap) // Emit the map directly
    }.flowOn(Dispatchers.IO)

    override fun fetchContacts(): Flow<Map<String, Contact>> = flow {
        val contactMap = mutableMapOf<String, Contact>()

        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI
        )

        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.MIMETYPE} IN (?, ?, ?, ?)",
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)) ?: ""
                val phoneNumber = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)) ?:
                it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))?.replace("\\s".toRegex(), "") ?: continue
                //val phoneNumber = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))?.replace("\\s".toRegex(), "") ?: continue
                //val phoneNumber = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))?.let { number -> normalizePhoneNumber(number) } ?: continue
                //val phoneNumber = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: continue

                val mimeType = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)) ?: ""

                val contact = contactMap.getOrPut(phoneNumber) {
                    Contact(contactId = contactId, phoneNumbers = mutableListOf(phoneNumber))
                }

                when (mimeType) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        contact.displayName = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            ?: ""
                        if (!contact.phoneNumbers.contains(phoneNumber)) { contact.phoneNumbers.add(phoneNumber) }
                    }

                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        contact.firstName = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        contact.lastName = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    }

                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))?.let { email ->
                            if (!contact.emails.contains(email)) {
                                contact.emails.add(email)
                            }
                        }
                    }

                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE -> {
                        contact.photoUri = it.getStringOrNull(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                    }
                }
            }
        }
        //contactMap.forEach{ Log.i(BASE_TAG, "fetchContacts: key:${it.key} - P:${it.value.phoneNumbers} - D:${it.value.displayName} - F:${it.value.firstName} - L:${it.value.lastName}", ) }

        emit(contactMap) // ✅ Emits Map<String, Contact>
    }.flowOn(Dispatchers.IO) // ✅ Runs on IO thread

































    fun fetchMessages1(): Flow<List<Message>> = flow {
        val uri = Telephony.Sms.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
            Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
            Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
            Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
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
                        messageBody = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.BODY)) ?: "",
                        timestamp = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE)) ?: 0L,
                        dateSent = it.getLongOrNull(it.getColumnIndex(Telephony.Sms.DATE_SENT)) ?: 0L,
                        creator = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.CREATOR)),
                        errorCode = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.ERROR_CODE)) ?: 0,
                        locked = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.LOCKED)) ?: 0,
                        person = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PERSON)),
                        protocol = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.PROTOCOL)),
                        read = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.READ)) == 1,
                        replyPath = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                        seen = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SEEN)) == 1,
                        serviceCenter = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SERVICE_CENTER)),
                        status = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.STATUS)) ?: 0,
                        subject = it.getStringOrNull(it.getColumnIndex(Telephony.Sms.SUBJECT)),
                        subscriptionId = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)) ?: 0,
                        type = it.getIntOrNull(it.getColumnIndex(Telephony.Sms.TYPE)) ?: 0,
                        isArchived = false
                    )
                }
            }
        }

        emit(smsMap.values.toList()) // Returns only the latest message per thread
    }.flowOn(Dispatchers.IO) // Ensures it runs on the IO thread

    fun fetchContacts1(): Flow<List<Contact>> = flow {
        val contactMap = mutableMapOf<String, Contact>()

        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI
        )

        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.MIMETYPE} IN (?, ?, ?, ?)",
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            ),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getStringOrNull(it.getColumnIndex(ContactsContract.Data.CONTACT_ID)) ?: continue
                val mimeType = it.getStringOrNull(it.getColumnIndex(ContactsContract.Data.MIMETYPE)) ?: ""

                val contact = contactMap.getOrPut(contactId) { Contact(contactId) }

                when (mimeType) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))?.let { number ->
                            contact.phoneNumbers.add(number)
                        }
                        it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))?.let { name ->
                            contact.displayName = name
                        }
                    }

                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        contact.firstName = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        contact.lastName = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    }

                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))?.let { email ->
                            contact.emails.add(email)
                        }
                    }

                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE -> {
                        contact.photoUri = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                    }
                }
            }
        }

        emit(contactMap.values.toList()) // ✅ Correctly returning Flow<List<Contact>>
    }.flowOn(Dispatchers.IO) // ✅ Run in IO thread

}