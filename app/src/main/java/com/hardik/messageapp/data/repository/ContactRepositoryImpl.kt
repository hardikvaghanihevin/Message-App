package com.hardik.messageapp.data.repository

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
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
        emit(contactList)
    }.flowOn(Dispatchers.IO)

    override fun searchContact(phoneNumber: String): Flow<Contact?> = flow {
        var contact: Contact? = null
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
        val nameArgs = arrayOf(contact.contactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        val phoneArgs = arrayOf(contact.contactId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

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
        Log.i(BASE_TAG, "getPhoneNumberByRecipientId: $phoneNumber")
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

        Log.v(BASE_TAG, "getContactNameByPhoneNumber: $contactName", )
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

