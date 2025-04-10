package com.hardik.messageapp.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.hardik.messageapp.data.local.AppDatabase
import com.hardik.messageapp.databinding.ActivitySplashBinding
import com.hardik.messageapp.domain.usecase.conversation.fetch.GetConversationUseCase
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.Constants.KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY
import com.hardik.messageapp.helper.Constants.KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY
import com.hardik.messageapp.helper.SmsDefaultAppHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private val TAG = BASE_TAG + SplashActivity::class.java.simpleName

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var getConversationUseCase: GetConversationUseCase
    companion object {
        var splashScrn: SplashScreen? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > 31) {
            splashScrn = installSplashScreen()
            splashScrn?.setKeepOnScreenCondition { false }
        }
        super.onCreate(savedInstanceState)

        // Inflate the binding and set the content view
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Ensure DB tables are created
        CoroutineScope(Dispatchers.IO).launch { AppDatabase.getDatabase(this@SplashActivity).openHelper.readableDatabase }

        // Launch the next screen asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            if (SmsDefaultAppHelper.isDefaultSmsApp(this@SplashActivity)) { getConversationUseCase(TAG) }

            delay(100) // Short delay to mimic splash duration
            navigateToNextScreen()
        }
    }

    private suspend fun navigateToNextScreen() {
        withContext(Dispatchers.IO) {
            // Check if it's the first launch using SharedPreferences
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
            val isFirstLaunch = sharedPrefs.getBoolean(KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY, true)
            val isAppSetAsDefault = sharedPrefs.getBoolean(KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY, SmsDefaultAppHelper.isDefaultSmsApp(this@SplashActivity)) // todo: false until set as default

            val nextActivity = when {
                isFirstLaunch -> LanguageActivity::class.java
                !isFirstLaunch && !isAppSetAsDefault -> SetAsDefaultActivity::class.java
                else -> MainActivity::class.java
            }

            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashActivity, nextActivity)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun handleOnSoftBackPress(): Boolean {
        return false
    }
}