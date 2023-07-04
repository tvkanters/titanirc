package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors.*
import kotlin.test.assertEquals

class DiscordFormattingToIrcMessageTransformationTest {

    private val sut = DiscordFormattingToIrcMessageTransformation()

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
    fun testSpoilers() {
        // GIVEN
        val message = "||test||"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\u00031,1test\u00031,1", result)
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
        assertEquals("\u00031,1t\u00031,1e||st", result)
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
        assertEquals("\u00031,1te|st\u00031,1", result)
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

        // THEN
        assertEquals("https://test.com/test_test_test ${ITALICS}test$ITALICS", result)
    }

    @Test
    fun testItalicsAndHyperlinkUnderscore() {
        // GIVEN
        val message = "_test_ https://test.com/test_test_test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("${ITALICS}test$ITALICS https://test.com/test_test_test", result)
    }
}