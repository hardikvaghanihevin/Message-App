package com.hardik.messageapp.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.hardik.messageapp.R
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.ActivityBlockBinding
import com.hardik.messageapp.databinding.NavViewTopBinding
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.adapter.ViewPagerBlockAdapter
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.custom_view.TopNavManager
import com.hardik.messageapp.util.AnimationViewHelper
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import com.hardik.messageapp.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + BlockActivity::class.java.simpleName

    lateinit var binding: ActivityBlockBinding

    private lateinit var viewPager: ViewPager2
    lateinit var navBinding: NavViewTopBinding
    lateinit var viewPagerBlockAdapter: ViewPagerBlockAdapter

    lateinit var conversationAdapter: ConversationAdapter// use in blockMessageFragment

    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener
    private var isAllSelected = false // Track selection state

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        viewPagerBlockAdapter = ViewPagerBlockAdapter(this)
        viewPager.adapter = viewPagerBlockAdapter
        viewPager.isUserInputEnabled = false // Disable swipe navigation

        // Bind custom top navigation view
        navBinding = NavViewTopBinding.bind(binding.includedNavViewTop.root)

        TopNavManager.setup(
            binding = navBinding,
            onBlockNumberClick = { viewPager.setCurrentItem(0, false) },// Load BlockNumber Fragment
            onBlockMessageClick = { viewPager.setCurrentItem(1, false) }// Load BlockMessage Fragment
        )

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                blockViewModel.onTabStateChanged(position)
            }
        })
        //region Toolbar
        // show toolbarRLOptions & hide toolbarLLSearch
        binding.toolbarBack.setOnClickListener { if (!handleOnSoftBackPress()){ onBackPressedDispatcher.onBackPressed() } }

        binding.toolbarMore.setOnClickListener { showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management
        lifecycleScope.launch { blockViewModel.blockAndToolbarCombinedState.collectLatest { (tabPosition, toolbarState, blockAndSelectedThreads) ->
            val isCollapsed = toolbarState in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, )
            val isExpanded = toolbarState in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)

            val selectedThreads = blockAndSelectedThreads.second

            binding.toolbarRlOption.apply {}

            binding.toolbarTitle.apply {
                val visible = View.VISIBLE.takeIf { isCollapsed && (tabPosition == 0 || selectedThreads.isEmpty()) } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarMore.apply {
                val visible = View.VISIBLE.takeIf { tabPosition != 0 && selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }
            binding.toolbarTvSelectAll.apply {
                val visible = View.VISIBLE.takeUnless { tabPosition == 0 || selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.expandedContent.apply {
                val visible = View.VISIBLE.takeIf { isExpanded && (tabPosition == 0 || selectedThreads.isEmpty()) } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.tvSelectedCountMessages.apply {// todo: for collapsed count
                val visible = View.VISIBLE.takeIf { tabPosition != 0 && selectedThreads.isNotEmpty() && isExpanded } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.tvSelectedCountMessages1.apply {// todo: for toolbar count
                val visible = View.VISIBLE.takeIf { tabPosition != 0 && selectedThreads.isNotEmpty() && isCollapsed } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.toolbarBack.apply {
                val visible = View.VISIBLE.takeIf { tabPosition == 0 || selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                AnimationViewHelper.toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }
        } }

        //region toolbar management
        /** Initialize the manager */
        toolbarStateManager = CollapsingToolbarStateManager(binding.appbarLayout)

        /** Create an anonymous implementation of the listener */
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { blockViewModel.onToolbarStateChanged(newState) } }

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

    }


    fun unblockConversation(blockThreads: List<BlockThreadEntity>) {//unblockConversation
        blockViewModel.unblockConversations(blockThreads)

        lifecycleScope.launch {
            blockViewModel.isUnblockConversationThread.collectLatest { isUnblocked ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isUnblocked)
            }
        }

    }
    fun deleteBlockConversation(threadIds: List<Long>) {//deleteBlockConversation
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


    fun popupMenuUnblockAll() {
        //val blockThreads: List<Long> = blockViewModel.blockedConversations.value.map { it.threadId }
        val blockThreads: List<BlockThreadEntity> = blockViewModel.blockedConversations.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
        if (blockThreads.isNotEmpty()){
            unblockConversation(blockThreads = blockThreads) // unblock all bin threads
        }

    }
    fun popupMenuDeleteAll() {
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