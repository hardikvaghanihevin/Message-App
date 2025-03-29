package com.hardik.messageapp.presentation.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*object TimeFormatter {

    private const val TIME_FORMAT_HH_MM_A = "hh:mm a"
    private const val TIME_FORMAT_EEEE_HH_MM_A = "EEEE . hh:mm a"
    private const val TIME_FORMAT_EEEE_D_MMM_HH_MM_A = "EEEE d MMM . hh:mm a"

    private val shownDates = mutableSetOf<String>() // Track first occurrence per day

    fun formatTimestamp(timestamp: Long): String? {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val timeFormatted = SimpleDateFormat(TIME_FORMAT_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
        val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}" // Unique per day

        val formattedDate = when {
            isSameDay(calendar, todayCalendar) -> "Today . $timeFormatted"
            isSameDay(calendar, yesterdayCalendar) -> "Yesterday . $timeFormatted"
            isWithinAWeek(calendar, todayCalendar) -> SimpleDateFormat(TIME_FORMAT_EEEE_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat(TIME_FORMAT_EEEE_D_MMM_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
        }

        // Only show the first occurrence of a timestamp per day
        return if (shownDates.contains(dateKey)) {
            null // Skip duplicate times on the same day
        } else {
            shownDates.add(dateKey) // Mark day as displayed
            formattedDate
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinAWeek(target: Calendar, today: Calendar): Boolean {
        val diff = (today.timeInMillis - target.timeInMillis) / (1000 * 60 * 60 * 24)
        return diff in 1..6
    }
}*/


class TimeFormatter {

    private val TIME_FORMAT_HH_MM_A = "hh:mm a"
    private val TIME_FORMAT_EEEE_HH_MM_A = "EEEE . hh:mm a"
    private val TIME_FORMAT_EEEE_D_MMM_HH_MM_A = "EEEE d MMM . hh:mm a"

    private val shownDates = mutableSetOf<String>() // Track first occurrence per day

    fun formatTimestamp(timestamp: Long): String? {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val todayCalendar = Calendar.getInstance()
        val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val timeFormatted = SimpleDateFormat(TIME_FORMAT_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
        val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}" // Unique per day

        val formattedDate = when {
            isSameDay(calendar, todayCalendar) -> "Today . $timeFormatted"
            isSameDay(calendar, yesterdayCalendar) -> "Yesterday . $timeFormatted"
            isWithinAWeek(calendar, todayCalendar) -> SimpleDateFormat(TIME_FORMAT_EEEE_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat(TIME_FORMAT_EEEE_D_MMM_HH_MM_A, Locale.getDefault()).format(Date(timestamp))
        }

        // Only show the first occurrence of a timestamp per day
        return if (shownDates.contains(dateKey)) {
            null // Skip duplicate times on the same day
        } else {
            shownDates.add(dateKey) // Mark day as displayed
            formattedDate
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinAWeek(target: Calendar, today: Calendar): Boolean {
        val diff = (today.timeInMillis - target.timeInMillis) / (1000 * 60 * 60 * 24)
        return diff in 1..6
    }
}
