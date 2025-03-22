package com.hardik.messageapp.helper

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.helper.Constants.BASE_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

data class DataOfConversation(
    val threadId: Long,         //(From Thread or Sms) Unique ID for the conversation thread, common for Sms messages
    val id: Long,               //(From Sms) Unique ID for the conversation
    val sender: String,         //(From Sms)
    val messageBody: String,    //(From Sms)
    val creator: String?,       //(From Sms)
    val timestamp: Long,        //(From Sms)
    val dateSent: Long,         //(From Sms)
    val errorCode: Int,         //(From Sms)
    val locked: Int,            //(From Sms)
    val person: String?,        //(From Sms)
    val protocol: String?,      //(From Sms)
    val read: Boolean = false,  //(From Thread) Indicates if the last message was read (default: false)
    val replyPath: Boolean,     //(From Sms)
    val seen: Boolean,          //(From Sms)
    val serviceCenter: String?, //(From Sms)
    val status: Int,            //(From Sms)
    val subject: String?,       // (From Sms)
    val subscriptionId: Int,    // (From Sms)
    val type: Int,              // (From Sms)
    val isArchived: Boolean = false, // (From Sms)
    val snippet: String,        // (From Thread) Last message in the thread
    val date: Long,             // (From Thread) Timestamp of the last message (epoch time in milliseconds)
    val recipientIds: String,   // (From Thread) Recipient ID(s) linked to this thread
    val phoneNumber: String,    // (From canonical-addresses) Phone number associated with the recipient ID
    val contactName: String,    // (From CommonDataKinds.Phone) Contact name retrieved from the phone number (if available)
    val displayName: String,    // (From CommonDataKinds.Phone) Display name set byu Contact name retrieved from the phone number (if available)
){
    companion object{
        /**
         * Callback for calculating the differences between two `ConversationThread` objects
         * in a `ListAdapter` (for RecyclerView).
         */
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataOfConversation>() {

            /**
             * Checks if two conversation threads represent the same item by comparing their unique `threadId`.
             */
            override fun areItemsTheSame(
                oldItem: DataOfConversation,
                newItem: DataOfConversation
            ): Boolean = oldItem.threadId == newItem.threadId

            /**
             * Checks if the contents of two conversation threads are the same.
             * Compares all relevant fields to detect any updates.
             */
            override fun areContentsTheSame(oldItem: DataOfConversation, newItem: DataOfConversation): Boolean {
                return oldItem.threadId == newItem.threadId &&
                oldItem.id == newItem.id &&
                oldItem.sender == newItem.sender &&
                oldItem.messageBody == newItem.messageBody &&
                oldItem.creator == newItem.creator &&
                oldItem.timestamp == newItem.timestamp &&
                oldItem.dateSent == newItem.dateSent &&
                oldItem.errorCode == newItem.errorCode &&
                oldItem.locked == newItem.locked &&
                oldItem.person == newItem.person &&
                oldItem.protocol == newItem.protocol &&
                oldItem.read == newItem.read &&
                oldItem.replyPath == newItem.replyPath &&
                oldItem.seen == newItem.seen &&
                oldItem.serviceCenter == newItem.serviceCenter &&
                oldItem.status == newItem.status &&
                oldItem.subject == newItem.subject &&
                oldItem.subscriptionId == newItem.subscriptionId &&
                oldItem.type == newItem.type &&
                oldItem.isArchived == newItem.isArchived &&
                oldItem.snippet == newItem.snippet &&
                oldItem.date == newItem.date &&
                oldItem.recipientIds == newItem.recipientIds &&
                oldItem.phoneNumber == newItem.phoneNumber &&
                oldItem.contactName == newItem.contactName &&
                oldItem.displayName == newItem.displayName


            }
        }
    }
}


//----------------------------------------------------------------
// Function to convert a single DataOfConversation object to JSON string
fun DataOfConversation.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}

// Function to convert a list of DataOfConversation objects to JSON string
fun List<DataOfConversation>.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}

// Function to convert a JSON string to a DataOfConversation object
fun String.toDataOfConversation(): DataOfConversation {
    val gson = Gson()
    return gson.fromJson(this, DataOfConversation::class.java)
}

