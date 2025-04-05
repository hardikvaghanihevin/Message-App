package com.hardik.messageapp.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.hardik.messageapp.databinding.ActivityRecyclebinBinding
import com.hardik.messageapp.helper.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecyclebinActivity : BaseActivity() {
    private val TAG = Constants.BASE_TAG + RecyclebinActivity::class.java.simpleName

    lateinit var binding: ActivityRecyclebinBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclebinBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}