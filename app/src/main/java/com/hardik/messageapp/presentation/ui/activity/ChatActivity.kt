package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hardik.messageapp.databinding.ActivityChatBinding
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.model.Message.Companion.toJson
import com.hardik.messageapp.domain.repository.FindThreadIdByNormalizeNumber
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.helper.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.helper.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.helper.Constants.KEY_THREAD_ID
import com.hardik.messageapp.helper.LogUtil
import com.hardik.messageapp.helper.SmsDefaultAppHelper
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.adapter.ChatAdapter
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private val TAG = BASE_TAG + ChatActivity::class.java.simpleName

    private lateinit var binding: ActivityChatBinding

    private val conversationViewModel: ConversationThreadViewModel by viewModels()
    private val messageViewModel: MessageViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Register EventBus when Activity is created
        EventBus.getDefault().register(this)

        messageViewModel.fetchSmsMessages()

        var threadId = intent.getLongExtra(KEY_THREAD_ID, -1) // as Int
        val messageId = intent.getLongExtra(KEY_MESSAGE_ID, -1) // as Int
        val normalizeNumber = intent.getStringExtra(KEY_NORMALIZE_NUMBER) // as String
        val searchQuery = intent.getStringExtra(KEY_SEARCH_QUERY) // as String

        if (normalizeNumber.isNullOrEmpty()) { // If normalizeNumber is null or empty, return from the activity
            finish() ; return }


        threadId = if (threadId == -1L){
            val findThreadId = object : FindThreadIdByNormalizeNumber {}.findThreadIdByNormalizeNumber(this, normalizeNumber)
            findThreadId
        }else { threadId }

        Log.i(TAG, "onCreate: ThreadId:$threadId | MessageId:$messageId | NormalizeNumber:$normalizeNumber | SearchQuery:${searchQuery.orEmpty()}")
        messageViewModel.getMessagesByThreadId(threadId)

        chatAdapter = ChatAdapter(
            searchQuery = searchQuery ?: "",
            onItemClick = { message ->
                // Handle click on message
            },
            onSelectionChanged = { selectedMessages ->
                // Handle selection of messages
            }
        )

        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true /* Ensures the last item is at the bottom */ }
        binding.recyclerViewMessages.adapter = chatAdapter
        // Scroll to the last item

        lifecycleScope.launch {
            messageViewModel.messagesOfThread.collectLatest { messages ->
                chatAdapter.submitList(messages)
                LogUtil.d(TAG,"onCreate: ${messages.toJson()}")
                binding.recyclerViewMessages.postDelayed({
                    val positionByMessageId = messages.indexOfFirst { it.id == messageId }
                    val lastPosition = messages.size -1
                    val scrollPosition = lastPosition.takeIf { messageId == -1L } ?: positionByMessageId
                    binding.recyclerViewMessages.scrollToPosition(scrollPosition)
                }, 100)
            }
        }


        binding.etMessageInput.requestFocus()

    }



    override fun onResume() {
        super.onResume()
        if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
            navigateToSetAsDefaultScreen()
        } //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }
    override fun onDestroy() {
        super.onDestroy()
        // ✅ Unregister EventBus when Activity is destroyed
        EventBus.getDefault().unregister(this)
    }

    // ✅ Handle SMS Event (Auto Updates UI)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmsReceived(event: Message) {
        Log.d(TAG, "EventBus -> New SMS from ${event.sender}: ${event.messageBody}")

        // ✅ Fetch updated messages from ViewModel
        conversationViewModel.fetchConversationThreads(needToUpdate = true)

        // Show Toast (Optional)
        //Toast.makeText(this, "New SMS from ${event.sender}", Toast.LENGTH_SHORT).show()
    }

}