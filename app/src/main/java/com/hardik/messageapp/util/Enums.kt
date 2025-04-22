package com.hardik.messageapp.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.hardik.messageapp.R

enum class AppTheme(val value: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {
        private const val PREF_KEY_APP_THEME = "app_theme"
        private var appContext: Context? = null
        private var cachedTheme: AppTheme = SYSTEM

        fun init(context: Context) {
            appContext = context.applicationContext
            cachedTheme = getSavedAppTheme(appContext!!)
            applyAppTheme(cachedTheme)
        }

        var mTheme: AppTheme
            get() = cachedTheme
            set(value) { setTheme(value) }

        fun setThemeWithCallback(value: AppTheme, onChanged: () -> Unit) {
                setTheme(value)
                onChanged()

        }
        private fun setTheme(theme: AppTheme) {
            appContext?.let {
                cachedTheme = theme
                val prefs = PreferenceManager.getDefaultSharedPreferences(it)
                prefs.edit().putInt(PREF_KEY_APP_THEME, theme.value).apply()
                applyAppTheme(theme)
            }
        }

        private fun getSavedAppTheme(context: Context): AppTheme {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val savedValue = prefs.getInt(PREF_KEY_APP_THEME, SYSTEM.value)
            return entries.find { it.value == savedValue } ?: SYSTEM
        }

        private fun applyAppTheme(theme: AppTheme) {
            AppCompatDelegate.setDefaultNightMode(theme.value)
        }
    }
}


enum class SwipeAction {
    LEFT,
    RIGHT;

    enum class Action {
        NONE,
        ARCHIVE,
        DELETE,
        CALL,
        BLOCK,
        MARK_AS_READ,
        MARK_AS_UNREAD
    }

    companion object {
        private const val PREF_KEY_SWIPE_LEFT = "swipe_action_left"
        private const val PREF_KEY_SWIPE_RIGHT = "swipe_action_right"

        private var appContext: Context? = null
        private var cachedLeftAction: Action = Action.NONE
        private var cachedRightAction: Action = Action.NONE

        fun init(context: Context) {
            appContext = context.applicationContext
            cachedLeftAction = getSavedAction(LEFT)
            cachedRightAction = getSavedAction(RIGHT)
        }

        fun getAction(swipeAction: SwipeAction): Action {
            return when (swipeAction) {
                LEFT -> cachedLeftAction
                RIGHT -> cachedRightAction
            }
        }

        fun setAction(swipeAction: SwipeAction, action: Action) {
            appContext?.let {
                val prefs = PreferenceManager.getDefaultSharedPreferences(it)
                val key = when (swipeAction) {
                    LEFT -> PREF_KEY_SWIPE_LEFT
                    RIGHT -> PREF_KEY_SWIPE_RIGHT
                }
                prefs.edit().putString(key, action.name).apply()

                when (swipeAction) {
                    LEFT -> cachedLeftAction = action
                    RIGHT -> cachedRightAction = action
                }
            }
        }

        private fun getSavedAction(swipeAction: SwipeAction): Action {
            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext!!)
            val key = when (swipeAction) {
                LEFT -> PREF_KEY_SWIPE_LEFT
                RIGHT -> PREF_KEY_SWIPE_RIGHT
            }
            val name = prefs.getString(key, Action.NONE.name)
            return Action.entries.find { it.name == name } ?: Action.NONE
        }

        // Define display text and icon for each action
        fun getActionItems(): Map<SwipeAction.Action, Pair<String, Int?>> {
            val context = appContext ?: return emptyMap()
            return mapOf(
                SwipeAction.Action.NONE to Pair(context.getString(R.string.action_none), null),
                SwipeAction.Action.ARCHIVE to Pair(context.getString(R.string.action_archive), R.drawable.real_ic_archive),
                SwipeAction.Action.DELETE to Pair(context.getString(R.string.action_delete), R.drawable.real_ic_delete),
                SwipeAction.Action.CALL to Pair(context.getString(R.string.action_call), R.drawable.real_ic_call),
                SwipeAction.Action.BLOCK to Pair(context.getString(R.string.action_block), R.drawable.real_ic_block),
                SwipeAction.Action.MARK_AS_READ to Pair(context.getString(R.string.action_mark_as_read), R.drawable.real_ic_mark_as_read),
                SwipeAction.Action.MARK_AS_UNREAD to Pair(context.getString(R.string.action_mark_as_unread), R.drawable.real_ic_mark_as_unread)
            )
        }
    }
}


