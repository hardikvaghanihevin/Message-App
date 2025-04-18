package com.hardik.messageapp.presentation.ui.activity

import android.os.Bundle
import com.hardik.messageapp.databinding.ActivityThemeBinding
import com.hardik.messageapp.util.Constants.BASE_TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeActivity : BaseActivity() {
    private val TAG = BASE_TAG + ThemeActivity::class.java.simpleName

    private lateinit var binding: ActivityThemeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}