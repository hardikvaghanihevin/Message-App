package com.hardik.messageapp.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.databinding.ActivityNewConversationBinding
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.presentation.adapter.ContactAdapter
import com.hardik.messageapp.util.Constants
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.util.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.util.extractNumber
import com.hardik.messageapp.util.getBestMatchedNumber
import com.hardik.messageapp.util.removeCountryCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewConversationActivity : BaseActivity() {
    private val TAG = BASE_TAG + NewConversationActivity::class.java.simpleName

    lateinit var binding: ActivityNewConversationBinding

    private lateinit var contactAdapter: ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeContacts()
        setUpBackArrow()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        contactViewModel.fetchContacts()
        contactAdapter = ContactAdapter(onItemClick = {
            val number: String = getBestMatchedNumber(it.phoneNumbers, contactViewModel.searchQuery.value) ?: it.normalizeNumber
            //Log.e(TAG, "Contact clicked: ${number.removeCountryCode(phoneNumberUtil)}")
            gotoChatScreen(number.removeCountryCode(phoneNumberUtil))
        })
        binding.recyclerView.adapter = contactAdapter
    }

    private fun observeContacts() {
        lifecycleScope.launch {
            contactViewModel.filteredContacts.collectLatest { filteredList ->
                contactAdapter.updateSearchQuery(contactViewModel.searchQuery.value)
                contactAdapter.submitList(filteredList)
            }
        }
    }

    private fun setUpBackArrow() {
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }
    }

    private fun setupSearchBar() {
        binding.toolbarEdtSearch.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine(true)
            requestFocus()

            doAfterTextChanged { text ->
                lifecycleScope.launch {
                    delay(300) // debounce to avoid frequent updates
                    contactViewModel.updateSearchQuery(text.toString())
                }
            }

            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    val query = text.toString().trim()
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    }
                    true
                } else {
                    false
                }
            }
        }

        binding.toolbarCancel.setOnClickListener {
            deactivateSearchBar()
        }
    }

    private fun performSearch(query: String) {
        val firstContact: Contact? = contactViewModel.filteredContacts.value.firstOrNull()
        val conversationNumberOrName = extractNumber(contact = firstContact, query = query, phoneNumberUtil)
        val conversationNumber = if (conversationNumberOrName.isDigitsOnly()) conversationNumberOrName else ""

        //Log.e(TAG, "performSearch: $query - ${firstContact?.phoneNumbers} | $conversationNumberOrName ----->$conversationNumber")
        //todo : if null or empty then stay here else go to chatScreen by number
        gotoChatScreen(conversationNumber).takeIf { conversationNumber.isNotEmpty() }
    }

    private fun gotoChatScreen(conversationNumber: String) {
        //Log.i(TAG, "gotoChatScreen: $conversationNumber")
        val intent = Intent(this@NewConversationActivity, ChatActivity::class.java)

        intent.putExtra(Constants.KEY_THREAD_ID, -1) // as Int
        intent.putExtra(Constants.KEY_MESSAGE_ID, -1) // as Int
        intent.putExtra(Constants.KEY_NORMALIZE_NUMBER, conversationNumber) // as String
        intent.putExtra(Constants.KEY_SEARCH_QUERY, "") // as String

        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        if (!isDefaultSmsApp(this)) {
            navigateToSetAsDefaultScreen()
        } //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }

    private fun activeSearchBar() {
        binding.toolbarEdtSearch.apply {
            requestFocus()
            setText("")
            showKeyboard(this)
        }
    }

    private fun deactivateSearchBar() {
        binding.toolbarEdtSearch.apply {
            clearFocus()
            setText("")
            hideKeyboard(this)
        }
    }
    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}