package com.tvkdevelopment.titanirc.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeTest {

    @Test
    fun testRelativeTimeStringMinimum() {
        assertNull(Time.getRelativeTimeString(3.days + 1.hours, true, minimumTimePassed = 4.days))
    }

    @Test
    fun testRelativeTimeStringDaysShort() {
        assertEquals("3d", Time.getRelativeTimeString(3.days + 1.hours, true))
    }

    @Test
    fun testRelativeTimeStringDays() {
        assertEquals("3 days", Time.getRelativeTimeString(3.days + 1.hours, false))
    }

    @Test
    fun testRelativeTimeStringDay() {
        assertEquals("1 day", Time.getRelativeTimeString(1.days + 1.hours, false))
    }

    @Test
    fun testRelativeTimeStringHoursShort() {
        assertEquals("3h", Time.getRelativeTimeString(3.hours + 1.minutes, true))
    }

    @Test
    fun testRelativeTimeStringHours() {
        assertEquals("3 hours", Time.getRelativeTimeString(3.hours + 1.minutes, false))
    }

    @Test
    fun testRelativeTimeStringHour() {
        assertEquals("1 hour", Time.getRelativeTimeString(1.hours + 1.minutes, false))
    }

    @Test
    fun testRelativeTimeStringMinutesShort() {
        assertEquals("3m", Time.getRelativeTimeString(3.minutes + 1.seconds, true))
    }

    @Test
    fun testRelativeTimeStringMinutes() {
        assertEquals("3 minutes", Time.getRelativeTimeString(3.minutes + 1.seconds, false))
    }

    @Test
    fun testRelativeTimeStringMinute() {
        assertEquals("1 minute", Time.getRelativeTimeString(1.minutes + 1.seconds, false))
    }

    @Test
    fun testRelativeTimeStringSecondsShort() {
        assertEquals("3s", Time.getRelativeTimeString(3.seconds + 1.milliseconds, true))
    }

    @Test
    fun testRelativeTimeStringSeconds() {
        assertEquals("3 seconds", Time.getRelativeTimeString(3.seconds + 1.milliseconds, false))
    }

    @Test
    fun testRelativeTimeStringSecond() {
        assertEquals("1 second", Time.getRelativeTimeString(1.seconds + 1.milliseconds, false))
    }

    @Test
    fun testRelativeTimeStringMilliseconds() {
        assertEquals("0 seconds", Time.getRelativeTimeString(3.milliseconds, false))
    }
}