// Function to convert a JSON string to a list of DataOfConversation objects
fun String.toDataOfConversationList(): List<DataOfConversation> {
    val gson = Gson()
    val type = object : TypeToken<List<DataOfConversation>>() {}.type
    return gson.fromJson(this, type)
}

fun analyzeSender(sender: String?): Int = when {
    sender.isNullOrBlank() -> -1
    sender.any { it.isLetter() } -> 2
    sender.any { it.isDigit() } -> 1
    else -> -1
}


fun findContactByPhoneNumber(contactMap: Map<String, Contact>, phoneNumber: String): Contact? {
    return contactMap.values.find { contact ->
        val hasMatch = contact.phoneNumbers.any { savedNumber ->
            normalizePhoneNumber(savedNumber) == normalizePhoneNumber(phoneNumber)
        }
        //Log.d("PhoneMatch", "Checking ${contact.phoneNumbers} against $phoneNumber: Match? $hasMatch")
        hasMatch
    }
}


// Function to normalize phone numbers (remove spaces, dashes, country codes if needed)
fun normalizePhoneNumber(phone: String): String {
    return phone.replace(Regex("[^0-9]"), "") // Removes all non-numeric characters
}

private suspend fun getContactDetails(
    context: Context,
    smsSender: String,
    contactsMap: Map<String, Contact>
): Triple<String, String, String> {
    return withContext(Dispatchers.IO) {  // Ensure it's done on the IO thread
        val contactId = getContactIdFromPhoneNumber(context, smsSender)

        Log.v(BASE_TAG, "Checking contact ID for $smsSender: $contactId") // ✅ Log ID retrieval

        val contactFromMap = contactId?.let { contactsMap[it] }
        Log.v(BASE_TAG, "Contact found for ID $contactId: $contactFromMap") // ✅ Log contact details

        return@withContext Triple(
            smsSender,
            contactsMap[smsSender]?.let { "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim() } ?: "Unknown",
            contactFromMap?.displayName ?: "Unknown"
        )
    }
}


//----------------------------------------------------------------

// Function to fetch conversations using Flow
fun getConversations(context: Context): Flow<List<DataOfConversation>> = flow {
    val threadMap = mutableMapOf<Long, DataOfConversation>()

    coroutineScope {
        val threadJob = async(Dispatchers.IO) { fetchThreads(context) }
        val smsJob = async(Dispatchers.IO) { fetchSms(context) }
        val contactsJob = async(Dispatchers.IO) { fetchContacts(context) }

        val threadList = threadJob.await()
        val smsList = smsJob.await()
        val contactsMap = contactsJob.await()

                        //sms.sender.takeIf { contactsMap.containsKey(it) }.orEmpty(),
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
                    phoneNumber = (contactId?.let { id -> contactsMap[id]?.phoneNumbers }.takeIf { analyze == 1 } ?: sms.sender).toString(),
                    contactName = (contactId?.let { id -> (contactsMap[id]?.firstName + " " + contactsMap[id]?.lastName).trim() } ?: ""),
                    displayName = contactId?.let { id -> contactsMap[id]?.displayName } ?: sms.sender ?: ""
                )
            }
        }
    }

    emit(threadMap.values.toList())
}.flowOn(Dispatchers.IO)

