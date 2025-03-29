package com.hardik.messageapp.presentation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.hardik.messageapp.databinding.FragmentMessageBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.ContactRepository
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.helper.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.helper.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.helper.Constants.KEY_THREAD_ID
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.helper.ConversationSwipeGestureHelper
import com.hardik.messageapp.presentation.ui.activity.ChatActivity
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class MessageFragment : Fragment() {
    private val TAG = BASE_TAG + MessageFragment::class.java.simpleName
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val conversationViewmodel: ConversationThreadViewModel by activityViewModels()
    private val messageViewModel: MessageViewModel by activityViewModels()
    private lateinit var conversationAdapter: ConversationAdapter

    @Inject
    lateinit var contactRepository: ContactRepository

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //EventBus.getDefault().register(this) // ✅ Register EventBus
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMessageBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item -> swipeLeft(item) },
            swipeRightBtn = { item -> swipeRight(item) },
            onItemClick = { conversation ->
                Log.e(TAG, "onViewCreated: clicked number:${conversation.normalizeNumber}" )

                //messageViewModel.getMessagesByThreadId(conversation.threadId) //call before to to chat screen
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra(KEY_THREAD_ID, conversation.threadId) // as Int
                intent.putExtra(KEY_MESSAGE_ID, conversation.id) // as Int
                intent.putExtra(KEY_NORMALIZE_NUMBER, conversation.normalizeNumber) // as String
                intent.putExtra(KEY_SEARCH_QUERY, "") // as String

                requireContext().startActivity(intent)

                          },
            onSelectionChanged = { selectedConversations -> Log.e(TAG, "onViewCreated: ${selectedConversations.size}", ) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = false }
        binding.recyclerView.adapter = conversationAdapter

        lifecycleScope.launch { conversationViewmodel.conversationThreads.collectLatest { conversationAdapter.submitList(it) } }

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



    }

    private fun swipeLeft(conversationThread: ConversationThread?) { Log.i(TAG, "onCreate: swipeLeft:- $conversationThread") }
    private fun swipeRight(conversationThread: ConversationThread?) { Log.v(TAG, "onCreate: swipeRight:- $conversationThread") }

    override fun onDestroy() {
        super.onDestroy()
        //EventBus.getDefault().unregister(this) // ✅ Unregister EventBus
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}