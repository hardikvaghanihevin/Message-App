package com.hardik.messageapp.domain.model

import androidx.recyclerview.widget.DiffUtil

sealed class SearchItem {
    data class Header(val title: String) : SearchItem()
    data class ContactItem(val contact: Contact) : SearchItem()
    data class MessageItem(val message: Message) : SearchItem()


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchItem>() {
            override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
                return when {
                    oldItem is MessageItem && newItem is MessageItem -> oldItem.message.id == newItem.message.id
                    oldItem is ContactItem && newItem is ContactItem -> oldItem.contact.contactId == newItem.contact.contactId
                    oldItem is Header && newItem is Header -> oldItem.title == newItem.title
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
                return when {
                    oldItem is MessageItem && newItem is MessageItem -> {
                        //oldItem.message == newItem.message // Compare Message data
                        Message.DIFF_CALLBACK.areContentsTheSame(oldItem.message, newItem.message)
                    }
                    oldItem is ContactItem && newItem is ContactItem -> {
                        //oldItem.contact == newItem.contact // Compare Contact data
                        Contact.DIFF_CALLBACK.areContentsTheSame(oldItem.contact, newItem.contact)
                    }
                    oldItem is Header && newItem is Header -> {
                        oldItem.title == newItem.title // Compare Header title
                    }
                    else -> oldItem == newItem // Fallback for other cases (shouldn't happen)
                }
            }
        }
    }
}