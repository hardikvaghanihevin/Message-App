package com.hardik.messageapp.data.repository

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.removeCountryCode
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

class ContactRepositoryImpl @Inject constructor(
    private val context: Context,
    private val phoneNumberUtil: PhoneNumberUtil
) : ContactRepository {
    private val TAG = BASE_TAG + ContactRepositoryImpl::class.java.simpleName

    override fun searchContact(phoneNumber: String): Flow<Contact?> = flow {
        val normalizedInput = phoneNumber.removeCountryCode(phoneNumberUtil).filter { it.isDigit() }

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection = null // Optional: you can apply filtering here too
        val selectionArgs = null

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        var matchedContact: Contact? = null

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getInt(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                val photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                val normalizedStored = number.removeCountryCode(phoneNumberUtil).filter { it.isDigit() }

                if (normalizedStored == normalizedInput) {
                    matchedContact = Contact(
                        contactId = contactId,
                        displayName = name,
                        photoUri = photoUri,
                        phoneNumbers = mutableListOf(number),
                        normalizeNumber = normalizedStored,
                        isHeader = false
                    )
                    break
                }
            }
        }

        emit(matchedContact)
    }.flowOn(Dispatchers.IO)

    override suspend fun getContactByNumber(inputNumber: String): Contact? = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(inputNumber))

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI,
            ContactsContract.PhoneLookup.NUMBER
        )

        val cursor = contentResolver.query(lookupUri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val contactId = it.getLong(0)
                val displayName = it.getString(1)
                val photoUri = it.getString(2)
                val phoneNumber = it.getString(3)

                return@withContext Contact(
                    contactId = contactId.toInt(),
                    displayName = displayName,
                    photoUri = photoUri,
                    normalizeNumber = phoneNumber
                )
            }
        }
        null
    }


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

    private var startTimeFC by Delegates.notNull<Long>()
    private var endTimeFC by Delegates.notNull<Long>()
    override fun getContacts(wantToNumberWiseContactList: Boolean): Flow<List<Contact>> = channelFlow {
        val contactMap = mutableMapOf<Int, MutableList<String>>() // contactId -> phoneNumbers
        val contactInfoMap = mutableMapOf<Int, Triple<String, String?, String>>() // contactId -> (name, photoUri, anyNumber)

        val emitList = mutableListOf<Contact>()
        val emitThreshold = 100
        var counter = 0

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

        startTimeFC = System.currentTimeMillis()

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val imageIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getInt(idIndex)
                val displayName = it.getString(nameIndex) ?: "Unknown"
                val imageUri = it.getString(imageIndex)
                val rawNumber = it.getString(numberIndex)
                //val cleanedNumber = rawNumber.replace(" ", "").replace("-", "")
                val cleanedNumber = rawNumber.removeCountryCode(phoneNumberUtil).trim().filter { it.isDigit() }

                contactMap.getOrPut(contactId) { mutableListOf() }.apply {
                    if (!contains(cleanedNumber)) add(cleanedNumber)
                }

                if (!contactInfoMap.containsKey(contactId)) {
                    contactInfoMap[contactId] = Triple(displayName, imageUri, cleanedNumber)
                }
            }
        }

        // Now prepare final list based on wantToNumberWiseContactList
        contactMap.forEach { (contactId, numbers) ->
            val (name, photoUri, firstNumber) = contactInfoMap[contactId] ?: return@forEach

            if (wantToNumberWiseContactList) {
                numbers.forEach { number ->
                    val normalized = number.removeCountryCode(phoneNumberUtil).trim().filter { it.isDigit() }

                    emitList.add(
                        Contact(
                            contactId = contactId,
                            displayName = name,
                            phoneNumbers = numbers,
                            photoUri = photoUri,
                            normalizeNumber = normalized
                        )
                    )

                    counter++
                    if (counter % emitThreshold == 0) {
                        send(emitList.toList())
                        emitList.clear()
                    }
                }
            } else {
                // Just one object with all numbers and normalizeNumber of first
                val normalized = firstNumber.removeCountryCode(phoneNumberUtil)
                emitList.add(
                    Contact(
                        contactId = contactId,
                        displayName = name,
                        phoneNumbers = numbers,
                        photoUri = photoUri,
                        normalizeNumber = normalized
                    )
                )

                counter++
                if (counter % emitThreshold == 0) {
                    send(emitList.toList())
                    emitList.clear()
                }
            }
        }

        if (emitList.isNotEmpty()) {
            send(emitList.toList())
        }

        endTimeFC = System.currentTimeMillis()
        Log.i(TAG, "$TAG - Total execution time Contact: ${endTimeFC - startTimeFC}ms")
    }.flowOn(Dispatchers.IO)


}

