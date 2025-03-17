package com.hardik.messageapp.helper

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.hardik.messageapp.helper.Constants.isOreoPlus

fun Context.openNotificationSettings() {
    if (isOreoPlus()) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    } else {
        // For Android versions below Oreo, you can't directly open the app's notification settings.
        // You can open the general notification settings instead.
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
    }
}