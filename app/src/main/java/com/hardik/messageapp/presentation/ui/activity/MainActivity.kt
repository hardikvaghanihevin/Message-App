package com.hardik.messageapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.hardik.messageapp.R
import com.hardik.messageapp.data.local.entity.BlockThreadEntity
import com.hardik.messageapp.databinding.ActivityMainBinding
import com.hardik.messageapp.databinding.NavViewBottomBinding
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.presentation.adapter.ViewPagerHomeAdapter
import com.hardik.messageapp.presentation.custom_view.BottomNavManager
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.presentation.custom_view.showBlockConversationDialog
import com.hardik.messageapp.presentation.custom_view.showMoveConversationToBinDialog
import com.hardik.messageapp.presentation.ui.fragment.MessageFragment
import com.hardik.messageapp.presentation.ui.fragment.PrivateFragment
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.util.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.util.convertPxToDpSp
import com.hardik.messageapp.util.evaluateSelectionGetHomeToolbarMenu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java.simpleName

    lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2

    private lateinit var fabNewConversation: ImageView
    lateinit var navBinding: NavViewBottomBinding
    lateinit var viewPagerHomeAdapter: ViewPagerHomeAdapter

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
            viewPagerHomeAdapter = ViewPagerHomeAdapter(this)
            viewPager.adapter = viewPagerHomeAdapter
            viewPager.isUserInputEnabled = false  // Disable swipe navigation


            // Floating Action Button Click (New Conversation)
            fabNewConversation.setOnClickListener {
                listOf<Float>(665f,340f,120f,105f,365f,115f,80f,75f,72f,55f,50f,48f,46f,45f,42f,40f,38f,36f,35f,32f,30f).forEach { Log.e(TAG, "onCreate: $it - ${convertPxToDpSp(this,it)}", ) }
                //startActivity(Intent(this, NewConversationActivity::class.java))
//                Toast.makeText(this, "${ conversationViewModel.unreadMessageCountGeneral.value }", Toast.LENGTH_SHORT).show()
//                binding.includedNavViewBottom.root.apply { isFocusable = true }
                startActivity(Intent(this, NewConversationActivity::class.java))
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
                conversationViewModel.unreadGeneralAndPrivateConversationThreadsCount.collectLatest { (generalThreads, privateThreads)->
                    BottomNavManager.updateCount(navBinding, BottomNavManager.CountType.GENERAL, count = generalThreads)
                    BottomNavManager.updateCount(navBinding, BottomNavManager.CountType.PRIVATE, count = privateThreads)
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
        val whichFragmentIsLive: Pair<Boolean, Fragment?> = isCurrentFragmentGeneral()

        //val unReadThreads: List<ConversationThread> = conversationViewModel.filteredConversationThreads.value.filter { !it.read }.map { it }
        val resultMenu = evaluateSelectionGetHomeToolbarMenu(conversationViewModel.filteredConversationThreads.value)

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = resultMenu.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.delete_all) -> { popupMenuDeleteAll(whichFragmentIsLive) }
                getString(R.string.block_conversation) -> { popupMenuBlockConversation() }
                getString(R.string.mark_all_as_read) -> { popupMenuMarkAsRead(whichFragmentIsLive) }
                getString(R.string.archived) -> { popupMenuArchived() }
                getString(R.string.scheduled) -> { popupMenuSchedule(whichFragmentIsLive) }
                getString(R.string.starred_message) -> { popupMenuStarredMessage(whichFragmentIsLive)}
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


    private fun popupMenuDeleteAll(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        //Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show()
        //val threadsGeneral: List<Long> = conversationViewModel.filteredConversationThreads.value.map { it.threadId }
        //val threadsPrivate: List<Long> = conversationViewModel.conversationThreadsPrivate.value.map { it.threadId }
        val threads = if (whichFragmentIsLive.first) conversationViewModel.filteredConversationThreads.value else conversationViewModel.conversationThreadsPrivate.value
        deleteConversation(threads) {} // delete all conversation

    }
    private fun popupMenuBlockConversation() { startActivity(Intent(this, BlockActivity::class.java)) }
    private fun popupMenuMarkAsRead(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        //val threadIds: List<Long> = conversationViewModel.filteredConversationThreads.value.filter { !it.read }.map { it.threadId }
        val threads = if (whichFragmentIsLive.first) conversationViewModel.filteredConversationThreads.value else conversationViewModel.conversationThreadsPrivate.value
        val threadIds = threads.map { it.threadId }
        conversationViewModel.markAsReadConversationByThreadIds(threadIds = threadIds) // popupMenuToolbar
//        lifecycleScope.launch {
//            conversationViewModel.isMarkAsReadConversationThread.collectLatest { isRead ->
//                super.fetchSmsMessages(needToUpdate = isRead) // Mark as read
//            }
//        }
    }
    private fun popupMenuArchived() { startActivity(Intent(this, ArchiveActivity::class.java)) }
    private fun popupMenuSchedule(whichFragmentIsLive: Pair<Boolean, Fragment?>) { /*startActivity(Intent(this, ArchiveActivity::class.java))*/ }
    private fun popupMenuStarredMessage(whichFragmentIsLive: Pair<Boolean, Fragment?>) { /*startActivity(Intent(this, ArchiveActivity::class.java))*/ }
    private fun popupMenuRecycleBin() { startActivity(Intent(this, RecyclebinActivity::class.java)) }
    private fun popupMenuSettings() { startActivity(Intent(this, SettingsActivity::class.java)) }

    fun showPopupMenuBottom(view: View, selectedMenu: PopupMenu){
        val whichFragmentIsLive: Pair<Boolean, Fragment?> = isCurrentFragmentGeneral()

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = selectedMenu.getMenuItems(this), showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                getString(R.string.mark_as_read) -> { popupMenuMarkAsReadBottom(whichFragmentIsLive) }
                getString(R.string.mark_as_unread) -> { popupMenuMarkAsUnreadBottom(whichFragmentIsLive) }
                getString(R.string.pin_conversation) -> { popupMenuPinConversationBottom(whichFragmentIsLive) }
                getString(R.string.unpin_conversation) -> { popupMenuUnpinConversationBottom(whichFragmentIsLive) }
                getString(R.string.block_conversation) -> { popupMenuBlockConversationBottom(whichFragmentIsLive) }
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

    fun deleteConversation(threads: List<ConversationThread>, callBack: () -> Unit) {
        //todo: show dialog before delete then wait for response or dialog ans is positive then do it other wise abort work
        showMoveConversationToBinDialog(this) { alsoBlock, isPositive ->
            if (!isPositive) return@showMoveConversationToBinDialog callBack()

            val threadIds = threads.map { it.threadId }

            lifecycleScope.launch {
                if (alsoBlock) {
                    val blockThreads = threads.map {
                        BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender)
                    }
                    conversationViewModel.blockConversationByThreads(blockThreads)
                }

                conversationViewModel.deleteConversationByThreads(threadIds)

                conversationViewModel.isDeleteConversationThread.collectLatest { isDelete ->
                    conversationViewModel.fetchConversationThreads(needToUpdate = isDelete)
                    callBack()
                }
            }
        }
    }


    private fun popupMenuMarkAsReadBottom(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
        conversationViewModel.markAsReadConversationByThreadIds(threadIds = threadIds)
//        lifecycleScope.launch {
//            conversationViewModel.isMarkAsReadConversationThread.collectLatest { isRead ->
//                Log.e(TAG, "popupMenuMarkAsReadBottom: $isRead", )
//                super.fetchSmsMessages(needToUpdate = isRead) // Mark as read
//            }
//        }
        if (whichFragmentIsLive.first) { (whichFragmentIsLive.second as? MessageFragment)?.conversationAdapter?.unselectAll() }// todo: unselectAll after work is done
        else { (whichFragmentIsLive.second as? PrivateFragment)?.conversationAdapter?.unselectAll() }//todo: unselectAll after work is done

    }

    private fun popupMenuMarkAsUnreadBottom(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
        conversationViewModel.markAsUnreadConversationByThreadIds(threadIds = threadIds)
        lifecycleScope.launch {
            conversationViewModel.isMarkAsUnreadConversationThread.collectLatest { isUnread ->
                super.fetchSmsMessages(needToUpdate = isUnread) // Mark as unread
            }
        }

        if (whichFragmentIsLive.first) { (whichFragmentIsLive.second as? MessageFragment)?.conversationAdapter?.unselectAll() }// todo: unselectAll after work is done
        else { (whichFragmentIsLive.second as? PrivateFragment)?.conversationAdapter?.unselectAll() }//todo: unselectAll after work is done

    }
    private fun popupMenuPinConversationBottom(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        lifecycleScope.launch {
            val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            conversationViewModel.pinConversationsByThreadIds(threadIds = threadIds)

            if (whichFragmentIsLive.first) { (whichFragmentIsLive.second as? MessageFragment)?.conversationAdapter?.unselectAll() }// todo: unselectAll after work is done
            else { (whichFragmentIsLive.second as? PrivateFragment)?.conversationAdapter?.unselectAll() }//todo: unselectAll after work is done

        }
    }

    private fun popupMenuUnpinConversationBottom(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        lifecycleScope.launch {
            val threadIds = conversationViewModel.countSelectedConversationThreads.value.map { it.threadId }
            conversationViewModel.unpinConversationsByThreadIds(threadIds = threadIds)

            if (whichFragmentIsLive.first) { (whichFragmentIsLive.second as? MessageFragment)?.conversationAdapter?.unselectAll() }// todo: unselectAll after work is done
            else { (whichFragmentIsLive.second as? PrivateFragment)?.conversationAdapter?.unselectAll() }//todo: unselectAll after work is done

        }
    }
    private fun popupMenuBlockConversationBottom(whichFragmentIsLive: Pair<Boolean, Fragment?>) {
        showBlockConversationDialog(this) { doBlock ->

            lifecycleScope.launch {
                if (doBlock){
                    val blockThreads = conversationViewModel.countSelectedConversationThreads.value.map { BlockThreadEntity(threadId = it.threadId, number = it.normalizeNumber, sender = it.sender) }
                    conversationViewModel.blockConversationByThreads(blockThreads = blockThreads) }

                if (whichFragmentIsLive.first) { (whichFragmentIsLive.second as? MessageFragment)?.conversationAdapter?.unselectAll() }// todo: unselectAll after work is done
                else { (whichFragmentIsLive.second as? PrivateFragment)?.conversationAdapter?.unselectAll() }//todo: unselectAll after work is done
            }
        }
    }

    private fun isCurrentFragmentGeneral(): Pair<Boolean,Fragment?> { //todo: note if true then 'MessageFragment' is live
        val currentPosition = viewPager.currentItem
        val currentFragment = viewPagerHomeAdapter.getFragment(currentPosition)
        return Pair(currentFragment is MessageFragment, currentFragment)
    }
    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}

