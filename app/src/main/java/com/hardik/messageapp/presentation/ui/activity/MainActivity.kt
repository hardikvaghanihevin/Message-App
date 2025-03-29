package com.hardik.messageapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hardik.messageapp.R
import com.hardik.messageapp.databinding.ActivityMainBinding
import com.hardik.messageapp.databinding.NavViewBottomBinding
import com.hardik.messageapp.domain.model.Message
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.adapter.ViewPagerAdapter
import com.hardik.messageapp.presentation.custom_view.BottomNavManager
import com.hardik.messageapp.presentation.custom_view.CustomPopupMenu
import com.hardik.messageapp.presentation.viewmodel.ConversationThreadViewModel
import com.hardik.messageapp.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java

//    private val conversationThreadViewModel: ConversationThreadViewModel by viewModels()
//    private val contactViewModel: ContactViewModel by viewModels()
//    private val blockViewModel: BlockViewModel by viewModels()
//    private val pinViewModel: PinViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private val conversationViewModel: ConversationThreadViewModel by viewModels()
    private val messageViewModel: MessageViewModel by viewModels()

    private lateinit var viewPager: ViewPager2

    private lateinit var fabNewConversation: FloatingActionButton
    private lateinit var navBinding: NavViewBottomBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Register EventBus when Activity is created
        EventBus.getDefault().register(this)

        viewPager = binding.viewPager

        fabNewConversation = binding.fabNewConversation

        if (isDefaultSmsApp(this)) {

            // ViewPager Adapter
            val adapter = ViewPagerAdapter(this)
            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = false  // Disable swipe navigation


            // Floating Action Button Click (New Conversation)
            fabNewConversation.setOnClickListener {
                //startActivity(Intent(this, NewConversationActivity::class.java))
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show()
            }

            // Bind custom bottom navigation view
            navBinding = NavViewBottomBinding.bind(binding.includedNavViewBottom.root)

            // Setup bottom navigation with optional click actions
            BottomNavManager.setup(
                binding = navBinding,
                onMessageClick = { viewPager.setCurrentItem(0, false) },// Load Message Fragment
                onPrivateClick = { viewPager.setCurrentItem(1, false) }// Load Private Fragment
            )

            val isGranted = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            Log.e(TAG, "onCreate: $isGranted", )
                //ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS_PERMISSION)

            binding.toolbarSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
            binding.toolbarMore.setOnClickListener {
                showPopupMenu(it)  // Show custom popup menu on click of more button in toolbar
            }

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

    private fun showPopupMenu(view: View){
        val menuItems = listOf("Edit", "Delete", "Share", "Settings") // Menu options

        val popupMenu = CustomPopupMenu(context = this, anchorView = view, menuItems = menuItems, showUnderLine = true) { selectedItem ->
            when (selectedItem) {
                "Edit" -> Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
                "Delete" -> Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show()
                "Share" -> Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show()
                "Settings" -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            }
        }

        popupMenu.show() // Show the custom popup
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

