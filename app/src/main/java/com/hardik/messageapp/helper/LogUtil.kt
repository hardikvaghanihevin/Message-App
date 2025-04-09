package com.hardik.messageapp.helper

object LogUtil {

    fun d(tag: String, message: String) {
        val maxLogSize = 2000
        var i = 0
        while (i < message.length) {
            val end = (i + maxLogSize).coerceAtMost(message.length)
            android.util.Log.d(tag, message.substring(i, end))
            i += maxLogSize
        }
    }
}
//Log.e(TAG, "popupMenuBlockConversation: \n${it.joinToString("\n") { thread -> "threadId=${thread.threadId}, sender=${thread.sender}, normalizeNumber=${thread.normalizeNumber}" }}")
