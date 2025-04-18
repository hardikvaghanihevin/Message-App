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
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.ActivityRecyclebinBinding
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.custom_view.showDeletePermanentConversationDialog
import com.hardik.messageapp.util.AnimationViewHelper
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import com.hardik.messageapp.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecyclebinActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + RecyclebinActivity::class.java.simpleName

    lateinit var binding: ActivityRecyclebinBinding

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener
    private var isAllSelected = false // Track selection state

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclebinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item ->  },
            swipeRightBtn = { item ->  },
            onItemClick = { conversation -> Log.e(TAG, "onCreate: ${conversation}", )},
            onSelectionChanged = { selectedConversations, listSize ->
                Log.e(TAG, "onCreate: ${selectedConversations.size} - $listSize", )
                recyclebinViewModel.onSelectedChanged(selectedConversations)

                showBottomMenu(BottomMenu.BOTTOM_MENU_3_RESTORE_BLOCK_DELETE).takeIf { selectedConversations.isNotEmpty() } ?: hideBottomMenu()
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
            //addItemDecoration(CustomDividerItemDecoration(this@RecyclebinActivity, marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
            addItemDecoration(CustomDividerItemDecoration(this@RecyclebinActivity, marginStartRes = R.dimen.item_recycle_decoration_dp_start, marginEndRes = R.dimen.item_recycle_decoration_dp_end, marginTop = 0, marginBottom = 0))

        }

        lifecycleScope.launch {
            recyclebinViewModel.recyclebinConversations.collectLatest { binConversationList ->
                //Log.e(TAG, "onCreate: ${binConversationList}", )
                //val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                conversationAdapter.setFullList(binConversationList) {
                    // After setting the new list, scroll back to the previous position
                    if (currentPosition != RecyclerView.NO_POSITION) { binding.recyclerView.scrollToPosition(currentPosition) }
                    //conversationAdapter.filter.filter(binding.toolbarEdtSearch.text.toString())// todo: if any case data update/change so show on query based
                }
            }
        }

        //region Toolbar
        // show toolbarRLOptions & hide toolbarLLSearch
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarMore.setOnClickListener { showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch { recyclebinViewModel.recyclebinAndToolbarCombinedState.collectLatest { (_, toolbarState, selectedThreads) ->
            val isCollapsed = toolbarState.second in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, )
            val isExpanded = toolbarState.second in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)

            val isShowSearch = toolbarState.first

            //binding.toolbarLlSearch.apply {
                //val visible = View.VISIBLE.takeIf { isShowSearch }?: View.GONE
                //AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L) }

            binding.toolbarRlOption.apply {
                //val visible = View.VISIBLE.takeUnless { isShowSearch }?: View.GONE
                //AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarTitle.apply {
                val visible = View.VISIBLE.takeIf { isCollapsed && selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarSearch.apply {// todo: show always
                //val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                //AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
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
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { recyclebinViewModel.onToolbarStateChanged(newState) } }

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
        /** Restore */
        binding.includedNavViewBottomMenu3.navViewBottomLlRestore.setOnClickListener {
            val senders = recyclebinViewModel.countSelectedConversationThreads.value.map { it.sender }
            restoreConversations(senders = senders) // restore all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        /** Block */
        binding.includedNavViewBottomMenu3.navViewBottomLlBlock.setOnClickListener {
            val blockThreads = recyclebinViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
            blockConversation(blockThreads = blockThreads) // block all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        /** Delete */
        binding.includedNavViewBottomMenu3.navViewBottomLlDelete.setOnClickListener {
            val senders = recyclebinViewModel.countSelectedConversationThreads.value.map { it.sender }
            deleteRecyclebinConversation(senders) { // delete (permanent) all selected bin threads
                conversationAdapter.unselectAll()// todo: unselectAll after work is done
            }
        }
        //endregion

    }

    private fun restoreConversations(senders: List<String>) {
        recyclebinViewModel.restoreConversations(senders)

        lifecycleScope.launch {
            recyclebinViewModel.isRestoreConversationThread.collectLatest { isRestored ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isRestored)
            }
        }
    }
    private fun blockConversation(blockThreads: List<BlockThreadEntity>) {
        recyclebinViewModel.blockRecyclebinConversationByThreadIds(blockThreads)

        lifecycleScope.launch {
            recyclebinViewModel.isBlockRecyclebinConversationThread.collectLatest { isBlocked ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isBlocked)
            }
        }

    }
    private fun deleteRecyclebinConversation(senders: List<String>, callBack: () -> Unit) {
        val isAll = senders.size == recyclebinViewModel.recyclebinConversations.value.size
        showDeletePermanentConversationDialog(this, senders.size, isAll) {isPositive ->
            
            if (isPositive){ recyclebinViewModel.deleteRecyclebinConversationBySenders(senders) }

            lifecycleScope.launch {
                recyclebinViewModel.isDeleteRecyclebinConversationThread.collectLatest { isPermanentDelete ->
                    conversationViewModel.fetchConversationThreads(needToUpdate = isPermanentDelete)
                    callBack()
                }
            }
        }

    }


    private fun showPopupMenu(view: View){

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = PopupMenu.RECYCLE_BIN.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.restore_all) -> { popupMenuRestoreAll() }
                getString(R.string.block_all) -> { popupMenuBlockAll() }
                getString(R.string.delete_all) -> { popupMenuDeleteAll() }
            }
        }

        // Show below and align to start
        popupMenu.show(showAbove = false, alignStart = false)
    }

    private fun popupMenuRestoreAll() {
        val selectedThreads = recyclebinViewModel.recyclebinConversations.value
        if (selectedThreads.isNotEmpty()) {
            val senders = selectedThreads.map { it.sender }
            restoreConversations(senders = senders) // restore all bin threads
        }
    }
    private fun popupMenuBlockAll() {
        val blockThreads: List<BlockThreadEntity> = recyclebinViewModel.recyclebinConversations.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
        if (blockThreads.isNotEmpty()){
            blockConversation(blockThreads = blockThreads) // block all bin threads
        }

    }
    private fun popupMenuDeleteAll() {
        val selectedThreads = recyclebinViewModel.recyclebinConversations.value
        if (selectedThreads.isNotEmpty()) {
            val senders = selectedThreads.map { it.sender }
            deleteRecyclebinConversation(senders = senders) { // delete all bin threads
                conversationAdapter.unselectAll()
            }
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}