package com.hardik.messageapp.domain.usecase.contact

import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(private val repository: ContactRepository) {
    fun execute(): Flow<List<Contact>> = repository.getContacts()
}
