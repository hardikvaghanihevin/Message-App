package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import com.hardik.messageapp.databinding.ActivityBackupAndRestoreBinding
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupAndRestoreActivity : BaseActivity() {
    private val TAG = BASE_TAG + BackupAndRestoreActivity::class.java.simpleName

    private lateinit var binding: ActivityBackupAndRestoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupAndRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}