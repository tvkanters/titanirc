package com.tvkdevelopment.titanirc.util

fun <T> T?.ifNull(fallback: () -> T) : T =
    this ?: fallback()

private operator fun IntRange.contains(other: IntRange) =
    other.first in this && other.last in this
