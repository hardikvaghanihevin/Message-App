package com.hardik.messageapp.presentation.util

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
        'n' to R.drawable.ic_placeholder_n,
        'o' to R.drawable.ic_placeholder_o,
        'p' to R.drawable.ic_placeholder_p,
        'q' to R.drawable.ic_placeholder_q,
        'r' to R.drawable.ic_placeholder_r,
        's' to R.drawable.ic_placeholder_s,
        't' to R.drawable.ic_placeholder_t,
        'u' to R.drawable.ic_placeholder_u,
        'v' to R.drawable.ic_placeholder_v,
        'w' to R.drawable.ic_placeholder_w,
        'x' to R.drawable.ic_placeholder_x,
        'y' to R.drawable.ic_placeholder_y,
        'z' to R.drawable.ic_placeholder_z
    )

    private fun getPlaceholderDrawable(letter: Char): Pair<Char,Int> {
        return Pair(letter, placeholderMap[letter.lowercaseChar()] ?: R.drawable.ic_user)
    }

    fun getPlaceholderDrawable(name: String): Pair<Char, Int> {
        val firstChar = name.trim().firstOrNull()?.lowercaseChar() ?: return Pair('?', R.drawable.ic_user)
        return getPlaceholderDrawable(firstChar)
    }
}