fun getConversations1(context: Context): Flow<List<DataOfConversation>> = flow {
    val threadMap = mutableMapOf<Long, DataOfConversation>()

    coroutineScope {
        // Query for Threads (async)
        val threadJob = async(Dispatchers.IO) { fetchThreads(context) }

        // Query for SMS (async)
        val smsJob = async(Dispatchers.IO) { fetchSms(context) }

        // Query for Contacts (async)
        val contactsJob = async(Dispatchers.IO) { fetchContacts(context) }

        // Await all queries
        val threadList = threadJob.await()
        val smsList = smsJob.await()
        val contactsMap = contactsJob.await()


        // Merge Data
        smsList.forEach { sms ->
            val threadData = threadList.find { it.threadId == sms.threadId }

            val analyze = analyzeSender(sms.sender)

            val (phoneNumber, contactName, displayName) = when (analyze) {
                -1 -> Triple("", "", "")
                1 -> {
                    val contact = contactsMap[sms.sender]
                    Triple(
                        sms.sender.takeIf { contactsMap.containsKey(it) }.orEmpty(),
                        contact?.let { "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}".trim() } ?: "Unknown",
                        contact?.displayName ?: "Unknown"
                    )
                }
                2 -> Triple("", sms.sender.orEmpty(), sms.sender.orEmpty())
                else -> Triple("", "", "")
            }

            if (threadData != null) {
                threadMap[sms.threadId] = DataOfConversation(
                    threadId = sms.threadId,//✅
                    id = sms.id,//✅
                    sender = sms.sender,//✅
                    messageBody = sms.messageBody,//✅
                    creator = sms.creator,//✅
                    timestamp = sms.timestamp,//✅
                    dateSent = sms.dateSent,//-
                    errorCode = sms.errorCode,//-
                    locked = sms.locked,//-
                    person = sms.person,//-
                    protocol = sms.protocol,//-
                    read = threadData.read,//✅
                    replyPath = sms.replyPath,//-
                    seen = sms.seen,//✅
                    serviceCenter = sms.serviceCenter,//-
                    status = sms.status,//-
                    subject = sms.subject,//-
                    subscriptionId = sms.subscriptionId,//✅
                    type = sms.type,//✅
                    isArchived = sms.isArchived,//✅

                    snippet = threadData.snippet,//✅
                    date = threadData.date,//✅
                    recipientIds = threadData.recipientIds,//✅
                    phoneNumber = phoneNumber,
                    contactName = contactName,
                    displayName = displayName,
                )
            }
        }
    }

    emit(threadMap.values.toList()) // Emit merged conversations
}.flowOn(Dispatchers.IO) // Run in background thread

// Function to fetch Threads


private fun fetchThreads(context : Context): List<DataOfConversation> {

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

    val threads = mutableListOf<DataOfConversation>()

    cursor?.use {
        while (it.moveToNext()) {
            threads.add(
                DataOfConversation(

                    threadId = it.getLongOrNull(it.getColumnIndex(Telephony.Threads._ID)) ?: continue,
                    snippet = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.SNIPPET)) ?: "",
                    date = it.getLongOrNull(it.getColumnIndex(Telephony.Threads.DATE)) ?: 0L,
                    recipientIds = it.getStringOrNull(it.getColumnIndex(Telephony.Threads.RECIPIENT_IDS)) ?: "Unknown",
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


// Function to fetch SMS
private fun fetchSms(context: Context): List<DataOfConversation> {
    val uri = Telephony.Sms.CONTENT_URI

    // ✅ Latest SMS for each thread directly fetch using sub-query
    //val selection = "${Telephony.Sms._ID} IN (SELECT MAX(${Telephony.Sms._ID})FROM smsGROUP BY ${Telephony.Sms.THREAD_ID})".trimIndent()
    //Log.e(BASE_TAG, "fetchSms: $selection", )

    val projection = arrayOf(
        Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
        Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
        Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
        Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
        Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
    )

    val cursor = context.contentResolver.query(uri, projection, null, null, "${Telephony.Sms.DATE} DESC")

    val smsMap = mutableMapOf<Long, DataOfConversation>()

    cursor?.use {
        while (it.moveToNext()) {
            val threadId = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)) ?: 0L

            if (!smsMap.containsKey(threadId)) {
                smsMap[threadId] = DataOfConversation(
                    id = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms._ID)) ?: 0L,
                    threadId = threadId,
                    sender = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "",
                    messageBody = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: "",
                    timestamp = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE)) ?: 0L,
                    dateSent = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT)) ?: 0L,
                    creator = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.CREATOR)),
                    errorCode = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE)) ?: 0,
                    locked = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.LOCKED)) ?: 0,
                    person = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PERSON)),
                    protocol = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)),
                    read = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                    replyPath = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                    seen = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                    serviceCenter = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)),
                    status = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.STATUS)) ?: 0,
                    subject = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)),
                    subscriptionId = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID)) ?: 0,
                    type = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.TYPE)) ?: 0,
                    isArchived = false, snippet = "", date = 0, recipientIds = "",
                    phoneNumber = "", contactName = "", displayName = ""
                )
            }
        }
    }

    return smsMap.values.toList() // Sirf unique threadId ke latest SMS return karna
}

