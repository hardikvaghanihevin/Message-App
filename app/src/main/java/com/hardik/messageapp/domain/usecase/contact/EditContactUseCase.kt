package com.hardik.messageapp.domain.usecase.contact

import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import javax.inject.Inject

class EditContactUseCase @Inject constructor(private val repository: ContactRepository) {
    suspend fun execute(contact: Contact) = repository.editContact(contact)
}

