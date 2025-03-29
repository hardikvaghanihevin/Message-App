package com.hardik.messageapp.domain.model

import com.google.gson.Gson

data class BlockedNumber(
    val number: String
) {
    companion object {
        fun List<BlockedNumber>.toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }
    }
}
