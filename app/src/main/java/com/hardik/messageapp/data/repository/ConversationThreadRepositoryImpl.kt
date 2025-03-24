package com.hardik.messageapp.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.domain.repository.ConversationThreadRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ConversationThreadRepositoryImpl @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
) : ConversationThreadRepository {
    private val TAG = BASE_TAG + ConversationThreadRepositoryImpl::class.java.simpleName

    override fun getConversationThreads(): Flow<List<ConversationThread>> = flow {
        val startTime = System.currentTimeMillis() // Start time

        val threadMap = mutableMapOf<Long, ConversationThread>()

        coroutineScope {
            val threadJob = async(Dispatchers.IO) { fetchThreads() }
            val smsJob = async(Dispatchers.IO) { fetchSms() }
            val contactsJob = async(Dispatchers.IO) { fetchContacts() }

            val threadList = threadJob.await()
            val smsList = smsJob.await()
            val contactsMap = contactsJob.await()

            smsList.forEach { sms ->
                val threadData = threadList.find { it.threadId == sms.threadId }
                val analyze = analyzeSender(sms.sender)

                val contactId = getContactIdFromPhoneNumber(context, sms.sender)

                threadData?.let {
                    threadMap[sms.threadId] = sms.copy(
                        snippet = it.snippet,
                        date = it.date,
                        recipientIds = it.recipientIds,
                        read = it.read,
                        phoneNumber = (contactId?.let { id -> contactsMap[id]?.phoneNumbers }
                            .takeIf { analyze == 1 } ?: sms.sender).toString(),
                        contactName = (contactId?.let { id -> (contactsMap[id]?.firstName + " " + contactsMap[id]?.lastName).trim() }
                            ?: ""),
                        displayName = contactId?.let { id -> contactsMap[id]?.displayName }
                            ?: sms.sender ?: ""
                    )
                }
            }
        }

        emit(threadMap.values.toList())
        val endTime = System.currentTimeMillis() // End time
        val executionTime = endTime - startTime
        Log.i(TAG, "Total execution time: ${executionTime}ms")
    }.flowOn(Dispatchers.IO)


    private fun fetchSms(): List<ConversationThread> {

        // ✅ Latest SMS for each thread directly fetch using sub-query
        //val selection = "${Telephony.Sms._ID} IN (SELECT MAX(${Telephony.Sms._ID})FROM smsGROUP BY ${Telephony.Sms.THREAD_ID})".trimIndent()

        val uri = Telephony.Sms.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
            Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
            Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
            Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
        )

        val cursor =
            context.contentResolver.query(uri, projection, null, null, "${Telephony.Sms.DATE} DESC")

        val smsMap = mutableMapOf<Long, ConversationThread>()

        cursor?.use {
            while (it.moveToNext()) {
                val threadId =
                    it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)) ?: 0L

                if (!smsMap.containsKey(threadId)) {
                    smsMap[threadId] = ConversationThread(
                        id = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms._ID)) ?: 0L,
                        threadId = threadId,
                        sender = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                            ?: "",
                        messageBody = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                            ?: "",
                        timestamp = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                            ?: 0L,
                        dateSent = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))
                            ?: 0L,
                        creator = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.CREATOR)),
                        errorCode = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE))
                            ?: 0,
                        locked = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.LOCKED))
                            ?: 0,
                        person = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PERSON)),
                        protocol = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)),
                        read = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                        replyPath = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                        seen = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                        serviceCenter = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)),
                        status = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.STATUS))
                            ?: 0,
                        subject = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)),
                        subscriptionId = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID))
                            ?: 0,
                        type = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.TYPE)) ?: 0,
                        isArchived = false, snippet = "", date = 0, recipientIds = "",
                        phoneNumber = "", contactName = "", displayName = ""
                    )
                }
            }
        }

        return smsMap.values.toList() // Sirf unique threadId ke latest SMS return karna
    }


    private fun fetchThreads(): List<ConversationThread> {

        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS,
        )

        val cursor = context.contentResolver.query(
            //Uri.parse("content://mms-sms/conversations/"),
            //projection, null, null, "date DESC"
            Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true"),
            projection, null, null, "${Telephony.Threads.DATE} DESC"
        )

        val threads = mutableListOf<ConversationThread>()

        cursor?.use {
            while (it.moveToNext()) {
                threads.add(
                    ConversationThread(

                        threadId = it.getLongOrNull(it.getColumnIndex(Telephony.Threads._ID))
                            ?: continue,
                        snippet = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.SNIPPET))
                            ?: "",
                        date = it.getLongOrNull(it.getColumnIndex(Telephony.Threads.DATE)) ?: 0L,
                        recipientIds = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.RECIPIENT_IDS))
                            ?: "Unknown",
                        read = it.getIntOrNull(it.getColumnIndex(Telephony.Threads.READ)) == 1,

                        phoneNumber = "", contactName = "", displayName = "",
                        id = 0, sender = "", messageBody = "", creator = null,
                        timestamp = 0, dateSent = 0, errorCode = 0, locked = 0,
                        person = null, protocol = null, replyPath = false,
                        seen = false, serviceCenter = null, status = 0, subject = null,
                        subscriptionId = 0, type = 0, isArchived = false
                    )
                )
            }
        }
        return threads
    }


    private fun fetchContacts(): Map<String, Contact> {
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
                val contactId =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
                val mimeType =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE))

                val contact = contactMap.getOrPut(contactId) { Contact(contactId) }

                when (mimeType) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contact.phoneNumbers.add(phoneNumber)
                        contact.displayName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    }

                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        contact.firstName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        contact.lastName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    }

                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val email = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        contact.emails.add(email)
                    }


                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE -> {
                        contact.photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                    }
                }
            }
        }
        //Log.e(BASE_TAG, "fetchContacts: $contactMap", )

        return contactMap // ✅ Return Map<String, Contact>
    }


    private fun getContactIdFromPhoneNumber(context: Context, phoneNumber: String): String? {
        val contentResolver = context.contentResolver
        val normalizedNumber = normalizePhoneNumber(phoneNumber)

        //Log.i(BASE_TAG, "🔍 Searching Contact ID for: phoneNumber=$phoneNumber, normalized=$normalizedNumber")

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        )

        val selection =
            "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$phoneNumber%", "%$normalizedNumber%")

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        var contactId: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val savedNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val savedNormalized = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                //Log.i(BASE_TAG, "Found Contact: ID=$contactId, Number=$savedNumber, Normalized=$savedNormalized")
            }
        }
        //Log.i(BASE_TAG, "Contact Not Found: phoneNumber=$phoneNumber, normalized=$normalizedNumber, contactId=$contactId")
        return contactId
    }

    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "") // Removes all non-numeric characters
    }


    private fun analyzeSender(sender: String?): Int = when {
        sender.isNullOrBlank() -> -1
        sender.any { it.isLetter() } -> 2
        sender.any { it.isDigit() } -> 1
        else -> -1
    }




    override fun getConversationThreads(threadId: Long): Flow<ConversationThread?> = flow {
        val threads = queryThreads("${Telephony.Threads._ID} = ?", arrayOf(threadId.toString()))
        emit(threads.firstOrNull())
    }.flowOn(Dispatchers.IO)

    override fun getConversationThreads(threadIds: List<Long>): Flow<List<ConversationThread>> =
        flow {
            if (threadIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            val selection = "${Telephony.Threads._ID} IN (${threadIds.joinToString(",") { "?" }})"
            val selectionArgs = threadIds.map { it.toString() }.toTypedArray()

            emit(queryThreads(selection, selectionArgs))
        }.flowOn(Dispatchers.IO)

    private suspend fun queryThreads(
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): List<ConversationThread> {
        val uri =
            Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true") // Use simple query for performance
        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS,
        )

        val threadList = mutableListOf<ConversationThread>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Threads.DATE} DESC"
        )?.use { cursor ->
            val threadIdIndex = cursor.getColumnIndex(Telephony.Threads._ID)
            val snippetIndex = cursor.getColumnIndex(Telephony.Threads.SNIPPET)
            val dateIndex = cursor.getColumnIndex(Telephony.Threads.DATE)
            val readIndex = cursor.getColumnIndex(Telephony.Threads.READ)
            val recipientIdsIndex = cursor.getColumnIndex(Telephony.Threads.RECIPIENT_IDS)

            if (threadIdIndex == -1 || snippetIndex == -1 || dateIndex == -1 || readIndex == -1 || recipientIdsIndex == -1) {
                Log.e(TAG, "Column index not found!")
                return emptyList()
            }

            while (cursor.moveToNext()) {
                val threadId =
                    cursor.getLongOrNull(threadIdIndex) ?: continue // If null, skip this iteration
                val lastMessage =
                    cursor.getStringOrNull(snippetIndex) ?: "" // Default to an empty string if null
                val timestamp = cursor.getLongOrNull(dateIndex) ?: 0L // Default to 0 if null
                val recipientIds = cursor.getStringOrNull(recipientIdsIndex)
                    ?: "Unknown" // Default to "Unknown" if null
                val read = cursor.getIntOrNull(readIndex) == 1 // Convert to Boolean safely

                // Blocking the coroutine to fetch contact details before adding to list
                //val (contactName, phoneNumber) = runBlocking { contactRepository.getPhoneNumberAndContactNameByRecipientId(context, recipientIds).first() }
                //threadList.add(ConversationThread(threadId = threadId, recipientIds = recipientIds, snippet = lastMessage, date = timestamp, read = read, phoneNumber = phoneNumber ?: "Unknown", contactName = contactName ?: "Unknown",))

                val contactFlow = contactRepository.getPhoneNumberAndContactNameByRecipientId(
                    context,
                    recipientIds
                )
                contactFlow.collect { (contactName, phoneNumber) ->
                    Log.e(TAG, "queryThreads: $contactName, $phoneNumber")


                }
            }
        }

        return threadList
    }

}