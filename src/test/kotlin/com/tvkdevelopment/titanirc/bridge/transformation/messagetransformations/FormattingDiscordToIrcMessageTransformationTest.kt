package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors.*
import kotlin.test.assertEquals

class FormattingDiscordToIrcMessageTransformationTest {

    private val sut = FormattingDiscordToIrcMessageTransformation()

    @Test
    fun testBold() {
        // GIVEN
        val message = "**test**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${BOLD}test$BOLD", result)
    }

    @Test
    fun testBoldUnclosed() {
        // GIVEN
        val message = "**test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testBoldSecondUnclosed() {
        // GIVEN
        val message = "**t**e**st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${BOLD}t${BOLD}e**st", result)
    }

    @Test
    fun testBoldIncomplete() {
        // GIVEN
        val message = "**te*st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("*${ITALICS}te${ITALICS}st", result)
    }

    @Test
    fun testBoldInterrupted() {
        // GIVEN
        val message = "**te*st**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${BOLD}te*st$BOLD", result)
    }

    @Test
    fun testBoldEscapedStart() {
        // GIVEN
        val message = "\\**test**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("*${ITALICS}test${ITALICS}*", result)
    }

    @Test
    fun testBoldEscapedEnd() {
        // GIVEN
        val message = "**test\\**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("**test**", result)
    }

    @Test
    fun testBoldEscapedStartEscaped() {
        // GIVEN
        val message = "\\\\**test**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\\${BOLD}test$BOLD", result)
    }

    @Test
    fun testBoldEscapedStartEscapedEscaped() {
        // GIVEN
        val message = "\\\\\\**test**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\\*${ITALICS}test$ITALICS*", result)
    }

    @Test
    fun testBoldEscapedEndEscaped() {
        // GIVEN
        val message = "**test\\\\**"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${BOLD}test\\$BOLD", result)
    }

    @Test
    fun testUnderline() {
        // GIVEN
        val message = "__test__"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${UNDERLINE}test$UNDERLINE", result)
    }

    @Test
    fun testUnderlineUnclosed() {
        // GIVEN
        val message = "__test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testUnderlineSecondUnclosed() {
        // GIVEN
        val message = "__t__e__st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${UNDERLINE}t${UNDERLINE}e__st", result)
    }

    @Test
    fun testUnderlineIncomplete() {
        // GIVEN
        val message = "__te_st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_${ITALICS}te${ITALICS}st", result)
    }

    @Test
    fun testUnderlineInterrupted() {
        // GIVEN
        val message = "__te_st__"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${UNDERLINE}te_st$UNDERLINE", result)
    }

    @Test
    fun testUnderlineEscapedStart() {
        // GIVEN
        val message = "\\__test__"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_${ITALICS}test${ITALICS}_", result)
    }

    @Test
    fun testUnderlineEscapedEnd() {
        // GIVEN
        val message = "__test\\__"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("__test__", result)
    }

    @Test
    fun testItalicsUnderscore() {
        // GIVEN
        val message = "_test_"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${ITALICS}test$ITALICS", result)
    }

    @Test
    fun testItalicsUnderscoreUnclosed() {
        // GIVEN
        val message = "_test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testItalicsSecondUnclosed() {
        // GIVEN
        val message = "_t_e_st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${ITALICS}t${ITALICS}e_st", result)
    }

    @Test
    fun testItalicsEscapedStart() {
        // GIVEN
        val message = "\\_test_"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_test_", result)
    }

    @Test
    fun testItalicsEscapedEnd() {
        // GIVEN
        val message = "_test\\_"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_test_", result)
    }

    @Test
    fun testItalicsAsterisk() {
        // GIVEN
        val message = "*test*"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${ITALICS}test$ITALICS", result)
    }

    @Test
    fun testItalicsAsteriskUnclosed() {
        // GIVEN
        val message = "*test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testItalicsAsteriskEscapedStart() {
        // GIVEN
        val message = "\\*test*"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("*test*", result)
    }

    @Test
    fun testItalicsAsteriskEscapedEnd() {
        // GIVEN
        val message = "*test\\*"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("*test*", result)
    }

    @Test
    fun testSpoilers() {
        // GIVEN
        val message = "||test||"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\u00031,1test\u0003", result)
    }

    @Test
    fun testSpoilersUnclosed() {
        // GIVEN
        val message = "||test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testSpoilersSecondUnclosed() {
        // GIVEN
        val message = "||t||e||st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\u00031,1t\u0003e||st", result)
    }

    @Test
    fun testSpoilersIncomplete() {
        // GIVEN
        val message = "||te|st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testSpoilersInterrupted() {
        // GIVEN
        val message = "||te|st||"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\u00031,1te|st\u0003", result)
    }

    @Test
    fun testSpoilersEscapedStart() {
        // GIVEN
        val message = "\\||test||"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("||test||", result)
    }

    @Test
    fun testSpoilersEscapedEnd() {
        // GIVEN
        val message = "||test\\||"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("||test||", result)
    }


    @Test
    fun testHyperlinkUnderscore() {
        // GIVEN
        val message = "https://test.com/test_test_test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testHyperlinkAndItalicsUnderscore() {
        // GIVEN
        val message = "https://test.com/test_test_test _test_"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN no formatting is parsed
        assertEquals(message, result)
    }

    @Test
    fun testCodeSnippet() {
        // GIVEN
        val message = "`this || that || it`"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNonEscapingBackslash() {
        // GIVEN
        val message = "back\\slash"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }
}