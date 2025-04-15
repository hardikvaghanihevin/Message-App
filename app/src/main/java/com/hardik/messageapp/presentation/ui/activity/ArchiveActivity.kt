package com.hardik.messageapp.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.ActivityArchiveBinding
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.util.AnimationViewHelper
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveActivity: BaseActivity() {
    private val TAG = BASE_TAG + ArchiveActivity::class.java.simpleName

    lateinit var binding: ActivityArchiveBinding

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener
    private var isAllSelected = false // Track selection state

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item ->  },
            swipeRightBtn = { item ->  },
            onItemClick = { conversation -> Log.e(TAG, "onCreate: ${conversation}", )},
            onSelectionChanged = { selectedConversations, listSize ->
                Log.e(TAG, "onCreate: ${selectedConversations.size} - $listSize", )
                archiveViewModel.onSelectedChanged(selectedConversations)

                showBottomMenu(BottomMenu.BOTTOM_MENU_2_UNARCHIVE_BLOCK_DELETE).takeIf { selectedConversations.isNotEmpty() } ?: hideBottomMenu()
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
            setPadding(0, marginInPx/2, 0, marginInPx*2)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            //addItemDecoration(CustomDividerItemDecoration(this@ArchiveActivity, marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
            addItemDecoration(CustomDividerItemDecoration(this@ArchiveActivity, marginStartRes = R.dimen.item_recycle_decoration_dp_start, marginEndRes = R.dimen.item_recycle_decoration_dp_end, marginTop = 0, marginBottom = 0))

        }

        lifecycleScope.launch {
            archiveViewModel.archivedConversations.collectLatest { archivedConversationList ->
                Log.e(TAG, "onCreate: ${archivedConversationList}", )
                //val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                //conversationAdapter.submitList(archivedConversationList) { if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) } }
                conversationAdapter.setFullList(archivedConversationList) {
                    // After setting the new list, scroll back to the previous position
                    if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) }
                    conversationAdapter.filter.filter(binding.toolbarEdtSearch.text.toString())// todo: if any case data update/change so show on query based
                }
            }
        }

        //region Toolbar
        // show toolbarRLOptions & hide toolbarLLSearch
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarCancel.setOnClickListener {
            archiveViewModel.onToolbarStateChanged(false)
            deactivateSearchBar()
        }

        //show toolbarLLSearch & hide toolbarRLOptions
        binding.toolbarSearch.setOnClickListener {
            archiveViewModel.onToolbarStateChanged(true)
            activeSearchBar()
        }
        binding.toolbarMore.setOnClickListener { showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        binding.toolbarEdtSearch.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()
            conversationAdapter.filter.filter(query)
        }

        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch { archiveViewModel.archiveAndToolbarCombinedState.collectLatest { (_, toolbarState, selectedThreads) ->
            val isCollapsed = toolbarState.second in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, )
            val isExpanded = toolbarState.second in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)

            val isShowSearch = toolbarState.first

            if (selectedThreads.isEmpty()) {
                archiveViewModel.onToolbarStateChanged(false)
                deactivateSearchBar()
            }// todo: When no item is select from list after selection

            binding.toolbarLlSearch.apply {
                val visible = View.VISIBLE.takeIf { isShowSearch }?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarRlOption.apply {
                val visible = View.VISIBLE.takeUnless { isShowSearch }?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarTitle.apply {
                val visible = View.VISIBLE.takeIf { isCollapsed && selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)

            }

            binding.toolbarSearch.apply {// todo: show always
                val visible = View.VISIBLE.takeUnless { selectedThreads.isEmpty() } ?: View.GONE
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarMore.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarTvSelectAll.apply {
                //val visible = View.VISIBLE.takeUnless { selectedThreads.isEmpty() || isShowSearch} ?: View.GONE
                val visible = View.VISIBLE.takeUnless { selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.expandedContent.apply {
                val visible = View.VISIBLE.takeIf { isExpanded && selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.tvSelectedCountMessages.apply {// todo: for collapsed count
                val visible = View.VISIBLE.takeIf { selectedThreads.isNotEmpty() && isExpanded } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.tvSelectedCountMessages1.apply {// todo: for toolbar count
                val visible = View.VISIBLE.takeIf { selectedThreads.isNotEmpty() && isCollapsed } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.toolbarBack.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }
        } }

        //region toolbar management
        /** Initialize the manager */
        toolbarStateManager = CollapsingToolbarStateManager(binding.appbarLayout)

        /** Create an anonymous implementation of the listener */
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { archiveViewModel.onToolbarStateChanged(newState) } }

        /** Register the anonymous listener */
        toolbarStateManager.addOnStateChangeListener(toolbarStateChangeListener)

        /** select all conversation */
        binding.toolbarTvSelectAll.setOnClickListener {
            isAllSelected = !isAllSelected // Toggle selection state

            if (isAllSelected) { conversationAdapter.selectAll() }
            else { conversationAdapter.unselectAll() }

            // Update drawable based on selection state
            binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(if (isAllSelected) R.drawable.ic_all_selected_item else R.drawable.ic_all_unselected_item, 0, 0, 0)
        }
        //endregion toolbar management

        //endregion Toolbar

        //region bottom menu
        binding.includedNavViewBottomMenu2.navViewBottomLlUnarchive.setOnClickListener {
            //Log.e(TAG, "onCreate: Unarchive",)
            val threadIds = archiveViewModel.countSelectedConversationThreads.value.map { it.threadId }
            unarchiveConversation(threadIds = threadIds) // unarchive all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        binding.includedNavViewBottomMenu2.navViewBottomLlBlock.setOnClickListener {
            Log.e(TAG, "onCreate: Block",)
            val blockThreads = archiveViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }

            blockArchiveConversation(blockThreads) // block all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        binding.includedNavViewBottomMenu2.navViewBottomLlDelete.setOnClickListener {
            //Log.e(TAG, "onCreate: Delete",)
            val threadIds = archiveViewModel.countSelectedConversationThreads.value.map { it.threadId }
            deleteArchiveConversation(threadIds) // delete all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        //endregion bottom menu

    }

    private fun activeSearchBar() {
        binding.toolbarLlSearch.visibility = View.VISIBLE
        binding.toolbarEdtSearch.apply {
            requestFocus()
            setText("")
            showKeyboard(this)
        }
    }

    private fun deactivateSearchBar() {
        binding.toolbarLlSearch.visibility = View.GONE
        binding.toolbarEdtSearch.apply {
            clearFocus()
            setText("")
            hideKeyboard(this)
        }
        conversationAdapter.filter.filter(null) // Reset to original data
    }



    private fun unarchiveConversation(threadIds: List<Long>){
        archiveViewModel.unarchiveConversationByThreadIds(threadIds)

        lifecycleScope.launch {
            archiveViewModel.isUnarchivedConversationThread.collectLatest { isArchived: Boolean ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isArchived)
            }
        }
    }

    private fun blockArchiveConversation(blockThreads: List<BlockThreadEntity>){
        archiveViewModel.blockArchiveConversationByThreads(blockThreads)
        archiveViewModel.unarchiveConversationByThreadIds(threadIds = blockThreads.map { it.threadId })// todo: once add in block list now unarchived that list

        lifecycleScope.launch {
            archiveViewModel.isBlockArchiveConversationThread.collectLatest { isBlocked: Boolean ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isBlocked)
            }
        }

    }
    private fun deleteArchiveConversation(threadIds: List<Long>){
        archiveViewModel.deleteArchiveConversationByThreadIds(threadIds)
        archiveViewModel.unarchiveConversationByThreadIds(threadIds = threadIds)// todo: once add in bin list now unarchived that list

        lifecycleScope.launch {
            archiveViewModel.isDeleteArchiveConversationThread.collectLatest { isDeleted: Boolean ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isDeleted)
            }
        }
    }


    private fun showPopupMenu(view: View){

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = PopupMenu.ARCHIVE.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.unarchive_all) -> { popupMenuUnarchiveAll() }
                getString(R.string.delete_all) -> { popupMenuDeleteAll() }
            }
        }

        //popupMenu.show() // Show the custom popup
        // Show below and align to start
        popupMenu.show(showAbove = false, alignStart = false)
    }

    private fun popupMenuUnarchiveAll() {
        val selectedThreads = archiveViewModel.archivedConversations.value
        if (selectedThreads.isNotEmpty()) {
            val threadIds = selectedThreads.map { it.threadId }
            unarchiveConversation(threadIds = threadIds) // unarchive all archived threads
        }
    }

    private fun popupMenuDeleteAll() {
        val selectedThreads = archiveViewModel.archivedConversations.value
        if (selectedThreads.isNotEmpty()) {
            val threadIds = selectedThreads.map { it.threadId }
            deleteArchiveConversation(threadIds = threadIds) // delete all archived threads
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