package com.hardik.messageapp.domain.model

data class Contact(
    val contactId: String = "",
    var displayName: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
    var phoneNumbers: MutableList<String> = mutableListOf(),
    var emails: MutableList<String> = mutableListOf(),
    var photoUri: String? = null
)