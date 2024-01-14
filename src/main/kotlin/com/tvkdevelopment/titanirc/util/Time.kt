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
        DurationString.options
            .takeIf { timePassed >= minimumTimePassed }
            ?.firstNotNullOfOrNull { it.get(timePassed, short) }

    private fun interface DurationString {
        fun get(duration: Duration, short: Boolean): String?

        class SimpleDurationString(
            private val base: String,
            private val unit: DurationUnit,
            private val minimum: Duration,
        ) : DurationString {

            override fun get(duration: Duration, short: Boolean) =
                duration
                    .takeIf { it >= minimum }
                    ?.toInt(unit)
                    ?.let {
                        when {
                            short -> "$it${base.take(1)}"
                            it != 1 -> "$it ${base}s"
                            else -> "$it $base"
                        }
                    }
        }

        companion object {
            val options = listOf(
                SimpleDurationString("day", DurationUnit.DAYS, 1.days),
                SimpleDurationString("hour", DurationUnit.HOURS, 1.hours),
                SimpleDurationString("minute", DurationUnit.MINUTES, 1.minutes),
                SimpleDurationString("second", DurationUnit.SECONDS, 0.seconds),
            )
        }
    }
}