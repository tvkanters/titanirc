package com.tvkdevelopment.titanirc.util

fun <T> T?.ifNull(fallback: () -> T) : T =
    this ?: fallback()