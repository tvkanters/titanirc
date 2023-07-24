package com.tvkdevelopment.titanirc.util

fun <T> T?.ifNull(fallback: () -> T) : T =
    this ?: fallback()

private operator fun IntRange.contains(other: IntRange) =
    other.first in this && other.last in this

fun String.limitLength(limit: Int): String =
    when {
        length > limit -> substring(0, limit)
        else -> this
    }
