package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import com.hardik.messageapp.databinding.ActivitySwipeActionBinding
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SwipeActionActivity : BaseActivity() {
    private val TAG = BASE_TAG + SwipeActionActivity::class.java.simpleName

    private lateinit var binding: ActivitySwipeActionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeActionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}