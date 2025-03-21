package com.hardik.messageapp.data.repository

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(private val context: Context) : ContactRepository {

    override fun getContacts1(): Flow<List<Contact>> = flow {
        val contactList = mutableListOf<Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                contactList.add(Contact(contactId = id, displayName = name, phoneNumbers = mutableListOf(number)))
            }
        }
        emit(contactList)
    }.flowOn(Dispatchers.IO)

    override fun searchContact(phoneNumber: String): Flow<Contact?> = flow {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup._ID
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        var contact: Contact? = null

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)

                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                contact = Contact(id, name, phoneNumber)
            }
        }
        emit(contact)
    }.flowOn(Dispatchers.IO)

    override suspend fun addContact(contact: Contact) {
        val operations = ArrayList<ContentProviderOperation>()
        val rawContactUri = ContactsContract.RawContacts.CONTENT_URI

        operations.add(
            ContentProviderOperation.newInsert(rawContactUri)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Insert Name
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)
                .build()
        )

        // Insert Phone Number
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumbers[0])
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )

        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun editContact(contact: Contact) {
        val where = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val nameArgs = arrayOf(contact.contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        val phoneArgs = arrayOf(contact.contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

        // Update Name
        val nameValues = android.content.ContentValues().apply {
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)
        }
        context.contentResolver.update(ContactsContract.Data.CONTENT_URI, nameValues, where, nameArgs)

        // Update Phone Number
        val phoneValues = android.content.ContentValues().apply {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumbers[0])
        }
        context.contentResolver.update(ContactsContract.Data.CONTENT_URI, phoneValues, where, phoneArgs)
    }

    override suspend fun deleteContact(contactId: String) {
        try {
            val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getContacts(): Flow<List<Contact>> = flow {
        val contactsMap = mutableMapOf<String, Contact>()

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

                val contact = contactsMap.getOrPut(contactId) { Contact(contactId) }

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

        emit(contactsMap.values.toList()) // Emit the list of contacts
    }.flowOn(Dispatchers.IO) // Run on background thread

    override fun getPhoneNumberByRecipientId(recipientId: String): Flow<String?> = flow {
        val uri = Uri.parse("content://mms-sms/canonical-addresses")
        val projection = arrayOf("_id", "address")
        val selection = "_id = ?"
        val selectionArgs = arrayOf(recipientId)

        val phoneNumber = context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow("address"))
            } else null
        }
        emit(phoneNumber) // Emit either a phone number or null
    }.flowOn(Dispatchers.IO)

    override fun getContactNameByPhoneNumber(phoneNumber: String):  Flow<String?> = flow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            emit(null) // Return null if permission is not granted
            return@flow
        }

        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$phoneNumber%") // Using LIKE to handle different formats

        val contactName = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            } else null
        }

        emit(contactName)
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPhoneNumberAndContactNameByRecipientId(context: Context, recipientId: String): Flow<Pair<String?, String?>> =
        flow {
            if (recipientId.isBlank()) {
                emit(Pair(null, null))
                return@flow
            }
            emitAll(getPhoneNumberByRecipientId(recipientId)
                .flatMapLatest { phoneNumber ->
                    if (phoneNumber != null) {
                        getContactNameByPhoneNumber(phoneNumber)
                            .map { contactName -> Pair(contactName, phoneNumber) }
                    } else {
                        flowOf(Pair(null, null))
                    }
                })
        }.flowOn(Dispatchers.IO)

    private fun getPhoneNumberFromRecipientId(recipientIds: String): String {
        val uri = Uri.parse("content://mms-sms/canonical-addresses")
        val projection = arrayOf("_id", "address")
        val formattedIds = recipientIds.split(" ").joinToString(",") { it.trim() }
        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, "_id IN ($formattedIds)", null, null
        )
        val phoneNumbers = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                phoneNumbers.add(it.getString(it.getColumnIndexOrThrow("address")))
            }
        }
        return phoneNumbers.joinToString(", ")
    }
}

