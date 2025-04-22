package com.hardik.messageapp.presentation.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.databinding.ActivityChatBinding
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.domain.repository.FindThreadIdByNormalizeNumber
import com.hardik.messageapp.presentation.adapter.ChatAdapter
import com.hardik.messageapp.presentation.custom_view.LastItemBottomPaddingDecoration
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.Constants.KEY_MESSAGE_ID
import com.hardik.messageapp.util.Constants.KEY_NORMALIZE_NUMBER
import com.hardik.messageapp.util.Constants.KEY_SEARCH_QUERY
import com.hardik.messageapp.util.Constants.KEY_THREAD_ID
import com.hardik.messageapp.util.SmsDefaultAppHelper
import com.hardik.messageapp.util.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class ChatActivity : BaseActivity() {
    private val TAG = BASE_TAG + ChatActivity::class.java.simpleName

    private lateinit var binding: ActivityChatBinding

//    private val conversationViewModel: ConversationThreadViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Register EventBus when Activity is created
        EventBus.getDefault().register(this)

        var threadId = intent.getLongExtra(KEY_THREAD_ID, -1) // as Int
        val messageId = intent.getLongExtra(KEY_MESSAGE_ID, -1) // as Int
        val normalizeNumber = intent.getStringExtra(KEY_NORMALIZE_NUMBER) // as String
        val searchQuery = intent.getStringExtra(KEY_SEARCH_QUERY) // as String

//        if (normalizeNumber.isNullOrEmpty() && threadId == -1L) { // If normalizeNumber is null or empty, return from the activity
//            finish() ; return
//        }

        threadId = if(threadId != -1L) {
            threadId
        } else if (!normalizeNumber.isNullOrEmpty()){
            val findThreadId = object : FindThreadIdByNormalizeNumber {}.findThreadIdByNormalizeNumber(this, normalizeNumber)
            findThreadId
        }else { threadId }

        Log.i(TAG, "onCreate: ThreadId:$threadId | MessageId:$messageId | NormalizeNumber:$normalizeNumber | SearchQuery:${searchQuery.orEmpty()}")
        messageViewModel.getMessagesByThreadId(threadId)

        chatAdapter = ChatAdapter(
            searchQuery = searchQuery ?: "",
            onItemClick = { message ->
                // Handle click on message
                Log.e(TAG, "onCreate: $message", )
                //messageViewModel.deleteSms(listOf(message.id))
            },
            onSelectionChanged = { selectedMessages ->
                // Handle selection of messages
            }
        )

        binding.recyclerViewMessages.adapter = chatAdapter
        // Scroll to the last item

        val bottomPaddingInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, this.resources.displayMetrics).toInt()

        binding.recyclerViewMessages.addItemDecoration(LastItemBottomPaddingDecoration(bottomPaddingInPx))

