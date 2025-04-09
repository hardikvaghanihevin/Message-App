package com.hardik.messageapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.hardik.messageapp.R
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.ActivityMainBinding
import com.hardik.messageapp.databinding.NavViewBottomBinding
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.adapter.ViewPagerAdapter
import com.hardik.messageapp.presentation.custom_view.BottomNavManager
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.ui.fragment.MessageFragment
import com.hardik.messageapp.presentation.ui.fragment.PrivateFragment
import com.hardik.messageapp.presentation.util.evaluateSelectionGetHomeToolbarMenu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java.simpleName

//    private val conversationThreadViewModel: ConversationThreadViewModel by viewModels()
//    private val contactViewModel: ContactViewModel by viewModels()
//    private val blockViewModel: BlockViewModel by viewModels()
//    private val pinViewModel: PinViewModel by viewModels()
//    private val archiveViewModel: ArchiveViewModel by viewModels()


    lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2

    private lateinit var fabNewConversation: ImageView
    lateinit var navBinding: NavViewBottomBinding
    lateinit var viewPagerAdapter: ViewPagerAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Register EventBus when Activity is created
        EventBus.getDefault().register(this)

        viewPager = binding.viewPager

        fabNewConversation = binding.imgNewConversation

        if (isDefaultSmsApp(this)) {

            // ViewPager Adapter
            viewPagerAdapter = ViewPagerAdapter(this)
            viewPager.adapter = viewPagerAdapter
            viewPager.isUserInputEnabled = false  // Disable swipe navigation


            // Floating Action Button Click (New Conversation)
            fabNewConversation.setOnClickListener {
                //startActivity(Intent(this, NewConversationActivity::class.java))
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show()
                binding.includedNavViewBottom.root.apply {
                    isFocusable = true
                }
            }

            // Bind custom bottom navigation view
            navBinding = NavViewBottomBinding.bind(binding.includedNavViewBottom.root)

            // Setup bottom navigation with optional click actions
            BottomNavManager.setup(
                binding = navBinding,
                onMessageClick = { viewPager.setCurrentItem(0, false) },// Load Message Fragment
                onPrivateClick = { viewPager.setCurrentItem(1, false) }// Load Private Fragment
            )

            lifecycleScope.launch {
                conversationViewModel.countUnreadGeneralAndPrivateConversationThreads.collectLatest { (generalThreads, privateThreads)->
                    BottomNavManager.updateCount(navBinding, BottomNavManager.CountType.GENERAL, count = generalThreads.size)
                    BottomNavManager.updateCount(navBinding, BottomNavManager.CountType.PRIVATE, count = privateThreads.size)
                }
            }

            val isGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            Log.e(TAG, "onCreate: $isGranted")
                //ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS_PERMISSION)


        } else {
            Log.e(TAG, "onCreate: do nothing")
        }
    }


    override fun onResume() {
        super.onResume()
        if (!isDefaultSmsApp(this)) {
            navigateToSetAsDefaultScreen()
        } //Todo :- this line should put on 'onResume()' in all activities and fragments.
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Unregister EventBus when Activity is destroyed
        EventBus.getDefault().unregister(this)
    }

    fun showPopupMenu(view: View){
        val unReadThreads = conversationViewModel.filteredConversationThreads.value.filter { !it.read }.map { it }
        val resultMenu = evaluateSelectionGetHomeToolbarMenu(conversationViewModel.filteredConversationThreads.value)

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = resultMenu.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.delete_all) -> { popupMenuDeleteAll() }
                getString(R.string.block_conversation) -> { popupMenuBlockConversation() }
                getString(R.string.mark_all_as_read) -> { popupMenuMarkAsRead() }
                getString(R.string.archived) -> { popupMenuArchived() }
                getString(R.string.scheduled) -> { popupMenuSchedule() }
                getString(R.string.starred_message) -> { popupMenuStarredMessage()}
                getString(R.string.recycle_bin) -> { popupMenuRecycleBin() }
                getString(R.string.settings) -> { popupMenuSettings() }
            }
        }

        // Show below and align to End Show the custom popup
        popupMenu.showNearAnchorWithMargin(showAbove = false, alignStart = false,
            marginTopDp = 0,
            marginBottomDp = 0,
            marginStartDp = 0,
            marginEndDp = 17
        )
    }


    private fun popupMenuDeleteAll() {
        //Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show()
        val threadsGeneral: List<Long> = conversationViewModel.filteredConversationThreads.value.map { it.threadId }
        //val threadsPrivate: List<Long> = conversationViewModel.conversationThreadsPrivate.value.map { it.threadId }
        deleteConversation(threadsGeneral)// delete all conversation
    }
    private fun popupMenuBlockConversation() { startActivity(Intent(this, BlockActivity::class.java)) }
    private fun popupMenuMarkAsRead() {
        val threadIds: List<Long> = conversationViewModel.filteredConversationThreads.value.filter { !it.read }.map { it.threadId }
        conversationViewModel.markAsReadConversationByThreadIds(threadIds = threadIds) // popupMenuToolbar
        lifecycleScope.launch {
            conversationViewModel.isMarkAsReadConversationThread.collectLatest { isRead ->
                super.fetchSmsMessages(needToUpdate = isRead) // Mark as read
            }
        }
    }
    private fun popupMenuArchived() { startActivity(Intent(this, ArchiveActivity::class.java)) }
    private fun popupMenuSchedule() { /*startActivity(Intent(this, ArchiveActivity::class.java))*/ }
    private fun popupMenuStarredMessage() { /*startActivity(Intent(this, ArchiveActivity::class.java))*/ }
    private fun popupMenuRecycleBin() { startActivity(Intent(this, RecyclebinActivity::class.java)) }
    private fun popupMenuSettings() { /*startActivity(Intent(this, ArchiveActivity::class.java))*/ }

    fun showPopupMenuBottom(view: View, selectedMenu: PopupMenu){

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = selectedMenu.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.mark_as_read) -> { popupMenuMarkAsReadBottom() }
                getString(R.string.mark_as_unread) -> { popupMenuMarkAsUnreadBottom() }
                getString(R.string.pin_conversation) -> { popupMenuPinConversationBottom() }
                getString(R.string.unpin_conversation) -> { popupMenuUnpinConversationBottom() }
                getString(R.string.block_conversation) -> { popupMenuBlockConversationBottom() }
            }
        }

        // Show above and align to End Show the custom popup
        popupMenu.showNearAnchorWithMargin(showAbove = true, alignStart = false,
            marginTopDp = 0,
            marginBottomDp = 24,
            marginStartDp = 0,
            marginEndDp = 9
        )

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


    fun archiveConversation(threadIds: List<Long>){
        conversationViewModel.archiveConversationThread(threadIds)

        lifecycleScope.launch {
            conversationViewModel.isArchivedConversationThread.collectLatest { isArchived: Boolean ->
                conversationViewModel.fetchConversationThreads(needToUpdate = isArchived)
            }
        }
    }

    fun deleteConversation(threadIds: List<Long>){
        //val threads: List<Long> = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
        conversationViewModel.deleteConversationByThreads(threadIds)
    }

    private fun popupMenuMarkAsReadBottom() {
        val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
        conversationViewModel.markAsReadConversationByThreadIds(threadIds = threadIds)
        lifecycleScope.launch {
            conversationViewModel.isMarkAsReadConversationThread.collectLatest { isRead ->
                Log.e(TAG, "popupMenuMarkAsReadBottom: $isRead", )
                super.fetchSmsMessages(needToUpdate = isRead) // Mark as read
            }
        }
        val currentPosition = viewPager.currentItem
        val currentFragment = viewPagerAdapter.getFragment(currentPosition)

        if (currentFragment is MessageFragment) {
            currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
        }
        if (currentFragment is PrivateFragment) {
            currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
        }
    }

    private fun popupMenuMarkAsUnreadBottom() {
        val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
        conversationViewModel.markAsUnreadConversationByThreadIds(threadIds = threadIds)
        lifecycleScope.launch {
            conversationViewModel.isMarkAsUnreadConversationThread.collectLatest { isUnread ->
                super.fetchSmsMessages(needToUpdate = isUnread) // Mark as unread
            }
        }
        val currentPosition = viewPager.currentItem
        val currentFragment = viewPagerAdapter.getFragment(currentPosition)

        if (currentFragment is MessageFragment) {
            currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
        }
        if (currentFragment is PrivateFragment) {
            currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
        }
    }
    private fun popupMenuPinConversationBottom() {
        lifecycleScope.launch {
            val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            conversationViewModel.pinConversationsByThreadIds(threadIds = threadIds)

            val currentPosition = viewPager.currentItem
            val currentFragment = viewPagerAdapter.getFragment(currentPosition)

            if (currentFragment is MessageFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }
            if (currentFragment is PrivateFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }

        }
    }

    private fun popupMenuUnpinConversationBottom() {
        lifecycleScope.launch {
            val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            conversationViewModel.unpinConversationsByThreadIds(threadIds = threadIds)

            val currentPosition = viewPager.currentItem
            val currentFragment = viewPagerAdapter.getFragment(currentPosition)

            if (currentFragment is MessageFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }
            if (currentFragment is PrivateFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }

        }
    }
    private fun popupMenuBlockConversationBottom() {
        lifecycleScope.launch {
            val blockThreads = conversationViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
            conversationViewModel.blockConversationByThreads(blockThreads = blockThreads)

            val currentPosition = viewPager.currentItem
            val currentFragment = viewPagerAdapter.getFragment(currentPosition)

            if (currentFragment is MessageFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }
            if (currentFragment is PrivateFragment) {
                currentFragment.conversationAdapter.unselectAll() //todo: unselectAll after work is done
            }
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}