/*
@SuppressLint("Range")
private fun fetchSms1(context: Context): List<DataOfConversation> {
    val uri = Telephony.Sms.CONTENT_URI

    // ✅ Corrected subquery with proper spacing and fixed syntax
    val selection = """
        _id IN (
            SELECT MAX(_id)
            FROM sms
            GROUP BY thread_id
        )
    """.trimIndent()


    val projection = arrayOf(
        Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
        Telephony.Sms.DATE, Telephony.Sms.CREATOR, Telephony.Sms.DATE_SENT, Telephony.Sms.ERROR_CODE,
        Telephony.Sms.LOCKED, Telephony.Sms.PERSON, Telephony.Sms.PROTOCOL, Telephony.Sms.READ,
        Telephony.Sms.REPLY_PATH_PRESENT, Telephony.Sms.SEEN, Telephony.Sms.SERVICE_CENTER,
        Telephony.Sms.STATUS, Telephony.Sms.SUBJECT, Telephony.Sms.SUBSCRIPTION_ID, Telephony.Sms.TYPE
    )

    val cursor = context.contentResolver.query(uri, projection, selection, null, "${Telephony.Sms.DATE} DESC")
    val smsList = mutableListOf<DataOfConversation>()

    cursor?.use {
        while (it.moveToNext()) {
            val threadId = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)) ?: 0L

            val newSmsData = DataOfConversation(
                id = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms._ID)) ?: 0L,
                threadId = threadId,
                sender = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "",
                messageBody = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: "",
                timestamp = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE)) ?: 0L,
                dateSent = it.getLongOrNull(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT)) ?: 0L,
                creator = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.CREATOR)),
                errorCode = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE)) ?: 0,
                locked = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.LOCKED)) ?: 0,
                person = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PERSON)),
                protocol = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)),
                read = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1,
                replyPath = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) == 1,
                seen = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SEEN)) == 1,
                serviceCenter = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)),
                status = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.STATUS)) ?: 0,
                subject = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)),
                subscriptionId = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID)) ?: 0,
                type = it.getIntOrNull(it.getColumnIndexOrThrow(Telephony.Sms.TYPE)) ?: 0,
                isArchived = false, snippet = "", date = 0, recipientIds = "",
                phoneNumber = "", contactName = "", displayName = ""
            )

            smsList.add(newSmsData)
        }
    }

    return smsList // ✅ Latest SMS for each thread directly returned
}



fun fetchContacts1(context: Context): Map<String, Contact> {
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
            val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
            val mimeType = it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE))
            val phoneNumber = if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            } else null

            if (phoneNumber != null) {
                val contact = contactMap.getOrPut(phoneNumber) { Contact(contactId) }

                when (mimeType) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        contact.phoneNumbers.add(phoneNumber)
                        contact.displayName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    }
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val email = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        contact.emails.add(email)
                    }
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        contact.firstName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        contact.lastName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    }
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE -> {
                        contact.photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                    }
                }
            }
        }
    }

    return contactMap // ✅ Return Map<String, Contact>
}*/

fun fetchContacts(context: Context): Map<String, Contact> {
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
            val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
            val mimeType = it.getString(it.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE))

            // ✅ Always retrieve or create a contact based on `contactId`
            val contact = contactMap.getOrPut(contactId) { Contact(contactId) }

            when (mimeType) {
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                    val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    contact.phoneNumbers.add(phoneNumber)
                    contact.displayName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                }
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                    val email = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                    contact.emails.add(email)
                }
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                    contact.firstName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                    contact.lastName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
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


fun getContactIdFromPhoneNumber(context: Context, phoneNumber: String): String? {
    val contentResolver = context.contentResolver
    val normalizedNumber = normalizePhoneNumber(phoneNumber)

    //Log.i(BASE_TAG, "🔍 Searching Contact ID for: phoneNumber=$phoneNumber, normalized=$normalizedNumber")

    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
    )

    val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ?"
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

            //Log.i(BASE_TAG, "✅ Found Contact: ID=$contactId, Number=$savedNumber, Normalized=$savedNormalized")
        }
    }

    //Log.i(BASE_TAG, "❌ Contact Not Found: phoneNumber=$phoneNumber, normalized=$normalizedNumber, contactId=$contactId")
    return contactId
}





