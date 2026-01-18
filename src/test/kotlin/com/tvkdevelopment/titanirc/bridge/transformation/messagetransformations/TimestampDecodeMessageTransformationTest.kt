package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.util.Time
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class TimestampDecodeMessageTransformationTest {

    private val sut = TimestampDecodeMessageTransformation()

    @Test
    fun testNumericDateTimeWithSeconds() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:S>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 2026-01-18 14:31:08 CET", result)
    }

    @Test
    fun testNumericDateTime() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:s>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 2026-01-18 14:31 CET", result)
    }

    @Test
    fun testTimeZoneCest() {
        // GIVEN
        val halfYearInSeconds = 30.days.inWholeSeconds * 6
        val message = "Time: <t:${EPOCH_TIMESTAMP + halfYearInSeconds}:S>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 2026-07-17 15:31:08 CEST", result)
    }

    @Test
    fun testWrittenDateTime() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:f>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 18 January 2026 14:31:08 CET", result)
    }

    @Test
    fun testWrittenDateTimeWithDay() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:F>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: Sunday, 18 January 2026 14:31:08 CET", result)
    }

    @Test
    fun testNumericDate() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:d>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 2026-01-18", result)
    }

    @Test
    fun testWrittenDate() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:D>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 18 January 2026", result)
    }

    @Test
    fun testTimeWithSeconds() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:T>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 14:31:08 CET", result)
    }

    @Test
    fun testTime() {
        // GIVEN
        val message = "Time: <t:$EPOCH_TIMESTAMP:t>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 14:31 CET", result)
    }

    @Test
    fun testRelativeFuture() {
        // GIVEN
        mockkTime(Instant.fromEpochSeconds(EPOCH_TIMESTAMP))
        val message = "Time: <t:${EPOCH_TIMESTAMP + 5.hours.inWholeSeconds}:R>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: in 5 hours", result)
    }

    @Test
    fun testRelativePast() {
        // GIVEN
        mockkTime(Instant.fromEpochSeconds(EPOCH_TIMESTAMP))
        val message = "Time: <t:${EPOCH_TIMESTAMP - 5.hours.inWholeSeconds}:R>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: 5 hours ago", result)
    }

    @Test
    fun testRelativeNow() {
        // GIVEN
        mockkTime(Instant.fromEpochSeconds(EPOCH_TIMESTAMP))
        val message = "Time: <t:${EPOCH_TIMESTAMP}:R>"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("Time: now", result)
    }

    companion object {
        private const val EPOCH_TIMESTAMP = 1768743068L

        private fun mockkTime(now: Instant) {
            mockkObject(Time)
            every { Time.currentTime } returns now
        }
    }
}