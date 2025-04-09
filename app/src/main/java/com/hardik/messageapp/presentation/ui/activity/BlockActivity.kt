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
import com.hardik.messageapp.databinding.ActivityBlockBinding
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
class BlockActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + BlockActivity::class.java.simpleName

    lateinit var binding: ActivityBlockBinding

    private lateinit var conversationAdapter: ConversationAdapter
    private var isAllSelected = false // Track selection state

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item ->  },
            swipeRightBtn = { item ->  },
            onItemClick = { conversation -> Log.e(TAG, "onCreate: ${conversation}", )},
            onSelectionChanged = { selectedConversations, listSize ->
                Log.e(TAG, "onCreate: ${selectedConversations.size} - $listSize", )
                blockViewModel.onSelectedChanged(selectedConversations)

                showBottomMenu(BottomMenu.BOTTOM_MENU_4_DELETE_UNBLOCK).takeIf { selectedConversations.isNotEmpty() } ?: hideBottomMenu()
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
            addItemDecoration(CustomDividerItemDecoration(this@BlockActivity, marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
        }

        lifecycleScope.launch {
            blockViewModel.blockedConversations.collectLatest { binConversationList ->
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
        lifecycleScope.launch { blockViewModel.blockAndToolbarCombinedState.collectLatest { (_, isShowSearch, selectedThreads) ->

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
        binding.includedNavViewBottomMenu4.navViewBottomLlDelete.setOnClickListener {
            //Log.e(TAG, "onCreate: Delete",)
            val threadIds = blockViewModel.countSelectedConversationThreads.value.map { it.threadId }
            deleteBlockConversation(threadIds) // delete (permanent) all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        binding.includedNavViewBottomMenu4.navViewBottomLlUnblock.setOnClickListener {
            Log.e(TAG, "onCreate: Block",)
            //val blockThreads: List<Long> = blockViewModel.blockedConversations.value.map { it.threadId }
            val blockThreads: List<BlockThreadEntity> = blockViewModel.blockedConversations.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
            unblockConversation(blockThreads = blockThreads) // block all selected bin threads

            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        //endregion

    }


    private fun unblockConversation(blockThreads: List<BlockThreadEntity>) {//unblockConversation
        blockViewModel.unblockConversations(blockThreads)

        lifecycleScope.launch {
            blockViewModel.isUnblockConversationThread.collectLatest { isBlocked ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isBlocked)
            }
        }

    }
    private fun deleteBlockConversation(threadIds: List<Long>) {//deleteBlockConversation
        blockViewModel.deleteBlockConversationByThreadIds(threadIds)

        lifecycleScope.launch {
            blockViewModel.isDeleteBlockConversationThread.collectLatest { isPermanentDelete ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isPermanentDelete)
            }
        }
    }


    private fun showPopupMenu(view: View){

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = PopupMenu.BLOCK.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.unblock_all) -> { popupMenuUnblockAll() }
                getString(R.string.delete_all) -> { popupMenuDeleteAll() }
            }
        }

        // Show below and align to start
        popupMenu.show(showAbove = false, alignStart = false)
    }


    private fun popupMenuUnblockAll() {
        //val blockThreads: List<Long> = blockViewModel.blockedConversations.value.map { it.threadId }
        val blockThreads: List<BlockThreadEntity> = blockViewModel.blockedConversations.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
        if (blockThreads.isNotEmpty()){
            unblockConversation(blockThreads = blockThreads) // unblock all bin threads
        }

    }
    private fun popupMenuDeleteAll() {
        val selectedThreads = blockViewModel.blockedConversations.value
        if (selectedThreads.isNotEmpty()) {
            val threadIds = selectedThreads.map { it.threadId }
            deleteBlockConversation(threadIds = threadIds) // delete all bin threads
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}