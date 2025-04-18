package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import com.hardik.messageapp.databinding.ActivityAfterCallBinding
import com.hardik.messageapp.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AfterCallActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + AfterCallActivity::class.java.simpleName

    private lateinit var binding: ActivityAfterCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}