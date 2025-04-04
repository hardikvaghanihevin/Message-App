package com.hardik.messageapp.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.hardik.messageapp.databinding.ActivityLanguageBinding
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.Constants.KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class LanguageActivity : BaseActivity() {
    private val TAG = BASE_TAG + LanguageActivity::class.java.simpleName

    private var isFirstLaunch by Delegates.notNull<Boolean>()

    private lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFirstLaunch = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY, true)

        // If it's the first launch, update the SharedPreferences
        if (isFirstLaunch) {
            Toast.makeText(this,"is first time ", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"is second time ", Toast.LENGTH_SHORT).show()
        }

        binding.btn.setOnClickListener{
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY, false).apply()
            val intent = Intent(this@LanguageActivity, SetAsDefaultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}