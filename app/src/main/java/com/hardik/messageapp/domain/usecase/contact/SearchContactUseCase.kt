package com.hardik.messageapp.domain.usecase.contact

import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchContactUseCase @Inject constructor(private val repository: ContactRepository) {
    fun execute(phoneNumber: String): Flow<Contact?> = repository.searchContact(phoneNumber)
}
