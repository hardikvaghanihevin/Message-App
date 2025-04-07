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
import com.hardik.messageapp.helper.Constants
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.util.AnimationViewHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecyclebinActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + RecyclebinActivity::class.java.simpleName

    lateinit var binding: ActivityRecyclebinBinding

    private lateinit var conversationAdapter: ConversationAdapter
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
            setPadding(0, 0, 0, marginInPx)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            addItemDecoration(CustomDividerItemDecoration(this@RecyclebinActivity, marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
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
                    conversationAdapter.filter.filter(binding.toolbarEdtSearch.text.toString())// todo: if any case data update/change so show on query based
                }
            }
        }

        //region Toolbar
        // show toolbarRLOptions & hide toolbarLLSearch
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarMore.setOnClickListener { showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch { recyclebinViewModel.recyclebinAndToolbarCombinedState.collectLatest { (_, isShowSearch, selectedThreads) ->

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
        binding.includedNavViewBottomMenu3.navViewBottomLlRestore.setOnClickListener {
            //Log.e(TAG, "onCreate: Unarchive",)
            val threadIds = recyclebinViewModel.countSelectedConversationThreads.value.map { it.threadId }
            restoreConversation(threadIds = threadIds) // restore all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        binding.includedNavViewBottomMenu3.navViewBottomLlBlock.setOnClickListener {
            Log.e(TAG, "onCreate: Block",)
            val blockThreads = recyclebinViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber) }
            blockConversation(blockThreads = blockThreads) // block all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        binding.includedNavViewBottomMenu3.navViewBottomLlDelete.setOnClickListener {
            //Log.e(TAG, "onCreate: Delete",)
            val threadIds = recyclebinViewModel.countSelectedConversationThreads.value.map { it.threadId }
            deleteRecyclebinConversation(threadIds) // delete (permanent) all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        //endregion

    }

    private fun restoreConversation(threadIds: List<Long>) {
        recyclebinViewModel.restoreConversation(threadIds)

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
    private fun deleteRecyclebinConversation(threadIds: List<Long>) {
        recyclebinViewModel.deleteRecyclebinConversationByThreadIds(threadIds)

        lifecycleScope.launch {
            recyclebinViewModel.isDeleteRecyclebinConversationThread.collectLatest { isPermanentDelete ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isPermanentDelete)
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
            val threadIds = selectedThreads.map { it.threadId }
            restoreConversation(threadIds = threadIds) // restore all bin threads
        }
    }
    private fun popupMenuBlockAll() {
        val blockThreads: List<BlockThreadEntity> = recyclebinViewModel.recyclebinConversations.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber) }
        if (blockThreads.isNotEmpty()){
            blockConversation(blockThreads = blockThreads) // block all bin threads
        }

    }
    private fun popupMenuDeleteAll() {
        val selectedThreads = recyclebinViewModel.recyclebinConversations.value
        if (selectedThreads.isNotEmpty()) {
            val threadIds = selectedThreads.map { it.threadId }
            deleteRecyclebinConversation(threadIds = threadIds) // delete all bin threads
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}