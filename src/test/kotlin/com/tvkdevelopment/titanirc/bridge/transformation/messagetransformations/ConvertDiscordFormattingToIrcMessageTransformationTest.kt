package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors.*
import kotlin.test.assertEquals

class ConvertDiscordFormattingToIrcMessageTransformationTest {

    private val sut = ConvertDiscordFormattingToIrcMessageTransformation()

    @Test
    fun testBold() {
        // GIVEN
        val message = "**test**"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${BOLD}test$BOLD", result)
    }

    @Test
    fun testBoldUnclosed() {
        // GIVEN
        val message = "**test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("**test", result)
    }

    @Test
    fun testBoldIncomplete() {
        // GIVEN
        val message = "**te*st"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("*${ITALICS}te${ITALICS}st", result)
    }

    @Test
    fun testBoldInterrupted() {
        // GIVEN
        val message = "**te*st**"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${BOLD}te*st$BOLD", result)
    }

    @Test
    fun testUnderline() {
        // GIVEN
        val message = "__test__"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${UNDERLINE}test$UNDERLINE", result)
    }

    @Test
    fun testUnderlineUnclosed() {
        // GIVEN
        val message = "__test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("__test", result)
    }

    @Test
    fun testUnderlineIncomplete() {
        // GIVEN
        val message = "__te_st"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("_${ITALICS}te${ITALICS}st", result)
    }

    @Test
    fun testUnderlineInterrupted() {
        // GIVEN
        val message = "__te_st__"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${UNDERLINE}te_st$UNDERLINE", result)
    }

    @Test
    fun testItalicsUnderscore() {
        // GIVEN
        val message = "_test_"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${ITALICS}test$ITALICS", result)
    }

    @Test
    fun testItalicsUnderscoreUnclosed() {
        // GIVEN
        val message = "_test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("_test", result)
    }

    @Test
    fun testItalicsAsterisk() {
        // GIVEN
        val message = "*test*"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("${ITALICS}test$ITALICS", result)
    }

    @Test
    fun testItalicsAsteriskUnclosed() {
        // GIVEN
        val message = "*test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("*test", result)
    }

    @Test
    fun testSpoilers() {
        // GIVEN
        val message = "||test||"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("\u00031,1test\u00031,1", result)
    }

    @Test
    fun testSpoilersUnclosed() {
        // GIVEN
        val message = "||test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("||test", result)
    }

    @Test
    fun testSpoilersIncomplete() {
        // GIVEN
        val message = "||te|st"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("||te|st", result)
    }

    @Test
    fun testSpoilersInterrupted() {
        // GIVEN
        val message = "||te|st||"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("\u00031,1te|st\u00031,1", result)
    }
}