//        val conversation: ConversationThread = conversationViewModel.conversationThreads.value.filter { it.threadId == threadId }.first()
//        binding.toolbarTitle.text = conversation.displayName
        normalizeNumber?.let { contactViewModel.searchContact(it) }
        lifecycleScope.launch {
            contactViewModel.searchedContact.collectLatest {
                Log.i(TAG, "onCreate: ->>$it")
                if (it != null) {
                    binding.toolbarTitle.text = it.displayName
                }
            }
        }

        lifecycleScope.launch {
            messageViewModel.messagesOfThread.collectLatest { messages ->
                val wasAtBottom = !binding.recyclerViewMessages.canScrollVertically(1) // Check if already at bottom

                chatAdapter.submitList(messages) {
                    if (wasAtBottom || messageId == -1L) {
                        scrollToBottom()
                    }
                }

                // Mark messages as read
                chatMessagesAreReadNow(messages)
            }
        }
        // Detect keyboard open/close and adjust RecyclerView
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (isKeyboardVisible(binding.root)) {
                scrollToBottom()
            }
        }

        binding.etMessageInput.requestFocus()

        binding.btnSend.setOnClickListener {
            it.isEnabled = false
            normalizeNumber?.let { number ->
                val message = Message(
                    id = 0L,
                    threadId = threadId,
                    sender = number,
                    messageBody = binding.etMessageInput.text.toString(),
                    timestamp = System.currentTimeMillis(),
                    type = Telephony.Sms.MESSAGE_TYPE_SENT,
                    seen = true,
                    read = true,
                )
                sendSms(message)
            }
            binding.etMessageInput.text.clear()
            it.postDelayed({ it.isEnabled = true }, 1000) // Re-enable after 1 second
        }
    }


    private fun scrollToBottom() {
        binding.recyclerViewMessages.post {
            val lastPosition = chatAdapter.itemCount - 1
            if (lastPosition >= 0) {
                binding.recyclerViewMessages.scrollToPosition(lastPosition)
            }
        }
    }

    private var readMessagesJob: Job? = null // ✅ Stores the running job
    private val mutex = Mutex() // ✅ Ensures only one execution at a time
    private val readMessageIds = mutableSetOf<Long>()

    private fun chatMessagesAreReadNow(messages: List<Message>) {
        //Log.i(TAG, "chatMessagesAreReadNow: Checking unread messages")

        val unreadMessages = messages.filter { !it.read && it.id !in readMessageIds }

        if (unreadMessages.isNotEmpty()) {
            //Log.i(TAG, "chatMessagesAreReadNow: Marking messages as read count: ${unreadMessages.size}")

            // ✅ Cancel any existing job before starting a new one
            readMessagesJob?.cancel()

            readMessagesJob = lifecycleScope.launch {
                mutex.withLock {
                    if (!isActive) return@launch // ✅ Prevents execution if coroutine is canceled

                    readMessageIds.addAll(unreadMessages.map { it.id })

                    messageViewModel.insertOrUpdateMessages(unreadMessages.map { it.copy(read = true) }) // chat show (mark as read all from the threadId)
                }
            }
        } else {
            //Log.i(TAG, "chatMessagesAreReadNow: No unread messages to update")

            // ✅ Cancel the job if there are no unread messages
            readMessagesJob?.cancel()
            readMessagesJob = null
        }
    }

    private fun sendSms(message: Message) {
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            return
        }

        val subscriptionManager = getSystemService(SubscriptionManager::class.java)

        // ✅ Check if we have permission to access subscription info
        val activeSubscriptions = try {
            subscriptionManager.activeSubscriptionInfoList
        } catch (e: SecurityException) {
            Log.e(TAG, "$TAG -Permission required for accessing subscription info.", e)
            return
        }

        val subscriptionId = when {
            activeSubscriptions.isNullOrEmpty() -> SubscriptionManager.getDefaultSmsSubscriptionId()
            activeSubscriptions.size == 1 -> activeSubscriptions.first().subscriptionId
            else -> activeSubscriptions[0].subscriptionId
        }

        // ✅ Call ViewModel with a valid subscriptionId
        messageViewModel.insertSms(message, subscriptionId, this@ChatActivity)

        lifecycleScope.launch {
            messageViewModel.smsSent.collectLatest {
                messageViewModel.fetchSmsMessages(needToUpdate = true)
                conversationViewModel.fetchConversationThreads(needToUpdate = true)
            }
        }
    }
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS
    )
    private val REQUEST_CODE_PERMISSIONS = 101

    // ✅ Helper function to check permissions
    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("ChatActivity", "Permissions granted. Proceeding with SMS sending.")
            } else {
                Toast.makeText(this, "Permissions required to send SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!SmsDefaultAppHelper.isDefaultSmsApp(this)) {
            this@ChatActivity.navigateToSetAsDefaultScreen()
        } //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ", )
        //chatMessagesAreReadNow(messageViewModel.messagesOfThread.value)
        // ✅ Unregister EventBus when Activity is destroyed
        EventBus.getDefault().unregister(this)
    }

    // ✅ Handle SMS Event (Auto Updates UI)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmsReceived(event: Message) {
        Log.d(TAG, "EventBus -> New SMS from ${event.sender}: ${event.messageBody}")

        // ✅ Fetch updated messages from ViewModel
        conversationViewModel.fetchConversationThreads(needToUpdate = true)
        messageViewModel.fetchSmsMessages(needToUpdate = true)

        // Show Toast (Optional)
        //Toast.makeText(this, "New SMS from ${event.sender}", Toast.LENGTH_SHORT).show()
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}