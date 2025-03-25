package com.hardik.messageapp.presentation.ui.fragment

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
import com.hardik.messageapp.presentation.adapter.ConversationAdapter
import com.hardik.messageapp.presentation.helper.ConversationSwipeGestureHelper
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMessageBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //conversationViewmodel.fetchConversationThreads()
        messageViewModel.fetchSmsMessages()

        conversationAdapter = ConversationAdapter (
            swipeLeftBtn = { item -> swipeLeft(item) },
            swipeRightBtn = { item -> swipeRight(item) },
            onItemClick = { conversation -> //Log.i(TAG, "onViewCreated: $conversation")
                        Log.e(TAG, "onViewCreated: ${conversation}", )
                          },
            onSelectionChanged = { selectedConversations -> Log.e(TAG, "onViewCreated: ", ) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = conversationAdapter

        lifecycleScope.launch { conversationViewmodel.conversationThreads.collectLatest {
            conversationAdapter.submitList(it)
            it.forEach {
                //if (it.phoneNumber.contains("9428202279"))
                    //Log.e(TAG, "onViewCreated: ${it.phoneNumber} is selected ----> name ${it.displayName}")

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



    }

    private fun swipeLeft(conversationThread: ConversationThread?) { Log.i(TAG, "onCreate: swipeLeft:- $conversationThread") }
    private fun swipeRight(conversationThread: ConversationThread?) { Log.v(TAG, "onCreate: swipeRight:- $conversationThread") }



//    fun fetchContactInfo(context: Context, recipientId: String): Flow<Pair<String?, String?>> = flow {
//        val phoneNumber = getPhoneNumberByRecipientId(context, recipientId)
//        val contactName = phoneNumber?.let { getContactNameByPhoneNumber(context, it) }
//        emit(Pair(contactName, phoneNumber))
//    }.flowOn(Dispatchers.IO)
//
//    fun getPhoneNumberByRecipientId(context: Context, recipientId: String): String? {
//        val uri = Uri.parse("content://mms-sms/canonical-addresses")
//        val projection = arrayOf("_id", "address")
//        val selection = "_id = ?"
//        val selectionArgs = arrayOf(recipientId)
//
//        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
//            if (cursor.moveToFirst()) {
//                return cursor.getString(cursor.getColumnIndexOrThrow("address"))
//            }
//        }
//        return null
//    }
//
//
//    fun getContactNameByPhoneNumber(context: Context, phoneNumber: String): String? {
//        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
//        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
//
//        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
//            if (cursor.moveToFirst()) {
//                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
//            }
//        }
//        return null
//    }


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