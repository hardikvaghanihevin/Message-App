package com.hardik.messageapp.data.repository

import android.content.Context
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.model.SearchItem
import com.hardik.messageapp.domain.repository.SearchRepository
import com.hardik.messageapp.helper.analyzeSender
import com.hardik.messageapp.helper.removeCountryCode
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val context: Context,
    private val phoneNumberUtil: PhoneNumberUtil,
) : SearchRepository {
    override fun search(query: String): Flow<List<SearchItem>> =
        combine(searchContacts(query), searchMessages(query)) { contacts, messages ->
            buildList {
                if (messages.isNotEmpty()) {
                    add(SearchItem.Header("Messages"))
                    addAll(messages.map { (threadId, message) ->

                        val sender = message.sender.trim()

                        // Skip if sender is null, empty, or blank
                        if (sender.isEmpty()) return@buildList

                        val senderType = analyzeSender(message.sender)

                        val contact: Contact = if (senderType == 1) { // numbers
                            val phoneNumberKey: String = sender.removeCountryCode(phoneNumberUtil)

                            val foundContact: Contact? = contacts[phoneNumberKey]

                            if (foundContact != null) {
                                val normalizeNumber: String = foundContact.normalizeNumber
                                val photoUri: String = foundContact.photoUri ?: ""
                                val displayName: String = foundContact.displayName ?: sender

                                foundContact.copy(normalizeNumber = normalizeNumber, photoUri = photoUri, displayName = displayName)
                            } else {
                                // Provide a default Contact object when not found
                                Contact(
                                    contactId = -1, // Or generate a unique ID
                                    displayName = sender,
                                    phoneNumbers = mutableListOf(sender), // Or an empty list
                                    photoUri = null,
                                    normalizeNumber = sender
                                )
                            }
                        } else {
                            // Provide a default Contact object when senderType != 1
                            Contact(
                                contactId = -1, // Or generate a unique ID
                                displayName = sender,
                                phoneNumbers = mutableListOf(sender), // Or an empty list
                                photoUri = null,
                                normalizeNumber = sender
                            )
                        }

                        SearchItem.MessageItem(
                            message.copy(
                                normalizeNumber = contact.normalizeNumber,
                                displayName = contact.displayName,
                                photoUri = contact.photoUri ?: ""
                            )
                        )
                    })
                }

                if (contacts.isNotEmpty()) {
                    add(SearchItem.Header("Contacts"))
                    addAll(contacts.values.map { SearchItem.ContactItem(it) })
                }
            }
        }.flowOn(Dispatchers.IO)

    // 🔹 Fetch Messages (Latest per Thread)
    private fun searchMessages(query: String): Flow<Map<Long, Message>> = flow {
        val messages = mutableMapOf<Long, Message>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)

        context.contentResolver.query(
            uri, projection,
            "${Telephony.Sms.ADDRESS} LIKE ? OR ${Telephony.Sms.BODY} LIKE ?",
            arrayOf("%$query%", "%$query%"),
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                val message = Message(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)),
                    threadId = threadId,
                    sender = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "",
                    messageBody = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: "",
                    timestamp = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)) ?: 0L,
                    matchFoundCount = (messages[threadId]?.matchFoundCount ?: 0) + 1 // Increment match count
                )

                // Store only the latest message in each thread, keeping the highest timestamp
                //messages[threadId] = messages[threadId]?.takeIf { it.timestamp >= message.timestamp } ?: message
                messages[threadId] = if ((messages[threadId]?.timestamp ?: 0) >= message.timestamp) {
                    messages[threadId]?.copy(matchFoundCount = message.matchFoundCount) ?: message
                } else {
                    message
                }

            }
        }
        emit(messages)
    }.flowOn(Dispatchers.IO)

    // 🔹 Fetch Contacts (Mapped by Normalized Number)
    private fun searchContacts(query: String): Flow<Map<String, Contact>> = flow {
        val contacts = mutableMapOf<String, Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            uri, projection,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace(" ", "").replace("-", "")
                val phoneNumberKey = number.removeCountryCode(phoneNumberUtil)

                contacts.getOrPut(phoneNumberKey) {
                    Contact(
                        contactId = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) ?: -1,
                        displayName = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Unknown",
                        phoneNumbers = mutableListOf(),
                        photoUri = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)),
                        normalizeNumber = phoneNumberKey
                    )
                }.phoneNumbers.add(number)
            }
        }
        emit(contacts)
    }.flowOn(Dispatchers.IO)
}
