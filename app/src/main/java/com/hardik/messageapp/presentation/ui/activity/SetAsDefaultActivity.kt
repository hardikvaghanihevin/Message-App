package com.hardik.messageapp.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.hardik.messageapp.databinding.ActivitySetAsDefaultBinding
import com.hardik.messageapp.util.Constants.BASE_TAG
import com.hardik.messageapp.util.Constants.KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY
import com.hardik.messageapp.util.SmsDefaultAppHelper.registerResultLauncher
import com.hardik.messageapp.util.SmsDefaultAppHelper.requestDefaultSmsApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetAsDefaultActivity : BaseActivity() {
    private val TAG = BASE_TAG + SetAsDefaultActivity::class.java

    private lateinit var binding: ActivitySetAsDefaultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAsDefaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            requestDefaultSmsApp()
        }
        registerResultLauncher(this,){isSetAsDefault ->
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY, isSetAsDefault).apply() // true todo: true/false is set as default

            if (isSetAsDefault){
                val intent = Intent(this@SetAsDefaultActivity, MainActivity::class.java)
                this@SetAsDefaultActivity.startActivity(intent)
                this@SetAsDefaultActivity.finish()
            }
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}
//PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY, SmsDefaultAppHelper.isDefaultSmsApp(this@SetAsDefaultActivity)).apply()
