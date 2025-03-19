package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.usecase.contact.AddContactUseCase
import com.hardik.messageapp.domain.usecase.contact.DeleteContactUseCase
import com.hardik.messageapp.domain.usecase.contact.EditContactUseCase
import com.hardik.messageapp.domain.usecase.contact.GetContactsUseCase
import com.hardik.messageapp.domain.usecase.contact.SearchContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactUseCase: SearchContactUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val editContactUseCase: EditContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    fun fetchContacts() {
        viewModelScope.launch {
            getContactsUseCase.execute().collect { _contacts.value = it }
        }
    }

    private val _searchedContact = MutableStateFlow<Contact?>(null)
    val searchedContact: StateFlow<Contact?> = _searchedContact.asStateFlow()

    fun searchContact(phoneNumber: String) {
        viewModelScope.launch {
            searchContactUseCase.execute(phoneNumber).collect { _searchedContact.value = it }
        }
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            addContactUseCase.execute(contact)
            fetchContacts()
        }
    }

    fun editContact(contact: Contact) {
        viewModelScope.launch {
            editContactUseCase.execute(contact)
            fetchContacts()
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            deleteContactUseCase.execute(contactId = contactId)
            fetchContacts()
        }
    }
}