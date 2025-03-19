package com.hardik.messageapp.data.repository

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(private val context: Context) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> = flow {
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
                contactList.add(Contact(id, name, number))
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
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                .build()
        )

        // Insert Phone Number
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
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
        val nameArgs = arrayOf(contact.id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        val phoneArgs = arrayOf(contact.id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

        // Update Name
        val nameValues = android.content.ContentValues().apply {
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
        }
        context.contentResolver.update(ContactsContract.Data.CONTENT_URI, nameValues, where, nameArgs)

        // Update Phone Number
        val phoneValues = android.content.ContentValues().apply {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
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
}

