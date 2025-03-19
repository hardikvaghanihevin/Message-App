package com.hardik.messageapp.presentation.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.R
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.viewmodel.BlockViewModel
import com.hardik.messageapp.presentation.viewmodel.ContactViewModel
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import com.hardik.messageapp.presentation.viewmodel.PinViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java

    private val messageViewModel: MessageViewModel by viewModels()
    private val conversationThreadViewModel: ConversationThreadViewModel by viewModels()
    private val contactViewModel: ContactViewModel by viewModels()
    private val blockViewModel: BlockViewModel by viewModels()
    private val pinViewModel: PinViewModel by viewModels()


    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            messageViewModel.fetchSmsMessages() // Update SMS list when a new message arrives
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        
        // Button Click to Request Default SMS Permission
        findViewById<Button>(R.id.btnRequestPermissions).setOnClickListener {
            messageViewModel.fetchSmsMessages()
            conversationThreadViewModel.fetchConversationThreads()
            blockViewModel.fetchBlockedNumbers()
            pinViewModel.fetchPinnedConversations()
            contactViewModel.fetchContacts()
            //blockViewModel.blockNumbers(listOf("+918866335532"))
        }

        if (isDefaultSmsApp(this)){

            lifecycleScope.launch {
                messageViewModel.messages.collect { messages ->
                    updateUI(messages)
                }
            }
            lifecycleScope.launch {
                conversationThreadViewModel.conversationThreads.collect {conversationThreads ->
                    conversationThreads.forEach { Log.i(TAG, "onCreate: $it") }
                    //if (it != null) Log.i(TAG, "onCreate: ThreadId: ${it.threadId}, Sender: ${it.sender}, Timestamp: ${it.timestamp}, Message: ${it.lastMessage}, ")

                }
            }
            lifecycleScope.launch {
                blockViewModel.blockedNumbers.collect { blockedNumbers ->
                    blockedNumbers.forEach { Log.i(TAG, "onCreate: $it") }
                    blockViewModel.unblockNumbers(listOf("+918866335532"))
                }
            }
            lifecycleScope.launch {
                pinViewModel.pinnedConversations.collect { pinnedConversations ->
                    pinnedConversations.forEach { Log.e(TAG, "onCreate: $it") }
                    pinViewModel.pinConversations(listOf(8L, 456L))
                }
            }
            lifecycleScope.launch{
                contactViewModel.contacts.collect { contacts ->
                    contacts.forEach {contact ->  Log.e(TAG, "onCreate: $contact")

                        contact.id.takeIf { contact.phoneNumber == "+918866335532" }
                            ?.let { contactViewModel.deleteContact(contactId = it) }
                    }
                    //contactViewModel.addContact(Contact(phoneNumber = "+918866335532", name = "John Doe"))
                }
            }
        }else{
            Log.e(TAG, "onCreate: do nothing", )
        }
    }



    private fun updateUI(messages: List<Message>) {
        messages.forEach { Log.d(TAG, "Id: ${it.id}, From: ${it.sender}, Message: ${it.messageBody}") }
    }

    override fun onResume() {
        super.onResume()
        if(!isDefaultSmsApp(this)){navigateToSetAsDefaultScreen()} //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }
    override fun onDestroy() {
        super.onDestroy()
    }



}

