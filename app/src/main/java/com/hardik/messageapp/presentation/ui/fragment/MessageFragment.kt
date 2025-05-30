package com.hardik.messageapp.presentation.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.FragmentMessageBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.ui.activity.ChatActivity
import com.hardik.messageapp.presentation.ui.activity.MainActivity
import com.hardik.messageapp.presentation.ui.activity.SearchActivity
import com.hardik.messageapp.presentation.ui.activity.UnreadMessageActivity
import com.hardik.messageapp.presentation.ui.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.ui.viewmodel.MessageViewModel
import com.hardik.messageapp.util.AnimationViewHelper.toggleViewVisibilityWithAnimation
import com.hardik.messageapp.util.CollapsingToolbarStateManager
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.util.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.util.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.util.Constants.KEY_THREAD_ID
import com.hardik.messageapp.util.ConversationSwipeGestureHelper
import com.hardik.messageapp.util.SwipeAction
import com.hardik.messageapp.util.evaluateSelectionGetHomeBottomMenu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class MessageFragment : BaseFragment(R.layout.fragment_message) {
    private val TAG = BASE_TAG + MessageFragment::class.java.simpleName
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val mainBinding get() = (activity as? MainActivity)?.binding

    private lateinit var conversationViewModel: ConversationThreadViewModel
    private lateinit var messageViewModel: MessageViewModel
    lateinit var conversationAdapter: ConversationAdapter

    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener
    private var isAllSelected = false // Track selection state

    @Inject
    lateinit var contactRepository: ContactRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
        //EventBus.getDefault().register(this) // ✅ Register EventBus

        conversationViewModel = (activity as MainActivity).conversationViewModel
        messageViewModel = (activity as MainActivity).messageViewModel
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = FragmentMessageBinding.inflate(inflater,container, false)
//        return binding.root
//    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Log.e(TAG, "$TAG - onViewCreated: ", )

        _binding = FragmentMessageBinding.bind(view)

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item -> swipeLeft(item) },
            swipeRightBtn = { item -> swipeRight(item) },
            onItemClick = { conversation ->
                //Log.e(TAG, "onViewCreated: clicked number:${conversation.normalizeNumber}" )

                //messageViewModel.getMessagesByThreadId(conversation.threadId) //call before to to chat screen
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra(KEY_THREAD_ID, conversation.threadId) // as Int
                intent.putExtra(KEY_MESSAGE_ID, conversation.id) // as Int
                intent.putExtra(KEY_NORMALIZE_NUMBER, conversation.normalizeNumber) // as String
                intent.putExtra(KEY_SEARCH_QUERY, "") // as String

                requireActivity().startActivity(intent)

                          },
            onSelectionChanged = { selectedConversations, listSize ->
                conversationViewModel.onSelectedChanged(selectedConversations)

                (activity as MainActivity).showBottomMenu(BottomMenu.BOTTOM_MENU_1_ARCHIVE_DELETE_MORE).takeIf { selectedConversations.isNotEmpty() } ?: (activity as MainActivity).hideBottomMenu()
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
        val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34f, requireContext().resources.displayMetrics).toInt()

        binding.recyclerView.apply {
            setPadding(0, marginInPx/2, 0, marginInPx*2)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            addItemDecoration(CustomDividerItemDecoration(requireContext(), marginStartRes = R.dimen.item_recycle_decoration_dp_start, marginEndRes = R.dimen.item_recycle_decoration_dp_end, marginTop = 0, marginBottom = 0))
        }

        lifecycleScope.launch {
            conversationViewModel.filteredConversationThreads.collectLatest { newList ->
                //val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                conversationAdapter.submitList(newList) {
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        binding.recyclerView.scrollToPosition(currentPosition)
                    }
                }
            }
        }

        // Attach Swipe Gesture
        val swipeHelper = ConversationSwipeGestureHelper(requireContext(),
            conversationAdapter,
            leftAction = { position -> swipeLeft(conversationAdapter.currentList[position]) },
            rightAction = { position -> swipeRight(conversationAdapter.currentList[position]) }
        )

        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // Attach scroll listener separately
        binding.recyclerView.addOnScrollListener(swipeHelper.getScrollListener())

        binding.viewButton.setOnClickListener { startActivity(Intent(requireContext(), UnreadMessageActivity::class.java)) }
        binding.toolbarSearch.setOnClickListener { startActivity(Intent(requireContext(), SearchActivity::class.java)) }
        binding.toolbarMore.setOnClickListener { (activity as MainActivity).showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management countUnreadGeneralAndPrivateConversationThreads
        lifecycleScope.launch { conversationViewModel.cvThreadAndToolbarCombinedState.collectLatest { (selectedThreads, toolbarState, unreadCombined) ->

            val unreadGeneralMap = unreadCombined.first.first   // Map<Long, Long>
            val unreadGeneralCount = unreadCombined.first.second // Int
            //val unreadPrivateMap = unreadCombined.second.first  // Map<Long, Long>
            //val unreadPrivateCount = unreadCombined.second.second // Int

            val isCollapsed = toolbarState in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, )
            val isExpanded = toolbarState in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)

            val totalUnreadCount = unreadGeneralMap.values.sum()

            binding.toolbarExpandedTitle.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() && isExpanded && totalUnreadCount == 0L } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 10L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }
            binding.toolbarExpandedTvUnreadMessages.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() && isExpanded && totalUnreadCount != 0L } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 10L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "$totalUnreadCount ${getString(R.string.unread_messages)}"
            }
            binding.viewButton.apply {
                val visible = View.VISIBLE.takeIf { selectedThreads.isEmpty() && isExpanded && totalUnreadCount != 0L } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 10L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarTitle.apply {
                val visible = View.GONE.takeUnless { isCollapsed } ?: View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)

            }
            binding.toolbarTitleIndicator.apply {
                val visible = View.GONE.takeUnless { isCollapsed } ?: View.VISIBLE.takeIf { selectedThreads.isEmpty() && unreadGeneralCount > 0 } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarMore.apply {
                val visible = View.GONE.takeUnless { selectedThreads.isEmpty() } ?: View.VISIBLE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.toolbarTvSelectAll.apply {
                val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.expandedContent.apply {
                val visible = View.GONE.takeUnless { selectedThreads.isEmpty() } ?: View.VISIBLE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
            }

            binding.tvSelectedCountMessages.apply {// todo: for collapsed count
                //val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE.takeIf { isExpanded } ?: View.GONE
                val visible = View.VISIBLE.takeIf { selectedThreads.isNotEmpty() && isExpanded } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }

            binding.tvSelectedCountMessages1.apply {// todo: for toolbar count
                //val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE.takeIf { isCollapsed } ?: View.GONE
                val visible = View.VISIBLE.takeIf { selectedThreads.isNotEmpty() && isCollapsed } ?: View.GONE
                val duration = if (visible == View.VISIBLE) 300L else 100L
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = duration)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }
        }}
        //endregion toolbar selected item count management & Toolbar selected count management countUnreadGeneralAndPrivateConversationThreads

        //region toolbar management
        // Initialize the manager
        toolbarStateManager = CollapsingToolbarStateManager(binding.appbarLayout)

        // Create an anonymous implementation of the listener
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { conversationViewModel.onToolbarStateChanged(newState) } }

        // Register the anonymous listener
        toolbarStateManager.addOnStateChangeListener(toolbarStateChangeListener)

        // select all conversation
        binding.toolbarTvSelectAll.setOnClickListener {
            isAllSelected = !isAllSelected // Toggle selection state

            if (isAllSelected) { conversationAdapter.selectAll() }
            else { conversationAdapter.unselectAll() }

            // Update drawable based on selection state
            binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(if (isAllSelected) R.drawable.ic_all_selected_item else R.drawable.ic_all_unselected_item, 0, 0, 0)
        }
        //endregion toolbar management

        //region bottom menu
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlArchive?.setOnClickListener {
            val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            (activity as MainActivity).archiveConversation(threadIds) // from bottom
            conversationAdapter.unselectAll()// todo: unselectAll after work is done

        }
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlDelete?.setOnClickListener {
            //val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            (activity as MainActivity).deleteConversation(conversationViewModel.countSelectedConversationThreads.value) { // from bottom
                conversationAdapter.unselectAll() }// todo: unselectAll after work is done

        }
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlMore?.setOnClickListener {
            val resultMenu = evaluateSelectionGetHomeBottomMenu(conversationViewModel.countSelectedConversationThreads.value)
            (activity as MainActivity).showPopupMenuBottom(it, selectedMenu = resultMenu)
        }
        //endregion bottom menu

    }

    private fun swipeLeft(conversationThread: ConversationThread?) {
        Log.i(TAG, "onCreate: swipeLeft:- $conversationThread")
        if (conversationThread != null) {
            // Get current actions
            val whichFragmentIsLive: Pair<Boolean, Fragment?> = (activity as MainActivity).isCurrentFragmentGeneral()
            when (SwipeAction.getAction(SwipeAction.LEFT)) {
                SwipeAction.Action.NONE -> {}
                SwipeAction.Action.ARCHIVE -> { (activity as MainActivity).archiveConversation(listOf(conversationThread.threadId)) }
                SwipeAction.Action.DELETE -> { (activity as MainActivity).deleteConversation(listOf(conversationThread)) { } }
                SwipeAction.Action.CALL -> { }
                SwipeAction.Action.BLOCK -> { (activity as MainActivity).blockConversation(listOf(conversationThread), whichFragmentIsLive) }
                SwipeAction.Action.MARK_AS_READ -> { (activity as MainActivity).markAsReadConversation(listOf(conversationThread.threadId), whichFragmentIsLive)  }
                SwipeAction.Action.MARK_AS_UNREAD -> { (activity as MainActivity).markAsUnreadConversation(listOf(conversationThread.threadId), whichFragmentIsLive) }
            }
        }
    }
    private fun swipeRight(conversationThread: ConversationThread?) {
        Log.v(TAG, "onCreate: swipeRight:- $conversationThread")
        if (conversationThread != null) {
            // Get current actions
            val whichFragmentIsLive: Pair<Boolean, Fragment?> = (activity as MainActivity).isCurrentFragmentGeneral()
            when (SwipeAction.getAction(SwipeAction.RIGHT)) {
                SwipeAction.Action.NONE -> {}
                SwipeAction.Action.ARCHIVE -> { (activity as MainActivity).archiveConversation(listOf(conversationThread.threadId)) }
                SwipeAction.Action.DELETE -> { (activity as MainActivity).deleteConversation(listOf(conversationThread)) { } }
                SwipeAction.Action.CALL -> { }
                SwipeAction.Action.BLOCK -> { (activity as MainActivity).blockConversation(listOf(conversationThread), whichFragmentIsLive) }
                SwipeAction.Action.MARK_AS_READ -> { (activity as MainActivity).markAsReadConversation(listOf(conversationThread.threadId), whichFragmentIsLive)  }
                SwipeAction.Action.MARK_AS_UNREAD -> { (activity as MainActivity).markAsUnreadConversation(listOf(conversationThread.threadId), whichFragmentIsLive) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        messageViewModel.fetchSmsMessages(needToUpdate = true)
    }
    override fun onDestroy() {
        super.onDestroy()
        //EventBus.getDefault().unregister(this) // ✅ Unregister EventBus
    }

    override fun handleSoftBackPress(): Boolean {
        return if (isAllSelected || conversationAdapter.getSelectedItemCount() > 0) {
            conversationAdapter.unselectAll()
            isAllSelected = false
            binding.toolbarTvSelectAll.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_all_unselected_item, 0, 0, 0)
            (activity as MainActivity).hideBottomMenu()
            true // Consume back press
        } else {
            false // Allow normal back press
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}