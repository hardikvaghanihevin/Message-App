package com.hardik.messageapp.presentation.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.FragmentMessageBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.helper.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.helper.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.helper.Constants.KEY_THREAD_ID
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.custom_view.BottomMenu
import com.hardik.messageapp.presentation.custom_view.CustomDividerItemDecoration
import com.hardik.messageapp.presentation.helper.ConversationSwipeGestureHelper
import com.hardik.messageapp.presentation.ui.activity.ChatActivity
import com.hardik.messageapp.presentation.ui.activity.MainActivity
import com.hardik.messageapp.presentation.ui.activity.SearchActivity
import com.hardik.messageapp.presentation.util.AnimationViewHelper.toggleViewVisibilityWithAnimation
import com.hardik.messageapp.presentation.util.CollapsingToolbarStateManager
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
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

    //private val conversationViewmodel: ConversationThreadViewModel by activityViewModels()
    //private val messageViewModel: MessageViewModel by activityViewModels()
    private lateinit var conversationViewmodel: ConversationThreadViewModel
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var conversationAdapter: ConversationAdapter

    private lateinit var toolbarStateManager: CollapsingToolbarStateManager
    private lateinit var toolbarStateChangeListener: CollapsingToolbarStateManager.OnStateChangeListener
    private var isAllSelected = false // Track selection state

    @Inject
    lateinit var contactRepository: ContactRepository

    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
        //EventBus.getDefault().register(this) // ✅ Register EventBus

        conversationViewmodel = (activity as MainActivity).conversationViewModel
        messageViewModel = (activity as MainActivity).messageViewModel
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = FragmentMessageBinding.inflate(inflater,container, false)
//        return binding.root
//    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "$TAG - onViewCreated: ", )

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
            onSelectionChanged = { selectedConversations, listSize -> conversationViewmodel.onSelectedChanged(selectedConversations)

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
            setPadding(0, 0, 0, marginInPx)  // Add padding programmatically
            clipToPadding = false            // Allow scrolling into padding
            overScrollMode = View.OVER_SCROLL_NEVER // Disable overscroll effect
            addItemDecoration(CustomDividerItemDecoration(requireContext(), marginStart = marginInPx * 2, marginEnd = marginInPx /2, marginTop = 0, marginBottom = 0))
        }

        lifecycleScope.launch {
            conversationViewmodel.filteredConversationThreads.collectLatest { newList ->
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
            editAction = { position -> swipeLeft(conversationAdapter.currentList[position]) },
            deleteAction = { position -> swipeRight(conversationAdapter.currentList[position]) }
        )

        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        // Attach scroll listener separately
        binding.recyclerView.addOnScrollListener(swipeHelper.getScrollListener())

        binding.toolbarSearch.setOnClickListener { startActivity(Intent(requireContext(), SearchActivity::class.java)) }
        binding.toolbarMore.setOnClickListener { (activity as MainActivity).showPopupMenu(it) }  // Show custom popup menu on click of more button in toolbar

        //region toolbar selected item count management & Toolbar selected count management countUnreadGeneralAndPrivateConversationThreads
        lifecycleScope.launch { conversationViewmodel.cvThreadAndToolbarCombinedState.collectLatest { (selectedThreads, toolbarState, unReadGeneralPrivateThreadsPair) ->

            val isCollapsed = toolbarState in listOf(CollapsingToolbarStateManager.STATE_COLLAPSED, CollapsingToolbarStateManager.STATE_INTERMEDIATE)
            val isExpanded = toolbarState in listOf(CollapsingToolbarStateManager.STATE_EXPANDED, )

            binding.toolbarTitle.apply {
                val visible = View.GONE.takeUnless { isCollapsed } ?: View.VISIBLE.takeIf { selectedThreads.isEmpty() } ?: View.GONE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }
            binding.toolbarTitleIndicator.apply {
                val visible = View.GONE.takeUnless { isCollapsed } ?: View.VISIBLE.takeIf { selectedThreads.isEmpty() && unReadGeneralPrivateThreadsPair.first.isNotEmpty() } ?: View.GONE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible, duration = 0L)
            }

            binding.toolbarTvSelectAll.apply {
                val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
            }

            binding.expandedContent.apply {
                val visible = View.GONE.takeUnless { selectedThreads.isEmpty() } ?: View.VISIBLE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
            }

            binding.tvSelectedCountMessages.apply {// todo: for collapsed count
                val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE.takeIf { isExpanded } ?: View.GONE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"
            }
            binding.tvSelectedCountMessages1.apply {// todo: for toolbar count
                val visible = View.GONE.takeIf { selectedThreads.isEmpty() } ?: View.VISIBLE.takeIf { isCollapsed } ?: View.GONE
                toggleViewVisibilityWithAnimation(view = this@apply, isVisible = visible)
                text = "${selectedThreads.size} ${getString(R.string.selected)}"

            }
        }}
        //endregion

        lifecycleScope.launch { messageViewModel.countMessages.collectLatest { messages ->
            Log.i(TAG, "$TAG -onViewCreated: ${messages.size}", )
            binding.unreadMessages.apply {
                text = "${messages.size} ${getString(R.string.unread_messages)}"
            }
        } }

        //region toolbar management
        // Initialize the manager
        toolbarStateManager = CollapsingToolbarStateManager(binding.appbarLayout)

        // Create an anonymous implementation of the listener
        toolbarStateChangeListener = object : CollapsingToolbarStateManager.OnStateChangeListener { override fun onStateChanged(newState: Int) { conversationViewmodel.onToolbarStateChanged(newState) } }

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
        //endregion

        //region bottom menu
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlArchive?.setOnClickListener { Log.e(TAG, "onViewCreated: archive",)
            val threadIds = conversationViewmodel.countSelectedConversationThreads.value.map { it.threadId }
            (activity as MainActivity).archiveConversation(threadIds)
            conversationAdapter.unselectAll()// todo: unselectAll after work is done

        }
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlDelete?.setOnClickListener { Log.e(TAG, "onViewCreated: delete",)
            val threadIds = conversationViewmodel.countSelectedConversationThreads.value.map { it.threadId }
            (activity as MainActivity).deleteConversation(threadIds)
            conversationAdapter.unselectAll()// todo: unselectAll after work is done
        }
        mainBinding?.includedNavViewBottomMenu1?.navViewBottomLlMore?.setOnClickListener { Log.e(TAG, "onViewCreated: more",) }
        //endregion

    }

    private fun swipeLeft(conversationThread: ConversationThread?) { Log.i(TAG, "onCreate: swipeLeft:- $conversationThread") }
    private fun swipeRight(conversationThread: ConversationThread?) { Log.v(TAG, "onCreate: swipeRight:- $conversationThread") }

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