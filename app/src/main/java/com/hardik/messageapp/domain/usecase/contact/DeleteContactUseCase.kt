package com.hardik.messageapp.domain.usecase.contact

import com.hardik.messageapp.domain.repository.ContactRepository
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(private val repository: ContactRepository) {
    suspend fun execute(contactId: String) = repository.deleteContact(contactId = contactId)
}