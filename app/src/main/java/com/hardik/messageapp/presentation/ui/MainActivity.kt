package com.hardik.messageapp.presentation.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.R
import com.hardik.messageapp.domain.model.SmsMessage
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.SmsDefaultAppHelper
import com.hardik.messageapp.helper.SmsDefaultAppHelper.isDefaultSmsApp
import com.hardik.messageapp.helper.SmsDefaultAppHelper.navigateToSetAsDefaultScreen
import com.hardik.messageapp.presentation.viewmodel.SmsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = BASE_TAG + MainActivity::class.java

    private val smsViewModel: SmsViewModel by viewModels()


    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            smsViewModel.fetchSmsMessages() // Update SMS list when a new message arrives
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        
        // Button Click to Request Default SMS Permission
        findViewById<Button>(R.id.btnRequestPermissions).setOnClickListener {}

        if (SmsDefaultAppHelper.isDefaultSmsApp(this)){
            
            smsViewModel.fetchSmsMessages()

            lifecycleScope.launch {
                smsViewModel.smsMessages.collect { messages ->
                    updateUI(messages)
                }
            }
        }else{
            Log.e(TAG, "onCreate: notastafdg", )
        }
    }



    private fun updateUI(messages: List<SmsMessage>) {
        messages.forEach { Log.d(TAG, "Id: ${it.id}, From: ${it.sender}, Message: ${it.message}") }
    }

    override fun onResume() {
        super.onResume()
        if(!isDefaultSmsApp(this)){navigateToSetAsDefaultScreen()}
    }
    override fun onDestroy() {
        super.onDestroy()
    }



}

