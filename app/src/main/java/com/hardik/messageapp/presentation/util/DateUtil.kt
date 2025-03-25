package com.hardik.messageapp.presentation.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.hardik.messageapp.helper.Constants.BASE_TAG
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {
    private val TAG = BASE_TAG + DateUtil::class.java.simpleName

    // Define common date formats
    const val TIME_FORMAT_h_mm_a = "h:mm a"
    const val TIME_FORMAT_mm = "mm"
    const val TIME_FORMAT_hh_mm_a = "hh:mm a"  // 12-hour format
    const val TIME_FORMAT_HH_mm = "HH:mm"     // 24-hour format
    const val DATE_FORMAT_MMMM_yyyy = "MMMM yyyy"
    const val DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd"
    const val DATE_FORMAT_dd_MM_yyyy = "dd MM yyyy"
    const val DATE_FORMAT_dd_MM_yyyy_1 = "dd-MM-yyyy"
    const val DATE_FORMAT_dd_MMM_yyyy =  "dd MMM yyyy" // For "03 Dec 2024" format
    const val DATE_FORMAT_dd_MMM =  "dd MMM" // For "03 Dec" format
    const val DATE_TIME_FORMAT_yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm"
    const val DATE_TIME_FORMAT_yyyy_MM_dd_T_HH_MM_ss_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    /** Function to format Date object to String*/
    /** Get SimpleDateFormat for a given pattern */
    private fun getDateFormat(pattern: String): SimpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())

    /** Format Date -> String */
    fun dateToString(date: Date, pattern: String = DATE_FORMAT_yyyy_MM_dd): String {
        val format = getDateFormat(pattern)
        return format.format(date)
    }

    /** Convert Date -> Long (Timestamp) */
    fun dateToLong(date: Date): Long = date.time

    /** Parse String -> Date */
    fun stringToDate(dateString: String, pattern: String = DATE_FORMAT_yyyy_MM_dd): Date? {
        val format = getDateFormat(pattern)
        return try {
            format.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Parse String -> String */
    fun stringToString(dateString: String, inputPattern: String = DATE_FORMAT_yyyy_MM_dd, outputPattern: String = DATE_FORMAT_dd_MMM_yyyy): String {
        // Convert String -> Date using the inputPattern
        val date = stringToDate(dateString, inputPattern)
            ?: throw IllegalArgumentException("Invalid date string or pattern: $dateString, $inputPattern")

        // Convert Date -> String using the outputPattern
        return dateToString(date, outputPattern)
    }

    /** Convert String -> Long (Timestamp) */
    fun stringToLong(dateString: String, pattern: String = DATE_FORMAT_yyyy_MM_dd): Long {
        return stringToDate(dateString, pattern)?.time ?: 0L
    }

    /** Convert Long (Timestamp) -> Date */
    fun longToDate(timestamp: Long): Date = Date(timestamp)

    /** Convert Long (Timestamp) -> String */
    fun longToString(timestamp: Long, pattern: String = DATE_FORMAT_yyyy_MM_dd): String {
        val date = longToDate(timestamp)
        return dateToString(date, pattern)
    }

    fun formatDate(epochTime: Long): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_dd_MM_yyyy, Locale.getDefault())
        val date = Date(epochTime)
        return dateFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTimePeriod(startTimeEpoch: Long, endTimeEpoch: Long): Map<String, String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        val startInstant = Instant.ofEpochMilli(startTimeEpoch)
        val endInstant = Instant.ofEpochMilli(endTimeEpoch)

        // Convert Instant to LocalDateTime
        val startDateTime = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault())
        val endDateTime = LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault())

        // Calculate the difference using Period for years, months, and days
        val period = Period.between(startDateTime.toLocalDate(), endDateTime.toLocalDate())

        // Calculate the difference in seconds
        val duration = Duration.between(startDateTime, endDateTime)

        // Extract hours, minutes, and seconds from the duration
        val totalSeconds = duration.seconds
        val hours = totalSeconds / 3600 % 24
        val minutes = totalSeconds / 60 % 60
        val seconds = totalSeconds % 60

        // Format the start and end time to the required format
        val startFormatted = startDateTime.format(formatter)
        val endFormatted = endDateTime.format(formatter)

        // region Times gaps
        // Create the "total_time_gaps" formatted string
        //Todo:val totalTimeGaps = String.format("%02d Year, %02d Month, %02d Days, %02d:%02d:%02d hours", period.years, period.months, period.days, hours, minutes, seconds)
        // or
        // Build the "total_time_gaps" string dynamically
        val timeGaps = mutableListOf<String>()
        if (period.years > 0) timeGaps.add(String.format("%02d Year", period.years))
        if (period.months > 0) timeGaps.add(String.format("%02d Month", period.months))
        if (period.days > 0) timeGaps.add(String.format("%02d Days", period.days))
        if (hours > 0 || minutes > 0 || seconds > 0) {
            timeGaps.add(String.format("%02d:%02d:%02d hours", hours, minutes, seconds))
        }
        // If all components are zero, return "NA time duration"
        val totalTimeGaps = if (timeGaps.isEmpty()) "NA time duration" else timeGaps.joinToString(", ")
        //endregion

        // Return the results as a map
        return mapOf(
            "start_time" to startFormatted,
            "end_time" to endFormatted,
            "duration_hours" to (duration.toHours()).toString(),
            "duration_minutes" to (duration.toMinutes()).toString(),
            "years" to period.years.toString(),
            "months" to period.months.toString(),
            "days" to period.days.toString(),
            "hours" to hours.toString(),
            "minutes" to minutes.toString(),
            "seconds" to seconds.toString(),
            "total_time_gaps" to totalTimeGaps
        )
    }

    /**
     * Converts a date string to a formatted string with a zero-based month.
     *
     * This extension function takes a date string in the format "yyyy-MM-dd",
     * adjusts the month to be zero-based (0 for January, 11 for December),
     * and removes leading zeros from the day of the month.
     *
     * @receiver A string representing a date in the format "yyyy-MM-dd".
     * @return A string in the format "yyyy-M-d", where the month is zero-based
     *         and leading zeros are removed from the day.
     *
     * Example:
     * ```
     * val date = "2024-12-01"
     * val formattedDate = date.getFormattedDate()
     * println(formattedDate) // Output: "2024-11-1"
     * ```
     *
     * Note:
     * - This function assumes the input string is in a valid "yyyy-MM-dd" format.
     * - If the input string is not correctly formatted, it may throw an exception.
     */
    fun String.getFormattedDate(): String {
        val parts = this.split("-") // Split the date into year, month, and day
        val year = parts[0]
        val month = parts[1].toInt()// - 1 // Convert to zero-based month by subtracting 1
        val day = parts[2].toInt()       // Convert to integer to remove leading zeros
        return "$year-${month-1}-$day"       // Combine into the new format
    }

    /**
     * Merges the date and time represented by two separate epoch times into one combined epoch time.
     *
     * The `dateEpoch` represents the date part, and `timeEpoch` represents the time part. The function
     * combines these to produce a new epoch time that includes both the date and the time.
     *
     * For example:
     * - If `dateEpoch` represents "2024-12-03" at 00:00:00, and
     * - `timeEpoch` represents "12:30:00" on the same day,
     *
     * The result will be the epoch time corresponding to "2024-12-03 12:30:00".
     *
     * @param dateEpoch The epoch time representing the date part (in milliseconds).
     * @param timeEpoch The epoch time representing the time part (in milliseconds).
     * @return The combined epoch time that includes both date and time (in milliseconds).
     */
    fun mergeDateAndTime(dateEpoch: Long, timeEpoch: Long): Long {
        // Get the Calendar instance for the date
        val dateCalendar = Calendar.getInstance().apply {
            timeInMillis = dateEpoch
        }

        // Get the time parts from the timeEpoch
        val timeCalendar = Calendar.getInstance().apply {
            timeInMillis = timeEpoch
        }

        // Set the time of the dateCalendar to the time from timeEpoch
        dateCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        dateCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        dateCalendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
        dateCalendar.set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND))

        // Return the merged date and time as epoch
        return dateCalendar.timeInMillis
    }

    /**
     * Separates a combined epoch time into two separate epoch times: one for the date and one for the time.
     *
     * The `mergedEpoch` represents both the date and time. This function splits it into:
     * - The date part (midnight of the same day) and
     * - The time part (the exact time of the `mergedEpoch`).
     *
     * For example:
     * - If `mergedEpoch` represents "2024-12-03 12:30:00",
     *
     * The result will be:
     * - `dateEpoch` as the epoch time for "2024-12-03 00:00:00" (midnight),
     * - `timeEpoch` as the epoch time for "2024-12-03 12:30:00".
     *
     * @param mergedEpoch The combined epoch time (in milliseconds) representing both the date and the time.
     * @return A pair of epoch times:
     *         - The first element is the date part (midnight of the same day),
     *         - The second element is the time part.
     */
    fun separateDateTime(mergedEpoch: Long): Pair<Long, Long> {
        // Get the Calendar instance for the merged epoch
        val calendar = Calendar.getInstance().apply {
            timeInMillis = mergedEpoch
        }

        // Extract date part (without time)
        val dateCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0) // Set time to 00:00:00
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateEpoch = dateCalendar.timeInMillis // Epoch time for the date part

        // Extract time part (without date)
        val timeEpoch = calendar.timeInMillis // Time part will be the full epoch time

        return Pair(dateEpoch, timeEpoch)
    }

    /**
     * Calculates the start and end of the day (in epoch milliseconds) for a given timestamp.
     *
     * This function takes an epoch time (in milliseconds) as input and returns a pair of values:
     * - The start of the day (midnight: 00:00:00.000)
     * - The end of the day (just before midnight: 23:59:59.999)
     *
     * **Usage:**
     * Use this function when you need to retrieve the time boundaries for a specific day.
     * It is useful for scenarios such as:
     * - Filtering events, tasks, or data that belong to a particular day.
     * - Performing database queries or calculations for an entire day.
     * - Converting an epoch timestamp to the day's boundaries for time-sensitive operations.
     *
     * **Parameters:**
     * - `epochMillis`: A `Long` value representing the input epoch time in milliseconds.
     *   This timestamp will be used to calculate the day's boundaries.
     *
     * **Returns:**
     * - A `Pair<Long, Long>` where:
     *   - The first element is the epoch time for the start of the day (00:00:00.000).
     *   - The second element is the epoch time for the end of the day (23:59:59.999).
     *
     * **Example:**
     * ```
     * val epochTime = 1733250600000L // Example timestamp
     * val (startOfDay, endOfDay) = getStartAndEndOfDay(epochTime)
     *
     * println("Start of Day: $startOfDay") // Outputs: Start of Day: 1733250600000
     * println("End of Day: $endOfDay")     // Outputs: End of Day: 1733336999999
     * ```
     *
     * **Notes:**
     * - The function adjusts for the system's default timezone.
     * - Ensure the input epoch time is in milliseconds.
     * - Useful for working with calendars, events, and scheduling logic in applications.
     */
    fun getStartAndEndOfDay(epochMillis: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = epochMillis
        }

        // Start of the day (00:00:00)
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // End of the day (23:59:59)
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    /**
     * Converts an epoch time (in milliseconds) to a `Triple` containing the year, month, and day as strings.
     *
     * @param epochTime The epoch time in milliseconds (e.g., System.currentTimeMillis()).
     * @return A `Triple` where:
     *  - `first`: The year as a string (e.g., "2024").
     *  - `second`: The month as a string (1-based, e.g., "12" for December).
     *  - `third`: The day of the month as a string (e.g., "3").
     *
     * Example:
     * ```
     * val epochTime = 1733164200000L  // Epoch time for 03-Dec-2024
     * val dateTriple = epochToDateTriple(epochTime)
     * println("Year: ${dateTriple.first}, Month: ${dateTriple.second}, Day: ${dateTriple.third}")
     * // Output: Year: 2024, Month: 12, Day: 3
     * ```
     */
    fun epochToDateTriple(epochTime: Long): Triple<String, String, String> {
        val stringDate = longToString(epochTime)
        Log.i(TAG, "epochToDateTriple: $stringDate")
        return stringToDateTriple(stringDate)
    }

    /**
     * Converts a date string in the format "yyyy-MM-dd" into a `Triple` containing the year, month (0-based), and day.
     *
     * This function is useful when you need to break down a date string into its individual components (year, month, and day),
     * especially if you need to perform further operations on these parts, such as adjusting the month for 0-based indexing.
     *
     * User Case:
     * If you have a date string such as "2024-12-04" and want to separately access the year, month, and day as separate values,
     * this function will split the string and return the individual parts as a `Triple`.
     *
     * Example:
     * ```kotlin
     * val stringDate = "2024-12-04"
     * val dateTriple = stringToDateTriple(stringDate)
     * println(dateTriple)  // Output: (2024, 11, 4)
     * ```
     *
     * @param stringDate The date string to be split, expected in the format "yyyy-MM-dd".
     * @return A `Triple` where:
     *  - `first` is the year (e.g., "2024").
     *  - `second` is the month (e.g., "11" for December, adjusted from 1-based to 0-based).
     *  - `third` is the day (e.g., "4").
     *
     * @throws IndexOutOfBoundsException if the input string doesn't follow the "yyyy-MM-dd" format.
     */
    fun stringToDateTriple(stringDate:String): Triple<String, String, String>{
        return stringDate.split("-").let { parts ->
            val year = parts[0]
            val month = (parts[1].toInt() - 1).toString()  // Adjust month (1-based to 0-based)
            val day = parts[2].toInt().toString()  // Get the day as a string
            //Log.e(TAG, "collectState: ${item.start.date} -> $year,$month,$day")

            // Return a Triple with year, month, and day
            Triple(year, month, day)
        }
    }
    fun stringToDateTriple(stringDate: String, isZeroBased: Boolean = true): Triple<String, String, String> {
        return stringDate.split("-").let { parts ->
            val year = parts[0]
            val month = if (isZeroBased) {
                (parts[1].toInt() - 1).toString() // Convert 1-based to 0-based
            } else {
                parts[1] // Keep 1-based as is
            }
            val day = parts[2].toInt().toString() // Day as string

            // Return a Triple with year, month, and day
            Triple(year, month, day)
        }
    }

    /**
     * Splits a time string in the format "hh:mm a" into a `Triple` containing the hour, minute, and AM/PM parts.
     *
     * This function is useful when you need to extract and manipulate the individual components (hour, minute, AM/PM)
     * from a time string returned by functions like `longToString` that formats a timestamp into a time string.
     *
     * User Case:
     * If you have a time string such as "01:30 PM" and want to separately access the hour, minute, and AM/PM parts,
     * this function will break the string into a `Triple` and allow easy access to each component.
     *
     * Example:
     * ```kotlin
     * val timeString = "01:30 PM"
     * val timeTriple = splitTimeString(timeString)
     * println(timeTriple)  // Output: (01, 30, PM)
     * ```
     *
     * @param timeString The time string to be split, expected in the format "hh:mm a".
     * @return A `Triple` where:
     *  - `first` is the hour (e.g., "01").
     *  - `second` is the minute (e.g., "30").
     *  - `third` is the AM/PM part (e.g., "PM").
     * @throws IllegalArgumentException if the input time string is not in the expected format ("hh:mm a").
     */
    fun splitTimeString(timeString: String): Triple<String, String, String> {
        val timeParts = timeString.split(":", " ") // Split the string by ":" and space
        if (timeParts.size != 3) {
            throw IllegalArgumentException("Invalid time format. Expected format: hh:mm a")
        }
        return Triple(timeParts[0], timeParts[1], timeParts[2])
    }

    /**
     * Checks if the given time range is exactly 24 hours (all-day event).
     *
     * Use Case:
     * This function is useful when you need to determine if an event lasts a full day,
     * such as when handling all-day events in a calendar or scheduling system.
     *
     * When to Use:
     * Use this function when you want to check if the difference between the start time
     * and end time is exactly 24 hours, indicating that it is an all-day event.
     * This can be useful for event management where an all-day event needs special handling (e.g., no specific start or end time).
     *
     * How:
     * The function calculates the difference between the provided `startTime` and `endTime`
     * and checks if the duration is exactly 24 hours (i.e., 24 * 60 * 60 * 1000 milliseconds).
     *
     * Example:
     * val startTime = 1733337000000L // Example start time (milliseconds)
     * val endTime = 1733423399999L   // Example end time (milliseconds)
     *
     * val result = isAllDay(startTime, endTime)
     * println(result) // This will print "true" if the duration is exactly 24 hours, or "false" otherwise.
     *
     * Arguments:
     * - startTime: Long - The start time in milliseconds (epoch time).
     * - endTime: Long - The end time in milliseconds (epoch time).
     *
     * Return:
     * - Boolean - Returns `true` if the duration between `startTime` and `endTime` is exactly 24 hours, otherwise returns `false`.
     */
    fun isAllDay(startTime: Long, endTime: Long): Boolean {
        val durationInMillis = endTime - startTime
        val result = durationInMillis in (24 * 60 * 60 * 1000L - 1000)..(24 * 60 * 60 * 1000L + 1000)
        Log.i(TAG, (if (result) "This is an all-day event." else "This is not an all-day event."))
        return result
    }

    /**
    val firstApproachTime = measureExecutionTime {inside your block of code}
    Log.d(TAG, "First approach execution time: ${firstApproachTime / 1_000_000} ms")

    1 second (s) is equal to:
    1,000 milliseconds (ms)
    1,000,000 microseconds (µs)
    1,000,000,000 nanoseconds (ns)

    Log.d(TAG, "${(endTime - startTime)} ns")
    Log.d(TAG, "${(endTime - startTime) / 1_000} µs")
    Log.d(TAG, "${(endTime - startTime) / 1_000_000} ms")
    //Or
    val startTime = System.nanoTime()
    val endTime = System.nanoTime()
    Log.d(TAG, "execution time: ${(endTime - startTime)} ns, ${(endTime - startTime) / 1_000} µs, ${(endTime - startTime) / 1_000_000} ms")

     */
    fun measureExecutionTime(block: () -> Unit): Long {
        val startTime = System.nanoTime()
        block()
        val endTime = System.nanoTime()
        return endTime - startTime // Returns time in nanoseconds
    }


    /**
     * Converts the given number of minutes to milliseconds.
     *
     * @param minutes The number of minutes to convert.
     * @return The equivalent time in milliseconds.
     */
    fun minutesToTimestamp(minutes: Int): Long {
        return minutes * 60 * 1000L
    }

    /**
     * Converts the given time in milliseconds to minutes.
     *
     * @param milliseconds The time in milliseconds to convert.
     * @return The equivalent time in minutes.
     */
    fun timestampToMinutes(milliseconds: Long): Int {
        return (milliseconds / 60 / 1000).toInt()
    }

    // Get day name from date string
    fun getDayName(date: String, isShort: Boolean = false): String {
        val format = SimpleDateFormat(DATE_FORMAT_yyyy_MM_dd, Locale.getDefault())
        val parsedDate = format.parse(date)

        val dayFormat = if (isShort) "EEE" else "EEEE" // Choose the format based on the flag

        return SimpleDateFormat(dayFormat, Locale.getDefault()).format(parsedDate ?: Date())
    }
    fun getCurrentDate(pattern: String = DATE_FORMAT_yyyy_MM_dd): String {
        val format = getDateFormat(pattern)
        val calendar = Calendar.getInstance()
        return format.format(calendar.time) // Formats the current date to yyyy-MM-dd
    }
    // Get current date
    fun getCurrentDate(): Int { val calendar = Calendar.getInstance(); return calendar.get(Calendar.DATE) } // Gets the date of the month
    // Get current day string
    fun getCurrentDay(isShort: Boolean = false): String { return getDayName(getCurrentDate(pattern = DATE_FORMAT_yyyy_MM_dd), isShort = isShort) } // Gets the day of the month
    // Get current month int / String
    fun getCurrentMonth(isString: Boolean = false): Any { val calendar = Calendar.getInstance(); val month = calendar.get(Calendar.MONTH);return if (isString) DateFormatSymbols().months[month] else month } // Gets the month (0 OR January) of the year
    // Get current year
    fun getCurrentYear(): Int { val calendar = Calendar.getInstance(); return calendar.get(Calendar.YEAR) } // Gets the year

    // Get week range (e.g., "5-11 Jan 2025 | 26 Jan - 1 Feb 2025 | 29 Dec 2024 - 4 Jan 2025")
    fun getWeekRange(startDate: String, startOfWeek: Int): String {
        val format = SimpleDateFormat(DATE_FORMAT_dd_MM_yyyy_1, Locale.getDefault())
        val parsedDate = format.parse(startDate)
        val calendar = Calendar.getInstance().apply { time = parsedDate ?: Date() }

        // Adjust calendar to the start of the week
        calendar.firstDayOfWeek = startOfWeek
        while (calendar.get(Calendar.DAY_OF_WEEK) != startOfWeek) {
            calendar.add(Calendar.DAY_OF_WEEK, -1)
        }
        val startOfWeekDate = calendar.time

        // Calculate the end of the week
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeekDate = calendar.time

        // Format start and end of the week
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault()) // 3-letter month
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val startDay = dayFormat.format(startOfWeekDate)
        val endDay = dayFormat.format(endOfWeekDate)
        val startMonth = monthFormat.format(startOfWeekDate)
        val endMonth = monthFormat.format(endOfWeekDate)
        val startYear = yearFormat.format(startOfWeekDate)
        val endYear = yearFormat.format(endOfWeekDate)

        // Check if the week spans across months or years
        return when {
            startMonth == endMonth -> { // Same month
                "$startDay-$endDay $startMonth $startYear"
            }
            startYear == endYear -> { // Different months within the same year
                "$startDay $startMonth - $endDay $endMonth $startYear"
            }
            else -> { // Week spans across years
                "$startDay $startMonth $startYear - $endDay $endMonth $endYear"
            }
        }
    }

    fun getMonthWeekRanges(monthYear: String, startOfWeek: Int): List<String> {
        val format = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val parsedDate = format.parse(monthYear)
        val calendar = Calendar.getInstance().apply {
            time = parsedDate ?: Date()
            set(Calendar.DAY_OF_MONTH, 1) // Set to the first day of the month
        }

        val result = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Adjust calendar to the start of the week for the first day of the month
        calendar.firstDayOfWeek = startOfWeek
        while (calendar.get(Calendar.DAY_OF_WEEK) != startOfWeek) {
            calendar.add(Calendar.DAY_OF_WEEK, -1)
        }

        // Generate week ranges for the given month
        while (calendar.get(Calendar.MONTH) <= currentMonth || calendar.get(Calendar.YEAR) < currentYear) {
            val startOfWeekDate = calendar.time

            // Calculate the end of the week
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeekDate = calendar.time

            val startDay = dayFormat.format(startOfWeekDate)
            val endDay = dayFormat.format(endOfWeekDate)
            val startMonth = monthFormat.format(startOfWeekDate)
            val endMonth = monthFormat.format(endOfWeekDate)
            val startYear = yearFormat.format(startOfWeekDate)
            val endYear = yearFormat.format(endOfWeekDate)

            if (calendar.get(Calendar.MONTH) > currentMonth && calendar.get(Calendar.YEAR) == currentYear) break
            if (calendar.get(Calendar.YEAR) > currentYear) break

            // Format the week range
            val weekRange = when {
                startMonth == endMonth -> "$startDay-$endDay $startMonth $startYear"
                else -> "$startDay $startMonth - $endDay $endMonth $startYear"
            }

            // Include only ranges that belong to the target month
            if (calendar.get(Calendar.MONTH) == currentMonth || calendar.time <= calendar.time) {
                result.add(weekRange)
            }

            // Move to the next week
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }

        return result
    }


    // Function to get the week number of the year based on the provided date format
    fun getWeekOfYear(startDate: String, dateFormat: String): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android O (API 26) and above, use java.time.LocalDate and DateTimeFormatter
            val formatter = DateTimeFormatter.ofPattern(dateFormat)
            val date = LocalDate.parse(startDate, formatter)

            // Using IsoFields.WEEK_OF_WEEK_BASED_YEAR for API >= 26
            date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        } else {
            // For versions below Android O, use SimpleDateFormat
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = formatter.parse(startDate)

            // Use Calendar to get the week number of the year
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                // Using Calendar.WEEK_OF_YEAR for versions below API 26
                return calendar.get(Calendar.WEEK_OF_YEAR)
            } else {
                // Handle the case when date parsing fails (return a default value or throw an exception)
                throw IllegalArgumentException("Invalid date format or date.")
            }
        }
    }

    // Function to extract the month name from a date string
    fun getMonthName(dateString: String, dateFormat: String): String {
        val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
        } else {
            "" // Return an empty string if date parsing fails
        }
    }

    /**
     * Extracts the year and month (as 0-based index) from a formatted string.
     *
     * @param s A string in the format "MMMM yyyy" (e.g., "January 2025").
     * @return A Pair containing the year (Int) and month (Int, 0-based index) if parsing is successful, or null if it fails.
     *
     * Example:
     * Input: "January 2025"
     * Output: Pair(2025, 0) // January is 0-based
     */
    fun reverseYearMonth(s: String, pattern: String = DATE_FORMAT_MMMM_yyyy): Pair<Int, Int>? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault()) // Pattern for "Month Year"
        return try {
            val date = dateFormat.parse(s)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) // Months are 0-based
            Pair(year, month)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if parsing fails
        }
    }

    /**
     * Calculates the current time and the time a specified number of days into the future.
     *
     * @param daysInFuture The number of days from the current date to calculate the future time. Defaults to 365 days.
     * @return A Pair<Long, Long> where:
     *         - first: The current time in milliseconds since epoch.
     *         - second: The time in milliseconds corresponding to the specified number of days into the future.
     */
    fun getCurrentAndFutureRange(daysInFuture: Int = 365): Pair<Long, Long> {
        val currentCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        endCalendar.add(Calendar.DAY_OF_YEAR, daysInFuture)

        val currentTime = currentCalendar.timeInMillis
        val endTime = endCalendar.timeInMillis

        return Pair(currentTime, endTime)
    }

}
