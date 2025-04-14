package com.hardik.messageapp.util

import com.hardik.messageapp.R

object IcPlaceholderHelper {
    // ic_placeholder_
    private val placeholderMap = mapOf(
        'a' to R.drawable.ic_placeholder_a,
        'b' to R.drawable.ic_placeholder_b,
        'c' to R.drawable.ic_placeholder_c,
        'd' to R.drawable.ic_placeholder_d,
        'e' to R.drawable.ic_placeholder_e,
        'f' to R.drawable.ic_placeholder_f,
        'g' to R.drawable.ic_placeholder_g,
        'h' to R.drawable.ic_placeholder_h,
        'i' to R.drawable.ic_placeholder_i,
        'j' to R.drawable.ic_placeholder_j,
        'k' to R.drawable.ic_placeholder_k,
        'l' to R.drawable.ic_placeholder_l,
        'm' to R.drawable.ic_placeholder_m,

        'n' to R.drawable.ic_placeholder_a,
        'o' to R.drawable.ic_placeholder_b,
        'p' to R.drawable.ic_placeholder_c,
        'q' to R.drawable.ic_placeholder_d,
        'r' to R.drawable.ic_placeholder_e,
        's' to R.drawable.ic_placeholder_f,
        't' to R.drawable.ic_placeholder_g,
        'u' to R.drawable.ic_placeholder_h,
        'v' to R.drawable.ic_placeholder_i,
        'w' to R.drawable.ic_placeholder_j,
        'x' to R.drawable.ic_placeholder_k,
        'y' to R.drawable.ic_placeholder_l,
        'z' to R.drawable.ic_placeholder_m
    )

    private fun getPlaceholderDrawable(letter: Char): Pair<Char,Int> {
        return Pair(letter, placeholderMap[letter.lowercaseChar()] ?: R.drawable.real_ic_user)
    }

    fun getPlaceholderDrawable(name: String): Pair<Char, Int> {
        val firstChar = name.trim().firstOrNull()?.lowercaseChar() ?: return Pair('?', R.drawable.real_ic_user)
        return getPlaceholderDrawable(firstChar)
    }

    val placeholderList = listOf(
        R.drawable.ic_placeholder_a,
        R.drawable.ic_placeholder_b,
        R.drawable.ic_placeholder_c,
        R.drawable.ic_placeholder_d,
        R.drawable.ic_placeholder_e,
        R.drawable.ic_placeholder_f,
        R.drawable.ic_placeholder_g,
        R.drawable.ic_placeholder_h,
        R.drawable.ic_placeholder_i,
        R.drawable.ic_placeholder_j,
        R.drawable.ic_placeholder_k,
        R.drawable.ic_placeholder_l,
        R.drawable.ic_placeholder_m,
    )
}
//(colorList[(possion % 12)])