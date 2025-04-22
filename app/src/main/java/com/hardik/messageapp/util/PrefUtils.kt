package com.hardik.messageapp.util

import android.content.Context
//import com.messagesms.basicmessages.data.models.ThemeMode
//import com.messagesms.basicmessages.utils.swipeutils.SwipeAction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "MessageAppPref"
        private const val SELECTED_SWIPE_ACTION_KEY_LEFT = "selectedSwipeActionLeft"
        private const val SELECTED_SWIPE_ACTION_KEY_RIGHT = "selectedSwipeActionRight"

        private const val LAST_BACK_UP = "last_backup_time"

        private const val PREF_THEME_MODE = "pref_theme_mode"

        private const val KEY_LANGUAGE_CODE = "lang_code"
        private const val KEY_FIRST_TIME_SELECTED = "first_time_select"

        private const val FONT_SIZE_KEY = "font_size_scale"

    }


   /* var lastBackupTime: Long
        get() = preferences.getLong(LAST_BACK_UP, 0L)
        set(value) = preferences.edit().putLong(LAST_BACK_UP, value).apply()


    var languageCode: String
        get() = preferences.getString(KEY_LANGUAGE_CODE, "en") ?: "en"
        set(value) {
            preferences.edit().putString(KEY_LANGUAGE_CODE, value).apply()
        }


    var languageSelectFirstTime: Boolean
        get() = preferences.getBoolean(KEY_FIRST_TIME_SELECTED, false)
        set(value) {
            preferences.edit().putBoolean(KEY_FIRST_TIME_SELECTED, value).apply()
        }


    var themeMode: ThemeMode
        get() = ThemeMode.fromValue(preferences.getInt(PREF_THEME_MODE, ThemeMode.SYSTEM.value))
        set(mode) {
            preferences.edit().putInt(PREF_THEME_MODE, mode.value).apply()
        }

    var fontScale: Float
        get() = preferences.getFloat(FONT_SIZE_KEY, 1.0f)
        set(scale) {
            preferences.edit().putFloat(FONT_SIZE_KEY, scale).apply()
        }


    fun getRightSwipeAction(): SwipeAction {
        val actionName =
            preferences.getString(SELECTED_SWIPE_ACTION_KEY_RIGHT, SwipeAction.NONE.name)
        return SwipeAction.valueOf(actionName ?: SwipeAction.NONE.name)
    }

    fun getLeftSwipeAction(): SwipeAction {
        val actionName =
            preferences.getString(SELECTED_SWIPE_ACTION_KEY_LEFT, SwipeAction.NONE.name)
        return SwipeAction.valueOf(actionName ?: SwipeAction.NONE.name)
    }

    fun saveRightSwipeAction(action: SwipeAction) {
        preferences.edit().putString(SELECTED_SWIPE_ACTION_KEY_RIGHT, action.name).apply()
    }

    fun saveLeftSwipeAction(action: SwipeAction) {
        preferences.edit().putString(SELECTED_SWIPE_ACTION_KEY_LEFT, action.name).apply()
    }


    fun saveMessageCount(fileName: String, messageCount: Int) {
        preferences.edit().putInt(fileName, messageCount).apply()
    }

    fun getMessageCount(fileName: String): Int {
        return preferences.getInt(fileName, 0)
    }
*/

}
