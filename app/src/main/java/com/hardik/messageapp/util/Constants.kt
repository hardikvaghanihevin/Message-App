package com.hardik.messageapp.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object Constants {
    const val BASE_TAG = "A_"

    const val KEY_IS_FIRST_TIME_LAUNCH_SHOW_LANGUAGE_ACTIVITY = "Is first time launch application, show language activity."
    const val KEY_IS_APP_SET_AS_DEFAULT_SHOW_SET_AS_DEFAULT_ACTIVITY = "Is app set as default message application or not!"
    const val KEY_THREAD_ID = "thread_id"
    const val KEY_MESSAGE_ID = "message_id"
    const val KEY_CONTACT_ID = "contact_id"
    const val KEY_CONTACT_NUMBER = "contact_number"
    const val KEY_NORMALIZE_NUMBER = "normalize_number"
    const val KEY_SEARCH_QUERY = "search_query"

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
    fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun isUpsideDownCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}