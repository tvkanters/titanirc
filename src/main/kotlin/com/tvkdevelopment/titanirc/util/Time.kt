package com.tvkdevelopment.titanirc.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

object Time {
    val currentTime: Instant
        get() = Clock.System.now()

    fun getRelativeTimeString(timePassed: Duration, short: Boolean, minimumTimePassed: Duration = 0.seconds): String? =
        when {
            timePassed < minimumTimePassed -> null
            timePassed >= 2.days -> "${timePassed.toInt(DurationUnit.DAYS)}${getDayString(plural = true, short)} ago"
            timePassed >= 1.days -> "1${getDayString(plural = false, short)} ago"
            timePassed >= 2.hours -> "${timePassed.toInt(DurationUnit.HOURS)}${getHourString(plural = true, short)} ago"
            timePassed >= 1.hours -> "1${getHourString(plural = false, short)} ago"
            timePassed >= 2.minutes -> "${timePassed.toInt(DurationUnit.MINUTES)}${getMinuteString(plural = true, short)} ago"
            timePassed >= 1.minutes -> "1${getMinuteString(plural = false, short)} ago"
            else -> "just now"
        }

    private fun getDayString(plural: Boolean, short: Boolean) =
        when {
            short -> "d"
            plural -> " days"
            else -> " day"
        }

    private fun getHourString(plural: Boolean, short: Boolean) =
        when {
            short -> "h"
            plural -> " hours"
            else -> " hour"
        }

    private fun getMinuteString(plural: Boolean, short: Boolean) =
        when {
            short -> "m"
            plural -> " minutes"
            else -> " minute"
        }
}