package com.hardik.messageapp.domain.repository

import android.content.Context
import com.hardik.messageapp.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts1(): Flow<List<Contact>>
    fun getContacts(): Flow<List<Contact>>
    fun searchContact(phoneNumber: String): Flow<Contact?>
    suspend fun addContact(contact: Contact)
    suspend fun editContact(contact: Contact)
    suspend fun deleteContact(contactId: String)

    fun getPhoneNumberByRecipientId(recipientId: String): Flow<String?> //todo: base on 'recipientId' can get 'PhoneNumber'
    fun getContactNameByPhoneNumber(phoneNumber: String): Flow<String?> //todo: base on 'phoneNumber' can get 'ContactName'

    fun getPhoneNumberAndContactNameByRecipientId(context: Context, recipientId: String): Flow<Pair<String?, String?>>
}