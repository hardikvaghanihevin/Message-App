package com.hardik.messageapp.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hardik.messageapp.databinding.ActivitySearchBinding
import com.hardik.messageapp.domain.model.SearchItem
import com.hardik.messageapp.presentation.adapter.SearchListAdapter
import com.hardik.messageapp.presentation.ui.viewmodel.SearchViewModel
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.util.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.util.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.util.Constants.KEY_THREAD_ID
import com.hardik.messageapp.util.SmsDefaultAppHelper
import com.hardik.messageapp.util.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : BaseActivity() {
    private val TAG = BASE_TAG + SearchActivity::class.java.simpleName

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: SearchListAdapter
    private val viewModel: SearchViewModel by viewModels() // ViewModel for managing search data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeSearchResults()
        setupSearchBar()
        setupToolbar()
    }


    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchListAdapter(onItemClick = { searchItem: SearchItem ->
            val intent = Intent(this, ChatActivity::class.java)

            if (searchItem is SearchItem.MessageItem) {
                intent.putExtra(KEY_THREAD_ID, searchItem.message.threadId) // as Int
                intent.putExtra(KEY_MESSAGE_ID, searchItem.message.id) // as Int
                intent.putExtra(KEY_NORMALIZE_NUMBER, searchItem.message.normalizeNumber) // as String
                intent.putExtra(KEY_SEARCH_QUERY, viewModel.searchQuery.value) // as String

                Log.e(TAG, "setupRecyclerView: MatchFund:${searchItem.message.matchFoundCount} - ThreadId:${searchItem.message.threadId} - MessageId:${searchItem.message.id} - ContactNumber:${searchItem.message.normalizeNumber} - SearchQuery:${viewModel.searchQuery.value}")

            }
               // Log.e(TAG, "setupRecyclerView: $searchItem", )
            if (searchItem is SearchItem.ContactItem){
                intent.putExtra(KEY_THREAD_ID, -1) // as Int
                intent.putExtra(KEY_MESSAGE_ID, -1) // as Int
                intent.putExtra(KEY_NORMALIZE_NUMBER, searchItem.contact.normalizeNumber) // as String
                intent.putExtra(KEY_SEARCH_QUERY, viewModel.searchQuery.value) // as String

                Log.i(TAG, "setupRecyclerView: ContactId:${searchItem.contact.contactId} - ContactNumber:${searchItem.contact.normalizeNumber} - SearchQuery:${viewModel.searchQuery.value}")
            }

            startActivity(intent)

        })
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
    }

    private fun observeSearchResults() {
        lifecycleScope.launch {
            viewModel.searchResults
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) // Ensures collection only in active states
                .collectLatest { results -> searchAdapter.submitList(results) }
        }
    }

    private fun setupSearchBar() {
        binding.searchEditText.requestFocus()

        binding.searchEditText.doAfterTextChanged { text ->
            lifecycleScope.launch {
                delay(300) // Small debounce to reduce frequent calls
                viewModel.updateSearchQuery(text.toString())
            }
            lifecycleScope.launch {
                viewModel.searchQuery.collectLatest { searchQuery ->
                    searchAdapter.updateSearchQuery(newQuery = searchQuery)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
            navigateToSetAsDefaultScreen()
        } //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }

    override fun onDestroy() {
        super.onDestroy() }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}