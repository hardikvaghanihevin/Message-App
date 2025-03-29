package com.hardik.messageapp.helper

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

fun analyzeSender(sender: String?): Int = when {
    sender.isNullOrBlank() -> -1
    sender.any { it.isLetter() } -> 2
    sender.any { it.isDigit() } -> 1
    else -> -1
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

data class Quad<T>(val first: T, val second: T, val third: T, val fourth: T)
