package com.hardik.messageapp.util

import android.content.Context
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.hardik.messageapp.R
import com.hardik.messageapp.domain.model.Contact
import com.hardik.messageapp.domain.model.ConversationThread
import com.hardik.messageapp.domain.repository.FindThreadIdByNormalizeNumber
import com.hardik.messageapp.presentation.custom_view.PopupMenu
import com.hardik.messageapp.util.Constants.BASE_TAG
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.Semaphore

/*
override fun getPhoneNumberByRecipientId(recipientId: String): Flow<String?> = flow {
    val uri = Uri.parse("content://mms-sms/canonical-addresses")
    val projection = arrayOf("_id", "address")
    val selection = "_id = ?"
    val selectionArgs = arrayOf(recipientId)

    val phoneNumber = context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("address"))
        } else null
    }
    Log.i(Constants.BASE_TAG, "getPhoneNumberByRecipientId: $phoneNumber")
    emit(phoneNumber) // Emit either a phone number or null
}.flowOn(Dispatchers.IO)

override fun getContactNameByPhoneNumber(phoneNumber: String):  Flow<String?> = flow {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        emit(null) // Return null if permission is not granted
        return@flow
    }

    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
    val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
    val selectionArgs = arrayOf("%$phoneNumber%") // Using LIKE to handle different formats

    val contactName = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
        } else null
    }

    Log.v(Constants.BASE_TAG, "getContactNameByPhoneNumber: $contactName", )
    emit(contactName)
}.flowOn(Dispatchers.IO)

@OptIn(ExperimentalCoroutinesApi::class)
override fun getPhoneNumberAndContactNameByRecipientId(context: Context, recipientId: String): Flow<Pair<String?, String?>> =
    flow {
        if (recipientId.isBlank()) {
            emit(Pair(null, null))
            return@flow
        }
        emitAll(getPhoneNumberByRecipientId(recipientId)
            .flatMapLatest { phoneNumber ->
                if (phoneNumber != null) {
                    getContactNameByPhoneNumber(phoneNumber)
                        .map { contactName -> Pair(contactName, phoneNumber) }
                } else {
                    flowOf(Pair(null, null))
                }
            })
    }.flowOn(Dispatchers.IO)

private fun getPhoneNumberFromRecipientId(recipientIds: String): String {
    val uri = Uri.parse("content://mms-sms/canonical-addresses")
    val projection = arrayOf("_id", "address")
    val formattedIds = recipientIds.split(" ").joinToString(",") { it.trim() }
    val cursor: Cursor? = context.contentResolver.query(
        uri, projection, "_id IN ($formattedIds)", null, null
    )
    val phoneNumbers = mutableListOf<String>()
    cursor?.use {
        while (it.moveToNext()) {
            phoneNumbers.add(it.getString(it.getColumnIndexOrThrow("address")))
        }
    }
    return phoneNumbers.joinToString(", ")
}*/
fun convertPxToDpSp(context: Context, px: Float): Pair<Float, Float> {
    val displayMetrics = context.resources.displayMetrics

    // Convert px to dp
    val dp = px / (displayMetrics.densityDpi / 160f)

    // Convert px to sp (considering scaledDensity for user font preferences)
    val sp = px / displayMetrics.scaledDensity

    return Pair((dp/1.3911f), (sp/1.6155F))
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun analyzeSender(sender: String?): Int = when {
    sender.isNullOrBlank() -> -1
    sender.any { it.isLetter() } -> 2
    sender.any { it.isDigit() } -> 1
    else -> -1
}
val countryPrefixes = listOf(
    "+1",    // USA, Canada
    "+7",    // Russia, Kazakhstan
    "+20",   // Egypt
    "+27",   // South Africa
    "+30",   // Greece
    "+31",   // Netherlands
    "+32",   // Belgium
    "+33",   // France
    "+34",   // Spain
    "+36",   // Hungary
    "+39",   // Italy
    "+40",   // Romania
    "+41",   // Switzerland
    "+43",   // Austria
    "+44",   // UK
    "+45",   // Denmark
    "+46",   // Sweden
    "+47",   // Norway
    "+48",   // Poland
    "+49",   // Germany
    "+51",   // Peru
    "+52",   // Mexico
    "+53",   // Cuba
    "+54",   // Argentina
    "+55",   // Brazil
    "+56",   // Chile
    "+57",   // Colombia
    "+58",   // Venezuela
    "+60",   // Malaysia
    "+61",   // Australia
    "+62",   // Indonesia
    "+63",   // Philippines
    "+64",   // New Zealand
    "+65",   // Singapore
    "+66",   // Thailand
    "+81",   // Japan
    "+82",   // South Korea
    "+84",   // Vietnam
    "+86",   // China
    "+90",   // Turkey
    "+91",   // India
    "+92",   // Pakistan
    "+93",   // Afghanistan
    "+94",   // Sri Lanka
    "+95",   // Myanmar
    "+98",   // Iran
    "+212",  // Morocco
    "+213",  // Algeria
    "+216",  // Tunisia
    "+218",  // Libya
    "+220",  // Gambia
    "+221",  // Senegal
    "+234",  // Nigeria
    "+237",  // Cameroon
    "+240",  // Equatorial Guinea
    "+241",  // Gabon
    "+251",  // Ethiopia
    "+254",  // Kenya
    "+255",  // Tanzania
    "+256",  // Uganda
    "+260",  // Zambia
    "+263",  // Zimbabwe
    "+264",  // Namibia
    "+265",  // Malawi
    "+267",  // Botswana
    "+268",  // Eswatini
    "+971",  // UAE
    "+972",  // Israel
    "+973",  // Bahrain
    "+974",  // Qatar
    "+975",  // Bhutan
    "+976",  // Mongolia
    "+977",  // Nepal
    "+992",  // Tajikistan
    "+993",  // Turkmenistan
    "+994",  // Azerbaijan
    "+995",  // Georgia
    "+996",  // Kyrgyzstan
    "+998"   // Uzbekistan
)

fun String.removeCountryPrefix(): String {
    var cleanedNumber = replace("\\s".toRegex(), "") // Remove spaces

    for (prefix in countryPrefixes) {
        if (cleanedNumber.startsWith(prefix)) {
            cleanedNumber = cleanedNumber.removePrefix(prefix)
            break // Stop after finding the matching prefix
        }
    }
    return cleanedNumber
}
fun normalizePhoneNumber(phoneNumber: String?): String {
    if (phoneNumber.isNullOrEmpty()) {
        return ""
    }
    var normalized = phoneNumber.replace("[^0-9]".toRegex(), "")
    if (normalized.startsWith("0") && normalized.length > 10) {
        normalized = normalized.substring(1)
    }
    if (normalized.startsWith("91") && normalized.length > 10) {
        normalized = normalized.substring(2)
    }
    return normalized
}

fun String.removeCountryCode(phoneInstance: PhoneNumberUtil): String {
    return try {
        val phoneNumber = phoneInstance.parse(this, null)
        var nationalNumber =
            phoneInstance.getNationalSignificantNumber(phoneNumber).replace(" ", "")
                .replace("-", "")
                .replace("(", "").replace(")", "")
        if (nationalNumber.startsWith("0")) {
            nationalNumber = nationalNumber.substring(1)
        }
        nationalNumber
    } catch (e: Exception) {
        var cleanedNumber = this.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        if (cleanedNumber.startsWith("0")) {
            cleanedNumber = cleanedNumber.substring(1)
        }
        cleanedNumber
    }
}


/**
 * This function for home bottom menu show option*/
fun evaluateSelectionGetHomeBottomMenu(list: List<ConversationThread>): PopupMenu {
    val allRead = list.all { it.read }
    val hasUnread = list.any { !it.read }
    //val allUnread = list.all { !it.read }

    val allPin = list.all { it.isPin }
    val hasUnpin = list.any { !it.isPin }
    //val allUnpin = list.all { !it.isPin }

    return when {
        allRead && allPin -> PopupMenu.HOME_UNREAD_UNPIN_BLOCK
        allRead && hasUnpin -> PopupMenu.HOME_UNREAD_PIN_BLOCK
        hasUnread && allPin -> PopupMenu.HOME_READ_UNPIN_BLOCK
        hasUnread && hasUnpin -> PopupMenu.HOME_READ_PIN_BLOCK
        else -> PopupMenu.HOME_READ_PIN_BLOCK
    }
}

fun evaluateSelectionGetHomeToolbarMenu(list: List<ConversationThread>): PopupMenu {
    val allRead = list.all { it.read }
    val hasUnread = list.any { !it.read }

    return when {
        allRead -> PopupMenu.HOME_UNLESS_READ
        hasUnread -> PopupMenu.HOME_ALL
        else -> PopupMenu.HOME_ALL
    }
}

/**
 * Calculates the optimal chunk size for batch operations (e.g., ContentResolver updates)
 * based on the total number of thread IDs to be processed.
 *
 * Why it matters:
 * - Performing operations on very large datasets all at once (e.g., 1 lakh threads) can
 *   cause performance bottlenecks, ANRs, or even binder transaction failures due to
 *   system-level limitations (IPC, SQLite limits).
 * - Smaller datasets benefit from less batching, avoiding the overhead of unnecessary loops.
 *
 * @param size Total number of thread IDs to process.
 * @return The ideal chunk size for batching updates.
 */
fun getOptimalChunkSize(size: Int): Int {
    return when {
        size >= 100_000 -> 1000   // For very large datasets, larger chunks reduce IPC overhead
        size >= 50_000 -> 500     // Balanced chunk size for mid-large datasets
        size >= 10_000 -> 300     // Safe and efficient for mid-sized datasets
        size >= 1000 -> 100       // Works well for moderately sized thread sets
        size >= 500 -> 50         // Safer for smaller batches with minimal delay
        size > 0 -> size          // For very small sets, use the actual size,  no need to chunk
        else -> 1                 // When size is 0 or negative, fallback to minimum safe chunk size
    }
}

suspend fun <T> Flow<List<T>>.flattenToList(): List<T> {
    val result = mutableListOf<T>()
    collect { chunk -> result += chunk }
    return result
}

suspend fun <K, V> Flow<Map<K, V>>.flattenToMap(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    collect { chunk -> result.putAll(chunk) }
    return result
}


fun String.firstUppercase(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}


fun getBestMatchedNumber(phoneNumbers: List<String>?, query: String): String? {
    if (phoneNumbers.isNullOrEmpty() || query.isEmpty()) return null

    return phoneNumbers
        .filter { it.contains(query, ignoreCase = true) }
        .maxByOrNull { number ->
            val matchIndex = number.indexOf(query, ignoreCase = true)
            if (matchIndex >= 0) query.length else 0
        }
}


fun extractNumber(contact: Contact?, query: String, phoneNumberUtil: PhoneNumberUtil): String {
    return (getBestMatchedNumber(contact?.phoneNumbers, query)?.removeCountryCode(phoneNumberUtil)) ?: contact?.normalizeNumber.takeUnless { it.isNullOrEmpty() } ?: query
}

fun resolveThreadId(context: Context, normalizeNumber: String?): Long {
    return if (!normalizeNumber.isNullOrEmpty()) {
        val findThreadId = object : FindThreadIdByNormalizeNumber {}.findThreadIdByNormalizeNumber(context, normalizeNumber)
        findThreadId
    } else {
        0
    }
}

suspend inline fun <T> retry(times: Int, block: () -> T): T {
    var attempt = 0
    var lastError: Throwable? = null

    while (attempt < times) {
        try {
            return block()
        } catch (e: Throwable) {
            lastError = e
            attempt++
            Log.w(BASE_TAG, "Retry attempt $attempt failed.")
            delay(100L) // small delay before retry
        }
    }
    throw lastError ?: RuntimeException("Unknown error during retry")
}


suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
    withContext(Dispatchers.IO) {
        this@withPermit.acquire()
    } // Acquire a permit
    return try {
        block() // Execute the block of code
    } finally {
        this.release() // Release the permit
    }
}

