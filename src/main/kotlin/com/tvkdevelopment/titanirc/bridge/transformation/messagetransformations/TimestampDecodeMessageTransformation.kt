package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.util.Time
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.*
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TimestampDecodeMessageTransformation :
    MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        return message
            .replace(REGEX_TIMESTAMP) { match ->
                val instant = match.groupValues[1]
                    .toLongOrNull()
                    ?.let { Instant.fromEpochSeconds(it) }
                    ?: return@replace match.value

                match.groupValues[2].let { key -> Format.entries.firstOrNull { it.key == key } }
                    ?.format(instant)
                    ?: match.value
            }
    }

    companion object {
        private enum class Format(val key: String, val format: (Instant) -> String) {
            NUMERIC_DATETIME_WITH_SECONDS(
                "S",
                LocalDateTime.Format {
                    dateNumeric()
                    char(' ')
                    timeSeconds()
                }
            ),
            NUMERIC_DATETIME(
                "s",
                LocalDateTime.Format {
                    dateNumeric()
                    char(' ')
                    time()
                }
            ),
            WRITTEN_DATETIME(
                "f",
                LocalDateTime.Format {
                    dateWritten()
                    char(' ')
                    timeSeconds()
                }
            ),
            WRITTEN_DATETIME_WITH_DAY(
                "F",
                LocalDateTime.Format {
                    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
                    chars(", ")
                    dateWritten()
                    char(' ')
                    timeSeconds()
                }
            ),
            NUMERIC_DATE(
                "d",
                LocalDateTime.Format {
                    dateNumeric()
                },
                timezone = false,
            ),
            WRITTEN_DATE(
                "D",
                LocalDateTime.Format {
                    dateWritten()
                },
                timezone = false,
            ),
            TIME_WITH_SECONDS(
                "T",
                LocalDateTime.Format {
                    timeSeconds()
                }
            ),
            TIME(
                "t",
                LocalDateTime.Format {
                    time()
                }
            ),
            RELATIVE(
                "R",
                { Time.getRelativeTimeString(instant = it, short = false) }
            );

            constructor(key: String, format: DateTimeFormat<LocalDateTime>, timezone: Boolean = true) :
                this(
                    key = key,
                    format = { instant ->
                        val base = format.format(instant.toLocalDateTime(TIMEZONE))
                        when {
                            timezone -> base + instant.getTimeZoneSuffix(TIMEZONE)
                            else -> base
                        }
                    }
                )
        }

        private val TIMEZONE = TimeZone.of("Europe/Amsterdam")
        private fun Instant.getTimeZoneSuffix(zone: TimeZone) =
            when (zone.offsetAt(this).totalSeconds.seconds.inWholeHours) {
                1L -> " CET"
                2L -> " CEST"
                else -> ""
            }

        private fun DateTimeFormatBuilder.WithDateTime.dateNumeric() {
            year()
            char('-')
            monthNumber()
            char('-')
            day()
        }

        private fun DateTimeFormatBuilder.WithDateTime.dateWritten() {
            day()
            char(' ')
            monthName(MonthNames.ENGLISH_FULL)
            char(' ')
            year()
        }

        private fun DateTimeFormatBuilder.WithDateTime.time() {
            hour()
            char(':')
            minute()
        }

        private fun DateTimeFormatBuilder.WithDateTime.timeSeconds() {
            hour()
            char(':')
            minute()
            char(':')
            second()
        }

        private val REGEX_TIMESTAMP =
            Regex(
                """<t:(\d+):(\w+)>""",
                RegexOption.IGNORE_CASE
            )
    }
}
