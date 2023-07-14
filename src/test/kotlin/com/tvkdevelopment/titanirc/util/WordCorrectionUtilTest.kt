package com.tvkdevelopment.titanirc.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WordCorrectionUtilTest {

    @Test
    fun testNoChange() {
        val old = "this is a test!"
        val new = "this is a test!"

        val diff = calculateWordCorrection(old, new)

        assertNull(diff)
    }

    @Test
    fun testRemoveChars() {
        val old = "this is an tests!"
        val new = "this is a test!"

        val diff = calculateWordCorrection(old, new)

        assertEquals("a test*", diff)
    }

    @Test
    fun testRemoveCharsAtEnd() {
        val old = "this is an tests"
        val new = "this is a test"

        val diff = calculateWordCorrection(old, new)

        assertEquals("a test*", diff)
    }

    @Test
    fun testRemoveCharsAtStart() {
        val old = "these is a test"
        val new = "this is a test"

        val diff = calculateWordCorrection(old, new)

        assertEquals("this*", diff)
    }

    @Test
    fun testAddChars() {
        val old = "this is a test!"
        val new = "this is a set of tests!"

        val diff = calculateWordCorrection(old, new)

        assertEquals("set of tests*", diff)
    }

    @Test
    fun testAddCharsAtEnd() {
        val old = "this is a test"
        val new = "this is a set of tests"

        val diff = calculateWordCorrection(old, new)

        assertEquals("set of tests*", diff)
    }

    @Test
    fun testAddCharsAtStart() {
        val old = "this are tests"
        val new = "these are tests"

        val diff = calculateWordCorrection(old, new)

        assertEquals("these*", diff)
    }

    @Test
    fun testLongString() {
        val old = "these are a very long test string with a numbers of edits in it"
        val new = "these are some very long test strings with a number of edits in it"

        val diff = calculateWordCorrection(old, new)

        assertEquals("some* strings* number*", diff)
    }

    @Test
    fun testTwoJoinedEdits() {
        val old = "this is tests and then this is more tests"
        val new = "these are tests and then these are more tests"

        val diff = calculateWordCorrection(old, new)

        assertEquals("these are*", diff)
    }

    @Test
    fun testTwoJoinedEditsWithDuplicates() {
        val old = "this is tests and then this is more tests"
        val new = "these are tests and then those are more tests"

        val diff = calculateWordCorrection(old, new)

        assertEquals("these are* those are*", diff)
    }
}