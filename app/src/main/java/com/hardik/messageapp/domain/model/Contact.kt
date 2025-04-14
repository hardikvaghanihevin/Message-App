package com.hardik.messageapp.domain.model

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson

data class Contact(
    val contactId: Int = -1,
    var displayName: String = "",
    var phoneNumbers: MutableList<String> = mutableListOf(),
    var photoUri: String? = null,
    var normalizeNumber: String,
    val isHeader: Boolean = false
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.contactId == newItem.contactId
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.contactId == newItem.contactId &&
                        oldItem.displayName == newItem.displayName &&
                        oldItem.phoneNumbers == newItem.phoneNumbers &&
                        oldItem.photoUri == newItem.photoUri &&
                        oldItem.normalizeNumber == newItem.normalizeNumber &&
                        oldItem.isHeader == newItem.isHeader
            }
        }
        fun List<Contact>.toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }
    }
}