/**
 * Applies a shape appearance style and optional stroke to this ShapeableImageView
 * from provided resource IDs.
 *
 * @param shapeAppearanceOverlayResId The resource ID of a ShapeAppearanceOverlay style
 * defining the desired shape. Can be null to only apply stroke.
 * @param strokeColorResId The resource ID of a color or color state list for the stroke color.
 * Can be null if no stroke color is to be applied or changed.
 * @param strokeWidthDimenResId The resource ID of a dimension for the stroke width.
 * Can be null if no stroke width is to be applied or changed.
 */
fun ShapeableImageView.applyStyledShape(
    @StyleRes shapeAppearanceOverlayResId: Int? = null,
    @ColorRes strokeColorResId: Int? = R.color.white,
    @DimenRes strokeWidthDimenResId: Int? = R.dimen.app_px_0_to_dp,
    @DimenRes paddingStartResId: Int? = null,
    @DimenRes paddingEndResId: Int? = null
) {
    // Apply shape appearance if a style resource ID is provided
    shapeAppearanceOverlayResId?.let { resId ->
        // Build the ShapeAppearanceModel from the provided ShapeAppearanceOverlay style resource.
        // This builder reads shape-related attributes (cornerFamily, cornerSize, etc.)
        // from the specified style resource ID.
        val shapeAppearanceModel = ShapeAppearanceModel.builder(
            context,        // Use the view's context
            null,           // AttributeSet is null when applying programmatically without inflation
            0,              // defStyleAttr is typically 0 in this case
            resId           // The resource ID of the ShapeAppearanceOverlay style
        ).build()

        // Apply the built ShapeAppearanceModel to this ShapeableImageView
        this.shapeAppearanceModel = shapeAppearanceModel
    }

    // Apply stroke color if a resource ID is provided
    strokeColorResId?.let { resId ->
        try {
            this.strokeColor = ContextCompat.getColorStateList(context, resId)
        } catch (e: Exception) {
            // Handle potential errors if the resource ID is not a valid color state list
            e.printStackTrace()
        }
    }

    // Apply stroke width if a dimension resource ID is provided
    strokeWidthDimenResId?.let { resId ->
        try {
            this.strokeWidth = context.resources.getDimension(resId)
        } catch (e: Exception) {
            // Handle potential errors if the resource ID is not a valid dimension
            e.printStackTrace()
        }
    }

    // Apply individual paddings (preserving existing ones)
    val start = paddingStartResId?.let { context.resources.getDimensionPixelSize(it) } ?: this.paddingStart
    val end = paddingEndResId?.let { context.resources.getDimensionPixelSize(it) } ?: this.paddingEnd

    // Apply padding while keeping top and bottom the same
    setPaddingRelative(start, paddingTop, end, paddingBottom)

    // NOTE: This extension function specifically handles the shape and stroke
    // properties controlled by ShapeableImageView and ShapeAppearanceModel.
    // It DOES NOT apply other standard ImageView or View properties
    // like android:background, android:src, android:scaleType, padding, etc.,
    // even if they are defined in the style resource you pass.
    // You would need to set those properties separately if your style includes them
    // and you want to apply them programmatically.
}
