package com.hardik.messageapp.domain.repository

import android.content.Context
import android.provider.Telephony

/**
 * Interface for finding or creating a thread ID based on a normalized phone number.
 */
interface FindThreadIdByNormalizeNumber {

    /**
     * Finds or creates a thread ID for a given normalized phone number.
     *
     * @param context The Android Context.
     * @param normalizeNumber The normalized phone number (e.g., without spaces, dashes, etc.).
     * @return The thread ID (Long), or 0L if an error occurs.
     */
    fun findThreadIdByNormalizeNumber(context: Context, normalizeNumber: String): Long {
        return try {
            Telephony.Threads.getOrCreateThreadId(context, setOf(normalizeNumber))
        } catch (e: Exception) {
            0L // Return 0L in case of an exception.
        }
    }

    /**
     * Example Usage (Anonymous Object with Implementation):
     *
     * This example demonstrates how to create an anonymous object that implements
     * the FindThreadIdByNormalizeNumber interface and overrides the
     * findThreadIdByNormalizeNumber method to provide the actual implementation.
     *
     * Usage:
       val findThreadId = object : FindThreadIdByNormalizeNumber {
          override fun findThreadIdByNormalizeNumber(context: Context, normalizeNumber: String): Long {
              return try {
                  Telephony.Threads.getOrCreateThreadId(context, setOf(normalizeNumber))
              } catch (e: Exception) {
                  0L
              }
          }
       }.findThreadIdByNormalizeNumber(this, normalizeNumber) // 'this' must be a valid Context
     *
     * Note: 'this' refers to the current Context (e.g., Activity or Fragment).
     * 'normalizeNumber' is the phone number string.
     */
    // Example 1: Anonymous Object with Implementation (Correct Usage)

    /**
     * Example Usage (Anonymous Object without Implementation - INCORRECT):
     *
     * This example shows what happens when you create an anonymous object
     * that implements FindThreadIdByNormalizeNumber but DO NOT override the
     * findThreadIdByNormalizeNumber method.
     *
     * This is incorrect and will cause a crash at runtime because the
     * interface method is not implemented, and there is no default implementation.
     *
     * Usage (DO NOT USE THIS):
       val findThreadId = object : FindThreadIdByNormalizeNumber {}.findThreadIdByNormalizeNumber(this, normalizeNumber)
     *
     * Note: This will result in a runtime error.
     */
    // Example 2: Anonymous Object without Implementation (Incorrect Usage)
}