package com.hardik.messageapp.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ActivityUnreadMessageBinding
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.util.AnimationViewHelper
import com.hardik.messageapp.presentation.util.firstUppercase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UnreadMessageActivity: BaseActivity() {
    private val TAG = BASE_TAG + UnreadMessageActivity::class.java.simpleName

    lateinit var binding: ActivityUnreadMessageBinding

    private lateinit var conversationAdapter: ConversationAdapter
    private var isAllSelected = false // Track selection state

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnreadMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarTitle.apply { text = text.toString().firstUppercase() }

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item ->  },
            swipeRightBtn = { item ->  },
            onItemClick = { conversation -> Log.e(TAG, "onCreate: ${conversation}", )},
            onSelectionChanged = { selectedConversations, listSize ->
                Log.e(TAG, "onCreate: ${selectedConversations.size} - $listSize", )
                unreadMessageViewModel.onSelectedChanged(selectedConversations)

                showBottomMenu(BottomMenu.BOTTOM_MENU_6_READ).takeIf { selectedConversations.isNotEmpty() } ?: hideBottomMenu()
                // Automatically update selection state & drawable
                isAllSelected = selectedConversations.size == listSize
                binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(
                    if (isAllSelected) R.drawable.ic_all_selected_item else R.drawable.ic_all_unselected_item,
                    0, 0, 0
                )
            }
        )

        binding.recyclerView.adapter = conversationAdapter

        // Add divider
        val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f, resources.displayMetrics).toInt()

        binding.recyclerView.apply {
            //setPadding(0, 0, 0, marginInPx)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            addItemDecoration(CustomDividerItemDecoration(this@UnreadMessageActivity, marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
        }

        lifecycleScope.launch {
            unreadMessageViewModel.unreadConversations.collectLatest { unreadConversationList ->
                Log.e(TAG, "onCreate: ${unreadConversationList.size}", )
                //val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                //conversationAdapter.submitList(archivedConversationList) { if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) } }
                conversationAdapter.setFullList(unreadConversationList) {
                    // After setting the new list, scroll back to the previous position
                    if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) }
                    conversationAdapter.filter.filter(binding.toolbarEdtSearch.text.toString())// todo: if any case data update/change so show on query based
                }
            }
        }

        //region Toolbar
        // show toolbarRLOptions & hide toolbarLLSearch
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarMore.setOnClickListener { showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch { unreadMessageViewModel.unreadAndToolbarCombinedState.collectLatest { (_, isShowSearch, selectedThreads) -> //todo->

            binding.toolbarLlSearch.apply {
                val visible = View.VISIBLE.takeIf { isShowSearch }?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarRlOption.apply {
                val visible = View.VISIBLE.takeUnless { isShowSearch }?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarTitle.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarSearch.apply {// todo: show always
                //val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                //AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarMore.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarTvSelectAll.apply {
                val visible = View.VISIBLE.takeUnless { selectedThreads.isEmpty() || isShowSearch} ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
            }

            binding.tvSelectedCountMessages1.apply {
                val visible = View.VISIBLE.takeUnless { selectedThreads.isEmpty() } ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.toolbarBack.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() || isShowSearch } ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }
        } }

        // select all conversation
        binding.toolbarTvSelectAll.setOnClickListener {
            isAllSelected = !isAllSelected // Toggle selection state

            if (isAllSelected) { conversationAdapter.selectAll() }
            else { conversationAdapter.unselectAll() }

            // Update drawable based on selection state
            binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(if (isAllSelected) R.drawable.ic_all_selected_item else R.drawable.ic_all_unselected_item, 0, 0, 0)
        }
        //endregion

        //endregion Toolbar

        //region bottom menu
        binding.includedNavViewBottomMenu6.navViewBottomLlRead.setOnClickListener {
            //Log.e(TAG, "onCreate: Delete",)
            val threadIds = unreadMessageViewModel.countSelectedConversationThreads.value.map { it.threadId }
            markAsReadByThreadIds(threadIds = threadIds) // read all selected threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        //endregion
    }


    private fun markAsReadByThreadIds(threadIds: List<Long>){
        unreadMessageViewModel.markAsReadConversationByThreadIds(threadIds = threadIds)

        lifecycleScope.launch {
            unreadMessageViewModel.isMarkAsReadConversationThread.collectLatest { isArchived: Boolean ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isArchived)
            }
        }
    }


    private fun showPopupMenu(view: View){

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = PopupMenu.VIEW_UNREAD_MESSAGE.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.mark_all_as_read) -> { popupMenuMarkAsRead() }

            }
        }

        // Show below and align to start
        popupMenu.show(showAbove = false, alignStart = false)
    }

    private fun popupMenuMarkAsRead() {
        val selectedThreads = unreadMessageViewModel.unreadConversations.value
        if (selectedThreads.isNotEmpty()) {
            val threadIds = selectedThreads.map { it.threadId }
            markAsReadByThreadIds(threadIds = threadIds) // read all threads
        }
    }



    override fun handleOnSoftBackPress(): Boolean {
        return if (isAllSelected || conversationAdapter.getSelectedItemCount() > 0 || isKeyboardVisible(window.decorView.rootView)) {
            conversationAdapter.unselectAll()
            isAllSelected = false
            binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_all_unselected_item, 0, 0, 0)
            hideBottomMenu()
            hideKeyboard(window.decorView.rootView)
            true // Consume back press
        } else {
            false // Allow normal back press
        }
    }

}