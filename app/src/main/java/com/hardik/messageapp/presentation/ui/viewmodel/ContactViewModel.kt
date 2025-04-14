package com.hardik.messageapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.usecase.contact.AddContactUseCase
import com.hardik.messageapp.domain.usecase.contact.DeleteContactUseCase
import com.hardik.messageapp.domain.usecase.contact.EditContactUseCase
import com.hardik.messageapp.domain.usecase.contact.GetContactsUseCase
import com.hardik.messageapp.domain.usecase.contact.SearchContactUseCase
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    private val TAG = BASE_TAG + ContactViewModel::class.java.simpleName

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredContacts = MutableStateFlow<List<Contact>>(emptyList())
    val filteredContacts: StateFlow<List<Contact>> = _filteredContacts.asStateFlow()

    private val _searchedContact = MutableStateFlow<Contact?>(null)
    val searchedContact: StateFlow<Contact?> = _searchedContact.asStateFlow()

    init {
        viewModelScope.launch {
            combine(contacts, searchQuery) { allContacts, query ->
                if (query.isBlank()) {
                    groupContactsWithHeaders(allContacts)
                } else {
                    allContacts.filter {
                        it.displayName.contains(query, ignoreCase = true) ||
                                it.normalizeNumber.contains(query) ||
                                it.phoneNumbers.any { number -> number.contains(query, ignoreCase = true) }
                    }
                }
            }.collectLatest { filtered ->
                _filteredContacts.value = filtered
            }
        }
    }

    fun fetchContacts(wantToNumberWiseContactList: Boolean = false) {
        viewModelScope.launch {
            getContactsUseCase.execute(wantToNumberWiseContactList).collect { _contacts.value = it }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

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

    private fun groupContactsWithHeaders(contacts: List<Contact>): List<Contact> {
        val groupedList = mutableListOf<Contact>()
        val grouped = contacts.groupBy { it.displayName.firstOrNull()?.uppercaseChar() ?: '#' }

        grouped.toSortedMap().forEach { (initial, contactGroup) ->
            groupedList.add(
                Contact(
                    contactId = -initial.code,
                    displayName = initial.toString(),
                    normalizeNumber = "",
                    photoUri = null,
                    isHeader = true
                )
            )
            groupedList.addAll(contactGroup)
        }

        return groupedList
    }
}
