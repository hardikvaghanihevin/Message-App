package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    fun searchContact(phoneNumber: String): Flow<Contact?>
    suspend fun addContact(contact: Contact)
    suspend fun editContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
}